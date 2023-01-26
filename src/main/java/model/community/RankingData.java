package model.community;

import java.util.ArrayList;
import java.util.List;

import model.match.Match;

public class RankingData {
    private final List<Match> matches = new ArrayList<>();
    private final List<RankingDataRank> ranks = new ArrayList<>();

    private int resultsHomeTeam;
    private int resultsAwayTeam;
    private int resultsDraw;

    public List<Match> getMatches() {
        return matches;
    }

    public List<RankingDataRank> getRanks() {
        return ranks;
    }

    public int getResultsHomeTeam() {
        return resultsHomeTeam;
    }

    public void setResultsHomeTeam(int resultsHomeTeam) {
        this.resultsHomeTeam = resultsHomeTeam;
    }

    public int getResultsAwayTeam() {
        return resultsAwayTeam;
    }

    public void setResultsAwayTeam(int resultsAwayTeam) {
        this.resultsAwayTeam = resultsAwayTeam;
    }

    public int getResultsDraw() {
        return resultsDraw;
    }

    public void setResultsDraw(int resultsDraw) {
        this.resultsDraw = resultsDraw;
    }
}
