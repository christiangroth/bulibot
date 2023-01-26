package model.community;

import java.util.ArrayList;
import java.util.List;

public class TestdataMatchdayResult {
    private final int season;
    private final int matchday;
    private final List<TestdataMatchResult> matchResults = new ArrayList<>();

    public TestdataMatchdayResult(int season, int matchday) {
        this.season = season;
        this.matchday = matchday;
    }

    public int getSeason() {
        return season;
    }

    public int getMatchday() {
        return matchday;
    }

    public List<TestdataMatchResult> getMatchResults() {
        return matchResults;
    }

    public long getResultsWithError() {
        return matchResults.stream().filter(r -> r.isError()).count();
    }

    public double getResultsWithErrorPercentage() {
        return getNumberOfResults() > 0 ? getPercentage(matchResults.stream().filter(r -> r.isError()).count()) : 0;
    }

    public long getResultsWithExactHit() {
        return matchResults.stream().filter(r -> r.isExactHit()).count();
    }

    public double getResultsWithExactHitPercentage() {
        return getNumberOfResults() > 0 ? getPercentage(matchResults.stream().filter(r -> r.isExactHit()).count()) : 0;
    }

    public long getResultsWithRelativeHit() {
        return matchResults.stream().filter(r -> r.isRelativeHit()).count();
    }

    public double getResultsWithRelativeHitPercentage() {
        return getNumberOfResults() > 0 ? getPercentage(matchResults.stream().filter(r -> r.isRelativeHit()).count()) : 0;
    }

    public long getResultsWithWinnerHit() {
        return matchResults.stream().filter(r -> r.isWinnerHit()).count();
    }

    public double getResultsWithWinnerHitPercentage() {
        return getNumberOfResults() > 0 ? getPercentage(matchResults.stream().filter(r -> r.isWinnerHit()).count()) : 0;
    }

    public long getResultsWithNoHit() {
        return matchResults.stream().filter(r -> r.isNoHit()).count();
    }

    public double getResultsWithNoHitPercentage() {
        return getNumberOfResults() > 0 ? getPercentage(matchResults.stream().filter(r -> r.isNoHit()).count()) : 0;
    }

    public int getPoints() {
        return matchResults.stream().mapToInt(r -> r.getPoints()).sum();
    }

    private double getPercentage(double value) {
        return getNumberOfResults() > 0 ? Math.round(value / getNumberOfResults() * 10000) / 100.0d : 0.0d;
    }

    private int getNumberOfResults() {
        return matchResults.size();
    }
}
