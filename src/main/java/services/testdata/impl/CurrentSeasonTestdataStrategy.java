package services.testdata.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Singleton;

import services.testdata.AbstractTestdataStrategy;

@Singleton
public class CurrentSeasonTestdataStrategy extends AbstractTestdataStrategy {

    private static final String CODE = "currentSeason";

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public boolean available() {
        return atLeastMatchdayFinished(1);
    }

    @Override
    public List<BulibotTestdata> data() {
        List<BulibotTestdata> data = new ArrayList<>();

        // ensure season
        Integer currentSeason = seasonService.currentSeason();
        if (currentSeason == null) {
            return data;
        }

        // ensure matchday
        Integer currentMatchday = seasonService.currentMatchday();
        if (currentMatchday == null) {
            return data;
        }

        // add all past matchdays
        for (int i = 1; i < currentMatchday; i++) {
            data.add(new BulibotTestdata(currentSeason, i));
        }

        // check if current matchday is finished
        if (matchdayFinished(currentSeason, currentMatchday)) {
            data.add(new BulibotTestdata(currentSeason, currentMatchday));
        }

        // done
        return data;
    }
}
