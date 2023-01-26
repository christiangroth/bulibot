package util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DateUtilsTest {

    private LocalDateTime oldest;
    private LocalDateTime newest;

    @Before
    public void init() {

        // create test data
        oldest = LocalDateTime.MIN;
        newest = LocalDateTime.MAX;
    }

    @Test
    public void newestFirst() {
        List<LocalDateTime> data = data(oldest, newest);
        Collections.sort(data, DateUtils.NEWEST_FIRST);
        assertOrder(data, newest, oldest);
    }

    @Test
    public void oldestLast() {
        List<LocalDateTime> data = data(newest, oldest);
        Collections.sort(data, DateUtils.OLDEST_FIRST);
        assertOrder(data, oldest, newest);
    }

    private List<LocalDateTime> data(LocalDateTime... dates) {
        List<LocalDateTime> data = new ArrayList<>();
        for (LocalDateTime date : dates) {
            data.add(date);
        }
        return data;
    }

    private void assertOrder(List<LocalDateTime> data, LocalDateTime... items) {
        Assert.assertEquals(items.length, data.size());
        for (int i = 0; i < items.length; i++) {
            Assert.assertEquals(items[i], data.get(i));
        }
    }

    @Test
    public void between() {
        Assert.assertEquals(TestUtils.date("02.01.2015 00:00"), DateUtils.between(TestUtils.date("01.01.2015 00:00"), TestUtils.date("03.01.2015 00:00")));
        Assert.assertEquals(TestUtils.date("02.01.2015 00:00"), DateUtils.between(TestUtils.date("03.01.2015 00:00"), TestUtils.date("01.01.2015 00:00")));
    }

    @Test
    public void parseOpenligaDbDate() {
        Assert.assertEquals(TestUtils.date("22.01.2016 20:30"), DateUtils.parseOpenligaDbDate("2016-01-22T20:30:00"));
    }
}
