package common

import spock.SpockWatcher
import spock.lang.Stepwise
import util.ActiveWindows
import util.SeleniumManager

/**
 * @author bchristiansen
 */
@Stepwise
class SpecBase extends UtilSpecBase
{
    private ActiveWindows baseWindow

    void startUI()
    {
        // initialization is only done once
        sm = new SeleniumManager()

        try
        {
            sm.start()
            driver = sm.getWebDriver()
            baseWindow = new ActiveWindows()
            baseWindow.windowReference = driver.getWindowHandle()
            baseWindow.state = false
            sm.activeWindows.put(SeleniumManager.CRM_WINDOW, baseWindow)
            mainWindow = baseWindow.windowReference

        } catch (Exception ex)
        {
            //catching the exception so we can die in test and clean up properly
            SpockWatcher.externalFailure(ex, "setupSpec > selenium")
            throw ex
        }

        // Let's maximize the window for best access.
        sm.maximize()
        driver.get("http://the-internet.herokuapp.com/")
    }
}