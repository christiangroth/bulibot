package model.statistics.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import model.statistics.MatchStatistics;

/**
 * Represents a team ranking.
 *
 * @author chris
 */
public class Ranking {

    public static enum Mode {
        HOME(true, false), AWAY(false, true), FULL(true, true);

        private final boolean home;
        private final boolean away;

        private Mode(boolean home, boolean away) {
            this.home = home;
            this.away = away;
        }

        public boolean isHome() {
            return home;
        }

        public boolean isAway() {
            return away;
        }
    }

    private final Mode mode;
    private final List<Rank> ranks = new ArrayList<>();

    private static class RankComparator implements Comparator<Rank> {

        @Override
        public int compare(Rank o1, Rank o2) {

            // compare points
            int o1Points = o1.getPoints();
            int o2Points = o2.getPoints();
            if (o1Points != o2Points) {
                return compare(o2Points, o1Points);
            }

            // compare goal difference
            int o1GoalDifference = o1.getGoalsDifference();
            int o2GoalDifference = o2.getGoalsDifference();
            if (o1GoalDifference != o2GoalDifference) {
                return compare(o2GoalDifference, o1GoalDifference);
            }

            // compare goals
            int o1Goals = o1.getGoalsScored();
            int o2Goals = o2.getGoalsScored();
            if (o1Goals != o2Goals) {
                return compare(o2Goals, o1Goals);
            }

            // compare number of games
            int o1Games = o1.getGames();
            int o2Games = o2.getGames();
            if (o1Games != o2Games) {
                return compare(o1Games, o2Games);
            }

            // equal position, compare team names (must not be equal otherwise ranks will be considered same!!)
            return Long.valueOf(o1.getTeamId()).compareTo(o2.getTeamId());
        }

        int compare(int one, int two) {
            return Integer.valueOf(one).compareTo(Integer.valueOf(two));
        }
    }

    /**
     * Creates a new instance based on given matches.
     *
     * @param matches
     *            matches to compute ranking from
     * @param mode
     *            the ranking mode to be used for calulcation
     */
    public Ranking(List<MatchStatistics> matches, Mode mode) {

        // collect data
        this.mode = mode != null ? mode : Mode.FULL;
        matches.forEach(m -> addMatch(m));

        // set positions values
        Collections.sort(ranks, new RankComparator());
        for (int i = 0; i < ranks.size(); i++) {
            ranks.get(i).setPosition(i + 1);
        }
    }

    private void addMatch(MatchStatistics match) {

        // search for team ranks
        Rank teamOneRank = getRank(match.getTeamOneId());
        Rank teamTwoRank = getRank(match.getTeamTwoId());

        // create new ranks if needed
        if (teamOneRank == null) {
            teamOneRank = new Rank(match.getTeamOneId(), match.getTeamOneName(), match.getTeamOneDisplayName(), match.getTeamOneIconUrl());
            ranks.add(teamOneRank);
        }
        if (teamTwoRank == null) {
            teamTwoRank = new Rank(match.getTeamTwoId(), match.getTeamTwoName(), match.getTeamTwoDisplayName(), match.getTeamTwoIconUrl());
            ranks.add(teamTwoRank);
        }

        // add match values
        if (mode.isHome()) {
            teamOneRank.addMatchValues(match.getGoalsTeamOne(), match.getGoalsTeamTwo());
        }
        if (mode.isAway()) {
            teamTwoRank.addMatchValues(match.getGoalsTeamTwo(), match.getGoalsTeamOne());
        }
    }

    /**
     * Returns the ranking defined mode.
     *
     * @return ranking mode
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Returns the position for given team id.
     *
     * @param teamId
     *            team id
     * @return position or -1 if team not found
     */
    public int getPosition(long teamId) {
        Rank rank = getRank(teamId);
        return rank != null ? rank.getPosition() : -1;
    }

    /**
     * Returns the rank for given team id.
     *
     * @param teamId
     *            team id
     * @return rank or null if team not found
     */
    public Rank getRank(long teamId) {
        return ranks.stream().filter(r -> r.getTeamId() == teamId).findFirst().orElse(null);
    }

    /**
     * Returns all ranks ordered by position.
     *
     * @return ranks, never null
     */
    public List<Rank> getRanks() {
        return new ArrayList<>(ranks);
    }
}
