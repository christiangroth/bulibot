package services.init;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import configuration.DataConfig;
import model.match.Match;
import model.openligadb.MatchData;
import ninja.utils.NinjaProperties;
import services.DataService;
import services.OpenligaDbService;
import services.init.api.Runlevel;

@Singleton
public class InitialDataRunlevel implements Runlevel {
    private static final Logger LOG = LoggerFactory.getLogger(InitialDataRunlevel.class);

    @Inject
    private DataService dataService;

    @Inject
    private OpenligaDbService openligaDbRestService;

    @Inject
    private NinjaProperties ninjaProperties;

    @Inject
    private CurrentSeasonAndMatchdayRunlevel next;

    @Override
    public Runlevel init() {

        // compute initial season
        Integer firstSeason = ninjaProperties.getIntegerOrDie(DataConfig.DATA_FIRST_SEASON.getKey());
        Integer thisYear = Calendar.getInstance().get(Calendar.YEAR);
        if (firstSeason > thisYear) {
            throw new IllegalStateException("misconfigured first season, must be <= current year");
        }

        // load initial data, if not present
        List<MatchData> synchronizedMatchData = new ArrayList<>();
        int syncSeason = firstSeason;
        do {

            // check if data is present
            if (dataService.matches(syncSeason).isEmpty()) {

                // load data
                LOG.info("loading initial data for season " + syncSeason);
                synchronizedMatchData.addAll(openligaDbRestService.retrieveMatchData(Match.LEAGUE_BUNDESLIGA_FIRST, syncSeason));
            }

            // increment
            syncSeason++;
        } while (syncSeason <= thisYear);

        // update storage
        openligaDbRestService.update(synchronizedMatchData);

        // done
        return next;
    }
}
