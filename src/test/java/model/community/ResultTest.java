package model.community;

import org.junit.Assert;
import org.junit.Test;

import model.match.Match;

public class ResultTest {

    @Test
    public void exact() {
        calc(Result.EXACT, bulibotExecution(2, 1), match(2, 1));
    }

    @Test
    public void exactButError() {
        calc(Result.ERROR, bulibotExecution(2, 1, "SomeException", "some message"), match(2, 1));
    }

    @Test
    public void exactDraw() {
        calc(Result.EXACT, bulibotExecution(1, 1), match(1, 1));
    }

    @Test
    public void exactDrawButError() {
        calc(Result.ERROR, bulibotExecution(1, 1, "SomeException", "some message"), match(1, 1));
    }

    @Test
    public void relative() {
        calc(Result.RELATIVE, bulibotExecution(3, 2), match(2, 1));
    }

    @Test
    public void relativeButError() {
        calc(Result.ERROR, bulibotExecution(3, 2, "SomeException", "some message"), match(2, 1));
    }

    @Test
    public void relativeDraw() {
        calc(Result.WINNER, bulibotExecution(2, 2), match(1, 1));
    }

    @Test
    public void relativeDrawButError() {
        calc(Result.ERROR, bulibotExecution(2, 2, "SomeException", "some message"), match(1, 1));
    }

    @Test
    public void winner() {
        calc(Result.WINNER, bulibotExecution(1, 2), match(0, 2));
    }

    @Test
    public void winnerButError() {
        calc(Result.ERROR, bulibotExecution(1, 2, "SomeException", "some message"), match(0, 2));
    }

    @Test
    public void wrong() {
        calc(Result.WRONG, bulibotExecution(1, 2), match(2, 1));
    }

    @Test
    public void wrongDraw() {
        calc(Result.WRONG, bulibotExecution(1, 2), match(1, 1));
    }

    @Test
    public void wrongNonDraw() {
        calc(Result.WRONG, bulibotExecution(1, 1), match(2, 1));
    }

    private BulibotExecution bulibotExecution(Integer goalsTeamOne, Integer goalsTeamTwo) {
        return bulibotExecution(goalsTeamOne, goalsTeamTwo, null, null);
    }

    private BulibotExecution bulibotExecution(Integer goalsTeamOne, Integer goalsTeamTwo, String errorCauseType, String errorCauseMessage) {

        // set data
        BulibotExecution bulibotExecution = new BulibotExecution();
        bulibotExecution.setGoalsTeamOne(goalsTeamOne);
        bulibotExecution.setGoalsTeamTwo(goalsTeamTwo);
        bulibotExecution.setErrorCauseType(errorCauseType);
        bulibotExecution.setErrorCauseMessage(errorCauseMessage);

        // done
        return bulibotExecution;
    }

    private Match match(int fulltimeGoalsTeamOne, int fulltimeGoalsTeamTwo) {

        // set data
        Match match = new Match();
        match.setGoalsTeamOneFullTime(fulltimeGoalsTeamOne);
        match.setGoalsTeamTwoFullTime(fulltimeGoalsTeamTwo);

        // done
        return match;
    }

    private void calc(Result result, BulibotExecution bulibotExecution, Match match) {
        Assert.assertEquals(result, Result.calc(bulibotExecution, match));
    }
}
