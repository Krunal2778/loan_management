package com.krunal.loan.common;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * This utility is used to get Dates in different time zone formats and convert
 * from one format to another.
 *
 * @author Krunal Gajbhiye
 *
 */
public class DateUtils {

    /**
     * Indian time zone
     */
    static public String IST = "Asia/Calcutta";
    /**
     * London time zone
     */
    static public String GMT = "Europe/London";
    /**
     * New York or Eastern time zone
     */
    static public String EST = "US/Eastern";
    /**
     * Central time zone
     */
    static public String CST = "US/Central";
    /**
     * Mountain time zone
     */
    static public String MST = "US/Mountain";
    /**
     * Los Angeles or Pacific time zone
     */
    static public String PST = "US/Pacific";
    /**
     * UTC time zone
     */
    static public String UTC = "Etc/UTC";
    /**
     * Gulf time zone
     */
    static public String GST = "Asia/Dubai";
    /**
     * "yyyy-MM-dd" Date format
     */
    static public String YMD = "yyyy-MM-dd";
    /**
     * "yyyy-MM-dd" Date format
     */
    static public String DMY = "dd-MM-yyyy";
    /**
     * "dd-MM-yyyy" Date format
     */
    static public String DMMY = "dd-MMM-yyyy";
    /**
     * "dd-MMM-yyyy" Date format
     */
    static public String MMY = "MMM-yyyy";
    /**
     * "MMM-yyyy" Date format
     */
    static public String MMMMDY = "MMMMM dd, yyyy";
    /**
     * "MMMMM dd, yyyy" Date format
     */
    static public String YMDHMS = "yyyy-MM-dd HH:mm:ss";
    /**
     * "yyyy-MM-dd HH:mm:ss" Date format
     */
    static public String YMDHM = "yyyy-MM-dd HH:mm";
    /**
     * "yyyy-MM-dd HH:mm" Date format
     */
    static public String DMYHMS = "dd-MM-yyyy HH:mm:ss";
    /**
     * "dd-MM-yyyy HH:mm:ss" Date format
     */
    static public String DMYHM = "dd-MM-yyyy HH:mm";
    /**
     * "dd-MM-yyyy HH:mm" Date format
     */
    static public String Y = "yyyy";
    static public String[] dayName = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    static public String[] monthName = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    /**
     * Returns the current date in a String format
     *
     * @param timeZone Select from the TimeZones available in DateUtils
     * @param format Select from the Formats available in DateUtils
     * @return Returns Date value in format specified for the given time zone.
     */
    static public String getCurrentDateString(String timeZone, String format) {
        Date now = new Date();
        DateFormat df = new SimpleDateFormat(format);
        df.setTimeZone(TimeZone.getTimeZone(timeZone));
        return df.format(now);
    }

    /**
     * Returns the current date object for the time zone specified
     *
     * @param timeZone Select from the TimeZones available in DateUtils
     * @return Returns current Date object for the given time zone.
     */
    static public Date getCurrentDateObject(String timeZone) {
        String format = DateUtils.YMDHMS;
        DateFormat df = new SimpleDateFormat(format);
        Date tmpDate = null;
        try {
            tmpDate = df.parse(getCurrentDateString(timeZone, format));
        } catch (ParseException pe) {
            tmpDate = new Date();
        }
        return tmpDate;
    }

    /**
     * Returns the date as an offset from the current date in a String format.
     *
     * @param timeZone Select from the TimeZones available in DateUtils
     * @param format Select from the Formats available in DateUtils
     * @param days The number of days that you want as the offset. Can be
     * positive or negative.
     * @return Returns Date value in format specified for the given time zone.
     */
    static public String getOffsetFromCurrentDateString(String timeZone, String format, int days) {
        DateFormat df = new SimpleDateFormat(format);
        df.setTimeZone(TimeZone.getTimeZone(timeZone));
        return df.format(getOffsetFromCurrentDateObject(timeZone, days));
    }

    /**
     * Returns the date as an offset from the current date as a Date object.
     *
     * @param timeZone Select from the TimeZones available in DateUtils
     * @param days The number of days that you want as the offset. Can be
     * positive or negative.
     * @return Returns Date object offset from the current date for the given
     * time zone.
     */
    static public Date getOffsetFromCurrentDateObject(String timeZone, int days) {
        Date now = getCurrentDateObject(timeZone);
        Calendar c1 = Calendar.getInstance();
        c1.setTime(now);
        c1.add(Calendar.DATE, days);
        return c1.getTime();
    }

    /**
     * Adds days to the Date object given. days can be positive or negative.
     *
     * @param dt Date object that you want to add days to
     * @param days Number of days you want to add to the Date object. Cane be
     * positive or negative.
     * @return Returns the new Date object after adding the days to the Date
     * object.
     */
    static public Date addDays(Date dt, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    /**
     * Format a Date object into a standard format.
     *
     * @param dt Date object that you want to work on.
     * @param format Select the format that you want output in. Select from the
     * formats available in DateUtils.
     * @return Returns the Date in your specified format.
     */
    static public String formatDate(Date dt, String format) {
        DateFormat df = new SimpleDateFormat(format);
        return df.format(dt);
    }

    /**
     * Compares two Date objects to see which one is greater. The assumption is
     * that both the Dates are from the same time zone returns -1 if d1 is less
     * than d2, 1 if d1 is greater than d2, 0 if both are equal.
     *
     * @param d1 Date to be compared
     * @param d2 Date to be compared
     * @return Returns -1 if d1 is less than d2, 1 if d1 is greater than d2, 0
     * if both are equal.
     */
    static public int compareDate(Date d1, Date d2) {
        if (d1.after(d2)) {
            return 1;
        } else if (d2.after(d1)) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * Compares two Date that are in yyyy-MM-dd format to see which one is
     * greater. The assumption is that both the Dates are from the same time
     * zone returns -1 if d1 is less than d2, 1 if d1 is greater than d2, 0 if
     * both are equal.
     *
     * @param d1 Date to be compared. Could be either yyyy-MM-dd or yyyy-MM-dd
     * HH:mm:ss formats.
     * @param d2 Date to be compared. Could be either yyyy-MM-dd or yyyy-MM-dd
     * HH:mm:ss formats.
     * @return Returns -1 if d1 is less than d2, 1 if d1 is greater than d2, 0
     * if both are equal.
     */
    static public int compareDates(String d1, String d2) {
        java.util.Calendar startDt = java.util.Calendar.getInstance();
        java.util.Calendar stopDt = java.util.Calendar.getInstance();
        // length is greater than 10 it is safe to assume that the hours min and seconds are also part of the string
        // if its not > 10 then only yyyy-mm-dd is the string pattern
        boolean compareTime;
        String time1 = "", time2 = "";
        if (d1.length() <= 10 || d2.length() <= 10) {
            startDt.set(Integer.parseInt(d1.substring(0, 4)), Integer.parseInt(d1.substring(5, 7)) - 1, Integer.parseInt(d1.substring(8, 10)));
            stopDt.set(Integer.parseInt(d2.substring(0, 4)), Integer.parseInt(d2.substring(5, 7)) - 1, Integer.parseInt(d2.substring(8, 10)));
            compareTime = false;
        } else {
            startDt.set(Integer.parseInt(d1.substring(0, 4)), Integer.parseInt(d1.substring(5, 7)) - 1, Integer.parseInt(d1.substring(8, 10)), Integer.parseInt(d1.substring(11, 13)), Integer.parseInt(d1.substring(14, 16)), Integer.parseInt(d1.substring(17, 19)));
            stopDt.set(Integer.parseInt(d2.substring(0, 4)), Integer.parseInt(d2.substring(5, 7)) - 1, Integer.parseInt(d2.substring(8, 10)), Integer.parseInt(d2.substring(11, 13)), Integer.parseInt(d2.substring(14, 16)), Integer.parseInt(d2.substring(17, 19)));
            compareTime = true;
            time1 = d1.substring(11, 19);
            time2 = d2.substring(11, 19);
        }
        int year1, year2, month1, month2, date1, date2;

        year1 = startDt.get(java.util.Calendar.YEAR);
        month1 = startDt.get(java.util.Calendar.MONTH);
        date1 = startDt.get(java.util.Calendar.DAY_OF_MONTH);

        year2 = stopDt.get(java.util.Calendar.YEAR);
        month2 = stopDt.get(java.util.Calendar.MONTH);
        date2 = stopDt.get(java.util.Calendar.DAY_OF_MONTH);

        if (year1 == year2 && month1 == month2 && date1 == date2) {
            if (compareTime) {
                return compareTime(time1, time2);
            } else {
                return (0);
            }
        } else if (year1 < year2 || (year1 == year2 && month1 < month2) || (year1 == year2 && month1 == month2 && date1 < date2)) {
            return (-1);
        } else {
            return (1);
        }
    }

    /**
     * Compares two Strings that are in HH:mm:ss format to see which one is
     * greater. Returns -1 if d1 is less than d2, 1 if d1 is greater than d2, 0
     * if both are equal.
     *
     * @param t1 Time to be compared. Must be in HH:mm:ss format.
     * @param t2 Time to be compared. Must be in HH:mm:ss format.
     * @return Returns -1 if d1 is less than d2, 1 if d1 is greater than d2, 0
     * if both are equal.
     */
    private static int compareTime(String t1, String t2) {
        int hour1 = Integer.parseInt(t1.substring(0, 2));
        int min1 = Integer.parseInt(t1.substring(3, 5));
        int sec1 = Integer.parseInt(t1.substring(6, 8));
        int hour2 = Integer.parseInt(t2.substring(0, 2));
        int min2 = Integer.parseInt(t2.substring(3, 5));
        int sec2 = Integer.parseInt(t2.substring(6, 8));
        if (hour1 < hour2) {
            return (-1);
        } else if (hour1 > hour2) {
            return (1);
        } else if (min1 < min2) {
            return (-1);
        } else if (min1 > min2) {
            return (1);
        } else if (sec1 < sec2) {
            return (-1);
        } else if (sec1 > sec2) {
            return (1);
        } else {
            return (0);
        }
    }

    /**
     * Parse the String and return the Date object
     *
     * @param date String that you want to parse
     * @param format Format that the date was passed in
     * @return Returns the Date object after parsing
     * @throws ParseException Throws ParseException if the date is not in the
     * correct format indicated.
     */
    public static Date getDateFromString(String date, String format) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.parse(date);
    }

    /**
     * Parse the String and return the Date object
     *
     * @param date String that you want to parse
     * @param format Format that the date was passed in
     * @return Returns the Date object after parsing
     * @deprecated use getDateFromString() instead
     * @see #getDateFromString(java.lang.String, java.lang.String)
     */
    @Deprecated
    public static Date convertStringToDate(String date, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        try {
            return df.parse(date);
        } catch (ParseException pe) {
            return null;
        }
    }

    /**
     * Format a Date object into a standard format.
     *
     * @param date Date object that you want to work on.
     * @param format Select the format that you want output in. Select from the
     * formats available in DateUtils.
     * @return Returns the Date in your specified format.
     * @deprecated use formatDate() instead
     * @see #formatDate(java.util.Date, java.lang.String)
     */
    @Deprecated
    public static String convertDateToString(Date date, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(date);
    }

    /**
     * Get the Date object corresponding to first date of the month for the
     * current date.
     *
     * @return Date object for the first day of the month for the current date.
     */
    public static Date getStartOfMonthObject() {
        return getStartOfMonthVariable(new Date());
    }

    /**
     * Get the Date in String format corresponding to first date of the month
     * for the current date.
     *
     * @param format Format that you want the result in. Must be selected from
     * standard formats from DateUtils.
     * @return Date in String format for the first day of the month for the
     * current date.
     */
    public static String getStartOfMonthString(String format) {
        Date d = getStartOfMonthObject();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(d);
    }

    /**
     * Get the Date object corresponding to last date of the month for the
     * current date.
     *
     * @return Date object for the last day of the month for the current date.
     */
    public static Date getEndOfMonthObject() {
        return getEndOfMonthVariable(new Date());
    }

    /**
     * Get the Date in String format corresponding to last date of the month for
     * the current date.
     *
     * @param format Format that you want the result in. Must be selected from
     * standard formats from DateUtils.
     * @return Date in String format for the last day of the month for the
     * current date.
     */
    public static String getEndOfMonthString(String format) {
        Date d = getEndOfMonthObject();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(d);
    }

    /**
     * Get the Date object corresponding to first date of the month for the
     * previous month.
     *
     * @return Date object for the first day of the month for the previous
     * month.
     */
    public static Date getStartOfPreviousMonthObject() {
        Date now = new Date();
        Calendar c1 = Calendar.getInstance();
        c1.setTime(now);
        c1.add(Calendar.MONTH, -1);
        c1.set(Calendar.DATE, 1);
        c1.set(Calendar.HOUR_OF_DAY, 0);
        c1.set(Calendar.MINUTE, 0);
        c1.set(Calendar.SECOND, 0);
        return c1.getTime();
    }

    /**
     * Get the Date in String format corresponding to first date of the month
     * for the previous month.
     *
     * @param format Format that you want the result in. Must be selected from
     * standard formats from DateUtils.
     * @return Date in String format for the first day of the month for the
     * previous month.
     */
    public static String getStartOfPreviousMonthString(String format) {
        Date d = getStartOfPreviousMonthObject();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(d);
    }

    /**
     * Get the Date object corresponding to last date of the previous month for
     * the current date.
     *
     * @return Date object for the last day of the month for the previous month.
     */
    public static Date getEndOfPreviousMonthObject() {
        Date now = new Date();
        Calendar c1 = Calendar.getInstance();
        c1.setTime(now);
        c1.set(Calendar.DATE, 1);
        c1.add(Calendar.DATE, -1);
        c1.set(Calendar.HOUR_OF_DAY, 23);
        c1.set(Calendar.MINUTE, 59);
        c1.set(Calendar.SECOND, 59);
        return c1.getTime();
    }

    /**
     * Get the Date in String format corresponding to last date of the previous
     * month for the current date.
     *
     * @param format Format that you want the result in. Must be selected from
     * standard formats from DateUtils.
     * @return Date in String format for the last day of the previous month.
     */
    public static String getEndOfPreviousMonthString(String format) {
        Date d = getEndOfPreviousMonthObject();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(d);
    }

    /**
     * Get the count of the number of days in between d1 and d2
     *
     * @param d1 Date that you want to compare
     * @param d2 Date that you want to compare
     * @return the number of days in between d1 and d2
     */
    public static int getDaysBetween(Date d1, Date d2) {
        if (d1.getTime() > d2.getTime()) {
            return (int) ((d1.getTime() - d2.getTime()) / (1000 * 60 * 60 * 24));
        } else {
            return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
        }
    }

    /**
     * Get the Date object corresponding to first date of the month for date
     * that was passed.
     *
     * @param d Date that you want the start of the month for
     * @return Date object for the first day of the month for the date that was
     * passed.
     */
    public static Date getStartOfMonthVariable(Date d) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(d);
        c1.set(Calendar.DATE, 1);
        c1.set(Calendar.HOUR_OF_DAY, 0);
        c1.set(Calendar.MINUTE, 0);
        c1.set(Calendar.SECOND, 0);
        return c1.getTime();
    }

    /**
     * Get the Date object corresponding to last date of the month for date that
     * was passed.
     *
     * @param d Date that you want the end of the month for
     * @return Date object for the last day of the month for the date that was
     * passed.
     */
    public static Date getEndOfMonthVariable(Date d) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(d);
        c1.add(Calendar.MONTH, 1);
        c1.set(Calendar.DATE, 1);
        c1.add(Calendar.DATE, -1);
        c1.set(Calendar.HOUR_OF_DAY, 23);
        c1.set(Calendar.MINUTE, 59);
        c1.set(Calendar.SECOND, 59);
        return c1.getTime();
    }

    /**
     * Get the Date object corresponding to first date of the month for date
     * that was passed.
     *
     * @param dStr Date that you want the start of the month for. Must be in
     * yyyy-MM-dd format
     * @return Date object for the first day of the month for the date that was
     * passed.
     * @throws ParseException Throws ParseException if the date passed is not in
     * correct format.
     */
    public static String getStartOfMonthVariable(String dStr) throws ParseException {
        Calendar c1 = Calendar.getInstance();
        Date d = getDateFromString(dStr, YMD);
        c1.setTime(d);
        c1.set(Calendar.DATE, 1);
        c1.set(Calendar.HOUR_OF_DAY, 0);
        c1.set(Calendar.MINUTE, 0);
        c1.set(Calendar.SECOND, 0);
        return new SimpleDateFormat(YMD).format(c1.getTime());
    }

    /**
     * Get the Date object corresponding to last date of the month for date that
     * was passed.
     *
     * @param dStr Date that you want the end of the month for. Must be in
     * yyyy-MM-dd format
     * @return Date object for the lasst day of the month for the date that was
     * passed.
     * @throws ParseException Throws ParseException if the date passed is not in
     * correct format.
     */
    public static String getEndOfMonthVariable(String dStr) throws ParseException {
        Calendar c1 = Calendar.getInstance();
        Date d = getDateFromString(dStr, YMD);
        c1.setTime(d);
        c1.add(Calendar.MONTH, 1);
        c1.set(Calendar.DATE, 1);
        c1.add(Calendar.DATE, -1);
        c1.set(Calendar.HOUR_OF_DAY, 23);
        c1.set(Calendar.MINUTE, 59);
        c1.set(Calendar.SECOND, 59);
        return new SimpleDateFormat(YMD).format(c1.getTime());
    }

    /**
     * Gives Version of Utils
     *
     * @return Version
     */
    public static String getVersion() {
        return "1.32";
    }
}
