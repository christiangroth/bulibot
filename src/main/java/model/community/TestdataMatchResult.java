package model.community;

import java.time.LocalDateTime;
import java.util.Map;

import model.match.Match;

public class TestdataMatchResult {

    // match data
    private final LocalDateTime assignedTime;
    private final long teamOneId;
    private final String teamOneName;
    private final String teamOneIconUrl;
    private final long teamTwoId;
    private final String teamTwoName;
    private final String teamTwoIconUrl;
    private final Integer goalsTeamOneHalfTime;
    private final Integer goalsTeamTwoHalfTime;
    private final Integer goalsTeamOneFullTime;
    private final Integer goalsTeamTwoFullTime;

    // bulibot execution data
    private final Long bulibotExecutionDuration;
    private final Integer bulibotExecutionGoalsTeamOne;
    private final Integer bulibotExecutionGoalsTeamTwo;
    private final String bulibotExecutionErrorCauseType;
    private final String bulibotExecutionErrorCauseMessage;
    private final String bulibotExecutionStdout;
    private final Map<String, String> bulibotExecutionState;

    // result data
    private final boolean error;
    private final boolean exactHit;
    private final boolean relativeHit;
    private final boolean winnerHit;
    private final boolean noHit;
    private final int points;

    public TestdataMatchResult(Match match, BulibotExecution bulibotExecution, Result result, int points) {
        assignedTime = match.getAssignedTime();
        teamOneId = match.getTeamOneId();
        teamOneName = match.getTeamOneDisplayName();
        teamOneIconUrl = match.getTeamOneIconUrl();
        teamTwoId = match.getTeamTwoId();
        teamTwoName = match.getTeamTwoDisplayName();
        teamTwoIconUrl = match.getTeamTwoIconUrl();
        goalsTeamOneHalfTime = match.getGoalsTeamOneHalfTime();
        goalsTeamTwoHalfTime = match.getGoalsTeamTwoHalfTime();
        goalsTeamOneFullTime = match.getGoalsTeamOneFullTime();
        goalsTeamTwoFullTime = match.getGoalsTeamTwoFullTime();
        bulibotExecutionDuration = bulibotExecution.getDuration();
        bulibotExecutionGoalsTeamOne = bulibotExecution.getGoalsTeamOne();
        bulibotExecutionGoalsTeamTwo = bulibotExecution.getGoalsTeamTwo();
        bulibotExecutionErrorCauseType = bulibotExecution.getErrorCauseType();
        bulibotExecutionErrorCauseMessage = bulibotExecution.getErrorCauseMessage();
        bulibotExecutionStdout = bulibotExecution.getStdout();
        bulibotExecutionState = bulibotExecution.getState();
        error = result == Result.ERROR;
        exactHit = result == Result.EXACT;
        relativeHit = result == Result.RELATIVE;
        winnerHit = result == Result.WINNER;
        noHit = result == Result.WRONG;
        this.points = points;
    }

    public LocalDateTime getAssignedTime() {
        return assignedTime;
    }

    public long getTeamOneId() {
        return teamOneId;
    }

    public String getTeamOneName() {
        return teamOneName;
    }

    public String getTeamOneIconUrl() {
        return teamOneIconUrl;
    }

    public long getTeamTwoId() {
        return teamTwoId;
    }

    public String getTeamTwoName() {
        return teamTwoName;
    }

    public String getTeamTwoIconUrl() {
        return teamTwoIconUrl;
    }

    public Integer getGoalsTeamOneHalfTime() {
        return goalsTeamOneHalfTime;
    }

    public Integer getGoalsTeamTwoHalfTime() {
        return goalsTeamTwoHalfTime;
    }

    public Integer getGoalsTeamOneFullTime() {
        return goalsTeamOneFullTime;
    }

    public Integer getGoalsTeamTwoFullTime() {
        return goalsTeamTwoFullTime;
    }

    public Long getBulibotExecutionDuration() {
        return bulibotExecutionDuration;
    }

    public Integer getBulibotExecutionGoalsTeamOne() {
        return bulibotExecutionGoalsTeamOne;
    }

    public Integer getBulibotExecutionGoalsTeamTwo() {
        return bulibotExecutionGoalsTeamTwo;
    }

    public String getBulibotExecutionErrorCauseType() {
        return bulibotExecutionErrorCauseType;
    }

    public String getBulibotExecutionErrorCauseMessage() {
        return bulibotExecutionErrorCauseMessage;
    }

    public String getBulibotExecutionStdout() {
        return bulibotExecutionStdout;
    }

    public Map<String, String> getBulibotExecutionState() {
        return bulibotExecutionState;
    }

    public boolean isError() {
        return error;
    }

    public boolean isExactHit() {
        return exactHit;
    }

    public boolean isRelativeHit() {
        return relativeHit;
    }

    public boolean isWinnerHit() {
        return winnerHit;
    }

    public boolean isNoHit() {
        return noHit;
    }

    public int getPoints() {
        return points;
    }
}
