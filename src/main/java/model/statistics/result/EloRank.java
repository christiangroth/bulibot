package model.statistics.result;

/**
 * Basic POJO for Elo team ranking data.
 *
 * @author chris
 */
public class EloRank {

    private int position;
    private final long teamId;
    private final String teamName;
    private final String teamDisplayName;
    private final String teamIconUrl;
    private int games;
    private int score;

    /**
     * Creates a new rank with given key data.
     *
     * @param teamId
     *            team id
     * @param teamName
     *            team name
     * @param teamDisplayName
     *            team display name
     * @param teamIconUrl
     *            team icon URL
     * @param score
     *            the starting score before first match
     */
    public EloRank(long teamId, String teamName, String teamDisplayName, String teamIconUrl, int score) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.teamDisplayName = teamDisplayName;
        this.teamIconUrl = teamIconUrl;
        this.score = score;
        games = 0;
    }

    /**
     * Adds the given score as result of a match computation regarding Elo rules to this rank as the current score.
     *
     * @param score
     *            score to be set
     */
    public void updateScore(int score) {
        this.score = score;
        games++;
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
     * Returns the number of games.
     *
     * @return games
     */
    public int getGames() {
        return games;
    }

    /**
     * Returns the current Elo score.
     *
     * @return score
     */
    public int getScore() {
        return score;
    }

    @Override
    public String toString() {
        return "EloRank [#" + position + " " + teamName + " " + games + " " + score + "]";
    }
}
