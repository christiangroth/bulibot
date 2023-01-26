package services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import configuration.BulibotConfig;
import configuration.NotificationEventConfig;
import model.community.BulibotExecution;
import model.community.BulibotExecutionResultsExport;
import model.community.Rank;
import model.community.Ranking;
import model.community.RankingData;
import model.community.RankingDataBulibotExecution;
import model.community.RankingDataRank;
import model.community.Result;
import model.community.TestdataMatchResult;
import model.community.TestdataMatchdayResult;
import model.community.TestdataResult;
import model.match.Match;
import model.match.Match.Status;
import model.statistics.MatchMetadata;
import model.statistics.Statistics;
import model.statistics.result.StatisticsResult;
import model.user.BulibotVersion;
import model.user.User;
import ninja.lifecycle.Dispose;
import ninja.lifecycle.Start;
import ninja.metrics.Timed;
import ninja.utils.NinjaProperties;
import services.model.ScriptServiceResult;
import services.testdata.BulibotTestdataStrategy;
import services.testdata.BulibotTestdataStrategy.BulibotTestdata;
import util.DateUtils;
import util.TimerUtils;

@Singleton
public class BulibotService {
    private static final Logger LOG = LoggerFactory.getLogger(BulibotService.class);

    private static final Comparator<TestdataMatchdayResult> TESTDATA_MATCHDAYRESULT_COMPARATOR = (one, two) -> {

        // season compare
        int seasonCompare = Integer.compare(two.getSeason(), one.getSeason());
        if (seasonCompare != 0) {
            return seasonCompare;
        }

        // matchday compare
        return Integer.compare(two.getMatchday(), one.getMatchday());
    };

    @Inject
    private Set<BulibotTestdataStrategy> testdataStrategies;

    @Inject
    private BulibotService bulibotService;

    @Inject
    private ScriptService scriptService;

    @Inject
    private DataExportService dataExportService;

    @Inject
    private DataService dataService;

    @Inject
    private NotificationService notificationService;

    @Inject
    private SeasonService seasonService;

    @Inject
    private NinjaProperties ninjaProperties;

    private Set<String> scriptReturnValues;

    private List<Integer> rankingDataSeasons;
    private Map<String, RankingData> rankingDataCache = new HashMap<>();
    private Map<Integer, List<RankingData>> seasonRankingDataCache = new HashMap<>();

    @Start
    public void startup() {
        LOG.info("configuring bulibot service...");
        scriptReturnValues = new HashSet<>();
        scriptReturnValues.add(BulibotExecution.SCRIPT_RETURN_VALUE_TEAM_ONE);
        scriptReturnValues.add(BulibotExecution.SCRIPT_RETURN_VALUE_TEAM_TWO);
        LOG.info("started bulibot service.");
    }

    public int points(BulibotExecution bulibotExecution, Match match) {
        switch (Result.calc(bulibotExecution, match)) {
            case EXACT:
                return getPointsExactHit();
            case RELATIVE:
                return getPointsRelativeHit();
            case WINNER:
                return getPointsWinnerHit();
            default:
                return getPointsNothing();
        }
    }

    public BulibotTestdataStrategy availableStrategy(String strategy) {
        return testdataStrategies.stream().filter(s -> StringUtils.equals(s.code(), strategy)).filter(s -> s.available()).findFirst().orElse(null);
    }

    public List<BulibotTestdataStrategy> availableStrategies() {
        return testdataStrategies.stream().filter(s -> s.available()).collect(Collectors.toList());
    }

    public TestdataResult test(User user, BulibotVersion version, BulibotTestdataStrategy strategy) {

        // get matchdays to test
        List<BulibotTestdata> testdata = strategy.data();
        Collections.reverse(testdata);

        // create results
        TestdataResult result = new TestdataResult();
        long duration = TimerUtils.measure(() -> {
            testdata.parallelStream().forEach(data -> {

                // add matchday result
                TestdataMatchdayResult matchdayResult = new TestdataMatchdayResult(data.getSeason(), data.getMatchday());
                result.getMatchdayResults().add(matchdayResult);

                // execute bulibot
                List<BulibotExecution> executions = execute(data.getSeason(), data.getMatchday(), user, version, true);
                for (BulibotExecution execution : executions) {

                    // load match
                    Match match = dataService.match(execution.getMatchId());
                    matchdayResult.getMatchResults().add(new TestdataMatchResult(match, execution, Result.calc(execution, match), points(execution, match)));
                }
            });
        });
        LOG.info("testing " + strategy.code() + " for " + user.getName() + " took " + duration + "ms");

        // sort - this is unsorted due to parallel processing
        Collections.sort(result.getMatchdayResults(), TESTDATA_MATCHDAYRESULT_COMPARATOR);

        // done
        return result;
    }

    public void executeSaveAndExport(Integer season, Integer matchday) {

        // prepare all bulibot versions
        Map<User, BulibotVersion> userSystemTags = new HashMap<>();
        for (User user : dataService.users()) {

            // skip locked and not verified users
            if (user.isLocked() || !user.isVerified()) {
                continue;
            }

            // copy live version
            String systemTag = tagBulibotliveVersion(user);
            if (StringUtils.isNotBlank(systemTag)) {
                userSystemTags.put(user, user.getBulibotVersionBySystemTag(systemTag));
            }
        }
        if (userSystemTags.isEmpty()) {
            LOG.info("no user data available, aborting.");
            return;
        }
        LOG.info("prepared " + userSystemTags.size() + " bulibot versions");

        // create executions per user and match
        List<BulibotExecution> bulibotExecutions = new ArrayList<>();
        for (Entry<User, BulibotVersion> entry : userSystemTags.entrySet()) {
            bulibotExecutions.addAll(execute(season, matchday, entry.getKey(), entry.getValue(), false));
        }

        // done if nothing happened
        if (bulibotExecutions.isEmpty()) {
            return;
        }

        // save all executions
        dataService.bulibotExecutions(bulibotExecutions);

        // update rankings
        bulibotService.updateRankings();

        // notify
        notificationService.send(NotificationEventConfig.BULIBOTS_EXECUTED, season + "/" + matchday);

        // invoke json export of bulibot execution results
        List<Match> matches = dataService.matches(season, matchday);
        for (User user : userSystemTags.keySet()) {

            // get bulibot results
            List<BulibotExecution> userExecutions = bulibotExecutions.stream().filter(be -> be.getUserId() == user.getId()).collect(Collectors.toList());
            if (userExecutions.isEmpty()) {
                continue;
            }

            // build export data
            BulibotExecutionResultsExport exportData = new BulibotExecutionResultsExport(season, matchday, matches, userExecutions);

            // export json
            exportJson(exportData, user);

            // export slack
            exportSlack(exportData, user);
        }
    }

    private void exportJson(BulibotExecutionResultsExport exportData, User user) {

        // check if JSON export enabled
        String exportBulibotResultsUrl = user.getJsonExportBulibotResultsUrl();
        if (!user.isJsonExportBulibotResults() || StringUtils.isBlank(exportBulibotResultsUrl)) {
            return;
        }

        // export data
        LOG.info("exporting executions for " + user.getEmail() + " to " + exportBulibotResultsUrl);
        dataExportService.jsonExportBulibotExecutionResults(user, exportData, false);
    }

    private void exportSlack(BulibotExecutionResultsExport exportData, User user) {

        // check if slack export enabled
        String exportBulibotResultsUrl = user.getSlackExportBulibotResultsUrl();
        if (!user.isSlackExportBulibotResults() || StringUtils.isBlank(exportBulibotResultsUrl)) {
            return;
        }

        // export data
        LOG.info("exporting executions for " + user.getEmail() + " to " + exportBulibotResultsUrl);
        dataExportService.slackExportBulibotExecutionResults(user, exportData, false);
    }

    public void executeAndSave(Integer season, Integer matchday, User user) {

        // skip locked and not verified users
        if (user.isLocked() || !user.isVerified()) {
            LOG.debug("user not active, aborting: " + user.getEmail());
            return;
        }

        // copy live version
        String systemTag = tagBulibotliveVersion(user);
        if (StringUtils.isBlank(systemTag)) {
            LOG.warn("no bulibot data available, aborting: " + user.getEmail());
            return;
        }

        // create executions
        List<BulibotExecution> bulibotExecutions = execute(season, matchday, user, user.getBulibotVersionBySystemTag(systemTag), false);

        // save all executions
        if (!bulibotExecutions.isEmpty()) {
            dataService.bulibotExecutions(bulibotExecutions);
        }
    }

    private String tagBulibotliveVersion(User user) {

        // get live version
        BulibotVersion liveVersion = user != null ? user.getBulibotLiveVersion() : null;
        if (liveVersion == null) {
            return null;
        }

        // check if same source was already tagged
        BulibotVersion existingTaggedVersion = user.getSystemTaggedBulibotVersionBySource(liveVersion.getSource());
        if (existingTaggedVersion != null) {
            return existingTaggedVersion.getName();
        }

        // create UUID system tag
        String systemTag;
        do {
            systemTag = UUID.randomUUID().toString();
        } while (user.getBulibotVersionBySystemTag(systemTag) != null);

        // create copy
        BulibotVersion taggedVersion = new BulibotVersion();
        taggedVersion.setName(systemTag);
        taggedVersion.setSource(liveVersion.getSource());
        taggedVersion.setLive(false);
        taggedVersion.setSystemTag(true);

        // save
        user.getBulibotVersions().add(taggedVersion);
        dataService.user(user);

        // done
        return systemTag;
    }

    private List<BulibotExecution> execute(Integer season, Integer matchday, User user, BulibotVersion version, boolean testMode) {
        List<BulibotExecution> bulibotExecutions = Collections.synchronizedList(new ArrayList<BulibotExecution>());

        // load match data
        List<Match> matches = dataService.matches(season, matchday);
        if (matches == null || matches.isEmpty()) {
            LOG.warn("no match data available, aborting.");
            return bulibotExecutions;
        }

        // prepare statistics data
        Statistics statistics = dataService.statisticsCopy(season, matchday);
        StatisticsResult overallStatisticsResult = statistics.builder().build();
        StatisticsResult lastSeasonStatisticsResult = statistics.builder().filterSeason(season - 1).build();
        StatisticsResult currentSeasonStatisticsResult = statistics.builder().filterSeason(season).filterMatchday(1, matchday - 1).build();

        // convert to match metadata
        Map<Long, MatchMetadata> matchMetadata = matches.stream().filter((match) -> testMode || match.getStatus() != Status.FINISHED).map(m -> new MatchMetadata(m))
                .collect(Collectors.toMap(m -> m.getMatchId(), m -> m));

        // create per match
        LOG.info("checking bulibot executions " + version.getName() + " by " + user.getEmail() + " for " + season + "/" + matchday);
        matchMetadata.values().parallelStream().forEach(match -> {

            // prevent duplicates if not in test mode
            BulibotExecution existingBulibotExecution = dataService.bulibotExecution(user.getId(), match.getMatchId());
            if (testMode || existingBulibotExecution == null) {
                bulibotExecutions.add(execute(statistics, overallStatisticsResult, lastSeasonStatisticsResult, currentSeasonStatisticsResult, user, version, match, testMode));
            }
        });

        // sort bulibot executions like their matches
        Collections.sort(bulibotExecutions,
                (b1, b2) -> DateUtils.OLDEST_FIRST.compare(matchMetadata.get(b1.getMatchId()).getMatchAssignedTime(), matchMetadata.get(b2.getMatchId()).getMatchAssignedTime()));

        // done
        return bulibotExecutions;
    }

    private BulibotExecution execute(Statistics statistics, StatisticsResult overallStatisticsResult, StatisticsResult lastSeasonStatisticsResult,
            StatisticsResult currentSeasonStatisticsResult, User user, BulibotVersion version, MatchMetadata match, boolean testMode) {

        // execute bulibot
        LOG.info("executing bulibot " + version.getName() + " by " + user.getEmail() + " for " + match.getSeason() + "/" + match.getMatchday() + "/" + match.getMatchId());
        final Map<String, Object> bindings = createBindings(overallStatisticsResult, currentSeasonStatisticsResult, lastSeasonStatisticsResult, statistics, match);
        ScriptServiceResult scriptServiceResult = scriptService.executeGroovy(bindings, user, version, scriptReturnValues);

        // convert goal values
        Integer goalsTeamOne = safeToInteger(scriptServiceResult.getReturnValues().get(BulibotExecution.SCRIPT_RETURN_VALUE_TEAM_ONE));
        Integer goalsTeamTwo = safeToInteger(scriptServiceResult.getReturnValues().get(BulibotExecution.SCRIPT_RETURN_VALUE_TEAM_TWO));

        // create execution data
        BulibotExecution execution = new BulibotExecution(match);
        execution.setUserId(user.getId());
        execution.setBulibotVersionName(version.getName());
        execution.setDuration(scriptServiceResult.getDuration());
        execution.setGoalsTeamOne(goalsTeamOne);
        execution.setGoalsTeamTwo(goalsTeamTwo);
        if (!scriptServiceResult.isSuccess()) {
            execution.setErrorCauseType(scriptServiceResult.getError().getClass().getName());
            execution.setErrorCauseMessage(scriptServiceResult.getError().getMessage());
        }

        // add debug data in test mode
        if (testMode) {

            // set non empty stdout
            String stdout = scriptServiceResult.getStdout().toString();
            if (StringUtils.isNotBlank(stdout)) {
                execution.setStdout(stdout);
            }

            // cleanup binding data
            Map<String, String> state = scriptServiceResult.getState();
            state.remove(BulibotExecution.SCRIPT_BINDING_SEASON_DATA);
            state.remove(BulibotExecution.SCRIPT_BINDING_LAST_SEASON_DATA);
            state.remove(BulibotExecution.SCRIPT_BINDING_OVERALL_DATA);
            state.remove(BulibotExecution.SCRIPT_BINDING_STATISTICS);
            state.remove(BulibotExecution.SCRIPT_BINDING_MATCH);

            // set non empty state
            if (!state.isEmpty()) {
                execution.setState(state);
            }
        }

        // done
        return execution;
    }

    private Map<String, Object> createBindings(StatisticsResult overallStatisticsResult, StatisticsResult currentSeasonStatisticsResult,
            StatisticsResult lastSeasonStatisticsResult, Statistics statistics, MatchMetadata match) {
        Map<String, Object> bindings = new HashMap<>();
        bindings.put(BulibotExecution.SCRIPT_BINDING_SEASON_DATA, currentSeasonStatisticsResult);
        bindings.put(BulibotExecution.SCRIPT_BINDING_LAST_SEASON_DATA, lastSeasonStatisticsResult);
        bindings.put(BulibotExecution.SCRIPT_BINDING_OVERALL_DATA, overallStatisticsResult);
        bindings.put(BulibotExecution.SCRIPT_BINDING_STATISTICS, statistics);
        bindings.put(BulibotExecution.SCRIPT_BINDING_MATCH, match);
        return bindings;
    }

    private Integer safeToInteger(Object value) {

        // null guard
        if (value == null) {
            return null;
        }

        // convert numbers
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        // try to parse
        if (value instanceof String) {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        // done
        return null;
    }

    public List<Integer> rankingDataSeasons() {
        return rankingDataSeasons;
    }

    public RankingData rankingData(int season, int matchday) {
        return rankingDataCache.get(buildRankingDataKey(season, matchday));
    }

    public List<RankingData> seasonRankingData(int season) {
        return seasonRankingDataCache.get(season);
    }

    @Timed
    public void updateRankings() {
        int pointsExactHit = getPointsExactHit();
        int pointsRelativeHit = getPointsRelativeHit();
        int pointsWinnerHit = getPointsWinnerHit();
        updateRankings(pointsExactHit, pointsRelativeHit, pointsWinnerHit);
    }

    private void updateRankings(int pointsExactHit, int pointsRelativeHit, int pointsWinnerHit) {

        // compute overall ranking and ranking for all known seasons
        rankingDataSeasons = new ArrayList<>();
        final Integer currentSeason = seasonService.currentSeason();
        final Integer currentMatchday = seasonService.currentMatchday();
        List<User> users = dataService.users().stream().filter(u -> u.isVerified()).collect(Collectors.toList());
        for (int season : dataService.seasons()) {

            // build season ranking
            final boolean allowOnEmptyBulibotsData = currentSeason != null && currentSeason.intValue() == season;
            Ranking seasonRanking = new Ranking(users, dataService.bulibotExecutions(season), allowOnEmptyBulibotsData, dataService.matches(season), pointsExactHit,
                    pointsRelativeHit, pointsWinnerHit);
            if (seasonRanking.getRanks().isEmpty()) {
                continue;
            }

            // add
            rankingDataSeasons.add(season);

            // build matchday rankings
            List<RankingData> seasonMatchdayRankingDataList = new ArrayList<>();
            LOG.info("computing ranking for " + season);
            int computeMatchdays = currentSeason != null && currentMatchday != null && season == currentSeason.intValue() ? currentMatchday.intValue() : Match.NUMBER_OF_MATCHDAYS_PER_SEASON;
            for (int i = 1; i <= computeMatchdays; i++) {
                String rankingDataKey = buildRankingDataKey(season, i);

                // build ranking and ranking per matchday only
                Ranking matchdayRanking = new Ranking(users, dataService.bulibotExecutions(season, 1, i), allowOnEmptyBulibotsData, dataService.matches(season, 1, i),
                        pointsExactHit, pointsRelativeHit, pointsWinnerHit);
                Ranking matchdayOnlyRanking = new Ranking(users, dataService.bulibotExecutions(season, i), allowOnEmptyBulibotsData, dataService.matches(season, i), pointsExactHit,
                        pointsRelativeHit, pointsWinnerHit);
                if (matchdayRanking.getRanks().isEmpty()) {
                    continue;
                }

                // store data
                final RankingData rankingData = buildRankingData(season, i, matchdayRanking, matchdayOnlyRanking);
                rankingDataCache.put(rankingDataKey, rankingData);

                // collect without matches to use for season chart data
                seasonMatchdayRankingDataList.add(buildRankingDataWithoutMatchesAndBulibotExecutions(rankingData));
            }

            // store data
            seasonRankingDataCache.put(season, seasonMatchdayRankingDataList);
        }
    }

    private String buildRankingDataKey(int season, int matchday) {
        return season + "/" + matchday;
    }

    private RankingData buildRankingData(int season, int matchday, Ranking ranking, Ranking matchdayOnlyRanking) {

        // get raw data
        List<Match> matches = dataService.matches(season, matchday);
        Collections.sort(matches, Match.COMPARATOR_DATE_ASC);

        // create ranking data
        RankingData rankingData = new RankingData();
        rankingData.getMatches().addAll(matches);
        if (ranking != null) {

            // copy statistics data
            rankingData.setResultsHomeTeam(ranking.getResultsHomeTeam());
            rankingData.setResultsDraw(ranking.getResultsDraw());
            rankingData.setResultsAwayTeam(ranking.getResultsAwayTeam());

            // create rank data
            for (Rank rank : ranking.getRanks()) {

                // add rank data
                RankingDataRank rankingDataRank = new RankingDataRank();
                rankingData.getRanks().add(rankingDataRank);

                // set ranks
                rankingDataRank.setRank(rank);
                if (matchdayOnlyRanking != null) {
                    rankingDataRank.setMatchdayRank(matchdayOnlyRanking.getRanks().stream().filter(r -> r.getUserId() == rank.getUserId()).findFirst().orElse(null));
                }

                // set bulibot executions
                if (matches != null) {

                    // create bulibot execution data
                    for (Match match : matches) {
                        RankingDataBulibotExecution bulibotExecutionData = new RankingDataBulibotExecution();
                        rankingDataRank.getBulibotExecutions().add(bulibotExecutionData);
                        BulibotExecution bulibotExecution = dataService.bulibotExecution(rank.getUserId(), match.getId());
                        bulibotExecutionData.setBulibotExecution(bulibotExecution);
                        if (bulibotExecution != null) {
                            bulibotExecutionData.setPoints(bulibotService.points(bulibotExecution, match));
                        }
                    }
                }
            }
        }

        // done
        return rankingData;
    }

    private RankingData buildRankingDataWithoutMatchesAndBulibotExecutions(RankingData original) {

        // create ranking data
        RankingData rankingData = new RankingData();

        // copy data without matches
        rankingData.setResultsHomeTeam(original.getResultsHomeTeam());
        rankingData.setResultsDraw(original.getResultsDraw());
        rankingData.setResultsAwayTeam(original.getResultsAwayTeam());

        // re create ranks without bulibot executions
        rankingData.getRanks().addAll(original.getRanks().stream().map(o -> {
            final RankingDataRank copy = new RankingDataRank();
            copy.setRank(o.getRank());
            copy.setMatchdayRank(o.getMatchdayRank());
            return copy;
        }).collect(Collectors.toList()));

        // done
        return rankingData;
    }

    private int getPointsExactHit() {
        return ninjaProperties.getIntegerWithDefault(BulibotConfig.POINTS_EXACT.getKey(), 4);
    }

    private int getPointsRelativeHit() {
        return ninjaProperties.getIntegerWithDefault(BulibotConfig.POINTS_RELATIVE.getKey(), 3);
    }

    private int getPointsWinnerHit() {
        return ninjaProperties.getIntegerWithDefault(BulibotConfig.POINTS_WINNER.getKey(), 2);
    }

    private int getPointsNothing() {
        return ninjaProperties.getIntegerWithDefault(BulibotConfig.POINTS_NOTHING.getKey(), 0);
    }

    public List<Match> findUnstartedMatchesWithExistingBulibotExecutions(int season) {

        // map executions to matches
        List<BulibotExecution> bulibotExecutions = dataService.bulibotExecutions(season);
        Map<BulibotExecution, Match> executionsAndMatches = bulibotExecutions.stream().collect(Collectors.toMap((e) -> e, (e) -> dataService.match(e.getMatchId())));

        // filter only not started matches
        return executionsAndMatches.entrySet().stream().filter((e) -> e.getValue().getStatus() == Status.WAITING).map((e) -> e.getValue()).distinct().collect(Collectors.toList());
    }

    public void deleteBulibotExecutions(int season, long matchId) {
        LOG.info("Deleting pending bulibot executions for " + season + "/" + matchId);
        dataService.bulibotExecutionsDelete(season, matchId);
        updateRankings();
    }

    @Dispose
    public void shutdown() {
        LOG.info("stopped bulibot service.");
    }
}
