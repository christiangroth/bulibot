package model.statistics;

import java.util.ArrayList;
import java.util.List;

import model.match.Goal;
import model.match.Match;

/**
 * Basic metadata POJO for a finished or in progress match with a valid result.
 *
 * @author chris
 */
public class MatchStatistics extends MatchMetadata {

    private final int goalsTeamOne;
    private final int goalsTeamTwo;
    private final Integer goalsTeamOneFirstHalf;
    private final Integer goalsTeamTwoFirstHalf;
    private final Integer goalsTeamOneSecondHalf;
    private final Integer goalsTeamTwoSecondHalf;
    private final List<GoalStatistics> goals = new ArrayList<>();
    private final Winner winner;

    /**
     * Copy-Constructor.
     *
     * @param source
     *            copy source
     */
    public MatchStatistics(MatchStatistics source) {
        super(source);

        goalsTeamOne = source.goalsTeamOne;
        goalsTeamTwo = source.goalsTeamTwo;
        goalsTeamOneFirstHalf = source.goalsTeamOneFirstHalf;
        goalsTeamTwoFirstHalf = source.goalsTeamTwoFirstHalf;
        goalsTeamOneSecondHalf = source.goalsTeamOneSecondHalf;
        goalsTeamTwoSecondHalf = source.goalsTeamTwoSecondHalf;
        for (GoalStatistics sourceGoal : source.goals) {
            goals.add(new GoalStatistics(sourceGoal));
        }
        winner = source.winner;
    }

    /**
     * Creates statistics from given match
     *
     * @param match
     *            match to be converted
     */
    public MatchStatistics(Match match) {
        super(match);

        // calculate results
        goalsTeamOne = match.getGoalsTeamOneFullTime();
        goalsTeamTwo = match.getGoalsTeamTwoFullTime();
        boolean hasHalfTimeResult = match.getGoalsTeamOneHalfTime() != null && match.getGoalsTeamTwoHalfTime() != null;
        goalsTeamOneFirstHalf = hasHalfTimeResult ? match.getGoalsTeamOneHalfTime() : null;
        goalsTeamTwoFirstHalf = hasHalfTimeResult ? match.getGoalsTeamTwoHalfTime() : null;
        goalsTeamOneSecondHalf = hasHalfTimeResult ? goalsTeamOne - goalsTeamOneFirstHalf : null;
        goalsTeamTwoSecondHalf = hasHalfTimeResult ? goalsTeamTwo - goalsTeamTwoFirstHalf : null;

        // add goals
        if (match.getGoals() != null) {
            for (Goal goal : match.getGoals()) {

                // ignore own goals
                if (!goal.isOwnGoal()) {
                    goals.add(new GoalStatistics(goal));
                }
            }
        }

        // calculate winner
        winner = Winner.calc(match);
    }

    /**
     * Returns goals scored by team one.
     *
     * @return goals scored by team one
     */
    public int getGoalsTeamOne() {
        return goalsTeamOne;
    }

    /**
     * Returns goals scored by team two.
     *
     * @return goals scored by team two
     */
    public int getGoalsTeamTwo() {
        return goalsTeamTwo;
    }

    /**
     * Returns number of goals for team one scored in first half.
     *
     * @return goals for team one scored in first half, or null in case of insufficient data.
     */
    public Integer getGoalsTeamOneFirstHalf() {
        return goalsTeamOneFirstHalf;
    }

    /**
     * Returns number of goals for team two scored in first half.
     *
     * @return goals for team two scored in first half, or null in case of insufficient data.
     */
    public Integer getGoalsTeamTwoFirstHalf() {
        return goalsTeamTwoFirstHalf;
    }

    /**
     * Returns number of goals for team one scored in second half.
     *
     * @return goals for team one scored in second half, or null in case of insufficient data.
     */
    public Integer getGoalsTeamOneSecondHalf() {
        return goalsTeamOneSecondHalf;
    }

    /**
     * Returns number of goals for team two scored in second half.
     *
     * @return goals for team two scored in second half, or null in case of insufficient data.
     */
    public Integer getGoalsTeamTwoSecondHalf() {
        return goalsTeamTwoSecondHalf;
    }

    /**
     * Returns all goals, if any.
     *
     * @return goals, never null
     */
    public List<GoalStatistics> getGoals() {
        return goals;
    }

    /**
     * Retrurn the winner.
     *
     * @return winner
     */
    public Winner getWinner() {
        return winner;
    }

    @Override
    public String toString() {
        return "MatchStatistics [season=" + season + ", matchday=" + matchday + ", lastUpdateTimeString=" + lastUpdateTimeString + ", matchId=" + matchId + ", matchAssignedTime="
                + matchAssignedTime + ", matchDayOfWeekAndTime=" + matchDayOfWeekAndTime + ", matchDayOfWeek=" + matchDayOfWeek + ", matchTime=" + matchTime + ", teamOneName="
                + teamOneName + ", teamOneIconUrl=" + teamOneIconUrl + ", teamTwoName=" + teamTwoName + ", teamTwoIconUrl=" + teamTwoIconUrl + ", goalsTeamOne=" + goalsTeamOne
                + ", goalsTeamTwo=" + goalsTeamTwo + ", goalsTeamOneFirstHalf=" + goalsTeamOneFirstHalf + ", goalsTeamTwoFirstHalf=" + goalsTeamTwoFirstHalf
                + ", goalsTeamOneSecondHalf=" + goalsTeamOneSecondHalf + ", goalsTeamTwoSecondHalf=" + goalsTeamTwoSecondHalf + ", goals=" + goals + ", winner=" + winner + "]";
    }
}
