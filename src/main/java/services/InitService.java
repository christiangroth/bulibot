package services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import configuration.NotificationEventConfig;
import ninja.lifecycle.Dispose;
import ninja.lifecycle.Start;
import services.init.InitialDataRunlevel;
import services.init.api.Runlevel;
import util.TimerUtils;

@Singleton
public class InitService {
    private static final Logger LOG = LoggerFactory.getLogger(InitService.class);

    private static final int SERVICE_ORDER = 90;

    @Inject
    private InitialDataRunlevel initialDataRunlevel;

    @Inject
    private NotificationService notificationService;

    @Start(order = SERVICE_ORDER)
    public void startup() {

        // notify
        LOG.info("started init service.");

        // initialize current runlevel until no next level is configured
        long duration = TimerUtils.measure(() -> {
            LOG.info("### ##### ##### ##### ##### #####");
            LOG.info("### initializing system runlevels");
            LOG.info("### ##### ##### ##### ##### #####");

            Runlevel level = initialDataRunlevel;
            while (level != null) {

                // initialize and get next level
                notificationService.send(NotificationEventConfig.SYSTEM_STARTUP_RUNLEVEL, level.getClass().getSimpleName());
                LOG.info("### ##### ##### ##### ##### #####");
                LOG.info("### entering " + level.getClass().getSimpleName() + "...");
                LOG.info("### ##### ##### ##### ##### #####");
                level = level.init();
            }
        });

        // done
        LOG.info("### ##### ##### ##### ##### #####");
        LOG.info("### system initialized in " + duration + "ms.");
        LOG.info("### ##### ##### ##### ##### #####");
        notificationService.send(NotificationEventConfig.SYSTEM_STARTUP);
    }

    @Dispose
    public void shutdown() {

        // notify
        notificationService.send(NotificationEventConfig.SYSTEM_SHUTDOWN);
        LOG.info("stopped init service.");
    }
}
