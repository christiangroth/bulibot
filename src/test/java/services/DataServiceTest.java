package services;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.chrgroth.jsonstore.json.FlexjsonHelper;
import de.chrgroth.jsonstore.store.JsonStore;
import model.match.Match;
import model.match.Match.Status;
import util.TestData;
import util.TestUtils;

public class DataServiceTest {

    private TestData testData;
    private JsonStore<Match> matchesStore;
    private DataService dataService;

    @Before
    public void init() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        // initialize test data
        testData = new TestData();

        // create transient store
        matchesStore = new JsonStore<>(Match.class, Match.VERSION, FlexjsonHelper.builder().build(), null, null, true, false, true, Match.HANDLERS);

        // add all matches
        matchesStore.addAll(Arrays.asList(testData.m1, testData.m2, testData.m3, testData.m4, testData.m5, testData.m6, testData.m7, testData.m8, testData.m9, testData.m10,
                testData.m11, testData.m12, testData.m13, testData.m14, testData.m15, testData.m16, testData.m17, testData.m18, testData.m19, testData.m20, testData.m21,
                testData.m22, testData.m23, testData.m24));

        // inject store to data service
        dataService = new DataService();
        Field field = DataService.class.getDeclaredField("matchesStore");
        field.setAccessible(true);
        field.set(dataService, matchesStore);
    }

    @Test
    public void matchesTodayAll() {
        assertMatchesToday(testData.m11_12at, false, 100, 2);
    }

    @Test
    public void matchesTodayHitLimit() {
        assertMatchesToday(testData.m11_12at, false, 2, 2);
    }

    @Test
    public void matchesTodayExceedLimit() {
        assertMatchesToday(testData.m11_12at, false, 1, 1);
    }

    @Test
    public void matchesTodayUnfinishedNoResults() {
        assertMatchesToday(testData.m11_12at, true, 100, 0);
    }

    @Test
    public void matchesTodayWaitingResult() {
        testData.m12.setStatus(Status.WAITING);
        assertMatchesToday(testData.m11_12at, true, 100, 1);
    }

    @Test
    public void matchesTodayInProgressResult() {
        testData.m12.setStatus(Status.IN_PROGRESS);
        assertMatchesToday(testData.m11_12at, true, 100, 1);
    }

    @Test
    public void matchesTodayWaitingResults() {
        testData.m11.setStatus(Status.WAITING);
        testData.m12.setStatus(Status.WAITING);
        assertMatchesToday(testData.m11_12at, true, 100, 2);
    }

    @Test
    public void matchesTodayInProgressResults() {
        testData.m11.setStatus(Status.IN_PROGRESS);
        testData.m12.setStatus(Status.IN_PROGRESS);
        assertMatchesToday(testData.m11_12at, true, 100, 2);
    }

    private void assertMatchesToday(LocalDateTime reference, boolean onlyUnfinishedDate, int maxResults, int results) {
        List<Match> matchesToday = dataService.matchesToday(reference, onlyUnfinishedDate, maxResults);
        Assert.assertNotNull(matchesToday);
        Assert.assertEquals(results, matchesToday.size());
    }

    @Test
    public void matchesBeforeAll() {
        List<Match> matchesBefore = dataService.matchesBefore(testData.m3at, 100);
        Assert.assertNotNull(matchesBefore);
        Assert.assertEquals(3, matchesBefore.size());
        Assert.assertEquals(testData.m3, matchesBefore.get(0));
        Assert.assertEquals(testData.m2, matchesBefore.get(1));
        Assert.assertEquals(testData.m1, matchesBefore.get(2));
    }

    @Test
    public void matchesBeforeLimit() {
        List<Match> matchesBefore = dataService.matchesBefore(TestUtils.date("13.02.2015 20:29"), 1);
        Assert.assertNotNull(matchesBefore);
        Assert.assertEquals(1, matchesBefore.size());
        Assert.assertEquals(testData.m2, matchesBefore.get(0));
    }

    @Test
    public void matchesAfterAll() {
        List<Match> matchesAfter = dataService.matchesAfter(TestUtils.date("05.04.2015 17:30"), 100);
        Assert.assertNotNull(matchesAfter);
        Assert.assertEquals(3, matchesAfter.size());
        Assert.assertEquals(testData.m22, matchesAfter.get(0));
        Assert.assertEquals(testData.m23, matchesAfter.get(1));
        Assert.assertEquals(testData.m24, matchesAfter.get(2));
    }

    @Test
    public void matchesAfterLimit() {
        List<Match> matchesAfter = dataService.matchesAfter(TestUtils.date("05.04.2015 17:31"), 1);
        Assert.assertNotNull(matchesAfter);
        Assert.assertEquals(1, matchesAfter.size());
        Assert.assertEquals(testData.m23, matchesAfter.get(0));
    }
}
