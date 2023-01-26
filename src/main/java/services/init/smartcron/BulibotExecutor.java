package services.init.smartcron;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import configuration.SmartcronConfig;
import de.chrgroth.smartcron.api.Smartcron;
import de.chrgroth.smartcron.api.SmartcronExecutionContext;
import model.match.Match;
import model.match.Match.Status;
import ninja.metrics.Timed;
import ninja.utils.NinjaProperties;
import services.BulibotService;
import services.DataService;

public class BulibotExecutor implements Smartcron {
    private static final Logger LOG = LoggerFactory.getLogger(BulibotExecutor.class);

    @Inject
    private BulibotService bulibotService;

    @Inject
    private DataService dataService;

    @Inject
    private NinjaProperties ninjaProperties;

    @Timed
    @Override
    public LocalDateTime run(SmartcronExecutionContext context) {

        // get next todays unfinished match
        LocalDateTime now = LocalDateTime.now();
        List<Match> matchesToday = dataService.matchesToday(now, true, 1);
        Match todaysNextMatch = matchesToday.isEmpty() ? null : matchesToday.get(0);
        if (todaysNextMatch == null) {

            // no waiting matches for today, run tomorrow again with configured hour and minute
            context.setMode("noMatchesToday");
            LOG.info("no matches today.");
            return executeTomorrow(now);
        }

        // get season and matchday
        Integer season = todaysNextMatch.getSeason();
        Integer matchday = todaysNextMatch.getMatchday();
        if (season == null || matchday == null) {

            // incomplete data, run tomorrow again with configured hour and minute
            context.setMode("incompleteData");
            LOG.error("incomplete match data: " + season + " / " + matchday);
            return executeTomorrow(now);
        }

        // check match threshold
        LocalDateTime assignedTime = todaysNextMatch.getAssignedTime();
        long secondsUntilMatch = Duration.between(now, assignedTime).getSeconds();
        Integer matchThreshold = ninjaProperties.getIntegerWithDefault(SmartcronConfig.EXECUTIONS_MATCH_THRESHOLD.getKey(), 43200);
        if (secondsUntilMatch > matchThreshold) {

            // run again if threshold is reached
            context.setMode("waitingForMatchThreshold");
            LOG.info("waiting for match threshold.");
            return assignedTime.minusSeconds(matchThreshold);
        }

        // create executions
        LOG.info("executing bulibots...");
        bulibotService.executeSaveAndExport(season, matchday);
        LOG.info("bulibot executions processed.");
        context.setMode("executionsCreated");

        // done
        return executeTomorrow(now);
    }

    private LocalDateTime executeTomorrow(LocalDateTime now) {
        return now.plus(1, ChronoUnit.DAYS).with(ChronoField.HOUR_OF_DAY, ninjaProperties.getIntegerWithDefault(SmartcronConfig.EXECUTIONS_DAILY_HOUR.getKey(), 8))
                .with(ChronoField.MINUTE_OF_HOUR, ninjaProperties.getIntegerWithDefault(SmartcronConfig.EXECUTIONS_DAILY_MINUTE.getKey(), 0)).with(ChronoField.SECOND_OF_MINUTE, 0)
                .with(ChronoField.MILLI_OF_SECOND, 0).with(ChronoField.NANO_OF_SECOND, 0);
    }

    @Override
    public boolean abortOnException() {
        return false;
    }

    @Override
    public LocalDateTime recover() {
        return delay(ninjaProperties.getIntegerWithDefault(SmartcronConfig.EXECUTIONS_RECOVERY.getKey(), 3600), ChronoUnit.SECONDS);
    }
}
