package model.statistics;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

import model.match.Match;

/**
 * Basic metadata POJO for a match.
 *
 * @author chris
 */
public class MatchMetadata {

    protected final int season;
    protected final int matchday;
    protected final String lastUpdateTimeString;

    protected final long matchId;
    protected final LocalDateTime matchAssignedTime;
    protected final String matchDayOfWeekAndTime;
    protected final String matchDayOfWeek;
    protected final String matchTime;
    protected final long teamOneId;
    protected final String teamOneName;
    protected final String teamOneDisplayName;
    protected final String teamOneIconUrl;
    protected final long teamTwoId;
    protected final String teamTwoName;
    protected final String teamTwoDisplayName;
    protected final String teamTwoIconUrl;

    /**
     * Copy-Constructor.
     *
     * @param source
     *            copy source
     */
    public MatchMetadata(MatchMetadata source) {
        season = source.season;
        matchday = source.matchday;
        lastUpdateTimeString = source.lastUpdateTimeString;

        matchId = source.matchId;
        matchAssignedTime = LocalDateTime.from(source.matchAssignedTime);
        matchDayOfWeekAndTime = source.matchDayOfWeekAndTime;
        matchDayOfWeek = source.matchDayOfWeek;
        matchTime = source.matchTime;
        teamOneId = source.teamOneId;
        teamOneName = source.teamOneName;
        teamOneDisplayName = source.teamOneDisplayName;
        teamOneIconUrl = source.teamOneIconUrl;
        teamTwoId = source.teamTwoId;
        teamTwoName = source.teamTwoName;
        teamTwoDisplayName = source.teamTwoDisplayName;
        teamTwoIconUrl = source.teamTwoIconUrl;
    }

    /**
     * Creates metadata from given match.
     *
     * @param match
     *            match to be converted
     */
    public MatchMetadata(Match match) {

        // match data
        season = match.getSeason();
        matchday = match.getMatchday();
        lastUpdateTimeString = match.getLastUpdateTimeString();
        matchId = match.getId();
        matchAssignedTime = match.getAssignedTime();
        ZonedDateTime zonedDateTime = ZonedDateTime.of(matchAssignedTime, ZoneId.systemDefault());
        matchDayOfWeek = zonedDateTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
        matchTime = String.format("%02d", zonedDateTime.getHour()) + ":" + String.format("%02d", zonedDateTime.getMinute());
        matchDayOfWeekAndTime = matchDayOfWeek + " " + matchTime;
        teamOneId = match.getTeamOneId();
        teamOneName = match.getTeamOneName();
        teamOneDisplayName = match.getTeamOneDisplayName();
        teamOneIconUrl = match.getTeamOneIconUrl();
        teamTwoId = match.getTeamTwoId();
        teamTwoName = match.getTeamTwoName();
        teamTwoDisplayName = match.getTeamTwoDisplayName();
        teamTwoIconUrl = match.getTeamTwoIconUrl();
    }

    /**
     * Returns the season.
     *
     * @return season
     */
    public int getSeason() {
        return season;
    }

    /**
     * Returns the matchday.
     *
     * @return matchday
     */
    public int getMatchday() {
        return matchday;
    }

    /**
     * Returns the last update key.
     *
     * @return last update key
     */
    public String getLastUpdateTimeString() {
        return lastUpdateTimeString;
    }

    /**
     * Returns the internal match id.
     *
     * @return match id
     */
    public long getMatchId() {
        return matchId;
    }

    /**
     * Returns the assigned date and time for the match.
     *
     * @return assigned date and time
     */
    public LocalDateTime getMatchAssignedTime() {
        return matchAssignedTime;
    }

    /**
     * Returns the day of week and time for the match.
     *
     * @return day of week and time
     */
    public String getMatchDayOfWeekAndTime() {
        return matchDayOfWeekAndTime;
    }

    /**
     * Returns the day of week for the match.
     *
     * @return da of week
     */
    public String getMatchDayOfWeek() {
        return matchDayOfWeek;
    }

    /**
     * Returns the assigned time for the match.
     *
     * @return assigned time
     */
    public String getMatchTime() {
        return matchTime;
    }

    /**
     * Returns the id for team one.
     *
     * @return team one id
     */
    public long getTeamOneId() {
        return teamOneId;
    }

    /**
     * Returns the name for team one.
     *
     * @return team one name
     */
    public String getTeamOneName() {
        return teamOneName;
    }

    /**
     * Returns the display name for team one. This may be shorter than the regular name provided by openligadb and is maintained as bulibot setting.
     *
     * @return display name
     */
    public String getTeamOneDisplayName() {
        return teamOneDisplayName;
    }

    /**
     * Returns the icon URL for team one.
     *
     * @return team one icon URL
     */
    public String getTeamOneIconUrl() {
        return teamOneIconUrl;
    }

    /**
     * Returns the id for team two.
     *
     * @return team two id
     */
    public long getTeamTwoId() {
        return teamTwoId;
    }

    /**
     * Returns the name for team two.
     *
     * @return team two name
     */
    public String getTeamTwoName() {
        return teamTwoName;
    }

    /**
     * Returns the display name for team two. This may be shorter than the regular name provided by openligadb and is maintained as bulibot setting.
     *
     * @return display name
     */
    public String getTeamTwoDisplayName() {
        return teamTwoDisplayName;
    }

    /**
     * Returns the icon URL for team two.
     *
     * @return team two icon URL
     */
    public String getTeamTwoIconUrl() {
        return teamTwoIconUrl;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (matchId ^ matchId >>> 32);
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
        MatchMetadata other = (MatchMetadata) obj;
        if (matchId != other.matchId) {
            return false;
        }
        return true;
    }
}
