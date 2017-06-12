package com.hoangthien.hackernews.utils;

/**
 * Created by hoangthien on 1/12/17.
 */

public class DateFormater {

    public final static long ONE_SECOND = 1;

    public final static long ONE_MINUTE = ONE_SECOND * 60;

    public final static long ONE_HOUR = ONE_MINUTE * 60;

    public final static long ONE_DAY = ONE_HOUR * 24;

    public final static long WEEK = ONE_DAY * 7;

    public final static long MONTH = ONE_DAY * 30;

    public final static long YEAR = ONE_DAY * 365;

    public static String convertToTimeAgo(long duration) {
        if (duration >= YEAR) {
            duration = duration / YEAR;
            if (duration > 1) {
                return duration + " years ago";
            } else {
                return duration + " year ago";
            }
        }

        if (duration >= MONTH) {
            duration = duration / MONTH;
            if (duration > 1) {
                return duration + " months ago";
            } else {
                return duration + " month ago";
            }
        }

        if (duration >= WEEK) {
            duration = duration / WEEK;
            if (duration > 1) {
                return duration + " weeks ago";
            } else {
                return duration + " week ago";
            }
        }

        if (duration >= ONE_DAY) {
            duration = duration / ONE_DAY;
            if (duration > 1) {
                return duration + " days ago";
            } else {
                return duration + " day ago";
            }
        }

        if (duration >= ONE_HOUR) {
            duration = duration / ONE_HOUR;
            if (duration > 1) {
                return duration + " hours ago";
            } else {
                return duration + " hour ago";
            }
        }

        if (duration >= ONE_MINUTE) {
            duration = duration / ONE_MINUTE;
            if (duration > 1) {
                return duration + " minutes ago";
            } else {
                return duration + " minute ago";
            }
        }

        return "seconds ago";
    }


}
