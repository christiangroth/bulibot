package model.statistics.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import model.statistics.MatchStatistics;
import model.statistics.Winner;

/**
 * Represents an Elo team ranking.
 *
 * @see EloHelper#compute(int, int, Winner, int)
 * @author chris
 */
public class EloRanking {

    private final List<EloRank> ranks = new ArrayList<>();

    private static class RankComparator implements Comparator<EloRank> {

        @Override
        public int compare(EloRank o1, EloRank o2) {

            // compare elo score
            int o1Score = o1.getScore();
            int o2Score = o2.getScore();
            if (o1Score != o2Score) {
                return compare(o2Score, o1Score);
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
     * @param eloHelper
     *            configured helper to be able to adjust Elo rankings as needed
     * @param matches
     *            matches to compute ranking from
     */
    public EloRanking(EloHelper eloHelper, List<MatchStatistics> matches) {

        // collect data
        matches.forEach(m -> addMatch(eloHelper, m));

        // set positions values
        Collections.sort(ranks, new RankComparator());
        for (int i = 0; i < ranks.size(); i++) {
            ranks.get(i).setPosition(i + 1);
        }
    }

    private void addMatch(EloHelper eloHelper, MatchStatistics match) {

        // search for team ranks
        EloRank teamOneRank = getRank(match.getTeamOneId());
        EloRank teamTwoRank = getRank(match.getTeamTwoId());

        // create new ranks if needed
        if (teamOneRank == null) {
            teamOneRank = new EloRank(match.getTeamOneId(), match.getTeamOneName(), match.getTeamOneDisplayName(), match.getTeamOneIconUrl(), eloHelper.getInitialRankScore());
            ranks.add(teamOneRank);
        }
        if (teamTwoRank == null) {
            teamTwoRank = new EloRank(match.getTeamTwoId(), match.getTeamTwoName(), match.getTeamTwoDisplayName(), match.getTeamTwoIconUrl(), eloHelper.getInitialRankScore());
            ranks.add(teamTwoRank);
        }

        // compute new scores
        Winner winner = Winner.calc(match);
        int goalsDiff = Math.abs(match.getGoalsTeamOne() - match.getGoalsTeamTwo());
        int[] newScores = eloHelper.compute(teamOneRank.getScore(), teamTwoRank.getScore(), winner, goalsDiff);

        // update ranks
        teamOneRank.updateScore(newScores[0]);
        teamTwoRank.updateScore(newScores[1]);
    }

    /**
     * Returns the position for given team id.
     *
     * @param teamId
     *            team id
     * @return position or -1 if team not found
     */
    public int getPosition(long teamId) {
        EloRank rank = getRank(teamId);
        return rank != null ? rank.getPosition() : -1;
    }

    /**
     * Returns the rank for given team id.
     *
     * @param teamId
     *            team id
     * @return rank or null if team not found
     */
    public EloRank getRank(long teamId) {
        return ranks.stream().filter(r -> r.getTeamId() == teamId).findFirst().orElse(null);
    }

    /**
     * Returns all ranks ordered by position.
     *
     * @return ranks, never null
     */
    public List<EloRank> getRanks() {
        return new ArrayList<>(ranks);
    }
}
