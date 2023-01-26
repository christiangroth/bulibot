package model.statistics.result;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import model.statistics.Winner;

public class EloHelperTest {

    private EloHelper eloHelper;
    private EloHelper eloHelperSameRisks;
    private EloHelper eloHelperNoGoalsMarginConfig;

    @Before
    public void setup() {
        eloHelper = EloHelper.builder().build();
        eloHelperSameRisks = EloHelper.builder().risks(0.1, 0.1).build();
        eloHelperNoGoalsMarginConfig = EloHelper.builder().goalsMarginScorePercentages(null).build();
    }

    @Test
    public void draw() {
        Assert.assertArrayEquals(new int[] { 990, 1010 }, eloHelper.compute(1000, 1000, Winner.DRAW, 0));
    }

    @Test
    public void homeOne() {
        Assert.assertArrayEquals(new int[] { 1035, 965 }, eloHelper.compute(1000, 1000, Winner.ONE, 1));
    }

    @Test
    public void homeTwo() {
        Assert.assertArrayEquals(new int[] { 1043, 957 }, eloHelper.compute(1000, 1000, Winner.ONE, 2));
    }

    @Test
    public void homeThree() {
        Assert.assertArrayEquals(new int[] { 1048, 952 }, eloHelper.compute(1000, 1000, Winner.ONE, 3));
    }

    @Test
    public void homeFour() {
        Assert.assertArrayEquals(new int[] { 1050, 950 }, eloHelper.compute(1000, 1000, Winner.ONE, 4));
    }

    @Test
    public void awayOne() {
        Assert.assertArrayEquals(new int[] { 951, 1049 }, eloHelper.compute(1000, 1000, Winner.TWO, 1));
    }

    @Test
    public void awayTwo() {
        Assert.assertArrayEquals(new int[] { 940, 1060 }, eloHelper.compute(1000, 1000, Winner.TWO, 2));
    }

    @Test
    public void awayThree() {
        Assert.assertArrayEquals(new int[] { 933, 1067 }, eloHelper.compute(1000, 1000, Winner.TWO, 3));
    }

    @Test
    public void awayFour() {
        Assert.assertArrayEquals(new int[] { 930, 1070 }, eloHelper.compute(1000, 1000, Winner.TWO, 4));
    }

    @Test
    public void drawSameRisks() {
        Assert.assertArrayEquals(new int[] { 1000, 1000 }, eloHelperSameRisks.compute(1000, 1000, Winner.DRAW, 0));
    }

    @Test
    public void homeOneSameRisks() {
        Assert.assertArrayEquals(new int[] { 1070, 930 }, eloHelperSameRisks.compute(1000, 1000, Winner.ONE, 1));
    }

    @Test
    public void homeTwoSameRisks() {
        Assert.assertArrayEquals(new int[] { 1085, 915 }, eloHelperSameRisks.compute(1000, 1000, Winner.ONE, 2));
    }

    @Test
    public void homeThreeSameRisks() {
        Assert.assertArrayEquals(new int[] { 1095, 905 }, eloHelperSameRisks.compute(1000, 1000, Winner.ONE, 3));
    }

    @Test
    public void homeFourSameRisks() {
        Assert.assertArrayEquals(new int[] { 1100, 900 }, eloHelperSameRisks.compute(1000, 1000, Winner.ONE, 4));
    }

    @Test
    public void awayOneSameRisks() {
        Assert.assertArrayEquals(new int[] { 930, 1070 }, eloHelperSameRisks.compute(1000, 1000, Winner.TWO, 1));
    }

    @Test
    public void awayTwoSameRisks() {
        Assert.assertArrayEquals(new int[] { 915, 1085 }, eloHelperSameRisks.compute(1000, 1000, Winner.TWO, 2));
    }

    @Test
    public void awayThreeSameRisks() {
        Assert.assertArrayEquals(new int[] { 905, 1095 }, eloHelperSameRisks.compute(1000, 1000, Winner.TWO, 3));
    }

    @Test
    public void awayFourSameRisks() {
        Assert.assertArrayEquals(new int[] { 900, 1100 }, eloHelperSameRisks.compute(1000, 1000, Winner.TWO, 4));
    }

    @Test
    public void homeOneNoGoalsMarginConfig() {
        Assert.assertArrayEquals(new int[] { 1050, 950 }, eloHelperNoGoalsMarginConfig.compute(1000, 1000, Winner.ONE, 1));
    }

    @Test
    public void homeTwoNoGoalsMarginConfig() {
        Assert.assertArrayEquals(new int[] { 1050, 950 }, eloHelperNoGoalsMarginConfig.compute(1000, 1000, Winner.ONE, 2));
    }

    @Test
    public void homeThreeNoGoalsMarginConfig() {
        Assert.assertArrayEquals(new int[] { 1050, 950 }, eloHelperNoGoalsMarginConfig.compute(1000, 1000, Winner.ONE, 3));
    }

    @Test
    public void homeFourNoGoalsMarginConfig() {
        Assert.assertArrayEquals(new int[] { 1050, 950 }, eloHelperNoGoalsMarginConfig.compute(1000, 1000, Winner.ONE, 4));
    }

    @Test
    public void awayOneNoGoalsMarginConfig() {
        Assert.assertArrayEquals(new int[] { 930, 1070 }, eloHelperNoGoalsMarginConfig.compute(1000, 1000, Winner.TWO, 1));
    }

    @Test
    public void awayTwoNoGoalsMarginConfig() {
        Assert.assertArrayEquals(new int[] { 930, 1070 }, eloHelperNoGoalsMarginConfig.compute(1000, 1000, Winner.TWO, 2));
    }

    @Test
    public void awayThreeNoGoalsMarginConfig() {
        Assert.assertArrayEquals(new int[] { 930, 1070 }, eloHelperNoGoalsMarginConfig.compute(1000, 1000, Winner.TWO, 3));
    }

    @Test
    public void awayFourNoGoalsMarginConfig() {
        Assert.assertArrayEquals(new int[] { 930, 1070 }, eloHelperNoGoalsMarginConfig.compute(1000, 1000, Winner.TWO, 4));
    }
}
