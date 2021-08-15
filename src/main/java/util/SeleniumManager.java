package util;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.openqa.grid.common.exception.RemoteNotReachableException;
import org.openqa.selenium.Point;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * This class helps to manage the Selenium WebDriver by starting the correct WebDriver
 * and handling several settings needed to run tests.
 *
 * @author bchristiansen
 */
@Slf4j
public class SeleniumManager extends SeleniumBase implements CleanerInterface
{
    // Window handling.
    public static final String CRM_WINDOW = "crmWindow";
    public static final String PB_WINDOW = "pbWindow";
    public static final String PLAY_WINDOW = "playWindow";
    public static final String EMAIL_TEMPLATE_WINDOW = "templateWindow";
    public static final String INSIGHTS_WINDOW = "insightsWindow";
    public static final String GMAIL_WINDOW = "gmailWindow";
    public HashMap<String, ActiveWindows> activeWindows;
    public ActiveWindows tempWindowHolder;

    // Window sizing flags.
    public static final int FULL_SIZE = -1;
    public static final int HALF_SIZE = -2;

    private boolean doNotClean;

    /**
     * This is a constructor of the util.SeleniumManager class. It will determine which cleanup this
     * manager instance will be set up as.
     */
    public SeleniumManager()
    {
        // Window handling setup.
        activeWindows = new HashMap<>();
        tempWindowHolder = new ActiveWindows();
        activeWindows.put(CRM_WINDOW, tempWindowHolder);

        tempWindowHolder = new ActiveWindows();
        activeWindows.put(EMAIL_TEMPLATE_WINDOW, tempWindowHolder);

        tempWindowHolder = new ActiveWindows();
        activeWindows.put(GMAIL_WINDOW, tempWindowHolder);
    }

    public void cleanup()
    {
        stop();
    }

    /**
     * Get the base URL.
     *
     * @return The base URL.
     */
    public String getBaseURL()
    {
        return SystemProperties.getParam(SystemProperties.BASEURL);
    }

    /**
     * Returns the current browser's name.
     *
     * @return The current browser being used.
     */
    public String getBrowser()
    {
        return SystemProperties.getParam(SystemProperties.BROWSER);
    }

    public String getLanguage()
    {
        return SystemProperties.getParam(SystemProperties.SYSTEM_LANGUAGE);
    }

    /**
     * Get the main window handle
     *
     * @return The string containing the main window handle
     */
    public String getMainWindow()
    {
        return this.mainWindow;
    }

    /**
     * Get the password for logging into the testing system.
     *
     * @return The default password.
     */
    public String getPassword()
    {
        return SystemProperties.getParam(SystemProperties.BILLING_PASSWORD);
    }

    public String getSystemConfig()
    {
        return SystemProperties.getParam(SystemProperties.SYSTEM);
    }

    /**
     * Get the username for logging into the testing system.
     *
     * @return The default user name for logging in.
     */
    public String getUsername()
    {
        return SystemProperties.getParam(SystemProperties.BILLING_USERNAME);
    }

    /**
     * @return True if the browser used is Google Chrome.
     */
    public Boolean isChrome()
    {
        return (getBrowser().contains("chrome"));
    }

    public boolean isClean()
    {
        return doNotClean;
    }

    /***
     * This is a method to determine if an element is visible that
     * does not throw an error if the element is not found.
     * @param e WebElement
     * @return boolean displayed
     */
    public boolean isElementVisible(WebElement e)
    {
        boolean displayed;
        try
        {
            displayed = e.isDisplayed();
        } catch (Exception ex)
        {
            displayed = false;
        }
        return displayed;
    }

    /**
     * @return True if the browser used is Firefox.
     */
    public Boolean isFirefox()
    {
        return (getBrowser().contains("firefox"));
    }

    /**
     * @return True if the browser used is Microsoft Internet Explorer.
     */
    public Boolean isIE()
    {
        return (getBrowser().contains("iexplore"));
    }

    /**
     * Sets the size of the window to the maximum width and height of the screen.
     */
    public void maximize()
    {
        java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setWindowSize(screenSize.width, screenSize.height);
    }

    /**
     * Open a new window.
     *
     * @return The handle to the new window.
     */
    public String openNewTab()
    {
        Set<String> before = driver.getWindowHandles();
        ((JavascriptExecutor) driver).executeScript("window.open();");

        String newHandle = this.waitForPopup(before);
        driver.switchTo().window(newHandle);
        //Window size should be dictated by test writer, leaving here as example.
        //setWindowSize(util.SeleniumManager.FULL_SIZE, util.SeleniumManager.FULL_SIZE);
        return newHandle;
    }

    /**
     * Cause a window refresh to occur.
     */
    public void refresh()
    {
        getWebDriver().navigate().refresh();
    }

    /**
     * Set the base URL.
     *
     * @param baseURL The baseURL for running tests.
     */
    public void setBaseURL(String baseURL)
    {
        SystemProperties.setParam(SystemProperties.BASEURL, baseURL);
    }

    public void setDoNotClean(boolean doNotClean)
    {
        this.doNotClean = doNotClean;
    }

    public void setLanguage(String language)
    {
        SystemProperties.setParam(SystemProperties.SYSTEM_LANGUAGE, language);
    }

    /**
     * Set the main window handle
     *
     * @param window The handle to the main window.
     */
    public void setMainWindow(String window)
    {
        this.mainWindow = window;
    }

    /**
     * Set the testing system password.
     *
     * @param password The system password.
     */
    public void setPassword(String password)
    {
        SystemProperties.setParam(SystemProperties.BILLING_PASSWORD, password);
    }

    /**
     * Set the password for logging into salesforce.
     *
     * @param password The password for logging into sales force.
     */
    public void setSFPassword(String password)
    {
        SystemProperties.setParam(SystemProperties.BILLING_PASSWORD, password);
    }

    /**
     * Set the username for logging into salesforce.
     *
     * @param username The username for logging into sales force.
     */
    public void setSFUsername(String username)
    {
        SystemProperties.setParam(SystemProperties.BILLING_USERNAME, username);
    }

    /**
     * Set a value into the security secret.
     *
     * @param securitySecret The security secret string.
     */
    public void setSecuritySecret(String securitySecret)
    {
        SystemProperties.setParam(SystemProperties.SECURITY_SECRET, securitySecret);
    }

    /**
     * Set a value into the security token.
     *
     * @param securityToken The security token string.
     */
    public void setSecurityToken(String securityToken)
    {
        SystemProperties.setParam(SystemProperties.SECURITY_TOKEN, securityToken);
    }

    public void setSystemConfig(String systemConfig)
    {
        SystemProperties.setParam(SystemProperties.SYSTEM, systemConfig);
    }

    /**
     * Set the testing system username.
     *
     * @param username The text of the user name.
     */
    public void setUsername(String username)
    {
        SystemProperties.setParam(SystemProperties.BILLING_USERNAME, username);
    }

    /**
     * Sets the WebDriver and initializes the waits.
     *
     * @param driver The WebDriver instance to be set.
     */
    private void setWebDriver(WebDriver driver)
    {
        this.driver = driver;

        // Setup the wait objects.
        SystemProperties.setLongWait(
            new WebDriverWait(driver, Integer.parseInt(SystemProperties.getParam(SystemProperties.LONG_WAIT))));
        SystemProperties.setMediumWait(
            new WebDriverWait(driver, Integer.parseInt(SystemProperties.getParam(SystemProperties.MED_WAIT))));
        SystemProperties.setShortWait(
            new WebDriverWait(driver, Integer.parseInt(SystemProperties.getParam(SystemProperties.SHORT_WAIT))));
        SystemProperties.setXLongWait(
            new WebDriverWait(driver, Integer.parseInt(SystemProperties.getParam(SystemProperties.XLONG_WAIT))));
    }

    /**
     * Set the size of the window. It will always default the upper left hand corner of the
     * window to 0, 0; always placing the window at that location, then adjusting the size.
     *
     * @param width  The width of the window.
     * @param height The height of the window.
     * @apiNote There are reserved values in the {@link SeleniumManager} class for setting
     * a window to full or half the screen width or height.<br>
     * <b><tt>HALF_SIZE, FULL_SIZE</tt></b>
     * @see #HALF_SIZE
     * @see #FULL_SIZE
     */
    public void setWindowSize(int width, int height)
    {
        int _width, _height;

        // Get the height of the task bar
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        GraphicsDevice gd = gs[0];
        GraphicsConfiguration[] gc = gd.getConfigurations();
        Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(gc[0]);
        int taskBarSize = scnMax.bottom;

        switch (width)
        {
            case FULL_SIZE:
                _width = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
                break;
            case HALF_SIZE:
                _width = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2;
                break;
            default:
                _width = width;
        }

        switch (height)
        {
            case FULL_SIZE:
                _height = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() - taskBarSize;
                break;
            case HALF_SIZE:
                _height = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2;
                break;
            default:
                _height = height;
        }

        Dimension size = new Dimension(_width, _height);
        Point pos = new Point(0, 0);
        try
        {
            driver.manage().window().setPosition(pos);
            driver.manage().window().setSize(size);
        } catch (Exception ex)
        {
            // If an exception happens, let's wait a moment then try it again.
            Utils.wait(1);
            driver.manage().window().setPosition(pos);
            driver.manage().window().setSize(size);
        }
    }

    public void switchToCrmWindow()
    {
        WebDriver driver = getWebDriver();
        tempWindowHolder = activeWindows.get(CRM_WINDOW);
        driver.switchTo().window(tempWindowHolder.windowReference);
        maximize();
    }

    public void switchToGmailWindow()
    {
        WebDriver driver = getWebDriver();
        tempWindowHolder = activeWindows.get(GMAIL_WINDOW);
        driver.switchTo().window(tempWindowHolder.windowReference);
    }

    /**
     * This method attempts to start the WebDriver.
     *
     * @throws IOException   For file handling errors (usually with the PB plugin).
     * @throws JSONException When trying to handle JSON data.
     * @throws Exception     For other general errors.
     */
    public void start() throws IOException, Exception
    {
        ChromeDriverConfig chromeDriverConfig;
        String driverExe = "";

        doNotClean = false;

        Dimension size = new Dimension(1280, 800);
        String os = System.getProperty("os.name").toLowerCase();

        switch (SystemProperties.getParam(SystemProperties.BROWSER))
        {
            case "firefox":
                /*
                 * Additional preferences.
                 * http://kb.mozillazine.org/Category:Preferences
                 *
                 * Possible preferences
                 * profile.setPreference("browser.helperApps.neverAsk.saveToDisk" , "application/octet-stream;application/csv;text/csv;application/vnd.ms-excel;");
                 * profile.setPreference("browser.helperApps.alwaysAsk.force", false);
                 * profile.setPreference("browser.download.manager.showWhenStarting",false);
                 * profile.setPreference("browser.download.folderList", 2);
                 */
                FirefoxProfile profile = new FirefoxProfile();
                profile.setPreference("browser.sessionstore.postdata", 1);
                profile.setPreference("security.mixed_content.block_active_content", false);

                DesiredCapabilities dc = DesiredCapabilities.firefox();
                dc.setCapability(FirefoxDriver.PROFILE, profile);
                setWebDriver(new FirefoxDriver(dc));
                setWindowSize(size.width, size.height);
                break;

            case "iexplore":
                /*
                 * The IEDriverServer.exe must be set in the system properties. If not,
                 * the IEDriverServer is supposed to look along the path for it. However,
                 * I have not seen this work so I am explicitly setting the system property.
                 */
                driverExe = "lib/IEDriverServer.exe";
                String osArch = System.getProperty("os.arch").toLowerCase();
                if (osArch.contains("x86"))
                    driverExe = "lib/IEDriverServer32.exe";
                System.setProperty("webdriver.ie.driver", driverExe);
                setWebDriver(new InternetExplorerDriver());
                setWindowSize(size.width, size.height);
                break;

            case "chrome":
            case "googlechrome":
            default:
                chromeDriverConfig = new ChromeDriverConfig();
                driver = chromeDriverConfig
                    .setDriverExe()
                    .buildChromeOptions()
                    .build();
                setWebDriver(driver);

                // Set the option to allow all popups.
                mainWindow = getWebDriver().getWindowHandle();

                // This makes sure that all popups are allowed. Only needed for PD.
                try
                {
                    if (!SystemProperties.getParam(SystemProperties.SYSTEM).contains("pb"))
                    {
                        // Open the chrome settings so we can set it to allow popups.
                        // This is probably not needed for Playbooks.
                        driver.get("chrome://settings/content/popups");

                        ArrayList<String> menuShadow = new ArrayList<>();
                        menuShadow.add("#main");
                        menuShadow.add("settings-basic-page");
                        menuShadow.add("#basicPage > settings-section.expanded > settings-privacy-page");
                        menuShadow.add("#pages > settings-subpage > category-default-setting");
                        menuShadow.add("settings-toggle-button#toggle");
                        menuShadow.add("#control");
                        expandShadowDom(menuShadow, By.cssSelector("#knob")).click();
                    }

                    log.debug("Chrome driver started. Setting position and size...");
                    setWindowSize(size.width, size.height);

                    // Focus back to the main window, just in case.
                    driver.switchTo().window(mainWindow);
                } catch (RemoteNotReachableException nrex)
                {
                    log.warn("Try to reach and size the windows one more time.");
                    //driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);
                    driver.get("about:blank");
                    setWindowSize(size.width, size.height);
                } catch (Exception ex)
                {
                    log.error("There was a problem in the content settings or setting up the chromedriver...");
                    driver.close();
                    throw ex;
                }
        }

        // set the main window handle
        this.mainWindow = driver.getWindowHandle();
        Cleaner.addCleanupObject(this);

        // Set the Selenium manager into the util.SystemProperties map.
        SystemProperties.setParam(SystemProperties.SELENIUM_MANAGER, this);
    }

    /**
     * This expands the number of shadow-root elements that are contained with the shadowList
     * list, then will return as a WebElement the actual item being looked for.
     *
     * @param shadowList A list containing all the root node CSS for the shadow-root elements.
     * @param element    The identifier of the actual element being looked for.
     * @return {@link WebElement}
     */
    private WebElement expandShadowDom(ArrayList<String> shadowList, By element)
    {
        // Short wait to make sure the items are in view.
        Utils.wait(SystemProperties.sVal);

        // Always start with the root of the DOM and driver.
        WebElement root = driver.findElement(By.cssSelector("body > settings-ui"));
        WebElement ele = expandRootElement(root);

        for (String loc : shadowList)
        {
            root = ele.findElement(By.cssSelector(loc));
            ele = expandRootElement(root);
        }

        return ele.findElement(element);
    }

    private WebElement expandRootElement(WebElement element)
    {
        WebElement ele = (WebElement) ((JavascriptExecutor) driver)
            .executeScript("return arguments[0].shadowRoot", element);
        return ele;
    }

    /**
     * Stops and closes the web driver.
     */

    public void stop()
    {
        if (getWebDriver() != null)
        {
            getWebDriver().close();
            getWebDriver().quit();
        }

        doNotClean = true;
    }
}