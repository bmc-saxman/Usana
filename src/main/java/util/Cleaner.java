package util;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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
}
