package actions

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import util.CleanerInterface
import util.SeleniumManager
import util.SystemProperties

class Usana implements CleanerInterface
{
    private SeleniumManager sm
    private WebDriver driver
    boolean doNotClean

    Usana(SeleniumManager sm = SystemProperties.getSeleniumManager())
    {
        this.sm = sm
        driver = sm.getWebDriver()

        doNotClean = false
    }

    /**
     * Is the Feedback button displayed on the page.
     * @return True if the button is displayed, otherwise false.
     */
    boolean isFeedbackPresent()
    {
        String path = ".//*[@id = 'QSIFeedbackButton-btn']"
        WebElement element = sm.waitForAvailable(By.xpath(path))

        return element.isDisplayed()
    }

    /**
     * Open Usana home page and click on the Usana logo.
     * @return {@link Usana}
     */
    Usana open()
    {
        driver.get("https://www.usana.com/ux/dotcom/enu-US/home")

        String path = ".//*[@data-automation = 'logo-home']"
        WebElement page = sm.waitForAvailable(By.xpath(path))
        sm.waitForAvailable(path)
        page.click()

        return this
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
