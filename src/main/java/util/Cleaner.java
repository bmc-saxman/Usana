package util;

import lombok.extern.slf4j.Slf4j;

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
        }
    }

    /**
     * Cleans up all of the objects in the cleanup list
     */
    public static void cleanup()
    {
        cleanup(getCleanupList());
    }

    /**
     * Cleans up all of the objects in the cleanup list
     */
    public static void cleanupTest()
    {
        cleanup(getTestCleanupList());
    }

    /**
     * Cleans up all of the objects in the cleanup list
     */
    public static void cleanup(List<CleanerInterface> cList)
    {
        ListIterator<CleanerInterface> iterator = cList.listIterator(
            cList.size());
        int counter = 0;

        while (iterator.hasPrevious() && (counter <= cList.size()))
        {
            try
            {
                CleanerInterface obj = iterator.previous();

                if (!obj.isClean())
                {
                    obj.cleanup();
                }
            } catch (Exception e)
            {
                System.out.print("[" + counter + "]" + " There was a problem cleaning the object.");
            } finally
            {
                counter++;
            }
        }

        cList.clear();
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
