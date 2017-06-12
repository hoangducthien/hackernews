package com.hoangthien.hackernews.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by thien on 6/12/17.
 */

public class DateFormaterTest {


    @Test
    public void convertToTimeAgo() {
        assertEquals(DateFormater.convertToTimeAgo(DateFormater.YEAR), "1 year ago");
        assertEquals(DateFormater.convertToTimeAgo(2 * DateFormater.MONTH), "2 months ago");
        assertEquals(DateFormater.convertToTimeAgo(10 * DateFormater.WEEK), "2 months ago");
        assertEquals(DateFormater.convertToTimeAgo(4 * DateFormater.ONE_DAY), "4 days ago");
        assertEquals(DateFormater.convertToTimeAgo(25 * DateFormater.ONE_HOUR), "1 day ago");
        assertEquals(DateFormater.convertToTimeAgo(10 * DateFormater.ONE_MINUTE), "10 minutes ago");
        assertEquals(DateFormater.convertToTimeAgo(-1), "seconds ago");
    }


}
