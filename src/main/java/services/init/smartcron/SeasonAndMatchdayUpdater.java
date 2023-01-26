package services.init.smartcron;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import configuration.SmartcronConfig;
import de.chrgroth.smartcron.api.Smartcron;
import de.chrgroth.smartcron.api.SmartcronExecutionContext;
import ninja.metrics.Timed;
import ninja.utils.NinjaProperties;
import services.SeasonService;

public class SeasonAndMatchdayUpdater implements Smartcron {
    private static final Logger LOG = LoggerFactory.getLogger(SeasonAndMatchdayUpdater.class);

    @Inject
    private SeasonService seasonService;

    @Inject
    private NinjaProperties ninjaProperties;

    @Timed
    @Override
    public LocalDateTime run(SmartcronExecutionContext context) {

        // run
        LOG.info("updating current season and matchday...");
        execute();

        // schedule for next execution
        return caluclateNextExecution(context);
    }

    public void execute() {

        // update current season
        LocalDateTime now = LocalDateTime.now();
        seasonService.updateCurrentSeason(now);

        // update current matchday
        seasonService.updateCurrentMatchday(now);
    }

    private LocalDateTime caluclateNextExecution(SmartcronExecutionContext context) {

        // next full hour
        return delay(1, ChronoUnit.HOURS).with(ChronoField.MINUTE_OF_HOUR, 0).with(ChronoField.SECOND_OF_MINUTE, 0).with(ChronoField.MILLI_OF_SECOND, 0)
                .with(ChronoField.NANO_OF_SECOND, 0);
    }

    @Override
    public boolean abortOnException() {
        return false;
    }

    @Override
    public LocalDateTime recover() {
        return delay(ninjaProperties.getIntegerWithDefault(SmartcronConfig.CURRENTSEASONMATCHDAY_RECOVERY.getKey(), 3600), ChronoUnit.SECONDS);
    }
}
