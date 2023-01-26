package model.community;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import model.match.Match;

public class RankingTest {

    private long userOne = 2l;
    private long userTwo = 1l;

    private List<BulibotExecution> bulibotExecutions = new ArrayList<>();
    private List<Match> matches = new ArrayList<>();

    private int pointsExactHit = 4;
    private int pointsRelativeHit = 2;
    private int pointsWinnerHit = 1;

    private long sequence = 0;

    @Test
    public void executionsMissing() {
        data(2, 1, -1, null, null, null, null);
        Ranking ranking = new Ranking(new ArrayList<>(), bulibotExecutions, false, matches, pointsExactHit, pointsRelativeHit, pointsWinnerHit);
        Assert.assertTrue(ranking.getRanks().isEmpty());
    }

    @Test
    public void matchMissing() {
        data(null, null, userOne, 2, 1, null, null);
        Ranking ranking = new Ranking(new ArrayList<>(), bulibotExecutions, false, matches, pointsExactHit, pointsRelativeHit, pointsWinnerHit);
        Assert.assertTrue(ranking.getRanks().isEmpty());
    }

    @Test
    public void points() {
        data(2, 1, userOne, 2, 1, null, null);
        data(1, 0, userTwo, 2, 1, null, null);
        Ranking ranking = new Ranking(new ArrayList<>(), bulibotExecutions, false, matches, pointsExactHit, pointsRelativeHit, pointsWinnerHit);
        assertRank(ranking, userOne, 1, 4, 0, 1, 0, 0, 0);
        assertRank(ranking, userTwo, 2, 2, 0, 0, 1, 0, 0);
    }

    @Test
    public void errors() {
        data(2, 1, userOne, 1, 2, null, null);
        data(1, 0, userTwo, null, null, "SomeException", "really bad man, really bad!!");
        Ranking ranking = new Ranking(new ArrayList<>(), bulibotExecutions, false, matches, pointsExactHit, pointsRelativeHit, pointsWinnerHit);
        assertRank(ranking, userOne, 1, 0, 0, 0, 0, 0, 1);
        assertRank(ranking, userTwo, 2, 0, 1, 0, 0, 0, 0);
    }

    @Test
    public void exacts() {
        data(2, 1, userOne, 2, 1, null, null);
        data(1, 0, userTwo, 2, 1, null, null);
        data(1, 0, userTwo, 2, 1, null, null);
        Ranking ranking = new Ranking(new ArrayList<>(), bulibotExecutions, false, matches, pointsExactHit, pointsRelativeHit, pointsWinnerHit);
        assertRank(ranking, userOne, 1, 4, 0, 1, 0, 0, 0);
        assertRank(ranking, userTwo, 2, 4, 0, 0, 2, 0, 0);
    }

    @Test
    public void relatives() {
        data(2, 1, userOne, 1, 0, null, null);
        data(1, 0, userTwo, 3, 1, null, null);
        data(1, 0, userTwo, 3, 1, null, null);
        Ranking ranking = new Ranking(new ArrayList<>(), bulibotExecutions, false, matches, pointsExactHit, pointsRelativeHit, pointsWinnerHit);
        assertRank(ranking, userOne, 1, 2, 0, 0, 1, 0, 0);
        assertRank(ranking, userTwo, 2, 2, 0, 0, 0, 2, 0);
    }

    @Test
    public void nameFallback() {
        data(1, 0, userOne, 3, 1, null, null);
        data(1, 0, userTwo, 3, 1, null, null);
        Ranking ranking = new Ranking(new ArrayList<>(), bulibotExecutions, false, matches, pointsExactHit, pointsRelativeHit, pointsWinnerHit);
        assertRank(ranking, userTwo, 1, 1, 0, 0, 0, 1, 0);
        assertRank(ranking, userOne, 2, 1, 0, 0, 0, 1, 0);
    }

    private void data(Integer matchGoalsTeamOne, Integer matchGoalsTeamTwo, long user, Integer goalsTeamOne, Integer goalsTeamTwo, String errorCauseType,
            String errorCauseMessage) {

        // match
        long id = sequence++;
        if (matchGoalsTeamOne != null && matchGoalsTeamTwo != null) {
            Match match = new Match();
            match.setId(id);
            match.setGoalsTeamOneFullTime(matchGoalsTeamOne);
            match.setGoalsTeamTwoFullTime(matchGoalsTeamTwo);
            matches.add(match);
        }

        // bulibbot execution
        if (goalsTeamOne != null || goalsTeamTwo != null || errorCauseType != null || errorCauseMessage != null) {
            BulibotExecution bulibotExecution = new BulibotExecution();
            bulibotExecution.setUserId(user);
            bulibotExecution.setMatchId(id);
            bulibotExecution.setGoalsTeamOne(goalsTeamOne);
            bulibotExecution.setGoalsTeamTwo(goalsTeamTwo);
            bulibotExecution.setErrorCauseType(errorCauseType);
            bulibotExecution.setErrorCauseMessage(errorCauseMessage);
            bulibotExecutions.add(bulibotExecution);
        }
    }

    private void assertRank(Ranking ranking, long userId, int position, int points, int errors, int exacts, int relatives, int winners, int wrongs) {
        Rank rank = ranking.getRank(userId);
        Assert.assertNotNull(rank);
        Assert.assertEquals(position, rank.getPosition());
        Assert.assertEquals(points, rank.getPoints());
        Assert.assertEquals(errors, rank.getResultsWithError());
        Assert.assertEquals(exacts, rank.getResultsWithExactHit());
        Assert.assertEquals(relatives, rank.getResultsWithRelativeHit());
        Assert.assertEquals(winners, rank.getResultsWithWinnerHit());
        Assert.assertEquals(wrongs, rank.getResultsWithNoHit());
    }
}
