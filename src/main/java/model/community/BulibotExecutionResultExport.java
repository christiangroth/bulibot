package model.community;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.match.Match;
import util.DateUtils;

public class BulibotExecutionResultExport {
    private static final Logger LOG = LoggerFactory.getLogger(BulibotExecutionResultExport.class);

    private final long matchId;
    private final String assignedTime;
    private final long teamOneId;
    private final String teamOneName;
    private final String teamOneDisplayName;
    private final long teamTwoId;
    private final String teamTwoName;
    private final String teamTwoDisplayName;
    private final Integer goalsTeamOne;
    private final Integer goalsTeamTwo;
    private final String errorCauseType;
    private final String errorCauseMessage;

    public BulibotExecutionResultExport(Match match, Integer goalsTeamOne, Integer goalsTeamTwo, String errorCauseType, String errorCauseMessage) {
        matchId = match.getId();
        String assignedTime = null;
        try {
            if (match.getAssignedTime() != null) {
                assignedTime = DateUtils.format(match.getAssignedTime(), DateUtils.PATTERN_BULIBOT);
            }
        } catch (Exception e) {
            LOG.error("ignoring assigned time during export: " + e.getMessage());
        }
        this.assignedTime = assignedTime;
        teamOneId = match.getTeamOneId();
        teamOneName = match.getTeamOneName();
        teamOneDisplayName = match.getTeamOneDisplayName();
        teamTwoId = match.getTeamTwoId();
        teamTwoName = match.getTeamTwoName();
        teamTwoDisplayName = match.getTeamTwoDisplayName();
        this.goalsTeamOne = goalsTeamOne;
        this.goalsTeamTwo = goalsTeamTwo;
        this.errorCauseType = errorCauseType;
        this.errorCauseMessage = errorCauseMessage;
    }

    public long getMatchId() {
        return matchId;
    }

    public String getAssignedTime() {
        return assignedTime;
    }

    public long getTeamOneId() {
        return teamOneId;
    }

    public String getTeamOneName() {
        return teamOneName;
    }

    public String getTeamOneDisplayName() {
        return teamOneDisplayName;
    }

    public long getTeamTwoId() {
        return teamTwoId;
    }

    public String getTeamTwoName() {
        return teamTwoName;
    }

    public String getTeamTwoDisplayName() {
        return teamTwoDisplayName;
    }

    public Integer getGoalsTeamOne() {
        return goalsTeamOne;
    }

    public Integer getGoalsTeamTwo() {
        return goalsTeamTwo;
    }

    public String getErrorCauseType() {
        return errorCauseType;
    }

    public String getErrorCauseMessage() {
        return errorCauseMessage;
    }
}
