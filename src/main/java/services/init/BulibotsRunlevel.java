package services.init;

import java.time.LocalDateTime;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import configuration.BulibotConfig;
import model.match.Match.Status;
import model.user.BulibotVersion;
import model.user.User;
import ninja.utils.NinjaProperties;
import services.BulibotService;
import services.DataService;
import services.SeasonService;
import services.init.api.Runlevel;

// TODO add checks for all user bots??
@Singleton
public class BulibotsRunlevel implements Runlevel {

    @Inject
    private SeasonService seasonService;

    @Inject
    private BulibotService bulibotService;

    @Inject
    private DataService dataService;

    @Inject
    private NinjaProperties ninjaProperties;

    @Inject
    private BackgroundTasksRunlevel next;

    @Override
    public Runlevel init() {

        // get current data
        Integer currentSeason = seasonService.currentSeason();
        Integer currentMatchday = seasonService.currentMatchday();
        if (currentSeason == null || currentMatchday == null) {
            throw new IllegalStateException("current season / matchday is not initialized properly: " + currentSeason + " / " + currentMatchday);
        }

        // check if parrot is enabled
        if (ninjaProperties.getBooleanOrDie(BulibotConfig.PARROT_ENABLED.getKey())) {

            // ensure parrot
            String parrotName = ninjaProperties.getOrDie(BulibotConfig.PARROT_NAME.getKey());
            User parrot = dataService.userByEmail(parrotName);
            if (parrot == null) {

                // create user
                LocalDateTime now = LocalDateTime.now();
                parrot = new User();
                parrot.setId(dataService.nextUserId());
                parrot.setName(parrotName);
                parrot.setEmail(parrotName);
                parrot.setSince(now);
                parrot.setVerified(true);
                parrot.setVerifiedSince(now);
                parrot.setGenerated(true);
                String bulibotName = ninjaProperties.getOrDie(BulibotConfig.PARROT_BULIBOT_NAME.getKey());
                parrot.setBulibotName(bulibotName);

                // create bulibot version
                BulibotVersion parrotVersion = new BulibotVersion();
                parrotVersion.setName(bulibotName);
                parrotVersion.setSource(ninjaProperties.getOrDie(BulibotConfig.PARROT_SOURCE.getKey()));
                parrotVersion.setLive(true);
                parrot.getBulibotVersions().add(parrotVersion);

                // save
                dataService.user(parrot);
            }

            // check if current matchday already started
            boolean matchdayStarted = dataService.matches(currentSeason, currentMatchday).stream().filter(m -> m.getStatus() != Status.WAITING).count() > 0;

            // ensure all bulibot executions for parrot for current season and matchdays until current
            for (int i = 1; matchdayStarted ? i <= currentMatchday : i < currentMatchday; i++) {
                bulibotService.executeAndSave(currentSeason, i, parrot);
            }

            // re-create bulibot rankings
            bulibotService.updateRankings();
        }

        // done
        return next;
    }
}
