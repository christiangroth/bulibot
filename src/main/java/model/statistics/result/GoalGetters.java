package model.statistics.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.statistics.GoalStatistics;
import model.statistics.MatchStatistics;

/**
 * Represents a ranking of {@link GoalGetter} instances.
 *
 * @author chris
 */
public class GoalGetters {

    private final List<GoalGetter> goalGetter = new ArrayList<>();

    private static class GoalGetterComparator implements Comparator<GoalGetter> {

        @Override
        public int compare(GoalGetter g1, GoalGetter g2) {

            // compare goals
            int goalsOne = g1.getGoals();
            int goalsTwo = g2.getGoals();
            if (goalsOne != goalsTwo) {
                return compare(goalsTwo, goalsOne);
            }

            // compare penalty goals
            int penaltiesOne = g1.getPenaltyGoals();
            int penaltiesTwo = g2.getPenaltyGoals();
            if (penaltiesOne != penaltiesTwo) {
                return compare(penaltiesOne, penaltiesTwo);
            }

            // compare names (must not be equal otherwise ranks will be considered same!!)
            return Long.valueOf(g1.getId()).compareTo(g2.getId());
        }

        int compare(int one, int two) {
            return Integer.valueOf(one).compareTo(Integer.valueOf(two));
        }
    }

    /**
     * Creates a new instance based on given matches.
     *
     * @param matches
     *            matches to compute goal getter ranking from
     */
    public GoalGetters(List<MatchStatistics> matches) {

        // collect data
        matches.forEach(m -> addMatch(m));

        // set positions values
        Collections.sort(goalGetter, new GoalGetterComparator());
        for (int i = 0; i < goalGetter.size(); i++) {
            goalGetter.get(i).setPosition(i + 1);
        }
    }

    private void addMatch(MatchStatistics match) {

        // add all goals
        Set<GoalGetter> cache = new HashSet<>();
        for (GoalStatistics goal : match.getGoals()) {

            // old data does not contain goal informations
            if (goal.getGoalGetterId() < 1) {
                continue;
            }

            // search for goal getter
            GoalGetter goalGetter = ensureGoalGetter(match, goal.getGoalGetterId(), goal.getGoalGetterName(), goal.getGoalGetterTeamId(), goal.getGoalGetterTeamName(),
                    goal.getGoalGetterTeamDisplayName());
            if (!cache.contains(goalGetter)) {
                cache.add(goalGetter);
                goalGetter.setMatches(goalGetter.getMatches() + 1);
            }

            // update values
            goalGetter.addGoalValues(goal);
        }
    }

    private GoalGetter ensureGoalGetter(MatchStatistics match, long id, String name, long teamId, String teamName, String teamDisplayName) {

        // get team icon url
        String teamIconUrl;
        if (match.getTeamOneId() == teamId) {
            teamIconUrl = match.getTeamOneIconUrl();
        } else {
            teamIconUrl = match.getTeamTwoIconUrl();
        }

        // find or create
        GoalGetter goalGetter = goalGetter(id);
        if (goalGetter == null) {
            goalGetter = new GoalGetter(id, name, teamId, teamName, teamDisplayName, teamIconUrl);
            this.goalGetter.add(goalGetter);
        }

        // done
        return goalGetter;
    }

    /**
     * Finds the {@link GoalGetter} for given parameters.
     *
     * @param id
     *            goal getter id
     * @return goal getter, or null if not found
     */
    public GoalGetter goalGetter(long id) {
        return goalGetter.stream().filter(g -> g.getId() == id).findFirst().orElse(null);
    }

    /**
     * Returns all goals getters ordered by position.
     *
     * @return goal getters, never null
     */
    public List<GoalGetter> getGoalGetter() {
        return new ArrayList<>(goalGetter);
    }
}
