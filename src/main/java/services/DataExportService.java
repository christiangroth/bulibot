package services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import configuration.SmartcronConfig;
import de.chrgroth.jsonstore.json.FlexjsonHelper;
import model.community.BulibotExecutionResultExport;
import model.community.BulibotExecutionResultsExport;
import model.community.Rank;
import model.community.RankingData;
import model.user.BulibotVersion;
import model.user.User;
import ninja.lifecycle.Dispose;
import ninja.lifecycle.Start;
import ninja.utils.NinjaProperties;

@Singleton
public class DataExportService {
    private static final Logger LOG = LoggerFactory.getLogger(DataExportService.class);

    @Inject
    private Provider<SlackService> slackServiceProvider;

    @Inject
    private SmartcronService smartcronService;

    @Inject
    private BulibotService bulibotService;

    @Inject
    private SeasonService seasonService;

    @Inject
    private DataService dataService;

    @Inject
    private NinjaProperties ninjaProperties;

    private FlexjsonHelper flexjsonHelper;

    @Start
    public void start() {
        LOG.info("configuring data export service...");
        flexjsonHelper = FlexjsonHelper.builder().build();
        LOG.info("started data export service.");
    }

    public void jsonExportBulibotExecutionResults(User user, BulibotExecutionResultsExport exportData, boolean testMode) {

        // get target
        String url = user.getJsonExportBulibotResultsUrl();

        // prepare data
        String json = toJson(exportData);

        // schedule smartcron
        if (testMode || ninjaProperties.isProd()) {
            int retryCount = testMode ? 0 : ninjaProperties.getIntegerWithDefault(SmartcronConfig.RESULT_EXPORT_JSON_RETRY_COUNT.getKey(), 5);
            int retryDelaySeconds = testMode ? 0 : ninjaProperties.getIntegerWithDefault(SmartcronConfig.RESULT_EXPORT_JSON_RETRY_DELAY_SECONDS.getKey(), 60);
            JsonDataExporter dataExporter = new JsonDataExporter(url, json, retryCount, retryDelaySeconds);
            smartcronService.schedule(dataExporter);
        } else {
            LOG.info("in production the data would have been sent to : " + url + System.lineSeparator() + json);
        }
    }

    public String toJson(Object exportData) {
        return flexjsonHelper.serializer(true).exclude("*.class").serialize(exportData);
    }

    public void slackExportBulibotExecutionResults(User user, BulibotExecutionResultsExport exportData, boolean testMode) {

        // get target
        String url = user.getSlackExportBulibotResultsUrl();

        // prepare data
        String json = toSlackText(exportData);
        BulibotVersion bulibotLiveVersion = user.getBulibotLiveVersion();

        // schedule smartcron
        if (testMode || ninjaProperties.isProd()) {
            int retryCount = testMode ? 0 : ninjaProperties.getIntegerWithDefault(SmartcronConfig.RESULT_EXPORT_SLACK_RETRY_COUNT.getKey(), 5);
            int retryDelaySeconds = testMode ? 0 : ninjaProperties.getIntegerWithDefault(SmartcronConfig.RESULT_EXPORT_SLACK_RETRY_DELAY_SECONDS.getKey(), 60);
            SlackDataExporter dataExporter = new SlackDataExporter(url, buildBulibotSlackName(user, bulibotLiveVersion), json, retryCount, retryDelaySeconds, slackServiceProvider);
            smartcronService.schedule(dataExporter);
        } else {
            LOG.info("in production the data would have been sent to slack : " + json);
        }
    }

    public String toSlackText(BulibotExecutionResultsExport exportData) {
        StringBuffer sb = new StringBuffer();
        if (exportData != null) {

            // header line
            sb.append(exportData.getSeason());
            sb.append("/");
            sb.append(exportData.getMatchday());
            sb.append(System.lineSeparator());

            // bulibot results
            List<BulibotExecutionResultExport> results = exportData.getResults();
            if (results != null) {
                for (BulibotExecutionResultExport result : results) {
                    sb.append(result.getAssignedTime());
                    sb.append(" ");
                    sb.append(result.getTeamOneDisplayName());
                    sb.append(" ");
                    sb.append(result.getGoalsTeamOne());
                    sb.append(" : ");
                    sb.append(result.getGoalsTeamTwo());
                    sb.append(" ");
                    sb.append(result.getTeamTwoDisplayName());
                    sb.append(System.lineSeparator());
                }
            }
        }
        return sb.toString();
    }

    public void slackExportCurrentMatchdayResults() {

        // check season and matchday
        Integer currentSeason = seasonService.currentSeason();
        Integer currentMatchday = seasonService.currentMatchday();
        if (currentSeason == null || currentMatchday == null) {
            return;
        }

        // check ranking
        RankingData rankingData = bulibotService.rankingData(currentSeason, currentMatchday);
        if (rankingData == null) {
            return;
        }

        // walk ranking users
        Set<User> users = rankingData.getRanks().stream().filter(data -> data.getRank() != null).map(data -> dataService.userById(data.getRank().getUserId()))
                .filter(Objects::nonNull).filter(u -> u.isVerified() && !u.isLocked()).collect(Collectors.toSet());
        for (User user : users) {

            // check if slack export enabled
            String exportBulibotResultsUrl = user.getSlackExportBulibotResultsUrl();
            if (!user.isSlackExportBulibotResults() || StringUtils.isBlank(exportBulibotResultsUrl)) {
                continue;
            }

            // export
            slackExportCurrentMatchdayResults(user, currentSeason, currentMatchday, rankingData);
        }
    }

    private void slackExportCurrentMatchdayResults(User user, int season, int matchday, RankingData rankingData) {

        // get target
        String url = user.getSlackExportBulibotResultsUrl();

        // prepare data
        String json = toSlackText(season, matchday, rankingData);
        BulibotVersion bulibotLiveVersion = user.getBulibotLiveVersion();

        // schedule smartcron
        if (ninjaProperties.isProd()) {
            int retryCount = ninjaProperties.getIntegerWithDefault(SmartcronConfig.RANKING_EXPORT_SLACK_RETRY_COUNT.getKey(), 5);
            int retryDelaySeconds = ninjaProperties.getIntegerWithDefault(SmartcronConfig.RANKING_EXPORT_SLACK_RETRY_DELAY_SECONDS.getKey(), 60);
            SlackDataExporter dataExporter = new SlackDataExporter(url, buildBulibotSlackName(user, bulibotLiveVersion), json, retryCount, retryDelaySeconds, slackServiceProvider);
            smartcronService.schedule(dataExporter);
        } else {
            LOG.info("in production the data would have been sent to slack : " + json);
        }
    }

    private String toSlackText(Integer season, Integer matchday, RankingData rankingData) {
        StringBuffer sb = new StringBuffer();
        if (rankingData.getRanks() == null) {
            return sb.toString();
        }

        // prepare data
        Comparator<? super Rank> rankPositionComparator = (o1, o2) -> new Integer(o1.getPosition()).compareTo(new Integer(o2.getPosition()));
        List<Rank> ranks = new ArrayList<>(rankingData.getRanks().stream().map(r -> r.getRank()).collect(Collectors.toList()));
        Collections.sort(ranks, rankPositionComparator);
        List<Rank> matchdayRanks = new ArrayList<>(rankingData.getRanks().stream().map(r -> r.getMatchdayRank()).collect(Collectors.toList()));
        Collections.sort(matchdayRanks, rankPositionComparator);

        // print matchday data
        sb.append("Ranking: ");
        sb.append(season);
        sb.append("/");
        sb.append(matchday);
        sb.append(System.lineSeparator());
        toSlackText(sb, matchdayRanks);
        sb.append(System.lineSeparator());

        // print overall data
        sb.append("Ranking: ");
        sb.append(season);
        sb.append(System.lineSeparator());
        toSlackText(sb, ranks);

        // done
        return sb.toString();
    }

    private void toSlackText(StringBuffer sb, List<Rank> ranks) {
        for (Rank rank : ranks) {
            sb.append(rank.getPosition());
            sb.append(". ");
            User rankUser = dataService.userById(rank.getUserId());
            if (rankUser != null) {
                sb.append(rankUser.getName());
            } else {
                sb.append("?user");
            }
            sb.append(" ");
            sb.append(rank.getPoints());
            sb.append(System.lineSeparator());
        }
    }

    private String buildBulibotSlackName(User user, BulibotVersion bulibotLiveVersion) {

        // null guard
        if (bulibotLiveVersion == null) {
            return null;
        }

        // done
        return user.getBulibotName() + " (" + bulibotLiveVersion.getName() + ")";
    }

    @Dispose
    public void shutdown() {
        LOG.info("stopped data export service.");
    }
}
