package model.statistics.result;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import model.statistics.MatchStatistics;
import model.statistics.Statistics;
import model.statistics.Winner;
import model.statistics.result.Ranking.Mode;

/**
 * Class holding results created based on {@link Statistics} using {@link StatisticsResultBuilder}.
 *
 * @author chris
 */
public class StatisticsResult {

    // data
    private final List<MatchStatistics> matches = new ArrayList<>();
    private final EloRanking eloRanking;
    private final Ranking ranking;
    private final Ranking rankingHome;
    private final Ranking rankingAway;
    private final GoalGetters goalGetters;

    // global statistics
    private final int matchdays;
    private final int goals;
    private final int goalsTeamOne;
    private final int goalsTeamTwo;
    private final double goalsPerMatch;
    private final double goalsTeamOnePerMatch;
    private final double goalsTeamTwoPerMatch;
    private final int teamOneWins;
    private final double teamOneWinsRatio;
    private final int teamTwoWins;
    private final double teamTwoWinsRatio;
    private final int draws;
    private final double drawsRatio;

    /**
     * Creates a new results instance based on given data.
     *
     * @param eloHelper
     *            configured helper to be able to adjust Elo rankings as needed
     * @param matches
     *            matches the results are based on
     */
    public StatisticsResult(EloHelper eloHelper, List<MatchStatistics> matches) {

        // set data
        if (matches != null) {
            this.matches.addAll(matches);
        }

        // build ranking and goal getters
        eloRanking = new EloRanking(eloHelper, matches);
        ranking = new Ranking(matches, Mode.FULL);
        rankingHome = new Ranking(matches, Mode.HOME);
        rankingAway = new Ranking(matches, Mode.AWAY);
        goalGetters = new GoalGetters(matches);

        // set global statistics
        Set<String> matchdayKeys = new HashSet<>();
        matches.forEach(m -> matchdayKeys.add(m.getSeason() + "/" + m.getMatchday()));
        matchdays = matchdayKeys.size();
        goalsTeamOne = matches.stream().mapToInt(m -> m.getGoalsTeamOne()).sum();
        goalsTeamTwo = matches.stream().mapToInt(m -> m.getGoalsTeamTwo()).sum();
        goals = goalsTeamOne + goalsTeamTwo;
        goalsPerMatch = matches.isEmpty() ? 0 : (double) goals / matches.size();
        goalsTeamOnePerMatch = matches.isEmpty() ? 0 : (double) goalsTeamOne / matches.size();
        goalsTeamTwoPerMatch = matches.isEmpty() ? 0 : (double) goalsTeamTwo / matches.size();
        teamOneWins = matches.stream().mapToInt(m -> m.getWinner() == Winner.ONE ? 1 : 0).sum();
        teamOneWinsRatio = matches.isEmpty() ? 0 : (double) teamOneWins / matches.size() * 100;
        teamTwoWins = matches.stream().mapToInt(m -> m.getWinner() == Winner.TWO ? 1 : 0).sum();
        teamTwoWinsRatio = matches.isEmpty() ? 0 : (double) teamTwoWins / matches.size() * 100;
        draws = matches.stream().mapToInt(m -> m.getWinner() == Winner.DRAW ? 1 : 0).sum();
        drawsRatio = matches.isEmpty() ? 0 : (double) draws / matches.size() * 100;
    }

    /**
     * Returns all matches used to create this result.
     *
     * @return matches, never null
     */
    @JsonIgnore
    public List<MatchStatistics> getMatches() {
        return matches;
    }

    /**
     * Returns the Elo ranking based on Elo configurations and the matches.
     *
     * @see StatisticsResultBuilder#eloRisks(double, double)
     * @see StatisticsResultBuilder#eloGoalsMarginScorePercentages(java.util.TreeMap)
     * @see EloHelper#compute(int, int, Winner, int)
     * @return ranking, never null
     */
    public EloRanking getEloRanking() {
        return eloRanking;
    }

    /**
     * Returns the ranking based on the matches.
     *
     * @return ranking, never null
     */
    public Ranking getRanking() {
        return ranking;
    }

    /**
     * Returns the ranking based on home matches only.
     *
     * @return ranking, never null
     */
    public Ranking getRankingHome() {
        return rankingHome;
    }

    /**
     * Returns the ranking based on away matches only.
     *
     * @return ranking, never null
     */
    public Ranking getRankingAway() {
        return rankingAway;
    }

    /**
     * Returns the goal getter ranking based on the matches.
     *
     * @return goal getter ranking, never null
     */
    public GoalGetters getGoalGetters() {
        return goalGetters;
    }

    /**
     * Returns the number of matches this results is based on.
     *
     * @return number of matches
     */
    @JsonProperty("matches")
    public int getNumberOfMatches() {
        return matches.size();
    }

    /**
     * Returns the number of goals.
     *
     * @return goals
     */
    public int getGoals() {
        return goals;
    }

    /**
     * Returns the number of goals by team one.
     *
     * @return goals by team one
     */
    public int getGoalsTeamOne() {
        return goalsTeamOne;
    }

    /**
     * Returns the number of goals by team two.
     *
     * @return goals by team two
     */
    public int getGoalsTeamTwo() {
        return goalsTeamTwo;
    }

    /**
     * Returns the number of goals per match.
     *
     * @return goals per match
     */
    public double getGoalsPerMatch() {
        return goalsPerMatch;
    }

    /**
     * Returns the number of goals by team one per match.
     *
     * @return goals by team one per match
     */
    public double getGoalsTeamOnePerMatch() {
        return goalsTeamOnePerMatch;
    }

    /**
     * Returns the number of goals by team two per match.
     *
     * @return goals by team two per match
     */
    public double getGoalsTeamTwoPerMatch() {
        return goalsTeamTwoPerMatch;
    }

    /**
     * Returns the number of matchdays this result is baed on.
     *
     * @return goals by team one per matchday
     */
    public int getMatchdays() {
        return matchdays;
    }

    /**
     * Returns the number of goals per matchday.
     *
     * @return goals per matchday
     */
    public double getGoalsPerMatchday() {
        return matchdays > 0 ? (double) goals / (double) matchdays : 0;
    }

    /**
     * Returns the number of goals by team one per matchday.
     *
     * @return goals by team one per matchday
     */
    public double getGoalsTeamOnePerMatchday() {
        return matchdays > 0 ? (double) goalsTeamOne / (double) matchdays : 0;
    }

    /**
     * Returns the number of goals by team two per matchday.
     *
     * @return goals by team two per matchday
     */
    public double getGoalsTeamTwoPerMatchday() {
        return matchdays > 0 ? (double) goalsTeamTwo / (double) matchdays : 0;
    }

    /**
     * Returns the number of wins for team one.
     *
     * @return team one wins
     */
    public int getTeamOneWins() {
        return teamOneWins;
    }

    /**
     * Returns the ration between wins for team one and number of matches.
     *
     * @return team one wins ratio
     */
    public double getTeamOneWinsRatio() {
        return teamOneWinsRatio;
    }

    /**
     * Returns the number of wins for team two.
     *
     * @return team two wins
     */
    public int getTeamTwoWins() {
        return teamTwoWins;
    }

    /**
     * Returns the ration between wins for team two and number of matches.
     *
     * @return team two wins ratio
     */
    public double getTeamTwoWinsRatio() {
        return teamTwoWinsRatio;
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
     * Returns the ration between draws and number of matches.
     *
     * @return draws ratio
     */
    public double getDrawsRatio() {
        return drawsRatio;
    }

    @Override
    public String toString() {
        return "StatisticsResult [matchdays=" + matchdays + ", goals=" + goals + ", goalsTeamOne=" + goalsTeamOne + ", goalsTeamTwo=" + goalsTeamTwo + ", goalsPerMatch="
                + goalsPerMatch + ", goalsTeamOnePerMatch=" + goalsTeamOnePerMatch + ", goalsTeamTwoPerMatch=" + goalsTeamTwoPerMatch + ", teamOneWins=" + teamOneWins
                + ", teamOneWinsRatio=" + teamOneWinsRatio + ", teamTwoWins=" + teamTwoWins + ", teamTwoWinsRatio=" + teamTwoWinsRatio + ", draws=" + draws + ", drawsRatio="
                + drawsRatio + "]";
    }
}
