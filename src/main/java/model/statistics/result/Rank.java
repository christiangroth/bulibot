package model.statistics.result;

/**
 * Basic POJO for team ranking data.
 *
 * @author chris
 */
public class Rank {

    private int position;
    private final long teamId;
    private final String teamName;
    private final String teamDisplayName;
    private final String teamIconUrl;
    private int wins;
    private int draws;
    private int defeats;
    private int goalsScored;
    private int goalsReceived;

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
     */
    public Rank(long teamId, String teamName, String teamDisplayName, String teamIconUrl) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.teamDisplayName = teamDisplayName;
        this.teamIconUrl = teamIconUrl;
    }

    void addMatchValues(int goalsScored, int goalsReceived) {

        // inc wins/draws/defeats
        if (goalsScored > goalsReceived) {
            wins++;
        } else if (goalsScored < goalsReceived) {
            defeats++;
        } else {
            draws++;
        }

        // inc goals statistics
        this.goalsScored += goalsScored;
        this.goalsReceived += goalsReceived;
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
        return wins + draws + defeats;
    }

    /**
     * Returns the number of wins.
     *
     * @return wins
     */
    public int getWins() {
        return wins;
    }

    /**
     * Returns the number of draws.
     *
     * @return draws
     */
    public int getDraws() {
        return draws;
    }

    /**
     * Returns the number of defeats.
     *
     * @return defeats
     */
    public int getDefeats() {
        return defeats;
    }

    /**
     * Returns the number of scored goals.
     *
     * @return scored goals
     */
    public int getGoalsScored() {
        return goalsScored;
    }

    /**
     * Returns the number of received goals.
     *
     * @return received goals
     */
    public int getGoalsReceived() {
        return goalsReceived;
    }

    /**
     * Returns the difference between scored and received goals.
     *
     * @return goals difference
     */
    public int getGoalsDifference() {
        return goalsScored - goalsReceived;
    }

    /**
     * Returns the points based on wins, draws and defeats.
     *
     * @return points
     */
    public int getPoints() {
        return wins * 3 + draws;
    }

    @Override
    public String toString() {
        return "Rank [#" + position + " " + teamName + " " + wins + "/" + draws + "/" + defeats + " " + goalsScored + ":" + goalsReceived + " " + getGoalsDifference() + " "
                + getPoints() + "]";
    }
}
