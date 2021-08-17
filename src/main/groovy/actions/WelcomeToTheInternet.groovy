package actions

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import util.CleanerInterface
import util.SeleniumManager
import util.SystemProperties

class WelcomeToTheInternet implements CleanerInterface
{
    private boolean doNotClean
    private SeleniumManager sm
    private WebDriver driver

    WelcomeToTheInternet(SeleniumManager sm = SystemProperties.getSeleniumManager())
    {
        this.sm = sm
        driver = sm.getWebDriver()

        setDoNotClean(false)
    }


    /**
     * Open WelcomeToTheInternet home page.
     * @return {@link WelcomeToTheInternet}
     */
    WelcomeToTheInternet open()
    {
        String path = ".//a[text() = 'Challenging DOM']"
        WebElement item = sm.waitForAvailable(By.xpath(path))
        item.click()

        return this
    }

    /**
     * Is the Canvas element on this page displayed.
     * @return {@link Boolean}
     */
    boolean isCanvasDisplayed()
    {
        String path = ".//*[@id = 'canvas']"
        WebElement canvas = sm.waitForAvailable(By.xpath(path))

        return canvas.isDisplayed()
    }

    @Override
    void cleanup()
    {

    }

    @Override
    boolean isClean()
    {
        return doNotClean
    }

    @Override
    void setDoNotClean(boolean doNotClean)
    {
        this.doNotClean = doNotClean
    }
}
