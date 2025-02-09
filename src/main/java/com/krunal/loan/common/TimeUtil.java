package com.krunal.loan.common;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {
    private TimeUtil() {

    }

    /**
     * @param time
     * @return
     */
    public static String toTimeString(ZonedDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    /**
     * @param duration
     * @param timeUnit
     * @return
     */
    public static long getTimeInMillis(int duration, String timeUnit) {
        long durationInMillis;
        switch (timeUnit) {
            case "minute":
                durationInMillis = duration * 60L;
                break;
            case "hour":
                durationInMillis = duration * 60 * 60L;
                break;
            case "day":
                durationInMillis = duration * 24 * 60 * 60L;
                break;
            default:
                durationInMillis = duration;
                break;
        }
        return durationInMillis * 1000L;
    }
}
