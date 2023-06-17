package com.bearlycattable.bait.utility;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class DurationUtils {

    /**
     * Shortcut for getting a convenient human-friendly output of current date
     * @return - e.g.: "Nov 10, 2022 9:40:13 PM"
     */
    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
    }

    /**
     * Shortcut to get duration in hours, minutes and seconds from the starting point in time
     * @param startNanos - nanoseconds of arbitrary point in time
     * @return - e.g.: "27 hours 46 mins and 40 seconds"
     */
    public static String getDurationFromStartHMS(long startNanos) {
        return getDurationHMS(getSecondsFromStart(startNanos));
    }

    /**
     * Shortcut for getting seconds from start time until now
     * @param startNanos - nanoseconds, usually obtained via System.nanoTime() at some point
     * @return - e.g.: 100000
     */
    public static long getSecondsFromStart(long startNanos) {
        return (System.nanoTime() - startNanos) / 1000000000L;
    }

    /**
     * Translates seconds to: days, hours, minutes and seconds
     * @param seconds - duration of time in seconds
     * @return - e.g.: "1 days 3 hours 46 mins and 40 seconds"
     */
    public static String getDurationDHMS(long seconds) {
        return (seconds / 86400) + " days " + ((seconds / 3600) % 24) + " hours " + ((seconds / 60) % 60) + " mins and " + (seconds % 60) + " seconds";
    }

    /**
     * Translates seconds to: hours, minutes and seconds
     * @param seconds - duration of time in seconds
     * @return - e.g.: "27 hours 46 mins and 40 seconds"
     */
    public static String getDurationHMS(long seconds) {
        return (seconds / 3600) + " hours " + ((seconds / 60) % 60) + " mins and " + (seconds % 60) + " seconds";
    }

    /**
     * Translates seconds to: minutes and seconds
     * @param seconds - duration of time in seconds
     * @return - e.g.: "1666 mins and 40 seconds"
     */
    public static String getDurationMS(long seconds) {
        return (seconds / 60) + " mins and " + (seconds % 60) + " seconds";
    }
}
