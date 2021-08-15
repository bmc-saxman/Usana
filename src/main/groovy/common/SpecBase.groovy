package common


import groovy.util.logging.Slf4j
import spock.SpockWatcher
import spock.lang.Stepwise
import util.ActiveWindows
import util.SeleniumManager
import util.SystemProperties

/**
 * Created by Evan Hicken on 6/21/18.
 */
@Slf4j
@Stepwise
class SpecBase extends UtilSpecBase
{
    private ActiveWindows baseWindow

    void startUI(String crm)
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

            // Output the browser name.
            log.debug("Using BROWSER={}", sm.getBrowser())
        } catch (Exception ex)
        {
            //catching the exception so we can die in test and clean up properly
            log.error("[SpecBase] Setup error 'starting and prepping the Selenium Manager'.\n\tException Message: {}\n\tFatal. Throwing...", ex.getMessage())
            SpockWatcher.externalFailure(ex, "setupSpec > selenium")
            throw ex
        }

        switch (crm)
        {
            case "sf":
                driver.get("https://login.salesforce.com")
                break
            case "sfl":
                driver.get("https://login.salesforce.com")
                break
            case "msd":
//                driver.get(SystemProperties.getParam(SystemProperties.MSD_REST_URL))
                // Land on the Sales Hub app URL directly.
//                driver.get("https://pbmsdprod.crm.dynamics.com/main.aspx?appid=58273490-8b69-e911-a981-000d3a1d7b67")
                driver.get(SystemProperties.getParam(SystemProperties.MSD_LOGIN_URL))
                break
            default:
                driver.get(crm)
        }
    }
}