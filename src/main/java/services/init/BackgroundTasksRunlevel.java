package services.init;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import services.SmartcronService;
import services.init.api.Runlevel;
import services.init.smartcron.BulibotExecutor;
import services.init.smartcron.OpenligaDbSynchronizer;

@Singleton
public class BackgroundTasksRunlevel implements Runlevel {

    @Inject
    private OpenligaDbSynchronizer openligaDbSynchronizer;

    @Inject
    private BulibotExecutor bulibotExecutor;

    @Inject
    private SmartcronService smartcronService;

    @Override
    public Runlevel init() {

        // schedule all smartcrons
        smartcronService.schedule(openligaDbSynchronizer);
        smartcronService.schedule(bulibotExecutor);

        // done
        return null;
    }
}
