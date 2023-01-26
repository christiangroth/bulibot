package model.openligadb;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchData {

    @JsonProperty("MatchID")
    private long id;

    private String league;
    private Integer season;
    private Integer matchday;

    @JsonProperty("MatchDateTime")
    private String dateTimeString;
    @JsonProperty("LastUpdateDateTime")
    private String lastUpdateString;

    @JsonProperty("Team1")
    private TeamData teamOne;
    @JsonProperty("Team2")
    private TeamData teamTwo;

    @JsonProperty("MatchIsFinished")
    private boolean finished;
    @JsonProperty("MatchResults")
    private List<MatchResultData> results;
    @JsonProperty("Goals")
    private List<MatchGoalData> goals;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLeague() {
        return league;
    }

    public void setLeague(String league) {
        this.league = league;
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

    public String getDateTimeString() {
        return dateTimeString;
    }

    public void setDateTimeString(String dateTimeString) {
        this.dateTimeString = dateTimeString;
    }

    public String getLastUpdateString() {
        return lastUpdateString;
    }

    public void setLastUpdateString(String lastUpdateString) {
        this.lastUpdateString = lastUpdateString;
    }

    public TeamData getTeamOne() {
        return teamOne;
    }

    public void setTeamOne(TeamData teamOne) {
        this.teamOne = teamOne;
    }

    public TeamData getTeamTwo() {
        return teamTwo;
    }

    public void setTeamTwo(TeamData teamTwo) {
        this.teamTwo = teamTwo;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public List<MatchResultData> getResults() {
        return results;
    }

    public void setResults(List<MatchResultData> results) {
        this.results = results;
    }

    public List<MatchGoalData> getGoals() {
        return goals;
    }

    public void setGoals(List<MatchGoalData> goals) {
        this.goals = goals;
    }

    @Override
    public String toString() {
        return "MatchData [id=" + id + ", league=" + league + ", season=" + season + ", matchday=" + matchday + ", dateTimeString=" + dateTimeString + ", lastUpdateString="
                + lastUpdateString + ", teamOne=" + teamOne + ", teamTwo=" + teamTwo + ", finished=" + finished + ", results=" + results + ", goals=" + goals + "]";
    }
}
