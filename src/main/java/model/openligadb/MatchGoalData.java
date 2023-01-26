package model.openligadb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchGoalData {

    @JsonProperty("GoalID")
    private long id;
    @JsonProperty("ScoreTeam1")
    private int pointsTeamOne;
    @JsonProperty("ScoreTeam2")
    private int pointsTeamTwo;
    @JsonProperty("MatchMinute")
    private int matchMinute;
    @JsonProperty("GoalGetterID")
    private long goalGetterId;
    @JsonProperty("GoalGetterName")
    private String goalGetterName;
    @JsonProperty("IsPenalty")
    private boolean penalty;
    @JsonProperty("IsOwnGoal")
    private boolean ownGoal;
    @JsonProperty("IsOvertime")
    private boolean overtime;
    @JsonProperty("Comment")
    private String comment;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPointsTeamOne() {
        return pointsTeamOne;
    }

    public void setPointsTeamOne(int pointsTeamOne) {
        this.pointsTeamOne = pointsTeamOne;
    }

    public int getPointsTeamTwo() {
        return pointsTeamTwo;
    }

    public void setPointsTeamTwo(int pointsTeamTwo) {
        this.pointsTeamTwo = pointsTeamTwo;
    }

    public int getMatchMinute() {
        return matchMinute;
    }

    public void setMatchMinute(int matchMinute) {
        this.matchMinute = matchMinute;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "MatchGoalData [id=" + id + ", pointsTeamOne=" + pointsTeamOne + ", pointsTeamTwo=" + pointsTeamTwo + ", matchMinute=" + matchMinute + ", goalGetterId="
                + goalGetterId + ", goalGetterName=" + goalGetterName + ", penalty=" + penalty + ", ownGoal=" + ownGoal + ", overtime=" + overtime + ", comment=" + comment + "]";
    }
}
