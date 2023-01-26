package util;

import java.time.LocalDateTime;

import model.match.Goal;
import model.match.Match;
import model.match.Match.Status;
import model.statistics.Statistics;

public class TestData {

    public static final int SEASON_ONE = 2014;
    public static final int SEASON_TWO = 2015;

    public Statistics statistics;
    public long idSequence;

    public long t1;
    public long t2;
    public long t3;
    public long t4;
    public long t5;

    public long t1s1g1;
    public long t2s1g1;
    public long t2s1g2;
    public long t3s1g1;
    public long t3Fromt4s1g2;
    public long t4s1g1;
    public long t4s1g2;
    public long t5s1g1;

    public Match m1;
    public Match m2;
    public Match m3;
    public Match m4;
    public Match m5;
    public Match m6;

    public Match m7;
    public Match m8;
    public Match m9;
    public Match m10;
    public Match m11;
    public Match m12;

    public Match m13;
    public Match m14;
    public Match m15;
    public Match m16;
    public Match m17;
    public Match m18;

    public Match m19;
    public Match m20;
    public Match m21;
    public Match m22;
    public Match m23;
    public Match m24;

    public LocalDateTime m1at;
    public LocalDateTime m3at;
    public LocalDateTime m11_12at;
    public LocalDateTime m13at;
    public LocalDateTime m22at;
    public LocalDateTime m24at;

    public TestData() {

        // create teams
        idSequence = 1;
        t1 = idSequence++;
        t1s1g1 = idSequence++;
        t2 = idSequence++;
        t2s1g1 = idSequence++;
        t2s1g2 = idSequence++;
        t3 = idSequence++;
        t3s1g1 = idSequence++;
        t3Fromt4s1g2 = idSequence++;
        t4 = idSequence++;
        t4s1g1 = idSequence++;
        t4s1g2 = idSequence++;
        t5 = idSequence++;
        t5s1g1 = idSequence++;

        // help is on the way: http://romtec.ch/spielplangenerator/ ;)
        statistics = new Statistics();

        // season one
        // sat / sun
        m1at = TestUtils.date("07.02.2015 15:30");
        m1 = match(SEASON_ONE, 1, m1at, t3, t4, 0, 1, 2, 1);
        goal(m1, t4, t4s1g2, 0, 1, 17);
        goal(m1, t3, t3s1g1, 1, 1, 46);
        goal(m1, t3, t3s1g1, 2, 1, 92);
        m2 = match(SEASON_ONE, 1, TestUtils.date("08.02.2015 15:30"), t1, t2, 1, 0, 1, 0);
        penalty(m2, t1, t1s1g1, 1, 0, 33);

        // fri / sat
        m3at = TestUtils.date("13.02.2015 20:30");
        m3 = match(SEASON_ONE, 2, m3at, t2, t3, 1, 0, 1, 1);
        ownGoal(m3, t3, t3s1g1, 1, 0, 17);
        goal(m3, t3, t3s1g1, 1, 1, 74);
        m4 = match(SEASON_ONE, 2, TestUtils.date("14.02.2015 15:30"), t4, t1, 0, 2, 0, 4);
        goal(m4, t1, t1s1g1, 0, 1, 2);
        goal(m4, t1, t1s1g1, 0, 2, 7);
        goal(m4, t1, t1s1g1, 0, 3, 56);
        goal(m4, t1, t1s1g1, 0, 4, 87);

        // sat / sun
        m5 = match(SEASON_ONE, 3, TestUtils.date("21.02.2015 18:30"), t3, t1, 1, 1, 1, 1);
        goal(m5, t3, t3s1g1, 1, 0, 12);
        goal(m5, t1, t1s1g1, 1, 1, 23);
        m6 = match(SEASON_ONE, 3, TestUtils.date("22.02.2015 17:30"), t2, t4, 0, 1, 0, 1);
        ownGoal(m6, t4, t4s1g2, 0, 1, 29);

        // player t4s1g2 changes to t3 and is used as t3Fromt4s1g2 / gets a new goal getter id

        // fri / sat
        m7 = match(SEASON_ONE, 4, TestUtils.date("27.02.2015 20:30"), t4, t3, 0, 0, 0, 0);
        m8 = match(SEASON_ONE, 4, TestUtils.date("28.02.2015 15:30"), t2, t1, 0, 0, 1, 0);
        goal(m7, t2, t2s1g2, 1, 0, 88);

        // tue / wed
        m9 = match(SEASON_ONE, 5, TestUtils.date("03.03.2015 20:00"), t3, t2, 0, 0, 2, 1);
        goal(m9, t3, t3Fromt4s1g2, 1, 0, 72);
        penalty(m9, t3, t3Fromt4s1g2, 2, 0, 74);
        goal(m9, t2, t2s1g1, 2, 1, 82);
        m10 = match(SEASON_ONE, 5, TestUtils.date("04.03.2015 20:00"), t1, t4, 2, 2, 3, 4);
        goal(m10, t1, t1s1g1, 1, 0, 1);
        goal(m10, t1, t1s1g1, 2, 0, 3);
        goal(m10, t4, t4s1g1, 2, 1, 11);
        goal(m10, t4, t4s1g1, 2, 2, 39);
        ownGoal(m10, t4, t4s1g1, 3, 2, 56);
        goal(m10, t4, t4s1g1, 3, 3, 62);
        penalty(m10, t4, t4s1g1, 3, 4, 77);

        // sun / sun
        m11_12at = TestUtils.date("08.03.2015 15:30");
        m11 = match(SEASON_ONE, 6, m11_12at, t1, t3, 2, 1, 2, 2);
        goal(m11, t3, t3s1g1, 0, 1, 29);
        goal(m11, t1, t1s1g1, 1, 1, 32);
        ownGoal(m11, t3, t3s1g1, 2, 1, 36);
        goal(m11, t3, t3Fromt4s1g2, 2, 2, 96);
        m12 = match(SEASON_ONE, 6, m11_12at, t4, t2, 0, 0, 1, 2);
        goal(m12, t2, t2s1g1, 0, 1, 51);
        goal(m12, t4, t4s1g1, 1, 1, 66);
        penalty(m12, t2, t2s1g2, 1, 2, 83);

        // season two (team 4 replaced by team 5)
        // fri / sat
        m13at = TestUtils.date("13.03.2015 20:30");
        m13 = match(SEASON_TWO, 1, m13at, t3, t5, 0, 1, 0, 1);
        m14 = match(SEASON_TWO, 1, TestUtils.date("14.03.2015 15:30"), t1, t2, 0, 0, 0, 0);

        // tue // wed
        m15 = match(SEASON_TWO, 2, TestUtils.date("17.03.2015 20:00"), t2, t3, 0, 1, 1, 2);
        m16 = match(SEASON_TWO, 2, TestUtils.date("18.03.2015 20:00"), t5, t1, 1, 0, 1, 0);

        // sat / sun
        m17 = match(SEASON_TWO, 3, TestUtils.date("21.03.2015 15:30"), t3, t1, 0, 1, 3, 3);
        m18 = match(SEASON_TWO, 3, TestUtils.date("22.03.2015 15:30"), t2, t5, 1, 0, 2, 0);

        // sat / sun
        m19 = match(SEASON_TWO, 4, TestUtils.date("28.03.2015 15:30"), t5, t3, 0, 0, 1, 0);
        m20 = match(SEASON_TWO, 4, TestUtils.date("29.03.2015 15:30"), t2, t1, 0, 1, 1, 3);

        // sat / sun
        m21 = match(SEASON_TWO, 5, TestUtils.date("04.04.2015 18:30"), t3, t2, 0, 1, 1, 1);
        m22at = TestUtils.date("05.04.2015 17:30");
        m22 = match(SEASON_TWO, 5, m22at, t1, t5, 0, 1, 0, 1);

        // sat / sun
        m23 = match(SEASON_TWO, 6, TestUtils.date("11.04.2015 15:30"), t1, t3, 0, 1, 3, 1);
        m24at = TestUtils.date("12.04.2015 15:30");
        m24 = match(SEASON_TWO, 6, m24at, t5, t2, 0, 1, 2, 5);
    }

    private Match match(int season, int matchday, LocalDateTime assignedTime, long teamOne, long teamTwo, int halftimeGoalsTeamOne, int halftimeGoalsTeamTwo,
            int fulltimeGoalsTeamOne, int fulltimeGoalsTeamTwo) {

        // create match
        Match match = new Match();
        match.setId(idSequence++);
        match.setSeason(season);
        match.setMatchday(matchday);

        // set data
        match.setAssignedTime(assignedTime);
        match.setTeamOneId(teamOne);
        match.setTeamTwoId(teamTwo);
        match.setGoalsTeamOneHalfTime(halftimeGoalsTeamOne);
        match.setGoalsTeamTwoHalfTime(halftimeGoalsTeamTwo);
        match.setGoalsTeamOneFullTime(fulltimeGoalsTeamOne);
        match.setGoalsTeamTwoFullTime(fulltimeGoalsTeamTwo);
        match.setStatus(Status.FINISHED);

        // add to statistics
        statistics.update(match);

        // done
        return match;
    }

    private void goal(Match match, long goalGetterTeam, long goalGetterId, int goalsTeamOne, int goalsTeamTwo, int minute) {
        goal(match, goalGetterTeam, goalGetterId, goalsTeamOne, goalsTeamTwo, minute, false, false);
    }

    private void penalty(Match match, long goalGetterTeam, long goalGetterId, int goalsTeamOne, int goalsTeamTwo, int minute) {
        goal(match, goalGetterTeam, goalGetterId, goalsTeamOne, goalsTeamTwo, minute, true, false);
    }

    private void ownGoal(Match match, long goalGetterTeam, long goalGetterId, int goalsTeamOne, int goalsTeamTwo, int minute) {
        goal(match, goalGetterTeam, goalGetterId, goalsTeamOne, goalsTeamTwo, minute, false, true);
    }

    private void goal(Match match, long goalGetterTeam, long goalGetterId, int goalsTeamOne, int goalsTeamTwo, int minute, boolean penalty, boolean ownGoal) {

        // create goal
        Goal goal = new Goal();
        goal.setGoalGetterId(goalGetterId);
        goal.setGoalGetterTeamId(goalGetterTeam);
        goal.setGoalsTeamOne(goalsTeamOne);
        goal.setGoalsTeamTwo(goalsTeamTwo);
        goal.setMinute(minute);
        goal.setOvertime(minute > 90);
        goal.setPenalty(penalty);
        goal.setOwnGoal(ownGoal);
        goal.setOrder(match.getGoals().size() + 1);

        // add goal
        match.getGoals().add(goal);

        // update statistics
        statistics.update(match);
    }
}
