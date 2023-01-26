package model.community;

import org.apache.commons.lang.StringUtils;

import model.match.Match;
import model.statistics.Winner;

public enum Result {
    EXACT, RELATIVE, WINNER, WRONG, ERROR;

    public static Result calc(BulibotExecution bulibotExecution, Match match) {

        // check error state
        if (!bulibotExecution.isResultAvailable() || StringUtils.isNotBlank(bulibotExecution.getErrorCauseType())
                || StringUtils.isNotBlank(bulibotExecution.getErrorCauseMessage())) {
            return ERROR;
        }

        // check exact match
        if (bulibotExecution.getGoalsTeamOne().equals(match.getGoalsTeamOneFullTime()) && bulibotExecution.getGoalsTeamTwo().equals(match.getGoalsTeamTwoFullTime())) {
            return Result.EXACT;
        }

        // check winner
        Winner actualWinner = Winner.calc(match);
        Winner expectedWinner = Winner.calc(bulibotExecution);
        if (actualWinner != null && expectedWinner != null && actualWinner == expectedWinner) {

            // check differences
            int matchGoalDifference = match.getGoalsTeamOneFullTime() - match.getGoalsTeamTwoFullTime();
            int bulibotExecutionGoalDifference = bulibotExecution.getGoalsTeamOne() - bulibotExecution.getGoalsTeamTwo();
            if (actualWinner != Winner.DRAW && matchGoalDifference == bulibotExecutionGoalDifference) {

                // relative hit, no draw
                return RELATIVE;
            } else {

                // winner matches
                return WINNER;
            }
        }

        // no match at all
        return WRONG;
    }
}
