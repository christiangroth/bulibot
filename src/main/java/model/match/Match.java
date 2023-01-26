package model.match;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.chrgroth.jsonstore.store.VersionMigrationHandler;
import flexjson.JSON;
import util.DateUtils;

public class Match {

    public enum Status {
        WAITING, IN_PROGRESS, FINISHED;
    }

    public static final Integer VERSION = 2;
    public static final VersionMigrationHandler[] HANDLERS = new VersionMigrationHandler[] {

            /*
             * 1 -> 2 - added team short names set update timestamp to null to re-sync current season matches.
             */
            new VersionMigrationHandler() {

                @Override
                public int sourceVersion() {
                    return 1;
                }

                @Override
                public void migrate(Map<String, Object> genericPayload) {
                    genericPayload.put("lastUpdateTimeString", null);
                }
            } };

    public static final String LEAGUE_BUNDESLIGA_FIRST = "bl1";

    public static final int NUMBER_OF_MATCHDAYS_PER_SEASON = 34;
    public static final int NUMBER_OF_MATCHES_PER_MATCHDAY = 9;
    public static final int NUMBER_OF_MATCHES_PER_SEASON = NUMBER_OF_MATCHDAYS_PER_SEASON * NUMBER_OF_MATCHES_PER_MATCHDAY;

    public static Comparator<Match> COMPARATOR_DATE_ASC = (o1, o2) -> DateUtils.OLDEST_FIRST.compare(o1.getAssignedTime(), o2.getAssignedTime());

    public static Comparator<Match> COMPARATOR_DATE_DESC = (o1, o2) -> DateUtils.NEWEST_FIRST.compare(o1.getAssignedTime(), o2.getAssignedTime());

    private long id;

    private Integer season;
    private Integer matchday;

    private LocalDateTime assignedTime;
    private String lastUpdateTimeString;

    private long teamOneId;
    private String teamOneName;
    private String teamOneShortName;
    private String teamOneIconUrl;
    private long teamTwoId;
    private String teamTwoName;
    private String teamTwoShortName;
    private String teamTwoIconUrl;

    private Status status;

    private Integer goalsTeamOneHalfTime;
    private Integer goalsTeamTwoHalfTime;
    private Integer goalsTeamOneFullTime;
    private Integer goalsTeamTwoFullTime;

    @JSON
    private List<Goal> goals = new ArrayList<>();

    @JSON(include = false)
    public String getTeamOneDisplayName() {
        return StringUtils.isNotBlank(teamOneShortName) ? teamOneShortName : teamOneName;
    }

    @JSON(include = false)
    public String getTeamTwoDisplayName() {
        return StringUtils.isNotBlank(teamTwoShortName) ? teamTwoShortName : teamTwoName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public LocalDateTime getAssignedTime() {
        return assignedTime;
    }

    public void setAssignedTime(LocalDateTime assignedTime) {
        this.assignedTime = assignedTime;
    }

    public String getLastUpdateTimeString() {
        return lastUpdateTimeString;
    }

    public void setLastUpdateTimeString(String lastUpdateTimeString) {
        this.lastUpdateTimeString = lastUpdateTimeString;
    }

    public long getTeamOneId() {
        return teamOneId;
    }

    public void setTeamOneId(long teamOneId) {
        this.teamOneId = teamOneId;
    }

    public String getTeamOneName() {
        return teamOneName;
    }

    public void setTeamOneName(String teamOneName) {
        this.teamOneName = teamOneName;
    }

    public String getTeamOneShortName() {
        return teamOneShortName;
    }

    public void setTeamOneShortName(String teamOneShortName) {
        this.teamOneShortName = teamOneShortName;
    }

    public String getTeamOneIconUrl() {
        return teamOneIconUrl;
    }

    public void setTeamOneIconUrl(String teamOneIconUrl) {
        this.teamOneIconUrl = teamOneIconUrl;
    }

    public long getTeamTwoId() {
        return teamTwoId;
    }

    public void setTeamTwoId(long teamTwoId) {
        this.teamTwoId = teamTwoId;
    }

    public String getTeamTwoName() {
        return teamTwoName;
    }

    public void setTeamTwoName(String teamTwoName) {
        this.teamTwoName = teamTwoName;
    }

    public String getTeamTwoShortName() {
        return teamTwoShortName;
    }

    public void setTeamTwoShortName(String teamTwoShortName) {
        this.teamTwoShortName = teamTwoShortName;
    }

    public String getTeamTwoIconUrl() {
        return teamTwoIconUrl;
    }

    public void setTeamTwoIconUrl(String teamTwoIconUrl) {
        this.teamTwoIconUrl = teamTwoIconUrl;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Integer getGoalsTeamOneHalfTime() {
        return goalsTeamOneHalfTime;
    }

    public void setGoalsTeamOneHalfTime(Integer goalsTeamOneHalfTime) {
        this.goalsTeamOneHalfTime = goalsTeamOneHalfTime;
    }

    public Integer getGoalsTeamTwoHalfTime() {
        return goalsTeamTwoHalfTime;
    }

    public void setGoalsTeamTwoHalfTime(Integer goalsTeamTwoHalfTime) {
        this.goalsTeamTwoHalfTime = goalsTeamTwoHalfTime;
    }

    public Integer getGoalsTeamOneFullTime() {
        return goalsTeamOneFullTime;
    }

    public void setGoalsTeamOneFullTime(Integer goalsTeamOneFullTime) {
        this.goalsTeamOneFullTime = goalsTeamOneFullTime;
    }

    public Integer getGoalsTeamTwoFullTime() {
        return goalsTeamTwoFullTime;
    }

    public void setGoalsTeamTwoFullTime(Integer goalsTeamTwoFullTime) {
        this.goalsTeamTwoFullTime = goalsTeamTwoFullTime;
    }

    public List<Goal> getGoals() {
        return goals;
    }

    public void setGoals(List<Goal> goals) {
        this.goals = goals;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ id >>> 32);
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
        Match other = (Match) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Match [id=" + id + ", season=" + season + ", matchday=" + matchday + ", assignedTime=" + assignedTime + ", lastUpdateTimeString=" + lastUpdateTimeString
                + ", teamOneId=" + teamOneId + ", teamOneName=" + teamOneName + ", teamOneShortName=" + teamOneShortName + ", teamOneIconUrl=" + teamOneIconUrl + ", teamTwoId="
                + teamTwoId + ", teamTwoName=" + teamTwoName + ", teamTwoShortName=" + teamTwoShortName + ", teamTwoIconUrl=" + teamTwoIconUrl + ", status=" + status
                + ", goalsTeamOneHalfTime=" + goalsTeamOneHalfTime + ", goalsTeamTwoHalfTime=" + goalsTeamTwoHalfTime + ", goalsTeamOneFullTime=" + goalsTeamOneFullTime
                + ", goalsTeamTwoFullTime=" + goalsTeamTwoFullTime + ", goals=" + goals + "]";
    }
}
