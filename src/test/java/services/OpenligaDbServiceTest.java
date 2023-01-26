package services;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import model.match.Match;
import util.DateUtils;
import util.TestUtils;

@RunWith(PowerMockRunner.class)
public class OpenligaDbServiceTest {

    private SeasonService seasonService;

    @Mock
    private BulibotService bulibotService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private DataService dataService;
    private List<Match> matchesBefore;
    private List<Match> matchesAfter;

    long idSeq = 0;

    private LocalDateTime dateEndSeason;
    private LocalDateTime dateBeforeMid;
    private LocalDateTime dateAfterMid;
    private LocalDateTime dateBeginNextSeason;

    @Before
    public void init() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        // some dates
        dateEndSeason = TestUtils.date("30.05.2015 15:30");
        dateBeforeMid = TestUtils.date("01.07.2015 12:00");
        dateAfterMid = TestUtils.date("15.07.2015 12:00");
        dateBeginNextSeason = TestUtils.date("21.08.2015 20:30");

        // mock data service
        MockitoAnnotations.initMocks(this);
        matchesBefore = new ArrayList<>();
        matchesAfter = new ArrayList<>();
        Mockito.when(dataService.matchesBefore(Mockito.any(), Mockito.anyInt())).thenReturn(matchesBefore);
        Mockito.when(dataService.matchesAfter(Mockito.any(), Mockito.anyInt())).thenReturn(matchesAfter);

        // create service
        seasonService = new SeasonService();

        // inject data service
        Field dataServiceField = SeasonService.class.getDeclaredField("dataService");
        dataServiceField.setAccessible(true);
        dataServiceField.set(seasonService, dataService);

        // inject bulibot service
        Field bulibotServiceField = SeasonService.class.getDeclaredField("bulibotService");
        bulibotServiceField.setAccessible(true);
        bulibotServiceField.set(seasonService, bulibotService);

        // inject notification service
        Field notificationServiceField = SeasonService.class.getDeclaredField("notificationService");
        notificationServiceField.setAccessible(true);
        notificationServiceField.set(seasonService, notificationService);
    }

    @Test
    @Ignore
    public void currentSeasonNoData() {

        // time travel to 2015
        PowerMockito.mockStatic(DateUtils.class);
        PowerMockito.when(DateUtils.currentYear()).thenReturn(2015);

        // do tests
        assertCurrentSeason(dateAfterMid, 2014);
        assertCurrentMatchday(dateAfterMid, null);
    }

    @Test
    public void matchBefore() {
        before(2015, 17, dateAfterMid);
        assertCurrentSeason(dateAfterMid, 2015);
        assertCurrentMatchday(dateAfterMid, 17);
    }

    @Test
    public void lastMatch() {
        before(2015, Match.NUMBER_OF_MATCHDAYS_PER_SEASON, dateAfterMid);
        assertCurrentSeason(dateAfterMid, 2015);
        assertCurrentMatchday(dateAfterMid, Match.NUMBER_OF_MATCHDAYS_PER_SEASON);
    }

    @Test
    public void matchAfter() {
        after(2015, 17, dateAfterMid);
        assertCurrentSeason(dateAfterMid, 2015);
        assertCurrentMatchday(dateAfterMid, 17);
    }

    @Test
    public void inSeason() {
        before(2015, 17, dateAfterMid);
        after(2015, 17, dateAfterMid);
        assertCurrentSeason(dateAfterMid, 2015);
        assertCurrentMatchday(dateAfterMid, 17);
    }

    @Test
    public void betweenSeasons() {
        before(2015, Match.NUMBER_OF_MATCHDAYS_PER_SEASON, dateEndSeason);
        after(2016, 1, dateBeginNextSeason);
        assertCurrentSeason(dateBeforeMid, 2015);
        assertCurrentMatchday(dateBeforeMid, Match.NUMBER_OF_MATCHDAYS_PER_SEASON);
        assertCurrentSeason(dateAfterMid, 2016);
        assertCurrentMatchday(dateAfterMid, 1);
    }

    private void before(Integer season, Integer matchday, LocalDateTime assignedTime) {
        matchesBefore.add(match(season, matchday, assignedTime));
    }

    private void after(Integer season, Integer matchday, LocalDateTime assignedTime) {
        matchesAfter.add(match(season, matchday, assignedTime));
    }

    private Match match(Integer season, Integer matchday, LocalDateTime assignedTime) {
        Match match = new Match();
        match.setId(idSeq++);
        match.setSeason(season);
        match.setMatchday(matchday);
        match.setAssignedTime(assignedTime);
        return match;
    }

    private void assertCurrentSeason(LocalDateTime reference, Integer expectedSeason) {
        seasonService.updateCurrentSeason(reference);
        Integer currentSeason = seasonService.currentSeason();
        Assert.assertEquals(expectedSeason, currentSeason);
    }

    private void assertCurrentMatchday(LocalDateTime reference, Integer expectedMatchday) {
        seasonService.updateCurrentMatchday(reference);
        Integer currentMatchday = seasonService.currentMatchday();
        Assert.assertEquals(expectedMatchday, currentMatchday);
    }
}
