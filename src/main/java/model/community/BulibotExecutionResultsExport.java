package model.community;

import java.util.ArrayList;
import java.util.List;

import flexjson.JSON;
import model.match.Match;

public class BulibotExecutionResultsExport {

    private final int season;
    private final int matchday;

    @JSON
    private final List<BulibotExecutionResultExport> results = new ArrayList<>();

    public BulibotExecutionResultsExport(int season, int matchday, List<Match> matches, List<BulibotExecution> executions) {
        this.season = season;
        this.matchday = matchday;
        if (executions != null && matches != null) {

            // create data
            for (BulibotExecution execution : executions) {

                // get match
                Match match = matches.stream().filter(m -> m.getId() == execution.getMatchId()).findFirst().orElse(null);
                if (match == null) {
                    continue;
                }

                // add data
                results.add(new BulibotExecutionResultExport(match, execution.getGoalsTeamOne(), execution.getGoalsTeamTwo(), execution.getErrorCauseType(),
                        execution.getErrorCauseMessage()));
            }
        }
    }

    public int getSeason() {
        return season;
    }

    public int getMatchday() {
        return matchday;
    }

    public List<BulibotExecutionResultExport> getResults() {
        return results;
    }
}
