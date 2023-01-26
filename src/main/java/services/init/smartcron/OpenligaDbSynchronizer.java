package services.init.smartcron;

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
import ninja.metrics.Timed;
import ninja.utils.NinjaProperties;
import services.DataExportService;
import services.DataService;
import services.OpenligaDbService;
import services.SeasonService;

public class OpenligaDbSynchronizer implements Smartcron {
    private static final Logger LOG = LoggerFactory.getLogger(OpenligaDbSynchronizer.class);

    private static enum OperationMode {
        UNINITIALIZED, NOTHING_TODAY, MATCH_TODAY, MATCH_IN_PROGRESS;
    }

    private OperationMode operationMode = OperationMode.UNINITIALIZED;

    @Inject
    private OpenligaDbService openligaDbRestService;

    @Inject
    private DataExportService dataExportService;

    @Inject
    private DataService dataService;

    @Inject
    private SeasonService seasonService;

    @Inject
    private NinjaProperties ninjaProperties;

    @Timed
    @Override
    public LocalDateTime run(SmartcronExecutionContext context) {

        // sync current season
        Integer currentSeason = seasonService.currentSeason();
        LOG.info("synchronizing openligadb data for season " + currentSeason);
        openligaDbRestService.synchronize(currentSeason);

        // done
        return caluclateNextExecution();
    }

    private LocalDateTime caluclateNextExecution() {

        // now
        LocalDateTime now = LocalDateTime.now();

        // get matches same day not finished
        List<Match> matchesToday = dataService.matchesToday(LocalDateTime.now(), true, 1);
        Match todaysNextUnfinishedMatch = matchesToday.isEmpty() ? null : matchesToday.get(0);
        if (todaysNextUnfinishedMatch != null) {

            // check if match already started
            if (now.isAfter(todaysNextUnfinishedMatch.getAssignedTime())) {

                // fast check of live data
                LOG.info("unfinished match today currently in progress...");
                operationMode = OperationMode.MATCH_IN_PROGRESS;
                return delay(ninjaProperties.getIntegerWithDefault(SmartcronConfig.OPENLIGADB_DELAY_MATCH_IN_PROGRESS.getKey(), 60), ChronoUnit.SECONDS);
            } else {

                // check if we already had matches in progress today and they ended, so we can export the updated data
                if (operationMode == OperationMode.MATCH_IN_PROGRESS) {
                    dataExportService.slackExportCurrentMatchdayResults();
                }

                // next sync after match started including configured delay
                LOG.info("unfinished match today not started yet.");
                operationMode = OperationMode.MATCH_TODAY;
                return todaysNextUnfinishedMatch.getAssignedTime().plus(ninjaProperties.getIntegerWithDefault(SmartcronConfig.OPENLIGADB_DELAY_MATCH_IN_PROGRESS.getKey(), 60),
                        ChronoUnit.SECONDS);
            }
        } else {

            // check if we already had matches in progress today and they ended, so we can export the updated data
            if (operationMode == OperationMode.MATCH_IN_PROGRESS) {
                dataExportService.slackExportCurrentMatchdayResults();
            }

            // no unfinished matches for today, run tomorrow again with configured hour and minute
            LOG.info("no unfinished match today.");
            operationMode = OperationMode.NOTHING_TODAY;
            return now.plus(1, ChronoUnit.DAYS).with(ChronoField.HOUR_OF_DAY, ninjaProperties.getIntegerWithDefault(SmartcronConfig.OPENLIGADB_DAILY_HOUR.getKey(), 6))
                    .with(ChronoField.MINUTE_OF_HOUR, ninjaProperties.getIntegerWithDefault(SmartcronConfig.OPENLIGADB_DAILY_MINUTE.getKey(), 0))
                    .with(ChronoField.SECOND_OF_MINUTE, 0).with(ChronoField.MILLI_OF_SECOND, 0).with(ChronoField.NANO_OF_SECOND, 0);
        }
    }

    @Override
    public boolean abortOnException() {
        return false;
    }

    @Override
    public LocalDateTime recover() {
        return delay(ninjaProperties.getIntegerWithDefault(SmartcronConfig.OPENLIGADB_RECOVERY.getKey(), 3600), ChronoUnit.SECONDS);
    }
}
