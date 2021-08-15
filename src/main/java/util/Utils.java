package util;

import lombok.extern.slf4j.Slf4j;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Utilities for interaction with some common features on LMP
 * pages.
 *
 * @author bchristiansen
 */
@Slf4j
public class Utils
{
    /**
     * Returns a new Calendar instance with the time zone set to 'America/Denver'.
     *
     * @return {@link Calendar}
     */
    public static Calendar getCalendarInstance()
    {
        return getCalendarInstance("America/Denver");
    }

    /**
     * Returns a new Calendar instance with the time zone set. By default we set this time zone
     * to 'America/Denver' since that's where all our test nodes are located, but other time zones
     * can be used for local testing.
     *
     * @param timeZone The desired time zone, i.e. "America/Denver"
     * @return {@link Calendar}
     */
    public static Calendar getCalendarInstance(String timeZone)
    {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        log.info("Setting the current time zone to : {}", calendar.getTimeZone().getDisplayName());
        return calendar;
    }
    /**
     * Puts the thread to sleep for the specified number of seconds.
     *
     * @param seconds The number of seconds to wait.
     * @see SeleniumManager getLongWait()
     */
    public static void wait(int seconds)
    {
        try
        {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ie)
        {
            System.out.println("Wait Interrupted");
        }
    }

    /**
     * Waits until the time specified
     *
     * @param timeToWaitUntil Calendar object that specifies a time
     */
    public static void waitUntil(Calendar timeToWaitUntil)
    {
        Calendar now = Calendar.getInstance();
        //find how long we need to wait, convert to seconds
        int secondsToWait = (int) (timeToWaitUntil.getTimeInMillis() - now.getTimeInMillis()) / 1000;
        if (secondsToWait > 0)
        {
            Utils.wait(secondsToWait);
        }
    }
}
