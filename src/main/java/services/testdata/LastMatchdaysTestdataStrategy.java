package services.testdata;

import java.util.ArrayList;
import java.util.List;

public abstract class LastMatchdaysTestdataStrategy extends AbstractTestdataStrategy {

    private final int lastMatchdays;

    protected LastMatchdaysTestdataStrategy(int lastMatchdays) {
        this.lastMatchdays = lastMatchdays;
    }

    @Override
    public boolean available() {
        return atLeastMatchdayFinished(lastMatchdays);
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

        // get last finished matchday
        int lastMatchday = matchdayFinished(currentSeason, currentMatchday) ? currentMatchday : currentMatchday - 1;

        // add data
        int firstMatchday = lastMatchday - (lastMatchdays - 1);
        for (int i = firstMatchday; i <= lastMatchday; i++) {
            data.add(new BulibotTestdata(currentSeason, i));
        }

        // done
        return data;
    }
}
