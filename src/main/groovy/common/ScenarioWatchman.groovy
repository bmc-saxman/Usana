package common

import groovy.util.logging.Slf4j
import org.json.JSONException
import org.json.JSONObject
import org.junit.rules.TestWatchman
import org.junit.runners.model.FrameworkMethod

/**
 * Created by jstoddard on 7/7/17.
 *
 */
@Slf4j
class ScenarioWatchman extends TestWatchman
{

    private static qTestJSON = [:]
    private static testResults = []
    private static boolean featurePassed = false


    public static ArrayList<JSONObject> getTestResults()
    {
        return testResults
    }

    /**
     * This is the implementation of TestWatchman method succeeded. It provides the interface method
     * needed by the Junit and Spock Test runner. When a pass is detected inside the runner it
     * will call this method with the FrameworkMethod object.
     *
     * @param e
     * @param method
     */
    @Override
    public void succeeded(FrameworkMethod method)
    {
        println method.getName() + " " + "success!\n" + method.getClass().getSimpleName()
        featurePassed = true

        try
        {
            qTestJSON.put("featureMethod", method.getName())
            qTestJSON.put("status", "PASS")
            qTestJSON.put('stacktrace', "")
            qTestJSON.put('failout', "")

        } catch (JSONException je) {
            je.printStackTrace()
        }

        testResults.add(qTestJSON)
    }

    /**
     * This is the implementation of TestWatchman method failed. It provides the interface method
     * needed by the Junit and Spock Test runner. When a failure is detected inside the runner it
     * will call this method with the Throwable error and FrameworkMethod object.
     *
     * @param e
     * @param method
     */
    @Override
    public void failed(Throwable e, FrameworkMethod method)
    {

        LinkedHashMap<String, String> listItems = new LinkedHashMap<>()

        featurePassed = false

        for (StackTraceElement ele : e.getStackTrace())
        {
            String traceText = ele.getFileName()
            if (Text.isSet(traceText))
            {
                if (traceText.contains(".groovy") || ele.toString().contains("insidesales"))
                    listItems.put(traceText, ele.toString())
            }
        }

        log.info(method.getName() + " failed\n" + method.getClass().getSimpleName())
        String stack = method.getName() + " failed\n" + e.getClass().getSimpleName() + "\n"
        if (listItems.size() > 0)
            listItems.each { stack += "${it}\n" }
        else
            e.getStackTrace().each { stack += "${it}\n" }


        String errorMsg = e.getMessage()

        try
        {
            qTestJSON.put("featureMethod", method.getName())
            qTestJSON.put("status", "FAIL")
            qTestJSON.put('stacktrace', stack)
            qTestJSON.put('failout', errorMsg)

        } catch (JSONException je) {
            je.printStackTrace();
        }

        testResults.add(qTestJSON)
    }

    /**
     * This is the method that should be invoked when an error happens outside of the Junit and Spock Test Runner.
     * Important Note:
     *     The invocation of this method provides the results needed for proper reporting to the micro services.
     *     Any time that this is called use the most clear, concise and direct throwable possible which will make
     *     finding the errors easier and quicker to diagnose.
     * @param e {@link Throwable , such as any exception}
     * @param method
     */
    public static void externalFailure(Throwable e, String method)
    {
        println method + " failed\n"

        featurePassed = false

        String stack = method + " failed\n" + e.getClass().getSimpleName() + "\n"
        e.getStackTrace().each { stack += "${it}\n" }

        String errorMsg = e.getMessage()

        try
        {
            qTestJSON.put("status", "FAIL")
            qTestJSON.put('stacktrace', stack)
            qTestJSON.put('failout', errorMsg)

        } catch (JSONException je) {
            je.printStackTrace()
        }

        testResults.add(qTestJSON)
    }

    public static boolean isTestPassed()
    {
        return featurePassed
    }

}
