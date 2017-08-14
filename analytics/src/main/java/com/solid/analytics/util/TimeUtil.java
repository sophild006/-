package com.solid.analytics.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtil {

    public static final long SECOND = 1000L * 1L;
    public static final long MINUTE = SECOND * 60L;
    public static final long HOUR = MINUTE * 60L;
    public static final long DAY = HOUR * 24L;
    public static final long WEEK = DAY * 7L;

    static final SimpleDateFormat sDateFormatUTC = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    static {
        sDateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static String dateNowUTC() {
        return sDateFormatUTC.format(new Date());
    }

    static final SimpleDateFormat sDateHourFormatUTC = new SimpleDateFormat("yyyy-MM-dd HH", Locale.US);

    static {
        sDateHourFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static String dateHourNowUTC() {
        return sDateHourFormatUTC.format(new Date());
    }

    static final SimpleDateFormat sDateHourMinuteFormatUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

    static {
        sDateHourMinuteFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static String dateHourMinuteNowUTC() {
        return sDateHourMinuteFormatUTC.format(new Date());
    }

    static final SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    public static String dateTimeNow() {
        Date now = new Date(System.currentTimeMillis());
        return sDateFormat.format(now);
    }

    public static long getDayStart(long timeInMillis) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        return cal.getTimeInMillis();
    }

    public static long getTodayStart() {
        return getDayStart(System.currentTimeMillis());
    }
}
