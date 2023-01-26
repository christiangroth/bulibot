package model.match;

public class Goal {

    private int order;
    private int goalsTeamOne;
    private int goalsTeamTwo;
    private int minute;
    private long goalGetterId;
    private String goalGetterName;
    private long goalGetterTeamId;
    private String goalGetterTeamName;
    private String goalGetterTeamDisplayName;
    private boolean penalty;
    private boolean ownGoal;
    private boolean overtime;

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getGoalsTeamOne() {
        return goalsTeamOne;
    }

    public void setGoalsTeamOne(int goalsTeamOne) {
        this.goalsTeamOne = goalsTeamOne;
    }

    public int getGoalsTeamTwo() {
        return goalsTeamTwo;
    }

    public void setGoalsTeamTwo(int goalsTeamTwo) {
        this.goalsTeamTwo = goalsTeamTwo;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public long getGoalGetterId() {
        return goalGetterId;
    }

    public void setGoalGetterId(long goalGetterId) {
        this.goalGetterId = goalGetterId;
    }

    public String getGoalGetterName() {
        return goalGetterName;
    }

    public void setGoalGetterName(String goalGetterName) {
        this.goalGetterName = goalGetterName;
    }

    public long getGoalGetterTeamId() {
        return goalGetterTeamId;
    }

    public void setGoalGetterTeamId(long goalGetterTeamId) {
        this.goalGetterTeamId = goalGetterTeamId;
    }

    public String getGoalGetterTeamName() {
        return goalGetterTeamName;
    }

    public void setGoalGetterTeamName(String goalGetterTeamName) {
        this.goalGetterTeamName = goalGetterTeamName;
    }

    public String getGoalGetterTeamDisplayName() {
        return goalGetterTeamDisplayName;
    }

    public void setGoalGetterTeamDisplayName(String goalGetterTeamDisplayName) {
        this.goalGetterTeamDisplayName = goalGetterTeamDisplayName;
    }

    public boolean isPenalty() {
        return penalty;
    }

    public void setPenalty(boolean penalty) {
        this.penalty = penalty;
    }

    public boolean isOwnGoal() {
        return ownGoal;
    }

    public void setOwnGoal(boolean ownGoal) {
        this.ownGoal = ownGoal;
    }

    public boolean isOvertime() {
        return overtime;
    }

    public void setOvertime(boolean overtime) {
        this.overtime = overtime;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + order;
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
        Goal other = (Goal) obj;
        if (order != other.order) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Goal [order=" + order + ", goalsTeamOne=" + goalsTeamOne + ", goalsTeamTwo=" + goalsTeamTwo + ", minute=" + minute + ", goalGetterId=" + goalGetterId
                + ", goalGetterName=" + goalGetterName + ", goalGetterTeamId=" + goalGetterTeamId + ", goalGetterTeamName=" + goalGetterTeamName + ", penalty=" + penalty
                + ", ownGoal=" + ownGoal + ", overtime=" + overtime + "]";
    }
}
