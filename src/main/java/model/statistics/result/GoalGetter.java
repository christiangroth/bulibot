package model.statistics.result;

import model.statistics.GoalStatistics;

/**
 * Basic POJO for goal getter data.
 *
 * @author chris
 */
public class GoalGetter {

    private int position;
    private final long id;
    private final String name;
    private final long teamId;
    private final String teamName;
    private final String teamDisplayName;
    private final String teamIconUrl;
    private int matches;
    private int goals;
    private int penaltyGoals;
    private int overtimeGoals;

    /**
     * Creates a new instance with given key data.
     *
     * @param id
     *            goal getter id
     * @param name
     *            goal getter name
     * @param teamId
     *            team id
     * @param teamName
     *            team name
     * @param teamDisplayName
     *            team display name
     * @param teamIconUrl
     *            team icon url
     */
    public GoalGetter(long id, String name, long teamId, String teamName, String teamDisplayName, String teamIconUrl) {
        this.id = id;
        this.name = name;
        this.teamId = teamId;
        this.teamName = teamName;
        this.teamDisplayName = teamDisplayName;
        this.teamIconUrl = teamIconUrl;
    }

    void addGoalValues(GoalStatistics goal) {
        goals++;
        if (goal.isPenalty()) {
            penaltyGoals++;
        }
        if (goal.isOvertime()) {
            overtimeGoals++;
        }
    }

    /**
     * Returns the position.
     *
     * @return position
     */
    public int getPosition() {
        return position;
    }

    void setPosition(int position) {
        this.position = position;
    }

    /**
     * Returns the goal getter id.
     *
     * @return id
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the goal getter name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the team id.
     *
     * @return team id
     */
    public long getTeamId() {
        return teamId;
    }

    /**
     * Returns the team name.
     *
     * @return team name
     */
    public String getTeamName() {
        return teamName;
    }

    /**
     * Returns the teams display name.
     *
     * @return team display name
     */
    public String getTeamDisplayName() {
        return teamDisplayName;
    }

    /**
     * Returns the team icon URL.
     *
     * @return team icon URL
     */
    public String getTeamIconUrl() {
        return teamIconUrl;
    }

    /**
     * Returns the number of matches in which the goal getter scored.
     *
     * @return number of matches.
     */
    public int getMatches() {
        return matches;
    }

    void setMatches(int matches) {
        this.matches = matches;
    }

    /**
     * Returns the overall number of goals.
     *
     * @return goals
     */
    public int getGoals() {
        return goals;
    }

    /**
     * Returns the number of penalty goals.
     *
     * @return penalty goals
     */
    public int getPenaltyGoals() {
        return penaltyGoals;
    }

    /**
     * Returns the number of overtime goals.
     *
     * @return overtime goals
     */
    public int getOvertimeGoals() {
        return overtimeGoals;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (teamName == null ? 0 : teamName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GoalGetter other = (GoalGetter) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (teamName == null) {
            if (other.teamName != null) {
                return false;
            }
        } else if (!teamName.equals(other.teamName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "GoalGetter [position=" + position + ", id=" + id + ", name=" + name + ", teamId=" + teamId + ", teamName=" + teamName + ", teamIconUrl=" + teamIconUrl
                + ", matches=" + matches + ", goals=" + goals + ", penaltyGoals=" + penaltyGoals + ", overtimeGoals=" + overtimeGoals + "]";
    }
}
