package util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import util.CleanerInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

@Slf4j
public class Cleaner
{
    private static List<CleanerInterface> cleanupList;
    private static List<CleanerInterface> cleanupTestList;

    static
    {
        cleanupList = new ArrayList<>();
        cleanupTestList = new ArrayList<>();
    }

    /**
     * Add an object for clean up
     *
     * @param obj The object that will need to be deleted
     */
    public static void addCleanupObject(CleanerInterface obj)
    {
        if (!cleanupList.contains(obj))
        {
            cleanupList.add(obj);
            log.debug("Base Object Added to the util.Cleaner :: {}", obj.toString());
        }
    }

    /**
     * Add an object for clean up in the Base
     *
     * @param obj The object that will need to be deleted
     */
    public static void addTestCleanupObject(CleanerInterface obj)
    {
        if (!cleanupList.contains(obj))
        {
            cleanupTestList.add(obj);
            log.debug("Local Object Added to the util.Cleaner :: {}", obj.toString());
        }
    }

    /**
     * Cleans up all of the objects in the cleanup list
     */
    public static void cleanup()
    {
        log.debug("------------- Starting Base Cleanup ----------------");
        cleanup(getCleanupList());
        log.debug("------------- Base Cleanup Finished ----------------");
    }

    /**
     * Cleans up all of the objects in the cleanup list
     */
    public static void cleanupTest()
    {
        log.debug("------------- Starting Test Cleanup ----------------");
        cleanup(getTestCleanupList());
        log.debug("------------- Test Cleanup Finished ----------------");
    }

    /**
     * Cleans up all of the objects in the cleanup list
     */
    public static void cleanup(List<CleanerInterface> cList)
    {
        log.debug("------------- Cleaning ----------------");

        ListIterator<CleanerInterface> iterator = cList.listIterator(
            cList.size());
        int counter = 0;

        while (iterator.hasPrevious() && (counter <= cList.size()))
        {
            try
            {
                CleanerInterface obj = iterator.previous();

                log.debug("[{}] Cleaning: {}", counter, obj.getClass().getName());

                if (!obj.isClean())
                {
                    obj.cleanup();
                    log.debug("[{}] Successfully cleaned", counter);
                }
                else
                    log.debug("[{}] Was already cleaned", counter);
            } catch (Exception e)
            {
                log.debug("[{}] There was a problem cleaning the object.", counter);
                log.trace(ExceptionUtils.getStackTrace(e));
            } finally
            {
                counter++;
            }
        }

        cList.clear();

        log.debug("------------- Finished Cleaning ----------------");
    }

    /**
     * Clear out the list of cleanup objects
     */
    public static void clearCleanupList()
    {
        cleanupList.clear();
    }

    /**
     * Clear out the list of base cleanup objects
     */
    public static void clearBaseCleanupList()
    {
        cleanupTestList.clear();
    }

    /**
     * Get the list of objects to be cleaned
     *
     * @return {@link List<CleanerInterface>}
     */
    public static List<CleanerInterface> getCleanupList()
    {
        return cleanupList;
    }

    /**
     * Get the base list of objects to be cleaned
     *
     * @return {@link List<CleanerInterface>}
     */
    public static List<CleanerInterface> getTestCleanupList()
    {
        return cleanupTestList;
    }

    /**
     * Set the list of objects to clean
     *
     * @param list The list to set
     */
    public static void setCleanupList(List<CleanerInterface> list)
    {
        cleanupList = list;
    }

    /**
     * Set the base list of objects to clean
     *
     * @param list The list to set
     */
    public static void setTestCleanupList(List<CleanerInterface> list)
    {
        cleanupTestList = list;
    }
}
