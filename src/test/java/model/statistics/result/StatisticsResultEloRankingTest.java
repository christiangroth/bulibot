package model.statistics.result;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import model.statistics.MatchStatistics;
import util.TestData;

public class StatisticsResultEloRankingTest {

    private TestData testData;

    @Before
    public void init() {
        testData = new TestData();
    }

    @Test
    public void eloRankingSimple() {

        // t3 2 : 1 t4
        EloRanking eloRanking = new EloRanking(EloHelper.builder().build(), Arrays.asList(new MatchStatistics(testData.m1)));
        Assert.assertEquals(2, eloRanking.getRanks().size());

        final EloRank eloRankOne = eloRanking.getRanks().get(0);
        Assert.assertEquals(1, eloRankOne.getPosition());
        Assert.assertEquals(1, eloRankOne.getGames());
        Assert.assertEquals(testData.t3, eloRankOne.getTeamId());
        Assert.assertEquals(1035, eloRankOne.getScore());

        final EloRank eloRankTwo = eloRanking.getRanks().get(1);
        Assert.assertEquals(2, eloRankTwo.getPosition());
        Assert.assertEquals(1, eloRankTwo.getGames());
        Assert.assertEquals(testData.t4, eloRankTwo.getTeamId());
        Assert.assertEquals(965, eloRankTwo.getScore());
    }
}
