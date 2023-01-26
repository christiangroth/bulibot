package model.statistics.result;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.ObjectUtils;

import model.statistics.MatchStatistics;
import model.statistics.Winner;

/**
 * A builder to filter match data and create a {@link StatisticsResult} instance.
 *
 * @author chris
 */
public class StatisticsResultBuilder {

    private EloHelper.Builder eloHelperBuilder;
    private Stream<MatchStatistics> stream;

    /**
     * Creates a new builder instance based on the given stream of match data.
     *
     * @param matches
     *            stream of match data
     */
    public StatisticsResultBuilder(List<MatchStatistics> matches) {

        // create elo helper builder
        eloHelperBuilder = EloHelper.builder();

        // create stream
        ArrayList<MatchStatistics> matchList = new ArrayList<>();
        if (matches != null) {
            matchList.addAll(matches);
        }
        stream = matchList.stream();
    }

    /**
     * @see EloHelper#compute(int, int, Winner, int)
     * @param riskHome
     *            risk for home team
     * @param riskAway
     *            risk for away team
     * @return this, for method chaining
     */
    public StatisticsResultBuilder eloRisks(double riskHome, double riskAway) {
        eloHelperBuilder.risks(riskHome, riskAway);
        return this;
    }

    /**
     * @see EloHelper#compute(int, int, Winner, int)
     * @param goalsMarginScorePercentages
     *            goals difference configuration
     * @return this, for method chaining
     */
    public StatisticsResultBuilder eloGoalsMarginScorePercentages(TreeMap<Integer, Double> goalsMarginScorePercentages) {
        eloHelperBuilder.goalsMarginScorePercentages(goalsMarginScorePercentages);
        return this;
    }

    /**
     * Selects all matches with matching season value.
     *
     * @param seasons
     *            seasons
     * @return this, for method chaining
     */
    public StatisticsResultBuilder filterSeason(Integer... seasons) {
        List<Integer> list = safeToList(seasons);
        stream = stream.filter(m -> list.contains(m.getSeason()));
        return this;
    }

    /**
     * Selects all matches with matching matchday.
     *
     * @param matchday
     *            matchday
     * @return this, for method chaining
     */
    public StatisticsResultBuilder filterMatchday(int matchday) {
        stream = stream.filter(m -> m.getMatchday() == matchday);
        return this;
    }

    /**
     * Selects all matches with matchday between or equals to min and max value.
     *
     * @param matchdayMin
     *            min value
     * @param matchdayMax
     *            max value
     * @return this, for method chaining
     */
    public StatisticsResultBuilder filterMatchday(int matchdayMin, int matchdayMax) {
        stream = stream.filter(m -> m.getMatchday() >= matchdayMin && m.getMatchday() <= matchdayMax);
        return this;
    }

    /**
     * Selects all matches with season/matchday between or equals to min and max values.
     *
     * @param seasonMin
     *            min value for season
     * @param matchdayMin
     *            min value for matchday
     * @param seasonMax
     *            max value for season
     * @param matchdayMax
     *            max value for matchday
     * @return this, for method chaining
     */
    public StatisticsResultBuilder filterSeasonMatchday(int seasonMin, int matchdayMin, int seasonMax, int matchdayMax) {
        stream = stream.filter(m -> m.getSeason() >= seasonMin && m.getSeason() <= seasonMax).filter(m -> {

            // check for min season
            if (m.getSeason() == seasonMin) {
                return m.getMatchday() >= matchdayMin;
            }

            // check for max season
            if (m.getSeason() == seasonMax) {
                return m.getMatchday() <= matchdayMax;
            }

            // some season in between, always ok
            return true;
        });
        return this;
    }

    /**
     * Selects all matches with matching assigned time.
     *
     * @param matchAssignedTime
     *            assigned time
     * @return this, for method chaining
     */
    public StatisticsResultBuilder filterMatchAssignedTime(LocalDateTime matchAssignedTime) {
        stream = stream.filter(m -> ObjectUtils.equals(m.getMatchAssignedTime(), matchAssignedTime));
        return this;
    }

    /**
     * Selects all matches with assigned time between or equal to min and max value.
     *
     * @param matchAssignedTimeMin
     *            min value
     * @param matchAssignedTimeMax
     *            max value
     * @return this, for method chaining
     */
    public StatisticsResultBuilder filterMatchAssignedTime(LocalDateTime matchAssignedTimeMin, LocalDateTime matchAssignedTimeMax) {
        LocalDateTime min = matchAssignedTimeMin != null ? matchAssignedTimeMin : LocalDateTime.MAX;
        LocalDateTime max = matchAssignedTimeMax != null ? matchAssignedTimeMax : LocalDateTime.MIN;
        stream = stream.filter(m -> (min.isBefore(m.getMatchAssignedTime()) || ObjectUtils.equals(m.getMatchAssignedTime(), min))
                && (max.isAfter(m.getMatchAssignedTime()) || ObjectUtils.equals(m.getMatchAssignedTime(), matchAssignedTimeMax)));
        return this;
    }

    /**
     * Selects matches with assigned time on Monday.
     *
     * @return this, for method chaining
     */
    public StatisticsResultBuilder filterMonday() {
        return filterMatchDayOfWeek(DayOfWeek.MONDAY);
    }

    /**
     * Selects matches with assigned time on Tuesday.
     *
     * @return this, for method chaining
     */
    public StatisticsResultBuilder filterTuesday() {
        return filterMatchDayOfWeek(DayOfWeek.TUESDAY);
    }

    /**
     * Selects matches with assigned time on Wednesday.
     *
     * @return this, for method chaining
     */
    public StatisticsResultBuilder filterWednesday() {
        return filterMatchDayOfWeek(DayOfWeek.WEDNESDAY);
    }

    /**
     * Selects matches with assigned time on Thursday.
     *
     * @return this, for method chaining
     */
    public StatisticsResultBuilder filterThursday() {
        return filterMatchDayOfWeek(DayOfWeek.THURSDAY);
    }

    /**
     * Selects matches with assigned time on Friday.
     *
     * @return this, for method chaining
     */
    public StatisticsResultBuilder filterFriday() {
        return filterMatchDayOfWeek(DayOfWeek.FRIDAY);
    }

    /**
     * Selects matches with assigned time on Saturday.
     *
     * @return this, for method chaining
     */
    public StatisticsResultBuilder filterSaturday() {
        return filterMatchDayOfWeek(DayOfWeek.SATURDAY);
    }

    /**
     * Selects matches with assigned time on Sunday.
     *
     * @return this, for method chaining
     */
    public StatisticsResultBuilder filterSunday() {
        return filterMatchDayOfWeek(DayOfWeek.SUNDAY);
    }

    /**
     * Selects matches with assigned time on Friday, Saturday or Sunday.
     *
     * @return this, for method chaining
     */
    public StatisticsResultBuilder filterWeekend() {
        return filterMatchDayOfWeek(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    }

    /**
     * Selects matches with assigned time on Monday, Tuesday, Wednesday or Thursday.
     *
     * @return this, for method chaining
     */
    public StatisticsResultBuilder filterNonWeekend() {
        return filterMatchDayOfWeek(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY);
    }

    /**
     * Selects matches with assigned time on matching day of week.
     *
     * @param daysOfWeek
     *            day of week
     * @return this, for method chaining
     */
    public StatisticsResultBuilder filterMatchDayOfWeek(DayOfWeek... daysOfWeek) {
        List<String> list = safeToList(daysOfWeek).stream().map(t -> t != null ? t.getDisplayName(TextStyle.FULL, Locale.getDefault()) : null).collect(Collectors.toList());
        stream = stream.filter(m -> list.contains(m.getMatchDayOfWeek()));
        return this;
    }

    /**
     * Selects matches with assigned time contained in given values.
     *
     * @param matchTimes
     *            assigned time
     * @return this, for method chaining
     */
    public StatisticsResultBuilder filterMatchTime(String... matchTimes) {
        List<String> list = safeToList(matchTimes);
        stream = stream.filter(m -> list.contains(m.getMatchTime()));
        return this;
    }

    /**
     * Selects matches with team one id or team two id contained in given team ids.
     *
     * @param teamIds
     *            team ids
     * @return this, for method chaining
     */
    public StatisticsResultBuilder filterTeam(Long... teamIds) {
        List<Long> list = safeToList(teamIds);
        stream = stream.filter(m -> list.contains(m.getTeamOneId()) || list.contains(m.getTeamTwoId())).distinct();
        return this;
    }

    /**
     * Selects matches with team one id contained in given team ids.
     *
     * @param teamIds
     *            team ids
     * @return this, for method chaining
     */
    public StatisticsResultBuilder filterTeamOne(Long... teamIds) {
        List<Long> list = safeToList(teamIds);
        stream = stream.filter(m -> list.contains(m.getTeamOneId())).distinct();
        return this;
    }

    /**
     * Selects matches with team two id contained in given team ids.
     *
     * @param teamIds
     *            team ids
     * @return this, for method chaining
     */
    public StatisticsResultBuilder filterTeamTwo(Long... teamIds) {
        List<Long> list = safeToList(teamIds);
        stream = stream.filter(m -> list.contains(m.getTeamTwoId())).distinct();
        return this;
    }

    private <T> List<T> safeToList(T[] values) {
        return values != null ? Arrays.asList(values) : new ArrayList<T>();
    }

    /**
     * Select matches with team one wins.
     *
     * @return this, for method chaining
     */
    public StatisticsResultBuilder filterTeamOneWins() {
        stream = stream.filter(m -> ObjectUtils.equals(m.getWinner(), Winner.ONE));
        return this;
    }

    /**
     * Select matches with draws.
     *
     * @return this, for method chaining
     */
    public StatisticsResultBuilder filterDraws() {
        stream = stream.filter(m -> ObjectUtils.equals(m.getWinner(), Winner.DRAW));
        return this;
    }

    /**
     * Select matches with team two wins.
     *
     * @return this, for method chaining
     */
    public StatisticsResultBuilder filterTeamTwoWins() {
        stream = stream.filter(m -> ObjectUtils.equals(m.getWinner(), Winner.TWO));
        return this;
    }

    /**
     * Builds the {@link StatisticsResult} based on filtered match data.
     *
     * @return statistics result, never null
     */
    public StatisticsResult build() {
        return new StatisticsResult(eloHelperBuilder.build(), stream.collect(Collectors.toList()));
    }
}
