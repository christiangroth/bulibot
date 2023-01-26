package services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import configuration.NotificationEventConfig;
import model.match.Match;
import model.statistics.Statistics;
import model.statistics.result.StatisticsResult;
import ninja.lifecycle.Dispose;
import ninja.lifecycle.Start;
import ninja.metrics.Timed;
import util.DateUtils;

@Singleton
public class SeasonService {
    private static final Logger LOG = LoggerFactory.getLogger(SeasonService.class);

    private static class ExecutionContext {

        private boolean dataBeforeAvailable;
        private boolean dataAfterAvailable;

        private LocalDateTime midBetween;
        private Integer valueBefore;
        private Integer valueAfter;

        private void load(DataService dataService, LocalDateTime reference, boolean season) {

            // get matches around reference date, 1 before, 1 after
            List<Match> matchesBefore = dataService.matchesBefore(reference, 1);
            List<Match> matchesAfter = dataService.matchesAfter(reference, 1);
            Match matchBefore = matchesBefore.isEmpty() ? null : matchesBefore.get(0);
            Match matchAfter = matchesAfter.isEmpty() ? null : matchesAfter.get(0);
            LOG.debug("match before: " + intelligentLog(matchBefore));
            LOG.debug("matchAfter: " + intelligentLog(matchAfter));

            // check which data is available
            dataBeforeAvailable = matchDataAvailable(matchBefore);
            dataAfterAvailable = matchDataAvailable(matchAfter);
            LOG.debug("dataBeforeAvailable: " + dataBeforeAvailable);
            LOG.debug("dataAfterAvailable: " + dataAfterAvailable);

            // set values
            if (before()) {
                valueBefore = season ? matchBefore.getSeason() : matchBefore.getMatchday();
            }
            if (after()) {
                valueAfter = season ? matchAfter.getSeason() : matchAfter.getMatchday();
            }
            if (both()) {
                midBetween = DateUtils.between(matchBefore.getAssignedTime(), matchAfter.getAssignedTime());
            }
        }

        private String intelligentLog(Match match) {

            // null guard
            if (match == null) {
                return null;
            }

            // build data
            StringBuffer sb = new StringBuffer();
            sb.append(match.getId());
            sb.append("|");
            sb.append(match.getSeason());
            sb.append("/");
            sb.append(match.getMatchday());
            sb.append("|");
            sb.append(match.getAssignedTime());
            sb.append("|");
            sb.append(match.getTeamOneDisplayName());
            sb.append(" vs ");
            sb.append(match.getTeamTwoDisplayName());
            return sb.toString();
        }

        private boolean matchDataAvailable(Match match) {
            return match != null && match.getAssignedTime() != null && match.getSeason() != null && match.getMatchday() != null;
        }

        private boolean both() {
            return dataBeforeAvailable && dataAfterAvailable;
        }

        private boolean before() {
            return dataBeforeAvailable;
        }

        private boolean after() {
            return dataAfterAvailable;
        }
    }

    @Inject
    private BulibotService bulibotService;

    @Inject
    private DataService dataService;

    @Inject
    private NotificationService notificationService;

    private Integer currentSeason;
    private Integer currentMatchday;

    private StatisticsResult statisticsResult;
    private Map<Integer, StatisticsResult> statisticsResultPerSeason = new HashMap<>();
    private Map<String, StatisticsResult> statisticsResultPerSeasonMatchday = new HashMap<>();

    @Start
    public void startup() {
        LOG.info("started season service.");
    }

    public void updateCurrentSeason(LocalDateTime reference) {

        // compute
        Integer oldValue = currentSeason;
        LOG.info("updating current season: " + oldValue);
        Integer newValue = compute(reference, true);

        // update
        currentSeason = newValue;
        if (currentSeason == null) {
            LOG.info("current season changed to " + currentSeason);
            currentSeason = DateUtils.currentYear() - 1;
        }

        // check if season changed
        if (!ObjectUtils.equals(oldValue, newValue)) {
            LOG.info("current season changed to " + currentSeason);
            notificationService.send(NotificationEventConfig.SEASON_CHANGED, "" + newValue);

            // update statistics and rankings
            updateStatistics();
            bulibotService.updateRankings();
        } else {
            LOG.info("no change");
        }
    }

    public Integer currentSeason() {
        return currentSeason;
    }

    public void updateCurrentMatchday(LocalDateTime reference) {

        // compute
        Integer oldValue = currentMatchday;
        LOG.info("updating current matchday: " + oldValue);
        currentMatchday = compute(reference, false);
        if (!ObjectUtils.equals(oldValue, currentMatchday)) {
            LOG.info("current matchday change to " + currentMatchday);
            notificationService.send(NotificationEventConfig.MATCHDAY_CHANGED, "" + currentMatchday);
        } else {
            LOG.info("no change");
        }
    }

    public Integer currentMatchday() {
        return currentMatchday;
    }

    private Integer compute(LocalDateTime reference, boolean season) {

        // get data
        ExecutionContext ec = new ExecutionContext();
        ec.load(dataService, reference, season);

        // full data available
        if (ec.both()) {

            // both matches available, check if same value
            if (ec.valueBefore == ec.valueAfter) {
                return ec.valueAfter;
            } else {

                // compare mid-point with reference
                return reference.isBefore(ec.midBetween) ? ec.valueBefore : ec.valueAfter;
            }
        }

        // value from next match, if no match before
        else if (!ec.before() && ec.after()) {
            return ec.valueAfter;
        }

        // value from previous match, if no match after
        else if (ec.before() && !ec.after()) {
            return ec.valueBefore;
        }

        // if no data is available we'll leave the value to null
        else {
            return null;
        }
    }

    @Timed
    public void updateStatistics() {

        // create new statistics
        Statistics statistics = new Statistics();

        // update match data (need oldest first to build correct Elo ranking)
        dataService.matches(Match.COMPARATOR_DATE_ASC).forEach(m -> statistics.update(m));

        // pre compute overall statistics and statistics for all known seasons and matchdays
        statisticsResultPerSeason.clear();
        statisticsResultPerSeasonMatchday.clear();
        statisticsResult = statistics.builder().build();
        for (int season : dataService.seasons()) {

            // season overall result
            LOG.info("computing statistics result for " + season);
            statisticsResultPerSeason.put(season, statistics.builder().filterSeason(season).build());

            // per matchday result
            int maxMatchday = Match.NUMBER_OF_MATCHDAYS_PER_SEASON;
            if (currentSeason != null && currentMatchday != null && currentSeason.intValue() == season) {
                maxMatchday = currentMatchday.intValue();
            }
            for (int matchday = 1; matchday <= maxMatchday; matchday++) {
                statisticsResultPerSeasonMatchday.put(season + "_" + matchday, statistics.builder().filterSeason(season).filterMatchday(1, matchday).build());
            }
        }
    }

    public StatisticsResult statisticsResult() {
        return statisticsResult;
    }

    public StatisticsResult statisticsResultSeason(int season) {
        return statisticsResultPerSeason.get(season);
    }

    public Object statisticsResultSeasonMatchday(int season, int matchday) {
        return statisticsResultPerSeasonMatchday.get(season + "_" + matchday);
    }

    @Dispose
    public void shutdown() {
        LOG.info("stopped season service.");
    }
}
