package model.statistics.result;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import util.TestData;

public class StatisticsResultTest {

    private TestData testData;

    @Before
    public void init() {
        testData = new TestData();
    }

    @Test
    public void rankingUnfiltered() {
        StatisticsResult statistics = builder().build();
        Assert.assertEquals(24, statistics.getNumberOfMatches());
        Assert.assertEquals(63, statistics.getGoals());
        Assert.assertEquals(2.625, statistics.getGoalsPerMatch(), 0.0);
        Assert.assertEquals(8, statistics.getTeamOneWins());
        Assert.assertEquals(33.33, statistics.getTeamOneWinsRatio(), 0.01);
        Assert.assertEquals(9, statistics.getTeamTwoWins());
        Assert.assertEquals(37.5, statistics.getTeamTwoWinsRatio(), 0.0);
        Assert.assertEquals(7, statistics.getDraws());
        Assert.assertEquals(29.16, statistics.getDrawsRatio(), 0.01);
        Ranking ranking = statistics.getRanking();
        Assert.assertEquals(5, ranking.getRanks().size());
        assertRank(ranking, testData.t1, 1, 4, 4, 4, 20, 15);
        assertRank(ranking, testData.t2, 2, 4, 3, 5, 15, 14);
        assertRank(ranking, testData.t3, 3, 3, 6, 3, 15, 16);
        assertRank(ranking, testData.t4, 5, 2, 1, 3, 7, 11);
        assertRank(ranking, testData.t5, 4, 4, 0, 2, 6, 7);
    }

    @Test
    public void rankingSeasonOne() {
        StatisticsResult statistics = builder().filterSeason(TestData.SEASON_ONE).build();
        Assert.assertEquals(12, statistics.getNumberOfMatches());
        Assert.assertEquals(31, statistics.getGoals());
        Assert.assertEquals(2.583, statistics.getGoalsPerMatch(), 0.01);
        Assert.assertEquals(4, statistics.getTeamOneWins());
        Assert.assertEquals(33.33, statistics.getTeamOneWinsRatio(), 0.01);
        Assert.assertEquals(4, statistics.getTeamTwoWins());
        Assert.assertEquals(33.33, statistics.getTeamTwoWinsRatio(), 0.01);
        Assert.assertEquals(4, statistics.getDraws());
        Assert.assertEquals(33.33, statistics.getDrawsRatio(), 0.01);
        Ranking ranking = statistics.getRanking();
        Assert.assertEquals(4, ranking.getRanks().size());
        assertRank(ranking, testData.t1, 2, 2, 2, 2, 11, 8);
        assertRank(ranking, testData.t2, 3, 2, 1, 3, 5, 6);
        assertRank(ranking, testData.t3, 1, 2, 4, 0, 8, 6);
        assertRank(ranking, testData.t4, 4, 2, 1, 3, 7, 11);
        Assert.assertNull(ranking.getRank(testData.t5));
    }

    @Test
    public void rankingSeasonOneHome() {
        StatisticsResult statistics = builder().filterSeason(TestData.SEASON_ONE).build();
        Ranking ranking = statistics.getRankingHome();
        Assert.assertEquals(4, ranking.getRanks().size());
        assertRank(ranking, testData.t1, 2, 1, 1, 1, 6, 6);
        assertRank(ranking, testData.t2, 3, 1, 1, 1, 2, 2);
        assertRank(ranking, testData.t3, 1, 2, 1, 0, 5, 3);
        assertRank(ranking, testData.t4, 4, 0, 1, 2, 1, 6);
    }

    @Test
    public void rankingSeasonOneAway() {
        StatisticsResult statistics = builder().filterSeason(TestData.SEASON_ONE).build();
        Ranking ranking = statistics.getRankingAway();
        Assert.assertEquals(4, ranking.getRanks().size());
        assertRank(ranking, testData.t1, 2, 1, 1, 1, 5, 2);
        assertRank(ranking, testData.t2, 4, 1, 0, 2, 3, 4);
        assertRank(ranking, testData.t3, 3, 0, 3, 0, 3, 3);
        assertRank(ranking, testData.t4, 1, 2, 0, 1, 6, 5);
    }

    @Test
    public void rankingSeasonTwo() {
        StatisticsResult statistics = builder().filterSeason(TestData.SEASON_TWO).build();
        Assert.assertEquals(12, statistics.getNumberOfMatches());
        Assert.assertEquals(32, statistics.getGoals());
        Assert.assertEquals(2.66, statistics.getGoalsPerMatch(), 0.01);
        Assert.assertEquals(4, statistics.getTeamOneWins());
        Assert.assertEquals(33.33, statistics.getTeamOneWinsRatio(), 0.01);
        Assert.assertEquals(5, statistics.getTeamTwoWins());
        Assert.assertEquals(41.66, statistics.getTeamTwoWinsRatio(), 0.01);
        Assert.assertEquals(3, statistics.getDraws());
        Assert.assertEquals(25.0, statistics.getDrawsRatio(), 0.0);
        Ranking ranking = statistics.getRanking();
        Assert.assertEquals(4, ranking.getRanks().size());
        assertRank(ranking, testData.t1, 3, 2, 2, 2, 9, 7);
        assertRank(ranking, testData.t2, 2, 2, 2, 2, 10, 8);
        assertRank(ranking, testData.t3, 4, 1, 2, 3, 7, 10);
        Assert.assertNull(ranking.getRank(testData.t4));
        assertRank(ranking, testData.t5, 1, 4, 0, 2, 6, 7);
    }

    private void assertRank(Ranking ranking, long teamId, int position, int wins, int draws, int defeats, int goalsScored, int goalsReceived) {
        Rank rank = ranking.getRank(teamId);
        Assert.assertNotNull(rank);
        Assert.assertEquals(teamId, rank.getTeamId());
        Assert.assertEquals(position, rank.getPosition());
        Assert.assertEquals(wins, rank.getWins());
        Assert.assertEquals(draws, rank.getDraws());
        Assert.assertEquals(defeats, rank.getDefeats());
        Assert.assertEquals(goalsScored, rank.getGoalsScored());
        Assert.assertEquals(goalsReceived, rank.getGoalsReceived());
    }

    @Test
    public void goalGettersSeasonOne() {
        GoalGetters goalGetters = builder().filterSeason(TestData.SEASON_ONE).build().getGoalGetters();
        Assert.assertEquals(7, goalGetters.getGoalGetter().size());
        assertGoalGetter(goalGetters, testData.t1, testData.t1s1g1, 1, 9, 1, 0);
        assertGoalGetter(goalGetters, testData.t3, testData.t3s1g1, 2, 5, 0, 1);
        assertGoalGetter(goalGetters, testData.t4, testData.t4s1g1, 3, 5, 1, 0);
        assertGoalGetter(goalGetters, testData.t3, testData.t3Fromt4s1g2, 4, 3, 1, 1);
        assertGoalGetter(goalGetters, testData.t2, testData.t2s1g1, 5, 2, 0, 0);
        assertGoalGetter(goalGetters, testData.t2, testData.t2s1g2, 6, 2, 1, 0);
        assertGoalGetter(goalGetters, testData.t4, testData.t4s1g2, 7, 1, 0, 0);
    }

    private void assertGoalGetter(GoalGetters goalGetters, long goalGetterTeamId, long goalGetterId, int position, int goals, int penalty, int overtime) {
        GoalGetter goalGetter = goalGetters.goalGetter(goalGetterId);
        Assert.assertNotNull(goalGetter);
        Assert.assertEquals(goalGetterId, goalGetter.getId());
        Assert.assertEquals(goalGetterTeamId, goalGetter.getTeamId());
        Assert.assertEquals(position, goalGetter.getPosition());
        Assert.assertEquals(goals, goalGetter.getGoals());
        Assert.assertEquals(penalty, goalGetter.getPenaltyGoals());
        Assert.assertEquals(overtime, goalGetter.getOvertimeGoals());
    }

    private StatisticsResultBuilder builder() {
        return testData.statistics.builder();
    }
}
