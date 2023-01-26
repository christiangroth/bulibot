package services.testdata.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Singleton;

import model.match.Match;
import services.testdata.AbstractTestdataStrategy;

@Singleton
public class LastSeasonTestdataStrategy extends AbstractTestdataStrategy {

    private static final String CODE = "lastSeason";

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public boolean available() {

        // ensure season
        return seasonService.currentSeason() != null;
    }

    @Override
    public List<BulibotTestdata> data() {
        List<BulibotTestdata> data = new ArrayList<>();

        // ensure season
        Integer currentSeason = seasonService.currentSeason();
        if (currentSeason == null) {
            return data;
        }

        // add all past matchdays
        for (int i = 1; i <= Match.NUMBER_OF_MATCHDAYS_PER_SEASON; i++) {
            data.add(new BulibotTestdata(currentSeason - 1, i));
        }

        // done
        return data;
    }
}
