package actions

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import util.CleanerInterface
import util.SeleniumManager
import util.SystemProperties

class WelcomeToTheInternet implements CleanerInterface
{
    private boolean doNotClean
    private SeleniumManager sm
    private WebDriver driver

    @FindBy (xpath = ".//input[@name = 'email']") private WebElement emailInput
    @FindBy (xpath = ".//button[@id = 'form_submit']") private WebElement formSubmit

    WelcomeToTheInternet(SeleniumManager sm = SystemProperties.getSeleniumManager())
    {
        this.sm = sm
        driver = sm.getWebDriver()

        // Initialize the POM elements
        PageFactory.initElements(driver, this)

        setDoNotClean(false)
    }

    /**
     * Open the Challenging DOM link
     * @return {@link WelcomeToTheInternet}
     */
    WelcomeToTheInternet challengingDOM()
    {
        String path = ".//a[text() = 'Challenging DOM']"
        WebElement item = sm.waitForAvailable(By.xpath(path))
        item.click()

        return this
    }

    /**
     * Open the Forgotten Password link
     * @return {@link WelcomeToTheInternet}
     */
    WelcomeToTheInternet forgottenPassword(String pwd)
    {
        String path = ".//a[text() = 'Forgot Password']"
        WebElement item = sm.waitForAvailable(By.xpath(path))
        item.click()

        // Enter the email
        sm.waitForExists(emailInput)
        emailInput.sendKeys(pwd)

        // Click the button to send the password
        formSubmit.click()
        Thread.sleep(2000) // Just some time to see the result.

        return this
    }

    /**
     * Open WelcomeToTheInternet home page.
     * @return {@link WelcomeToTheInternet}
     */
    WelcomeToTheInternet open()
    {
        driver.get("http://the-internet.herokuapp.com/")

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
