package model.statistics;

import model.match.Goal;

/**
 * Basic metadata POJO for a goal.
 *
 * @author chris
 */
public class GoalStatistics {

    private final long goalGetterId;
    private final String goalGetterName;
    private final long goalGetterTeamId;
    private final String goalGetterTeamName;
    private final String goalGetterTeamDisplayName;
    private final int minute;
    private final boolean penalty;
    private final boolean overtime;

    /**
     * Copy-Constructor.
     *
     * @param source
     *            copy source
     */
    public GoalStatistics(GoalStatistics source) {
        goalGetterId = source.goalGetterId;
        goalGetterName = source.goalGetterName;
        goalGetterTeamId = source.goalGetterTeamId;
        goalGetterTeamName = source.goalGetterTeamName;
        goalGetterTeamDisplayName = source.goalGetterTeamDisplayName;
        minute = source.minute;
        penalty = source.penalty;
        overtime = source.overtime;
    }

    /**
     * Creates statistics from given goal.
     *
     * @param goal
     *            goal to be converted
     */
    public GoalStatistics(Goal goal) {
        goalGetterId = goal.getGoalGetterId();
        goalGetterName = goal.getGoalGetterName();
        goalGetterTeamId = goal.getGoalGetterTeamId();
        goalGetterTeamName = goal.getGoalGetterTeamName();
        goalGetterTeamDisplayName = goal.getGoalGetterTeamDisplayName();
        minute = goal.getMinute();
        penalty = goal.isPenalty();
        overtime = goal.isOvertime();
    }

    /**
     * Returns the goal getter id.
     *
     * @return goal getter id
     */
    public long getGoalGetterId() {
        return goalGetterId;
    }

    /**
     * Returns goal getter name.
     *
     * @return goal getter name
     */
    public String getGoalGetterName() {
        return goalGetterName;
    }

    /**
     * Returns the goal getter team id.
     *
     * @return goal getter team id
     */
    public long getGoalGetterTeamId() {
        return goalGetterTeamId;
    }

    /**
     * Returns goal getter team name.
     *
     * @return goal getter team name
     */
    public String getGoalGetterTeamName() {
        return goalGetterTeamName;
    }

    /**
     * Returns goal getter teams display name.
     *
     * @return goal getter team display name
     */
    public String getGoalGetterTeamDisplayName() {
        return goalGetterTeamDisplayName;
    }

    /**
     * Returns the minute the goal was scored.
     *
     * @return goal minute
     */
    public int getMinute() {
        return minute;
    }

    /**
     * Returns true if the goal was scored by penalty.
     *
     * @return true if penalty, false otherwise
     */
    public boolean isPenalty() {
        return penalty;
    }

    /**
     * Returns true if the goal was scored in overtime.
     *
     * @return true if overtime, false otherwise
     */
    public boolean isOvertime() {
        return overtime;
    }

    @Override
    public String toString() {
        return "GoalStatistics [goalGetterId=" + goalGetterId + ", goalGetterName=" + goalGetterName + ", goalGetterTeamId=" + goalGetterTeamId + ", goalGetterTeamName="
                + goalGetterTeamName + ", minute=" + minute + ", penalty=" + penalty + ", overtime=" + overtime + "]";
    }
}
