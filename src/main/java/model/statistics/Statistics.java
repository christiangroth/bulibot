package model.statistics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import model.match.Match;
import model.match.Match.Status;
import model.statistics.result.StatisticsResult;
import model.statistics.result.StatisticsResultBuilder;

/**
 * Basic statistics class containing finished or in progress matches to build new {@link StatisticsResult}s using {@link #builder()} method.
 *
 * @author chris
 */
public class Statistics {

    private Set<MatchStatistics> matches = new HashSet<>();

    /**
     * Default constructor, used for JSON deserialization only.
     */
    public Statistics() {

    }

    /**
     * Copy-Constructor.
     *
     * @param source
     *            copy source
     */
    public Statistics(Statistics source) {
        for (MatchStatistics match : source.matches) {
            matches.add(new MatchStatistics(match));
        }
    }

    /**
     * If the given match is in progress or finished and has a valid result, it will be converted to {@link MatchStatistics} and used in this statistics base
     * data.
     *
     * @param match
     *            match to be added
     */
    public void update(Match match) {

        // skip not started matches
        if (match.getStatus() == Status.WAITING || match.getGoalsTeamOneFullTime() == null || match.getGoalsTeamTwoFullTime() == null) {
            return;
        }

        // create match statistics and replace in set
        MatchStatistics matchStatistics = new MatchStatistics(match);
        matches.remove(matchStatistics);
        matches.add(matchStatistics);
    }

    /**
     * Creates a new {@link StatisticsResultBuilder}. This method may be called as often as needed on same statistics instance.
     *
     * @return new result builder
     */
    public StatisticsResultBuilder builder() {
        return new StatisticsResultBuilder(new ArrayList<>(matches));
    }
}
