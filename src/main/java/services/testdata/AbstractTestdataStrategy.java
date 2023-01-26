package services.testdata;

import java.util.List;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import model.match.Match;
import model.match.Match.Status;
import services.DataService;
import services.SeasonService;

public abstract class AbstractTestdataStrategy implements BulibotTestdataStrategy {

    @Inject
    protected SeasonService seasonService;

    @Inject
    protected DataService dataService;

    protected boolean atLeastMatchdayFinished(int matchday) {

        // ensure season
        Integer currentSeason = seasonService.currentSeason();
        if (currentSeason == null) {
            return false;
        }

        // ensure matchday
        Integer currentMatchday = seasonService.currentMatchday();
        if (currentMatchday == null) {
            return false;
        }

        // check matchday
        if (currentMatchday > matchday) {
            return true;
        } else if (currentMatchday < matchday) {
            return false;
        }

        // check if matchday is finished
        return matchdayFinished(currentSeason, currentMatchday);
    }

    protected boolean matchdayFinished(int season, int matchday) {
        List<Match> matches = dataService.matches(season, matchday);
        return matches != null && !matches.isEmpty() && matches.stream().filter(m -> m.getStatus() != Status.FINISHED).collect(Collectors.toList()).isEmpty();
    }
}
