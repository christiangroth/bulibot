package model.community;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class TestdataResult {
    private final List<TestdataMatchdayResult> matchdayResults = Collections.synchronizedList(new ArrayList<>());

    public List<TestdataMatchdayResult> getMatchdayResults() {
        return matchdayResults;
    }

    public long getResultsWithError() {
        return getMatchResultsStream().filter(r -> r.isError()).count();
    }

    public double getResultsWithErrorPercentage() {
        return getNumberOfResults() > 0 ? getPercentage(getMatchResultsStream().filter(r -> r.isError()).count()) : 0;
    }

    public long getResultsWithExactHit() {
        return getMatchResultsStream().filter(r -> r.isExactHit()).count();
    }

    public double getResultsWithExactHitPercentage() {
        return getNumberOfResults() > 0 ? getPercentage(getMatchResultsStream().filter(r -> r.isExactHit()).count()) : 0;
    }

    public long getResultsWithRelativeHit() {
        return getMatchResultsStream().filter(r -> r.isRelativeHit()).count();
    }

    public double getResultsWithRelativeHitPercentage() {
        return getNumberOfResults() > 0 ? getPercentage(getMatchResultsStream().filter(r -> r.isRelativeHit()).count()) : 0;
    }

    public long getResultsWithWinnerHit() {
        return getMatchResultsStream().filter(r -> r.isWinnerHit()).count();
    }

    public double getResultsWithWinnerHitPercentage() {
        return getNumberOfResults() > 0 ? getPercentage(getMatchResultsStream().filter(r -> r.isWinnerHit()).count()) : 0;
    }

    public long getResultsWithNoHit() {
        return getMatchResultsStream().filter(r -> r.isNoHit()).count();
    }

    public double getResultsWithNoHitPercentage() {
        return getNumberOfResults() > 0 ? getPercentage(getMatchResultsStream().filter(r -> r.isNoHit()).count()) : 0;
    }

    public int getPoints() {
        return getMatchResultsStream().mapToInt(r -> r.getPoints()).sum();
    }

    private Stream<TestdataMatchResult> getMatchResultsStream() {
        return matchdayResults.stream().flatMap(r -> r.getMatchResults().stream());
    }

    private double getPercentage(double value) {
        return getNumberOfResults() > 0 ? Math.round(value / getNumberOfResults() * 10000) / 100.0d : 0.0d;
    }

    private long getNumberOfResults() {
        return getMatchResultsStream().count();
    }
}
