package services.init;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import services.SmartcronService;
import services.init.api.Runlevel;
import services.init.smartcron.SeasonAndMatchdayUpdater;

// TODO run once and then start smartcron which runs once directly ... this is somehow bad!!
@Singleton
public class CurrentSeasonAndMatchdayRunlevel implements Runlevel {

    @Inject
    private SmartcronService smartcronService;

    @Inject
    private SeasonAndMatchdayUpdater seasonAndMatchdayUpdater;

    @Inject
    private BulibotsRunlevel next;

    @Override
    public Runlevel init() {

        // run once
        seasonAndMatchdayUpdater.execute();

        // activate smartcron
        smartcronService.schedule(seasonAndMatchdayUpdater);

        // done
        return next;
    }
}
