package model.statistics;

import model.community.BulibotExecution;
import model.match.Match;

/**
 * Enum to define a match winner.
 *
 * @author chris
 */
public enum Winner {
    ONE, DRAW, TWO;

    /**
     * Computes the winner for given match.
     *
     * @param match
     *            match to be analyzed
     * @return winner, or null in case of insufficient data.
     */
    public static Winner calc(Match match) {
        return calc(match.getGoalsTeamOneFullTime(), match.getGoalsTeamTwoFullTime());
    }

    /**
     * Computes the winner for given match statistics.
     *
     * @param match
     *            match statistics to be analyzed
     * @return winner, or null in case of insufficient data.
     */
    public static Winner calc(MatchStatistics match) {
        return calc(match.getGoalsTeamOne(), match.getGoalsTeamTwo());
    }

    /**
     * Computes the winner for given bulibot execution.
     *
     * @param bulibotExecution
     *            bulibot execution to be analyzed.
     * @return winner, or null in case of insufficient data.
     */
    public static Winner calc(BulibotExecution bulibotExecution) {
        return calc(bulibotExecution.getGoalsTeamOne(), bulibotExecution.getGoalsTeamTwo());
    }

    private static Winner calc(Integer goalsTeamOne, Integer goalsTeamTwo) {
        if (goalsTeamOne == null || goalsTeamTwo == null) {
            return null;
        } else if (goalsTeamOne > goalsTeamTwo) {
            return Winner.ONE;
        } else if (goalsTeamTwo > goalsTeamOne) {
            return Winner.TWO;
        } else {
            return Winner.DRAW;
        }
    }
}
