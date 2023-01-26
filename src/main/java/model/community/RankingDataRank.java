package model.community;

import java.util.ArrayList;
import java.util.List;

public class RankingDataRank {
    private Rank rank;
    private Rank matchdayRank;
    private final List<RankingDataBulibotExecution> bulibotExecutions = new ArrayList<>();

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public Rank getMatchdayRank() {
        return matchdayRank;
    }

    public void setMatchdayRank(Rank matchdayRank) {
        this.matchdayRank = matchdayRank;
    }

    public List<RankingDataBulibotExecution> getBulibotExecutions() {
        return bulibotExecutions;
    }
}
