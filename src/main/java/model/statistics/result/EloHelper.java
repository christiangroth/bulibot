package model.statistics.result;

import java.math.BigDecimal;
import java.util.TreeMap;

import model.statistics.Winner;

/**
 * Raw Elo suggests that both teams ‘risk' a certain percentage of their rating in each contest, with the winner gaining the total pot, i.e. their rating
 * increases by the losing team’s ante. In the event of a draw, the pot is shared equally. This version adjusts raw Elo rating
 * <ul>
 * <li>using separate home and away risks</li>
 * <li>using weight factors for points difference based on margin of goals</li>
 * </ul>
 *
 * @see <a href="https://www.bettingexpert.com/en-au/learn/how-to-predict-sporting-events/applying-elo-ratings">https://www.bettingexpert.com/en-au/learn/how-to
 *      -predict-sporting-events/applying-elo-ratings</a>
 * @author chris
 */
public final class EloHelper {

    public static final int DEFAULT_INITIAL_RANK_SCORE = 1000;

    public static final double DEFAULT_RISK_HOME = 0.07;
    public static final double DEFAULT_RISK_AWAY = 0.05;

    public static final TreeMap<Integer, Double> DEFAULT_GOAL_MARGIN_SCORE_PERCENTAGES;
    static {
        DEFAULT_GOAL_MARGIN_SCORE_PERCENTAGES = new TreeMap<>();
        DEFAULT_GOAL_MARGIN_SCORE_PERCENTAGES.put(1, 0.7);
        DEFAULT_GOAL_MARGIN_SCORE_PERCENTAGES.put(2, 0.85);
        DEFAULT_GOAL_MARGIN_SCORE_PERCENTAGES.put(3, 0.95);
    }

    /**
     * Builder instance used to configure {@link EloHelper}.
     *
     * @author chris
     */
    public static class Builder {

        private int initialRankScore = DEFAULT_INITIAL_RANK_SCORE;
        private double riskHome = DEFAULT_RISK_HOME;
        private double riskAway = DEFAULT_RISK_AWAY;
        private TreeMap<Integer, Double> goalsMarginScorePercentages = DEFAULT_GOAL_MARGIN_SCORE_PERCENTAGES;

        private Builder() {

            // private builder constructor
        }

        /**
         * Defines the initial score of a newly created rank.
         *
         * @see EloRanking
         * @see EloHelper#compute(int, int, Winner, int)
         * @param initialRankScore
         *            initial rank score
         * @return builder instance for method chaining purpose
         */
        public Builder initialRankScore(int initialRankScore) {
            this.initialRankScore = initialRankScore;
            return this;
        }

        /**
         * @see EloHelper#compute(int, int, Winner, int)
         * @param riskHome
         *            risk for home team
         * @param riskAway
         *            risk for away team
         * @return builder instance for method chaining purpose
         */
        public Builder risks(double riskHome, double riskAway) {
            this.riskHome = riskHome;
            this.riskAway = riskAway;
            return this;
        }

        /**
         * @see EloHelper#compute(int, int, Winner, int)
         * @param goalsMarginScorePercentages
         *            goals difference configuration
         * @return builder instance for method chaining purpose
         */
        public Builder goalsMarginScorePercentages(TreeMap<Integer, Double> goalsMarginScorePercentages) {
            this.goalsMarginScorePercentages = goalsMarginScorePercentages;
            return this;
        }

        public EloHelper build() {
            return new EloHelper(initialRankScore, riskHome, riskAway, goalsMarginScorePercentages);
        }
    }

    /**
     * Creates a new builder instance to configure a new {@link EloHelper}.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    private final int initialRankScore;
    private final double riskHome;
    private final double riskAway;
    private final TreeMap<Integer, Double> goalsMarginScorePercentages;

    private EloHelper(int initialRankScore, double riskHome, double riskAway, TreeMap<Integer, Double> goalsMarginScorePercentages) {
        this.initialRankScore = initialRankScore;
        this.riskHome = riskHome;
        this.riskAway = riskAway;
        this.goalsMarginScorePercentages = goalsMarginScorePercentages;
    }

    /**
     * Computes the new Elo scores based on the given data and configured options for this {@link EloHelper} instance.<br>
     * <br>
     * In first case the risks for both teams will be calculated. Each team risks a relative amount based on current score. Adjustments can be made using
     * {@link Builder#risks(double, double)}. <br>
     * <br>
     * In case of a draw the pot of both risks will be split 50:50 and the new score will be oldScore - teamRisk + (pot * 0.5). <br>
     * <br>
     * In case a winner exists the losers risk will be used as pot and subtracted from losers score and added to winners score. You may adjust the percentage of
     * the pot used (both for loser and winner score adjustments) by specifying {@link Builder#goalsMarginScorePercentages(TreeMap)}. The key is the maximum
     * difference in goals and the value represents the percentage of the pot being used. If no map entry matches the pot will be used completely.<br>
     * <br>
     * Example:
     * <ul>
     * <li>key=1, value=0.8</li>
     * <li>key=2, value=0.9</li>
     * <li>key=3, value=1</li>
     * </ul>
     * The above configuration will result in 90% of the pot being used for results like 3:1, 2:0 and 4:2. 4:1 will match key=3 and use the pot completely which
     * in fact is the same as leaving this key away or any higher score like 4:0, 5:0, ...
     *
     * @param scoreOne
     *            score of first team before the match
     * @param scoreTwo
     *            score of second team before the match
     * @param winner
     *            the match winner
     * @param goalsDiff
     *            goals difference in case a winner exists, will be ignored for draw
     * @return array containing computed scores, index zero contains new score for team one, index one for team two, never null
     */
    public int[] compute(int scoreOne, int scoreTwo, Winner winner, int goalsDiff) {

        // compute team risks
        BigDecimal teamOneRisk = new BigDecimal(scoreOne).multiply(new BigDecimal(riskHome));
        BigDecimal teamTwoRisk = new BigDecimal(scoreTwo).multiply(new BigDecimal(riskAway));

        // calculate new Elo score
        BigDecimal newScoreOne;
        BigDecimal newScoreTwo;
        if (winner == Winner.DRAW) {

            /*
             * Raw Elo suggests that both teams ‘risk' a certain percentage of their rating in each contest, with the winner gaining the total pot, i.e. their
             * rating increases by the losing team’s ante. In the event of a draw, the pot is shared equally.
             */
            BigDecimal halfPot = teamOneRisk.add(teamTwoRisk).multiply(new BigDecimal(0.5));
            newScoreOne = new BigDecimal(scoreOne).subtract(teamOneRisk).add(halfPot);
            newScoreTwo = new BigDecimal(scoreTwo).subtract(teamTwoRisk).add(halfPot);
        } else {

            // adjust score result regarding goals margin, wins with higher margin in goals gain more points
            double riskPercentage = 1;
            if (goalsMarginScorePercentages != null) {
                riskPercentage = goalsMarginScorePercentages.entrySet().stream().filter(e -> goalsDiff <= e.getKey()).findFirst().map(e -> e.getValue()).orElse(1.0);
            }

            // calculate change in score based on losers risk
            BigDecimal pot = winner == Winner.ONE ? teamTwoRisk : teamOneRisk;
            BigDecimal scoreDiff = pot.multiply(new BigDecimal(riskPercentage));

            // calculate new scores
            if (winner == Winner.ONE) {
                newScoreOne = new BigDecimal(scoreOne).add(scoreDiff);
                newScoreTwo = new BigDecimal(scoreTwo).subtract(scoreDiff);
            } else {
                newScoreOne = new BigDecimal(scoreOne).subtract(scoreDiff);
                newScoreTwo = new BigDecimal(scoreTwo).add(scoreDiff);
            }
        }

        // done
        return new int[] { newScoreOne.setScale(0, BigDecimal.ROUND_HALF_UP).intValue(), newScoreTwo.setScale(0, BigDecimal.ROUND_HALF_UP).intValue() };
    }

    /**
     * Returns the configured initial rank score.
     *
     * @see Builder#initialRankScore(int)
     * @return initial rank score
     */
    public int getInitialRankScore() {
        return initialRankScore;
    }
}
