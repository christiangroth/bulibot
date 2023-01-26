package model.community;

import flexjson.JSON;
import model.statistics.Winner;

public class Rank {

    private int position;
    private final long userId;
    private int resultsWithError;
    private int resultsWithExactHit;
    private int resultsWithRelativeHit;
    private int resultsWithWinnerHit;
    private int resultsWithNoHit;
    private int points;
    private long bulibotVersions;

    private int resultsHomeTeam;
    private int resultsAwayTeam;
    private int resultsDraw;

    private int pointsWithHomeTeam;
    private int pointsWithAwayTeam;
    private int pointsWithDraw;

    private int pointsWithExactHit;
    private int pointsWithRelativeHit;
    private int pointsWithWinnerHit;

    public Rank(long userId) {
        this.userId = userId;
    }

    public void resultsWithErrorInc() {
        resultsWithError++;
    }

    public void resultsWithExactHitInc(int points, Winner winner) {
        resultsWithExactHit++;
        this.points += points;
        pointsWithExactHit += points;
        updateWinnerPoints(winner, points);
    }

    public void resultsWithRelativeHitInc(int points, Winner winner) {
        resultsWithRelativeHit++;
        this.points += points;
        pointsWithRelativeHit += points;
        updateWinnerPoints(winner, points);
    }

    public void resultsWithWinnerHitInc(int points, Winner winner) {
        resultsWithWinnerHit++;
        this.points += points;
        pointsWithWinnerHit += points;
        updateWinnerPoints(winner, points);
    }

    private void updateWinnerPoints(Winner winner, int points) {
        if (winner != null) {
            switch (winner) {
                case ONE:
                    pointsWithHomeTeam += points;
                    break;
                case DRAW:
                    pointsWithDraw += points;
                    break;
                case TWO:
                    pointsWithAwayTeam += points;
                    break;
            }
        }
    }

    public void resultsWithNoHitInc() {
        resultsWithNoHit++;
    }

    public void resultsHomeTeamInc() {
        resultsHomeTeam++;
    }

    public void resultsDrawInc() {
        resultsDraw++;
    }

    public void resultsAwayTeamInc() {
        resultsAwayTeam++;
    }

    public double getErrorPercentage() {
        return getPercentage(resultsWithError);
    }

    public double getExactHitPercentage() {
        return getPercentage(resultsWithExactHit);
    }

    public double getRelativeHitPercentage() {
        return getPercentage(resultsWithRelativeHit);
    }

    public double getWinnerHitPercentage() {
        return getPercentage(resultsWithWinnerHit);
    }

    public double getNoHitPercentage() {
        return getPercentage(resultsWithNoHit);
    }

    private double getPercentage(double value) {
        return getNumberOfResults() > 0 ? Math.round(value / getNumberOfResults() * 10000) / 100.0d : 0.0d;
    }

    @JSON(include = false)
    public int getNumberOfResults() {
        return resultsWithError + resultsWithExactHit + resultsWithRelativeHit + resultsWithWinnerHit + resultsWithNoHit;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public long getUserId() {
        return userId;
    }

    public int getResultsWithError() {
        return resultsWithError;
    }

    public void setResultsWithError(int resultsWithError) {
        this.resultsWithError = resultsWithError;
    }

    public int getResultsWithExactHit() {
        return resultsWithExactHit;
    }

    public void setResultsWithExactHit(int resultsWithExactHit) {
        this.resultsWithExactHit = resultsWithExactHit;
    }

    public int getResultsWithRelativeHit() {
        return resultsWithRelativeHit;
    }

    public void setResultsWithRelativeHit(int resultsWithRelativeHit) {
        this.resultsWithRelativeHit = resultsWithRelativeHit;
    }

    public int getResultsWithWinnerHit() {
        return resultsWithWinnerHit;
    }

    public void setResultsWithWinnerHit(int resultsWithWinnerHit) {
        this.resultsWithWinnerHit = resultsWithWinnerHit;
    }

    public int getResultsWithNoHit() {
        return resultsWithNoHit;
    }

    public void setResultsWithNoHit(int resultsWithNoHit) {
        this.resultsWithNoHit = resultsWithNoHit;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public long getBulibotVersions() {
        return bulibotVersions;
    }

    public void setBulibotVersions(long bulibotVersions) {
        this.bulibotVersions = bulibotVersions;
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

    public int getPointsWithHomeTeam() {
        return pointsWithHomeTeam;
    }

    public void setPointsWithHomeTeam(int pointsWithHomeTeam) {
        this.pointsWithHomeTeam = pointsWithHomeTeam;
    }

    public int getPointsWithAwayTeam() {
        return pointsWithAwayTeam;
    }

    public void setPointsWithAwayTeam(int pointsWithAwayTeam) {
        this.pointsWithAwayTeam = pointsWithAwayTeam;
    }

    public int getPointsWithDraw() {
        return pointsWithDraw;
    }

    public void setPointsWithDraw(int pointsWithDraw) {
        this.pointsWithDraw = pointsWithDraw;
    }

    public int getPointsWithExactHit() {
        return pointsWithExactHit;
    }

    public void setPointsWithExactHit(int pointsWithExactHit) {
        this.pointsWithExactHit = pointsWithExactHit;
    }

    public int getPointsWithRelativeHit() {
        return pointsWithRelativeHit;
    }

    public void setPointsWithRelativeHit(int pointsWithRelativeHit) {
        this.pointsWithRelativeHit = pointsWithRelativeHit;
    }

    public int getPointsWithWinnerHit() {
        return pointsWithWinnerHit;
    }

    public void setPointsWithWinnerHit(int pointsWithWinnerHit) {
        this.pointsWithWinnerHit = pointsWithWinnerHit;
    }
}
