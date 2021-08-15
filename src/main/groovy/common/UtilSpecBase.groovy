package common

import groovy.util.logging.Slf4j
import http.OkHttpUtils
import okhttp3.Response
import org.json.JSONObject
import org.junit.Rule
import org.openqa.selenium.WebDriver
import spock.lang.Specification
import spock.lang.Stepwise
import util.Cleaner
import util.SeleniumManager
import util.SystemProperties

/**
 * generic specbase setup for UI and API tests.
 * Created by evan.hicken 5/25/2018
 */
@Slf4j
@Stepwise
abstract class UtilSpecBase extends Specification
{

    static Boolean scenarioPassed = true
    static String uniqueId
    protected static SeleniumManager sm = null
    static String scenarioException = null
    protected static WebDriver driver
    protected static String mainWindow
    static String screenshotUrl = null
    static int indexFeature = 0

    //rest calls
    private OkHttpUtils okHttpUtils
    private JSONObject jsonObject
    private String payloadBody
    Response okHttpResponse

    protected static Map customMap = [:]

    @Rule
    public ScenarioWatchman scenarioWatchman = new ScenarioWatchman()

    abstract void startUI(String crmUrl)

    def customScenarioSetup()
    {
        //default scenario setup
        // Warning: any code that causes either an exception or an assert
        // will not be reported back to the microservice. Instead, "Step-0"() should
        // be defined in the test.
    }

    def customScenarioCleanup()
    {
        //default scenario cleanup
    }

    def customFeatureSetup()
    {
        //default feature setup
    }

    def customFeatureCleanup()
    {
        //default feature cleanup
    }

    def setupSpec()
    {
        // Initialize the random character sequence.
        uniqueId = Text.getRandomText("", 3)

        captureSystemOut()

        log.debug "in setupSpec()"

        try
        {
            customScenarioSetup()
        } catch (Exception e)
        {
            def msg = "<no exception message>"
            if (e.getMessage() != null)
                msg = e.getMessage()

            ScenarioWatchman.externalFailure(e, "customScenarioSetup()")
            assert false, "Exception occurred in customScenarioSetup(): " + msg
        }

        // If any errors occur in the SystemProperties, this is where they are handled.
        if (SystemProperties.getParam(SystemProperties.PROPERTIES_ERROR).equalsIgnoreCase("true"))
        {
            Exception ex = new Exception("There was a problem initializing the system properties.  Message: " +
                SystemProperties.getParam(SystemProperties.PROPERTIES_ERROR_MSG))
            ScenarioWatchman.externalFailure(ex, "SystemProperties.readProperties()")
            throw ex
        }

        scenarioStart()
    }

    def cleanupSpec()
    {

        log.debug "in cleanupSpec()"

        customScenarioCleanup()

        scenarioEnd()

        try
        {
            // Cleanup any objects created
            Cleaner.cleanup()
        } catch (Exception e)
        {
            log.warn("There was a problem in the cleanupSpec.", e)
        }
    }

    def setup()
    {
        try
        {
            log.debug "in setup()"

            customFeatureSetup()

            featureStart(getFeatureIndex(), getFeatureName(this))
        } catch (Exception e)
        {
//            def msg = "<no exception message>"
//            if (e.getMessage() != null)
//                msg = e.getMessage()

            // If we have an error in the featureStart we should throw an exception.
            ScenarioWatchman.externalFailure(e, "featureStart()")
            throw e
        }
    }

    def cleanup()
    {
        log.debug "in cleanup()"
        log.debug ScenarioWatchman.getTestResults().toString()

        customFeatureCleanup()

        featureEnd(getFeatureIndex(), ScenarioWatchman.getTestResults())

        try
        {
            Cleaner.cleanupTest()

        } catch (Exception e)
        {
            log.warn("There was a problem in the cleanup.", e)
        }
    }

    static def featureResources = []
    private PrintStream oldStream
    private ByteArrayOutputStream stdout


    int getFeatureIndex()
    {
        return indexFeature
    }

    String getFeatureName(thisPtr)
    {
        thisPtr.specificationContext.currentIteration.name
    }

    void resetSystemOut()
    {
        // Put things back
        System.out.flush()
        System.setOut(oldStream)
    }

    void scenarioStart()
    {
        if (Text.isSet(SystemProperties.getParam(SystemProperties.SCENARIO_START_URL)))
        {
            log.info("SystemParams = {}", SystemProperties.getMappedParams().toString())

            Map body = ["scenarioOutputId": SystemProperties.getParam(SystemProperties.SCENARIO_OUTPUT_ID),
                        "retryNum"        : SystemProperties.getParam(SystemProperties.RETRY_NUM).toInteger()]
            log.debug("")
            log.debug("")
            log.debug("SCENARIO START BODY = {}", body)
            log.debug("")
            log.debug("")

            jsonObject = new JSONObject(body)
            payloadBody = jsonObject.toString()
            log.info("Scenario Start Payload body is {}", payloadBody)
            okHttpUtils = new OkHttpUtils()
            okHttpResponse = okHttpUtils.postRequest(SystemProperties.getParam(SystemProperties.SCENARIO_START_URL), payloadBody)
            if (okHttpResponse.code() != 200)
            {
                log.error("Expected 200 got {}", okHttpResponse.code().toString())
            }
            okHttpResponse.close()

            sleep(500)  // wait needed for microservice
        }
    }

    void scenarioEnd()
    {
        if (Text.isSet(SystemProperties.getParam(SystemProperties.SCENARIO_RESULTS_URL)))
        {
            Map body = ["scenarioOutputId": SystemProperties.getParam(SystemProperties.SCENARIO_OUTPUT_ID),
                        "retryNum"        : SystemProperties.getParam(SystemProperties.RETRY_NUM).toInteger(),
                        "status"          : scenarioPassed ? "PASS" : "FAIL",
                        "stackTrace"      : scenarioException,
                        "resources"       : featureResources
            ]

            log.debug("")
            log.debug("")
            log.debug("SCENARIO RESULTS BODY = {}", body)
            log.debug("")
            log.debug("")

            if (screenshotUrl != null)
            {
                body.put("screenshotUrl", screenshotUrl)
            }

            jsonObject = new JSONObject(body)
            payloadBody = jsonObject.toString()
            log.info("Scenario End Payload body is {}", payloadBody)
            okHttpUtils = new OkHttpUtils()
            okHttpResponse = okHttpUtils.postRequest(SystemProperties.getParam(SystemProperties.SCENARIO_RESULTS_URL), payloadBody)
            if (okHttpResponse.code() != 200)
            {
                log.error("Expected 200 got {}", okHttpResponse.code().toString())
            }
            okHttpResponse.close()
        }

        // clear the results
        if (scenarioWatchman)
            scenarioWatchman.getTestResults().clear()

        resetSystemOut()
    }

    void featureStart(int index, String featureName)
    {
        if (Text.isSet(SystemProperties.getParam(SystemProperties.FEATURE_START_URL)))
        {
            Map body = ["scenarioOutputId": SystemProperties.getParam(SystemProperties.SCENARIO_OUTPUT_ID),
                        "index"           : index,
                        "retryNum"        : SystemProperties.getParam(SystemProperties.RETRY_NUM).toInteger(),
                        "name"            : featureName, "fullName": featureName]

            log.debug("")
            log.debug("")
            log.debug("FEATURE START BODY = {}", body)
            log.debug("")
            log.debug("")

            jsonObject = new JSONObject(body)
            payloadBody = jsonObject.toString()
            log.info("Feature Start Payload body is {}", payloadBody)
            okHttpUtils = new OkHttpUtils()
            okHttpResponse = okHttpUtils.postRequest(SystemProperties.getParam(SystemProperties.FEATURE_START_URL), payloadBody)
            if (okHttpResponse.code() != 200)
            {
                log.error("Expected 200 got {}", okHttpResponse.code().toString())
            }
            okHttpResponse.close()
        }
    }

    void featureEnd(int index, ArrayList result)
    {
        if (Text.isSet(SystemProperties.getParam(SystemProperties.FEATURE_RESULTS_URL)))
        {
            Map body = ["scenarioOutputId": SystemProperties.getParam(SystemProperties.SCENARIO_OUTPUT_ID),
                        "index"           : index,
                        "retryNum"        : SystemProperties.getParam(SystemProperties.RETRY_NUM).toInteger()]

            body.put("status", result[0].'status' ? result[0].'status' : "")
            body.put("stackTrace", result[0].'stacktrace' ? result[0].'stacktrace' : "")
            body.put("failout", result[0].'failout' ? result[0].'failout' : "")
            body.put("featureMethod", result[0].'featureMethod' ? result[0].'featureMethod' : "")

            if (body.status == 'FAIL')
            {
                scenarioPassed = false
                scenarioException = body.stackTrace
                SystemProperties.setParam(SystemProperties.FEATURE_RESULT, "FAIL")
            }

            if (!scenarioPassed)
            {
                if ((sm != null) && (SystemProperties.getParam(SystemProperties.SCREENSHOT_URL) == "null"))
                {
                    //This will only grab a screenshot for the first feature failure.
                    sm.getScreenShot()
                    log.info("Uploading screen shot to: {}",
                        SystemProperties.getParam(SystemProperties.SCREENSHOT_URL))
                    screenshotUrl = SystemProperties.getParam(SystemProperties.SCREENSHOT_URL)
                    body.put("screenshotUrl", SystemProperties.getParam(SystemProperties.SCREENSHOT_URL))
                }
            }

            log.debug("")
            log.debug("")
            log.debug("FEATURE RESULTS BODY = {}")
            log.debug(body.toString())
            log.debug("")
            log.debug("")

            jsonObject = new JSONObject(body)
            payloadBody = jsonObject.toString()
            log.info("Feature End Payload body is {}", payloadBody)
            okHttpUtils = new OkHttpUtils()
            okHttpResponse = okHttpUtils.postRequest(SystemProperties.getParam(SystemProperties.FEATURE_RESULTS_URL), payloadBody)
            if (okHttpResponse.code() != 200)
            {
                log.error("Expected 200 got {}", okHttpResponse.code().toString())
            }
            okHttpResponse.close()

            //this will increment the feature count each time it's called, which should be only at the start of each new feature.
            indexFeature++
        }
    }

    void addResource(String type, String value)
    {
        def resource = ["type": type, "value": value, "index": getFeatureIndex()]
        featureResources.add(resource)
    }

    void clearResources()
    {

        if (featureResources)
            featureResources = []
    }

    void captureSystemOut()
    {
        // Create a stream to hold the output
        stdout = new ByteArrayOutputStream()
        PrintStream ps = new PrintStream(stdout)

        oldStream = System.out  // Save the old System.out!
        System.setOut(ps)       // Tell Java to use the stream
    }
}