package services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import model.match.Goal;
import model.match.Match;
import model.match.Match.Status;
import model.openligadb.MatchData;
import model.openligadb.MatchGoalData;
import model.openligadb.MatchResultData;
import model.openligadb.TeamData;
import util.DateUtils;
import util.TestData;

public class DataTransformationServiceTest {

    private static final String MATCH_UPDATE = "2015-03-17T21:29:17";
    private static final String MATCH_TIME = "2015-03-17T18:30:00";
    private static final String MATCH_TIME_FUTURE = "2915-03-17T18:30:00";

    private Integer season = 2015;
    private Integer matchday = 13;
    private TestData testData = new TestData();

    private TeamData teamOne = team("teamOne", "teamOneIcon");
    private TeamData teamTwo = team("teamTwo", "teamTwoIcon");

    private DataTransformationService service = new DataTransformationService();

    @Test
    public void matchGoalMinuteAsc() {

        // prepare data
        MatchGoalData first = new MatchGoalData();
        first.setMatchMinute(12);
        MatchGoalData second = new MatchGoalData();
        second.setMatchMinute(74);
        List<MatchGoalData> data = new ArrayList<>();
        data.add(second);
        data.add(first);

        // sort
        Collections.sort(data, DataTransformationService.MATCH_GOAL_MINUTE_ASC);

        // assert order
        Assert.assertEquals(first, data.get(0));
        Assert.assertEquals(second, data.get(1));
    }

    @Test
    public void firstGoalTeamOne() {
        assertGoalGetterTeam(null, goal(1, 0, 12, 1, "John", false, false, false), teamOne);
    }

    @Test
    public void firstGoalTeamOneOwnGoal() {
        assertGoalGetterTeam(null, goal(1, 0, 12, 1, "John", false, true, false), teamTwo);
    }

    @Test
    public void firstGoalTeamTwo() {
        assertGoalGetterTeam(null, goal(0, 1, 12, 1, "John", false, false, false), teamTwo);
    }

    @Test
    public void firstGoalTeamTwoOwnGoal() {
        assertGoalGetterTeam(null, goal(0, 1, 12, 1, "John", false, true, false), teamOne);
    }

    @Test
    public void secondGoalTeamOne() {
        assertGoalGetterTeam(goal(1, 0, 12, 1, "John", false, false, false), goal(2, 0, 14, 1, "John", false, false, false), teamOne);
    }

    @Test
    public void secondGoalTeamOneOwnGoal() {
        assertGoalGetterTeam(goal(1, 0, 12, 1, "John", false, false, false), goal(2, 0, 14, 1, "John", false, true, false), teamTwo);
    }

    @Test
    public void secondGoalTeamTwo() {
        assertGoalGetterTeam(goal(1, 0, 12, 1, "John", false, false, false), goal(1, 1, 14, 1, "John", false, false, false), teamTwo);
    }

    @Test
    public void secondGoalTeamTwoOwnGoal() {
        assertGoalGetterTeam(goal(1, 0, 12, 1, "John", false, false, false), goal(1, 1, 14, 1, "John", false, true, false), teamOne);
    }

    private void assertGoalGetterTeam(MatchGoalData lastGoalData, MatchGoalData goalData, TeamData expectedGoalGetterTeam) {

        // create match data
        MatchData matchData = new MatchData();
        matchData.setTeamOne(teamOne);
        matchData.setTeamTwo(teamTwo);

        // compute and assert data
        TeamData goalGetterTeam = service.goalGetterTeam(matchData, lastGoalData, goalData);
        Assert.assertEquals(expectedGoalGetterTeam, goalGetterTeam);
    }

    @Test
    public void noDataMatchTimePast() {
        assertMatch(Status.IN_PROGRESS, MATCH_TIME, null, false, null, null);
    }

    @Test
    public void noDataMatchTimeFuture() {
        assertMatch(MATCH_TIME_FUTURE, null, false, null, null);
    }

    @Test
    public void inProgressMatchFirstHalfNoHalfTimeResult() {
        assertMatch(MATCH_TIME, MATCH_UPDATE, false, null, fullTime(1, 0), goal(1, 0, 27, 1, "John", false, false, false));
    }

    @Test
    public void inProgressMatchFirstHalfNoHalfTimeResultNoGoals() {
        assertMatch(MATCH_TIME, MATCH_UPDATE, false, null, fullTime(1, 0));
    }

    @Test
    public void inProgressMatchFirstHalfWithHalfTimeResult() {
        assertMatch(MATCH_TIME, MATCH_UPDATE, false, halfTime(1, 0), fullTime(1, 0), goal(1, 0, 27, 1, "John", false, false, false));
    }

    @Test
    public void inProgressMatchFirstHalfWithHalfTimeResultNoGoals() {
        assertMatch(MATCH_TIME, MATCH_UPDATE, false, halfTime(1, 0), fullTime(1, 0));
    }

    @Test
    public void inProgressMatchSecondHalfNoHalfTimeResult() {
        assertMatch(MATCH_TIME, MATCH_UPDATE, false, null, fullTime(2, 0), goal(1, 0, 27, 1, "John", false, false, false), goal(2, 0, 66, 1, "John", false, false, false));
    }

    @Test
    public void inProgressMatchSecondHalfNoHalfTimeResultNoGoals() {
        assertMatch(MATCH_TIME, MATCH_UPDATE, false, null, fullTime(2, 0));
    }

    @Test
    public void inProgressMatchSecondHalfWithHalfTimeResult() {
        assertMatch(MATCH_TIME, MATCH_UPDATE, false, halfTime(1, 0), fullTime(2, 0), goal(1, 0, 27, 1, "John", false, false, false),
                goal(1, 0, 27, 1, "John", false, false, false));
    }

    @Test
    public void inProgressMatchSecondHalfWithHalfTimeResultNoGoals() {
        assertMatch(MATCH_TIME, MATCH_UPDATE, false, halfTime(1, 0), fullTime(2, 0));
    }

    @Test
    public void finishedMatch() {
        assertMatch(MATCH_TIME, MATCH_UPDATE, true, halfTime(0, 1), fullTime(2, 1), goal(0, 1, 13, 1, "John", false, true, false), goal(1, 1, 47, 2, "Bobby", true, false, false),
                goal(2, 1, 92, 1, "John", false, false, true));
    }

    @Test
    public void finishedMatchNoHalfTimeResult() {
        assertMatch(MATCH_TIME, MATCH_UPDATE, true, null, fullTime(2, 1), goal(0, 1, 13, 1, "John", false, true, false), goal(1, 1, 47, 2, "Bobby", true, false, false),
                goal(2, 1, 92, 1, "John", false, false, true));
    }

    @Test
    public void finishedMatchNoGoals() {
        assertMatch(MATCH_TIME, MATCH_UPDATE, true, halfTime(0, 1), fullTime(2, 1));
    }

    @Test
    public void finishedMatchNoHalfTimeResultNoGoals() {
        assertMatch(MATCH_TIME, MATCH_UPDATE, true, null, fullTime(2, 1));
    }

    private void assertMatch(String dateTimeString, String lastUpdateString, boolean finished, MatchResultData halfTimeResult, MatchResultData fullTimeResult,
            MatchGoalData... goals) {
        assertMatch(null, dateTimeString, lastUpdateString, finished, halfTimeResult, fullTimeResult, goals);
    }

    private void assertMatch(Status expectedStatus, String dateTimeString, String lastUpdateString, boolean finished, MatchResultData halfTimeResult,
            MatchResultData fullTimeResult, MatchGoalData... goals) {

        // convert
        MatchData matchData = match(dateTimeString, lastUpdateString, finished, teamOne, teamTwo, halfTimeResult, fullTimeResult, goals);
        Match match = service.match(matchData);
        Assert.assertNotNull(match);

        // assert basic data
        Assert.assertEquals(matchData.getId(), match.getId());
        Assert.assertEquals(season, match.getSeason());
        Assert.assertEquals(matchday, match.getMatchday());
        Assert.assertEquals(DateUtils.parseOpenligaDbDate(dateTimeString), match.getAssignedTime());
        Assert.assertEquals(lastUpdateString, match.getLastUpdateTimeString());

        // assert team data
        Assert.assertEquals(teamOne.getName(), match.getTeamOneName());
        Assert.assertEquals(teamOne.getIconUrl(), match.getTeamOneIconUrl());
        Assert.assertEquals(teamTwo.getName(), match.getTeamTwoName());
        Assert.assertEquals(teamTwo.getIconUrl(), match.getTeamTwoIconUrl());

        // assert result data
        if (expectedStatus != null) {
            Assert.assertEquals(expectedStatus, match.getStatus());
        } else {
            Assert.assertEquals(finished ? Status.FINISHED : matchData.getResults() == null || matchData.getResults().isEmpty() ? Status.WAITING : Status.IN_PROGRESS,
                    match.getStatus());
        }
        Assert.assertEquals(halfTimeResult != null ? halfTimeResult.getPointsTeamOne() : null, match.getGoalsTeamOneHalfTime());
        Assert.assertEquals(halfTimeResult != null ? halfTimeResult.getPointsTeamTwo() : null, match.getGoalsTeamTwoHalfTime());
        Assert.assertEquals(fullTimeResult != null ? fullTimeResult.getPointsTeamOne() : null, match.getGoalsTeamOneFullTime());
        Assert.assertEquals(fullTimeResult != null ? fullTimeResult.getPointsTeamTwo() : null, match.getGoalsTeamTwoFullTime());
        if (goals == null || goals.length < 1) {
            Assert.assertTrue(match.getGoals().isEmpty());
        } else {
            Assert.assertEquals(goals.length, match.getGoals().size());
            int lastOrder = -1;
            for (int i = 0; i < goals.length; i++) {

                // get objects
                MatchGoalData expectedGoal = goals[i];
                Assert.assertNotNull(expectedGoal);
                Goal actualGoal = match.getGoals().get(i);
                Assert.assertNotNull(actualGoal);

                // assert increasing order value
                Assert.assertTrue(actualGoal.getOrder() > lastOrder);
                lastOrder = actualGoal.getOrder();

                // assert goal data
                Assert.assertEquals(expectedGoal.getPointsTeamOne(), actualGoal.getGoalsTeamOne());
                Assert.assertEquals(expectedGoal.getPointsTeamTwo(), actualGoal.getGoalsTeamTwo());
                Assert.assertEquals(expectedGoal.getMatchMinute(), actualGoal.getMinute());
                Assert.assertEquals(expectedGoal.getGoalGetterName(), actualGoal.getGoalGetterName());
                Assert.assertEquals(expectedGoal.isPenalty(), actualGoal.isPenalty());
                Assert.assertEquals(expectedGoal.isOwnGoal(), actualGoal.isOwnGoal());
                Assert.assertEquals(expectedGoal.isOvertime(), actualGoal.isOvertime());
            }
        }
    }

    private MatchData match(String dateTimeString, String lastUpdateString, boolean finished, TeamData teamOne, TeamData teamTwo, MatchResultData halfTimeResult,
            MatchResultData fullTimeResult, MatchGoalData... goals) {

        // set basic values
        MatchData matchData = new MatchData();
        matchData.setId(testData.idSequence++);
        matchData.setLeague(Match.LEAGUE_BUNDESLIGA_FIRST);
        matchData.setSeason(season);
        matchData.setMatchday(matchday);
        matchData.setDateTimeString(dateTimeString);
        matchData.setLastUpdateString(lastUpdateString);

        // set teams
        matchData.setTeamOne(teamOne);
        matchData.setTeamTwo(teamTwo);

        // set results
        matchData.setFinished(finished);
        matchData.setResults(new ArrayList<>());
        if (halfTimeResult != null) {
            matchData.getResults().add(halfTimeResult);
        }
        if (fullTimeResult != null) {
            matchData.getResults().add(fullTimeResult);
        }
        matchData.setGoals(new ArrayList<>());
        if (goals != null) {
            for (MatchGoalData goal : goals) {
                matchData.getGoals().add(goal);
            }
        }

        // done
        return matchData;
    }

    private TeamData team(String name, String iconUrl) {

        // set basic values
        TeamData teamData = new TeamData();
        teamData.setId(testData.idSequence++);
        teamData.setName(name);
        teamData.setIconUrl(iconUrl);

        // done
        return teamData;
    }

    private MatchResultData halfTime(int pointsTeamOne, int pointsTeamTwo) {
        return result(1, MatchResultData.NAME_HALF_TIME, pointsTeamOne, pointsTeamTwo);
    }

    private MatchResultData fullTime(int pointsTeamOne, int pointsTeamTwo) {
        return result(2, MatchResultData.NAME_FULL_TIME, pointsTeamOne, pointsTeamTwo);
    }

    private MatchResultData result(int orderId, String name, int pointsTeamOne, int pointsTeamTwo) {

        // set basic values
        MatchResultData resultData = new MatchResultData();
        resultData.setId(testData.idSequence++);
        resultData.setOrderId(orderId);
        resultData.setName(name);
        resultData.setPointsTeamOne(pointsTeamOne);
        resultData.setPointsTeamTwo(pointsTeamTwo);

        // done
        return resultData;
    }

    private MatchGoalData goal(int pointsTeamOne, int pointsTeamTwo, int matchMinute, long goalGetterId, String goalGetterName, boolean penalty, boolean ownGoal,
            boolean overtime) {

        // set basic values
        MatchGoalData goalData = new MatchGoalData();
        goalData.setId(testData.idSequence++);
        goalData.setPointsTeamOne(pointsTeamOne);
        goalData.setPointsTeamTwo(pointsTeamTwo);
        goalData.setMatchMinute(matchMinute);
        goalData.setGoalGetterId(goalGetterId);
        goalData.setGoalGetterName(goalGetterName);
        goalData.setPenalty(penalty);
        goalData.setOwnGoal(ownGoal);
        goalData.setOvertime(overtime);

        // done
        return goalData;
    }
}
