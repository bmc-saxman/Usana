package util;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.Platform;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.UnreachableBrowserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bchristiansen
 * Class to configure and build a ChromeWebDriver.
 */
@Slf4j
public class ChromeDriverConfig implements CleanerInterface
{
    private String driverExe = "";
    private DesiredCapabilities desiredCapabilities;
    private ChromeOptions chromeOptions;
    private String osPlatform;
    private String contentType;
    private boolean doNotClean;

    /**
     * Here we build the properties we need. Attention: the order matters!
     */
    public ChromeDriverConfig()
    {
        // Make sure that this gets cleaned up.
        doNotClean = false;
    }

    /**
     * Builds the chromedriver and starts up the browser.
     *
     * @return {@link ChromeDriver}
     */
    public ChromeDriver build() throws Exception
    {
        File driverFile;
        try
        {
            String version = SystemProperties.getParam(SystemProperties.CHROME_DRIVER_VERSION);
            String baseUrl = "chromedriver/" + version + "/" + driverExe;

            InputStream driverStream = SystemProperties.class.getClassLoader().getResourceAsStream(baseUrl);

            // Create a temporary file, which will be the chromedriver.
            driverFile = File.createTempFile(osPlatform, ".exe");
            driverFile.deleteOnExit();

            // Copy the byte stream to the temporary output file.
            OutputStream outputStream = new FileOutputStream(driverFile);
            IOUtils.copy(driverStream, outputStream);
            outputStream.close();
            driverStream.close();

            log.info("Temporary chromedriver file: {}", driverFile);

            log.debug("Setting the exe/writable attributes on the chromedriver file...");
            driverFile.setExecutable(true, false);
            driverFile.setWritable(true, false);
        } catch (Exception ex)
        {
            throw new Exception("Unable to create the temporary chromedriver.");
        }

        Cleaner.addCleanupObject(this);

        //*********************************************************
        // This section of code is needed for Linux systems to run in
        // the background using Xvfb graphics. The same chrome service
        // also runs under Windows without the DISPLAY being set.
        log.debug("Starting chrome driver service...");
        if (SystemProperties.isLinux())
        {
            String xD = SystemProperties.getParam(SystemProperties.XDISPLAY);
            xD = xD.startsWith(":") ? xD.substring(1) : xD;

            if (StringUtils.isNumeric(xD))
                SystemProperties.setParam(SystemProperties.XDISPLAY, ":" + xD);
        }

        // This sets up the service parameters for the chromedriver.
        ChromeDriverService serviceBuilder = new ChromeDriverService.Builder()
            .usingDriverExecutable(driverFile)
            .usingAnyFreePort()
            .withEnvironment(ImmutableMap.of("DISPLAY", SystemProperties.getParam(SystemProperties.XDISPLAY)))
            .build();

        log.debug("Creating the web driver and initializing...");
        desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
        ChromeDriver dr;
        try
        {
            // The service and options and passed into the ChromeDriver.
            dr = new ChromeDriver(serviceBuilder, desiredCapabilities);
        } catch (UnreachableBrowserException ube)
        {
            // We have seen, in a very few occasions, that the creation fails,
            // so we will try again just in case.
            dr = new ChromeDriver(serviceBuilder, desiredCapabilities);
        } catch (Exception ex)
        {
            log.error("Unable to create the chrome driver: {}", ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
        log.info("Chrome version: {}", dr.getCapabilities().getVersion());

        return dr;
    }

    /**
     * Set the chrome options. These options set the running state for the chrome browser.
     *
     * @return {@link ChromeDriverConfig}
     * @see "https://code.google.com/p/chromedriver/wiki/CapabilitiesAndSwitches#Using_the_class"
     */
    public ChromeDriverConfig buildChromeOptions()
    {
        // This option, set in the chrome properties, “-enable-easy-off-store-extension-install",
        // disables the prompt when installing a non-store extension.
        //
        // Code to load a user profile. Keeping for reference if ever wanted.
        //String path = "/AppData/Local/Google/Chrome/User Data";
        //String profilePath = System.getenv("HOMEPATH") + path;
        //options.addArguments("--user-data-dir=" + profilePath);

        chromeOptions = new ChromeOptions();
        chromeOptions
            .addArguments("--test-type=browser")
            .addArguments("--ignore-certificate-errors")
            .addArguments("--allow-running-insecure-content")
            .addArguments("--disable-infobars")
            .setExperimentalOption("excludeSwitches", new String[]{"enable-automation"})
            .addArguments("--force-fieldtrials=SiteIsolationExtensions/Control")
            .addArguments("--dns-prefetch-disable")
            .addArguments("--disable-notifications")
//            .addArguments("--disable-popup-blocking") // No longer using any popups.
            .addArguments("--no-sandbox")
            // These two options enable the mic use for the current browser session.
            .addArguments("use-fake-device-for-media-stream")
            .addArguments("use-fake-ui-for-media-stream");

        // This option is supposed to remove the save password prompt.
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        chromeOptions.setExperimentalOption("prefs", prefs);

        // Build the Desired Capabilities.
        desiredCapabilities = new DesiredCapabilities(BrowserType.CHROME, "", Platform.ANY);

        return this;
    }

    @Override public void cleanup()
    {
    }

    @Override public boolean isClean()
    {
        return doNotClean;
    }

    @Override public void setDoNotClean(boolean doNotClean)
    {
        this.doNotClean = doNotClean;
    }

    /**
     * Chooses the correct chromedriver based on the running operating system.
     *
     * @return {@link ChromeDriverConfig}
     */
    public ChromeDriverConfig setDriverExe()
    {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("nix") || os.contains("nux"))
        {
            log.debug("Linux/Unix OS");
            driverExe = "chromedriver_linux";
            contentType = "application/x-sharedlib";
            osPlatform = "chromedriver_linux";
        }
        else if (os.contains("mac") || os.contains("os x"))
        {
            log.debug("Mac");
            driverExe = "chromedriver_mac";
            contentType = "text/plain";
            osPlatform = "chromedriver_mac";
        }
        else
        {
            log.debug("Windows");
            driverExe = "chromedriver.exe";
            contentType = "application/x-executable";
            osPlatform = "chromedriver.exe";
        }

        return this;
    }
}