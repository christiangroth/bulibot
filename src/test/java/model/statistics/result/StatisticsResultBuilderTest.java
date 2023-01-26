package model.statistics.result;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import model.statistics.Winner;
import util.TestData;
import util.TestUtils;

public class StatisticsResultBuilderTest {

    private TestData testData;

    @Before
    public void init() {
        testData = new TestData();
    }

    @Test
    public void unfiltered() {
        check(builder().build(), 24);
    }

    @Test
    public void season() {
        check(builder().filterSeason(TestData.SEASON_ONE).build(), 12);
        check(builder().filterSeason(TestData.SEASON_TWO).build(), 12);
        check(builder().filterSeason(TestData.SEASON_ONE, TestData.SEASON_TWO).build(), 24);
        check(builder().filterSeason(13).build(), 0);
        check(builder().filterSeason().build(), 0);
        check(builder().filterSeason((Integer) null).build(), 0);
        check(builder().filterSeason(null, null).build(), 0);
    }

    @Test
    public void matchday() {
        check(builder().filterMatchday(1).build(), 4);
        check(builder().filterMatchday(1, 1).build(), 4);
        check(builder().filterMatchday(0, 1).build(), 4);
        check(builder().filterMatchday(6, 7).build(), 4);
        check(builder().filterMatchday(2, 4).build(), 12);
        check(builder().filterMatchday(4, 2).build(), 0);
        check(builder().filterMatchday(-1, 0).build(), 0);
    }

    @Test
    public void dateTime() {
        check(builder().filterMatchAssignedTime(TestUtils.date("07.02.2015 15:30")).build(), 1);
        check(builder().filterMatchAssignedTime(TestUtils.date("08.03.2015 15:30")).build(), 2);
        check(builder().filterMatchAssignedTime(TestUtils.date("08.03.2015 15:31")).build(), 0);
        check(builder().filterMatchAssignedTime(LocalDateTime.MIN).build(), 0);
        check(builder().filterMatchAssignedTime(LocalDateTime.MAX).build(), 0);
        check(builder().filterMatchAssignedTime(null).build(), 0);
    }

    @Test
    public void dateTimeRange() {
        check(builder().filterMatchAssignedTime(TestUtils.date("07.02.2015 15:30"), TestUtils.date("07.02.2015 15:30")).build(), 1);
        check(builder().filterMatchAssignedTime(TestUtils.date("08.03.2015 15:30"), TestUtils.date("08.03.2015 15:30")).build(), 2);
        check(builder().filterMatchAssignedTime(TestUtils.date("08.03.2015 15:29"), TestUtils.date("08.03.2015 15:30")).build(), 2);
        check(builder().filterMatchAssignedTime(TestUtils.date("08.03.2015 15:30"), TestUtils.date("08.03.2015 15:31")).build(), 2);
        check(builder().filterMatchAssignedTime(TestUtils.date("08.03.2015 15:29"), TestUtils.date("08.03.2015 15:31")).build(), 2);
        check(builder().filterMatchAssignedTime(TestUtils.date("08.03.2015 15:31"), TestUtils.date("08.03.2015 15:29")).build(), 0);
        check(builder().filterMatchAssignedTime(null, TestUtils.date("07.02.2015 15:30")).build(), 0);
        check(builder().filterMatchAssignedTime(TestUtils.date("07.02.2015 15:30"), null).build(), 0);
        check(builder().filterMatchAssignedTime(null, null).build(), 0);
    }

    @Test
    public void dayOfWeek() {
        check(builder().filterMatchDayOfWeek(DayOfWeek.MONDAY).build(), 0);
        check(builder().filterMatchDayOfWeek(DayOfWeek.TUESDAY).build(), 2);
        check(builder().filterMatchDayOfWeek(DayOfWeek.WEDNESDAY).build(), 2);
        check(builder().filterMatchDayOfWeek(DayOfWeek.THURSDAY).build(), 0);
        check(builder().filterMatchDayOfWeek(DayOfWeek.FRIDAY).build(), 3);
        check(builder().filterMatchDayOfWeek(DayOfWeek.SATURDAY).build(), 9);
        check(builder().filterMatchDayOfWeek(DayOfWeek.SUNDAY).build(), 8);
        check(builder().filterMatchDayOfWeek(DayOfWeek.SUNDAY, DayOfWeek.SUNDAY, DayOfWeek.SUNDAY).build(), 8);
        check(builder().filterMatchDayOfWeek(DayOfWeek.SUNDAY, DayOfWeek.MONDAY).build(), 8);
        check(builder().filterMatchDayOfWeek(DayOfWeek.SUNDAY, DayOfWeek.WEDNESDAY).build(), 10);
        check(builder().filterMatchDayOfWeek(DayOfWeek.SUNDAY).filterMatchDayOfWeek(DayOfWeek.WEDNESDAY).build(), 0);
        check(builder().filterWeekend().build(), 20);
        check(builder().filterNonWeekend().build(), 4);
        check(builder().filterNonWeekend().filterWeekend().build(), 0);
        check(builder().filterMatchDayOfWeek().build(), 0);
        check(builder().filterMatchDayOfWeek((DayOfWeek) null).build(), 0);
        check(builder().filterMatchDayOfWeek(null, null).build(), 0);
    }

    @Test
    public void time() {
        check(builder().filterMatchTime("15:30").build(), 13);
        check(builder().filterMatchTime("17:30").build(), 2);
        check(builder().filterMatchTime("18:30").build(), 2);
        check(builder().filterMatchTime("20:00").build(), 4);
        check(builder().filterMatchTime("20:30").build(), 3);
        check(builder().filterMatchTime("20:00", "20:00").build(), 4);
        check(builder().filterMatchTime("20:00", "20:30").build(), 7);
        check(builder().filterMatchTime("foo").build(), 0);
        check(builder().filterMatchTime("").build(), 0);
        check(builder().filterMatchTime().build(), 0);
        check(builder().filterMatchTime((String) null).build(), 0);
        check(builder().filterMatchTime(null, null).build(), 0);
    }

    @Test
    public void team() {
        check(builder().filterTeam(testData.t1).build(), 12);
        check(builder().filterTeam(testData.t2).build(), 12);
        check(builder().filterTeam(testData.t5).build(), 6);
        check(builder().filterTeam(testData.t1, testData.t1).build(), 12);
        check(builder().filterTeam(testData.t1).filterTeam(testData.t1).build(), 12);
        check(builder().filterTeam(testData.t1, testData.t2).build(), 20);
        check(builder().filterTeam(testData.t1).filterTeam(testData.t2).build(), 4);
        check(builder().filterTeam(Long.MAX_VALUE).build(), 0);
        check(builder().filterTeam().build(), 0);
        check(builder().filterTeam((Long) null).build(), 0);
        check(builder().filterTeam(null, null).build(), 0);
    }

    @Test
    public void teamOne() {
        check(builder().filterTeamOne(testData.t1).build(), 6);
        check(builder().filterTeamOne(testData.t2).build(), 6);
        check(builder().filterTeamOne(testData.t5).build(), 3);
        check(builder().filterTeamOne(testData.t1, testData.t1).build(), 6);
        check(builder().filterTeamOne(testData.t1).filterTeamOne(testData.t1).build(), 6);
        check(builder().filterTeamOne(testData.t1, testData.t2).build(), 12);
        check(builder().filterTeamOne(testData.t1).filterTeamOne(testData.t2).build(), 0);
        check(builder().filterTeamOne(Long.MAX_VALUE).build(), 0);
        check(builder().filterTeamOne().build(), 0);
        check(builder().filterTeamOne((Long) null).build(), 0);
        check(builder().filterTeamOne(null, null).build(), 0);
    }

    @Test
    public void teamTwo() {
        check(builder().filterTeamTwo(testData.t1).build(), 6);
        check(builder().filterTeamTwo(testData.t2).build(), 6);
        check(builder().filterTeamTwo(testData.t5).build(), 3);
        check(builder().filterTeamTwo(testData.t1, testData.t1).build(), 6);
        check(builder().filterTeamTwo(testData.t1).filterTeamTwo(testData.t1).build(), 6);
        check(builder().filterTeamTwo(testData.t1, testData.t2).build(), 12);
        check(builder().filterTeamTwo(testData.t1).filterTeamTwo(testData.t2).build(), 0);
        check(builder().filterTeamTwo(Long.MAX_VALUE).build(), 0);
        check(builder().filterTeamTwo().build(), 0);
        check(builder().filterTeamTwo((Long) null).build(), 0);
        check(builder().filterTeamTwo(null, null).build(), 0);
    }

    @Test
    public void teamOneWins() {
        check(builder().filterTeamOneWins().build(), 8).getMatches().forEach(m -> Assert.assertTrue(m.getWinner() == Winner.ONE));
    }

    @Test
    public void draws() {
        check(builder().filterDraws().build(), 7).getMatches().forEach(m -> Assert.assertTrue(m.getWinner() == Winner.DRAW));
    }

    @Test
    public void teamTwoWins() {
        check(builder().filterTeamTwoWins().build(), 9).getMatches().forEach(m -> Assert.assertTrue(m.getWinner() == Winner.TWO));
    }

    private StatisticsResultBuilder builder() {
        return testData.statistics.builder();
    }

    private StatisticsResult check(StatisticsResult result, int matches) {
        Assert.assertEquals(matches, result.getNumberOfMatches());
        return result;
    }
}
