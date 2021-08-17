package common


import org.openqa.selenium.WebDriver
import spock.lang.Specification
import spock.lang.Stepwise
import util.Cleaner
import util.SeleniumManager
import util.Text

/**
 * generic specbase setup for UI and API tests.
 * @author bchristiansen
 */
@Stepwise
abstract class UtilSpecBase extends Specification
{

    static String uniqueId
    protected static SeleniumManager sm = null
    protected static WebDriver driver
    protected static String mainWindow
    static int indexFeature = 0

    abstract void startUI()

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
    }

    def cleanupSpec()
    {
        try
        {
            // Cleanup any objects created
            Cleaner.cleanup()
        } catch (Exception e)
        {
            System.out.print("There was a problem in the cleanupSpec." + e.getMessage())
        }
    }

    def cleanup()
    {
        try
        {
            Cleaner.cleanupTest()

        } catch (Exception e)
        {
            System.out.print("There was a problem in the cleanup." + e.getMessage())
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