package model.community;

import java.util.Map;

import de.chrgroth.jsonstore.store.VersionMigrationHandler;
import flexjson.JSON;
import model.statistics.MatchMetadata;

public class BulibotExecution {

    public static final String SCRIPT_RETURN_VALUE_TEAM_ONE = "goalsTeamOne";
    public static final String SCRIPT_RETURN_VALUE_TEAM_TWO = "goalsTeamTwo";

    public static final String SCRIPT_BINDING_SEASON_DATA = "seasonData";
    public static final String SCRIPT_BINDING_LAST_SEASON_DATA = "lastSeasonData";
    public static final String SCRIPT_BINDING_OVERALL_DATA = "overallData";
    public static final String SCRIPT_BINDING_STATISTICS = "statistics";
    public static final String SCRIPT_BINDING_MATCH = "match";

    public static final Integer VERSION = 1;
    public static final VersionMigrationHandler[] HANDLERS = new VersionMigrationHandler[] {};

    // match reference
    private long matchId;
    private Integer season;
    private Integer matchday;

    // user / bulibot reference
    private long userId;
    private String bulibotVersionName;

    // execution data
    private Long duration;
    private Integer goalsTeamOne;
    private Integer goalsTeamTwo;
    private String errorCauseType;
    private String errorCauseMessage;

    // debug data
    private String stdout;
    private Map<String, String> state;

    public BulibotExecution() {
    }

    public BulibotExecution(MatchMetadata match) {
        matchId = match.getMatchId();
        season = match.getSeason();
        matchday = match.getMatchday();
    }

    @JSON(include = false)
    public boolean isResultAvailable() {
        return goalsTeamOne != null && goalsTeamOne.intValue() >= 0 && goalsTeamTwo != null && goalsTeamTwo.intValue() >= 0;
    }

    public long getMatchId() {
        return matchId;
    }

    public void setMatchId(long matchId) {
        this.matchId = matchId;
    }

    public Integer getSeason() {
        return season;
    }

    public void setSeason(Integer season) {
        this.season = season;
    }

    public Integer getMatchday() {
        return matchday;
    }

    public void setMatchday(Integer matchday) {
        this.matchday = matchday;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getBulibotVersionName() {
        return bulibotVersionName;
    }

    public void setBulibotVersionName(String bulibotVersionName) {
        this.bulibotVersionName = bulibotVersionName;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Integer getGoalsTeamOne() {
        return goalsTeamOne;
    }

    public void setGoalsTeamOne(Integer goalsTeamOne) {
        this.goalsTeamOne = goalsTeamOne;
    }

    public Integer getGoalsTeamTwo() {
        return goalsTeamTwo;
    }

    public void setGoalsTeamTwo(Integer goalsTeamTwo) {
        this.goalsTeamTwo = goalsTeamTwo;
    }

    public String getErrorCauseType() {
        return errorCauseType;
    }

    public void setErrorCauseType(String errorCauseType) {
        this.errorCauseType = errorCauseType;
    }

    public String getErrorCauseMessage() {
        return errorCauseMessage;
    }

    public void setErrorCauseMessage(String errorCauseMessage) {
        this.errorCauseMessage = errorCauseMessage;
    }

    @JSON(include = false)
    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    @JSON(include = false)
    public Map<String, String> getState() {
        return state;
    }

    public void setState(Map<String, String> state) {
        this.state = state;
    }
}
