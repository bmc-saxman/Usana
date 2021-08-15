package util;

import groovy.json.JsonSlurper;
import http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.json.JSONObject;
import org.junit.Assert;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;

/**
 * @author bchristiansen
 * Many (possible) values related to system information.
 */
@Slf4j
public class SystemProperties
{
    // General map keys.
    public static final String UTILS_VERSION = "utilsVersion";
    public static final String NEXUS_AUTH_TOKEN = "nexusAuth";
    public static final String REPO_SERVER_URL = "repositoryServerUrl";
    public static final String SELENIUM_MANAGER = "seleniumManager";
    public static final String PROPERTIES_ERROR = "propertiesError";
    public static final String PROPERTIES_ERROR_MSG = "propertiesErrorMsg";
    public static final String XDISPLAY = "xDisplay";
    public static final String SYSTEM = "subdomainName";
    public static final String BROWSER = "browser";
    public static final String LANGUAGE = "language";
    public static final String SYSTEM_LANGUAGE = "en";
    public static final String LOCALE = "locale";
    public static final String SHORT_WAIT = "shortWait";
    public static final String MED_WAIT = "mediumWait";
    public static final String LONG_WAIT = "longWait";
    public static final String XLONG_WAIT = "xLongWait";
    public static final String LOAD_SALES_FORCE_PACKAGE = "loadSalesForcePackage";
    public static final String DBURL = "dbUrl";
    public static final String DBUSERNAME = "dbUsername";
    public static final String DBPASSWORD = "dbPassword";
    public static final String SYSTEM_SERVER = "ciUrl";
    public static final String BASEURL = "loginUrl";
    public static final String BILLING_USERNAME = "billingUsername";
    public static final String BILLING_PASSWORD = "billingPassword";
    public static final String CIURL = "ciUrl";
    public static final String FILE_NAME = "fileName";
    public static final String CONFIG = "config";
    public static final String SUBDOMAIN_NAME = "subdomainName";
    public static final String SUBDOMAIN_URL = "subdomainUrl";
    public static final String PACKAGE_VERSION = "packageVersion";
    public static final String SECURITY_TOKEN = "security_token";
    public static final String SECURITY_SECRET = "security_secret";
    public static final String NET_SUITE_AUTHORIZATION = "netsuiteAuthorization";
    public static final String COMMS_REST_URL = "commsRestUrl";
    public static final String SFURL = "sfUrl";
    public static final String SERVICE_URL = "serviceUrl";
    public static final String SALES_FORCE_PACKAGE = "salesForcePackage";
    public static final String SCREENSHOT_URL = "screenshotUrl";
    public static final String CHROME_DRIVER_URL = "chromeDriverUrl";
    public static final String CHROME_DRIVER_VERSION = "chromeDriverVersion";
    public static final String POD = "pod";

    // AIM keys
    public static final String EXE_ID = "exeId";
    public static final String RUN_ID = "runId";
    public static final String TEST_ID = "testId";
    public static final String RESULT = "result";
    public static final String EXE_RESULT = "exeResult";
    public static final String XML_RESULTS = "xmlResults";
    public static final String DURATION = "duration";
    public static final String NODE = "node";
    public static final String PRODUCT = "product";
    public static final String ENV = "env";
    public static final String FILE_SERVER_URL = "fileServerUrl";
    public static final String URL = "url";
    public static final String GIT_BRANCH = "gitBranch";
    public static final String GIT_REPO = "gitRepo";
    public static final String JENKINS_BUILD = "jenkinsBuild";
    public static final String RUN_ORDER = "runOrder";
    public static final String PRIORITY = "priority";
    public static final String GRADLE_HOOK_URL = "gradleHookUrl";
    public static final String GRADLE_JENKINS_URL = "gradleJenkinsUrl";
    public static final String RETRY_NUM = "retryNum";

    // PB Specific keys
    public static final String PB_EXTENSION_URL = "extensionUrl";
    public static final String FEATURE_BRANCH = "featureBranch";
    public static final String PB_REST_URL = "pbRestUrl";
    public static final String IDM_REST_URL = "idmRestUrl";
    public static final String MSD_REST_URL = "msdRestUrl";
    public static final String MANAGER_URL = "managerUrl";
    public static final String MSD_LOGIN_URL = "msdLoginUrl";

    // Manager keys
    public static final String MGR_CALLPATH_URL = "mgrCallPathUrl";
    public static final String MGR_TEAM_URL = "mgrTeamUrl";
    public static final String MGR_TEAM_MEMBER_URL = "mgrTeamMemberUrl";

    // SIP URLs
    public static final String SIP_DB = "sipDb";
    public static final String SIP_TYPE = "sipType";

    // User URLs
//    public static final String USER_PRIMARY_URL_DEFAULT = "http://UserServices.ad.insidesales.com:3000/user";
    public static final String USER_PRIMARY_URL_DEFAULT = "http://aim.ad.insidesales.com:3000";
    public static final String USER_BACKUP_URL_DEFAULT = "http://aim.ad.insidesales.com:3000";
    public static final String USER_PRIMARY = "userPrimaryUrl";
    public static final String USER_BACKUP = "userBackupUrl";

    // Scenario keys
    public static final String SCENARIO_RUN_ID = "scenarioRunId";
    public static final String SCENARIO_OUTPUT_ID = "scenarioOutputId";
    public static final String SCENARIO_ID = "scenarioId";
    public static final String SCENARIO_NAME = "scenarioName";
    public static final String SCENARIO_START_URL = "scenarioStartUrl";
    public static final String SCENARIO_RESULTS_URL = "scenarioResultsUrl";
    public static final String SCENARIO_EXECUTOR_ID = "scenarioExecutorId";
    public static final String FEATURE = "feature";
    public static final String FEATURE_START_URL = "featureStartUrl";
    public static final String FEATURE_RESULTS_URL = "featureResultsUrl";
    public static final String FEATURE_RESULT = "featureResult";

    public static int sVal = 2, mVal = 10, lVal = 20, xVal = 300;
    private static Ini ini;

    // Local variables connected with the build options.
    // These are set when the system properties are being read.
    private static String operatingSystem;

    // Wait values.
    private static WebDriverWait longWait;
    private static WebDriverWait mediumWait;
    private static WebDriverWait shortWait;
    private static WebDriverWait xLongWait;

    //RootSpec Values
    private static Map testParams;
    private static Map<String, Object> allSystemProperties;

    static
    {
        allSystemProperties = new HashMap<>();
        readProperties();
    }

    // Suppress default constructor for non-instantiability
    private SystemProperties()
    {
        throw new AssertionError();
    }

    /**
     * Add default values to the system properties map.
     */
    private static void addDefaultsToMap()
    {
        // Default general values.
        allSystemProperties.put(PROPERTIES_ERROR, "false");
        allSystemProperties.put(PROPERTIES_ERROR_MSG, "");
        allSystemProperties.put(BROWSER, "googlechrome");
        allSystemProperties.put(LANGUAGE, "en");
        allSystemProperties.put(SHORT_WAIT, "2");
        allSystemProperties.put(MED_WAIT, "10");
        allSystemProperties.put(LONG_WAIT, "20");
        allSystemProperties.put(XLONG_WAIT, "300");
        allSystemProperties.put(LOAD_SALES_FORCE_PACKAGE, "false");
        allSystemProperties.put(XDISPLAY, ":1");
        allSystemProperties.put(MGR_TEAM_URL, "https://api.insidesales-playbooks.com/crm/v1/teams");
        allSystemProperties.put(FEATURE_RESULT, "PASS");

        // Default SIP and User values.
        allSystemProperties.put(SIP_DB, "jdbc:mysql://aqemysql.ad.insidesales.com:3306/sip");      // assumes DBUSERNAME and DBPASSWORD are the same for sip as for DBURL
        allSystemProperties.put(SIP_TYPE, "production");

        allSystemProperties.put(USER_PRIMARY, USER_PRIMARY_URL_DEFAULT);
        allSystemProperties.put(USER_BACKUP, USER_BACKUP_URL_DEFAULT);

        // Default DB values.
        allSystemProperties.put(DBURL, "jdbc:mysql://aqemysql.ad.insidesales.com/micro_v-4_2_0?useUnicode=true&characterEncoding=utf8");
        allSystemProperties.put(DBUSERNAME, "automation");
        allSystemProperties.put(DBPASSWORD, "Aut0m@t3");
        allSystemProperties.put(FILE_SERVER_URL, "null");
        allSystemProperties.put(SCREENSHOT_URL, "null");
        allSystemProperties.put(CHROME_DRIVER_URL, "http://nexusfiles:8081/repository/maven-releases/XANT/ai/chromedriver");

        // Add the Utils version to the map.
        allSystemProperties.put(CHROME_DRIVER_VERSION, "90");
        allSystemProperties.put(UTILS_VERSION, "2.0.63");

        //Add Nexus Auth Token
        allSystemProperties.put(NEXUS_AUTH_TOKEN, "Basic YWRtaW46QXV0MG1AdDM=");
    }

    /**
     * Add values from the ini file to the system properties map. Values could be over-written.
     */
    private static void addIniValuesToMap() throws Exception
    {
        // Get all the config section values.
        List<Section> cfgSections = ini.getAll("config");
        Set<String> iniSection = cfgSections.get(0).keySet();
        for (String optionKey : iniSection)
        {
            allSystemProperties.put(optionKey, ini.get("config", optionKey));
        }

        String subdomainSection;
        String systemSubdomain;
        if (testParams != null && !testParams.isEmpty() && Text.isSet((String) testParams.get(SUBDOMAIN_NAME)))
        {
            subdomainSection = (String) testParams.get(SUBDOMAIN_NAME);
        }
        else
        {
            // Get the system key in the config section.
            systemSubdomain = ini.get("config", SYSTEM);
            subdomainSection = Text.isSet(ini.get("config", systemSubdomain)) ? ini.get("config", systemSubdomain) : systemSubdomain;
        }

        // Get all the subdomain values.
        cfgSections = ini.getAll(subdomainSection);
        if (cfgSections == null)
        {
            log.error("The subsection: {} was not found in the properties.ini file.", subdomainSection);
            throw new Exception("The subsection: " + subdomainSection + " was not found in the properties.ini file.");
        }

        iniSection = cfgSections.get(0).keySet();
        for (String optionKey : iniSection)
        {
            allSystemProperties.put(optionKey, ini.get(subdomainSection, optionKey));
        }

        // The system parameter must be set to the redirected value if it exists. That gives
        // us the subdomain for acquiring users.
        setParam(SYSTEM, subdomainSection);

        // Set the wait internal variables.
        sVal = Text.isSet(getParam(SHORT_WAIT)) ? Integer.parseInt(getParam(SHORT_WAIT)) : 2;
        mVal = Text.isSet(getParam(MED_WAIT)) ? Integer.parseInt(getParam(MED_WAIT)) : 10;
        lVal = Text.isSet(getParam(LONG_WAIT)) ? Integer.parseInt(getParam(LONG_WAIT)) : 20;
        xVal = Text.isSet(getParam(XLONG_WAIT)) ? Integer.parseInt(getParam(XLONG_WAIT)) : 300;

        // Pod contains 'staging' or some other value. Any other value is assumed to be production.
        if (getParam(POD).equalsIgnoreCase("staging"))
        {
//            allSystemProperties.put(IDM_REST_URL, "https://idm-api.staging.insidesales.com");
            allSystemProperties.put(MGR_TEAM_MEMBER_URL, "https://teams-api-staging.insidesales.com");
            allSystemProperties.put(MGR_CALLPATH_URL, "https://call-path-manager-staging.pdlmpapis.insidesales.com");
        }
        else
        {
//            allSystemProperties.put(IDM_REST_URL, "https://idm-api.insidesales.com");
            allSystemProperties.put(MGR_TEAM_MEMBER_URL, "https://teams-api.insidesales.com");
            allSystemProperties.put(MGR_CALLPATH_URL, "https://call-path-manager-" + getParam(POD) + ".pdlmpapis.insidesales.com");
        }
    }

    /**
     * Add values from the AIM service to the system properties map. Values could be over-written.
     * This takes the highest precedence and should be called last, after addDefaultsToMap() and
     * addIniValuesToMap()
     */
    private static void addTestParamsToMap() throws Exception
    {
        Iterator it = testParams.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry pair = (Map.Entry) it.next();
            String key = (String) pair.getKey();
            if (Text.isSet(valueToStringOrEmpty(pair.getValue())))
            {
                if (!key.equals("configOverride"))
                {
                    allSystemProperties.put((String) pair.getKey(), pair.getValue());
                }
                else
                {
                    List configOverrides = (List) pair.getValue();
                    for (Object row : configOverrides)
                    {
                        Map entryPair = (Map) row;
                        boolean overrideValue = (boolean) entryPair.get("override");
                        // If the override is false we don't continue
                        if (!overrideValue)
                            continue;

                        // Otherwise continue as normal
                        String configKey = (String) entryPair.get("key");
                        String configValue = (String) entryPair.get("value");
                        allSystemProperties.put(configKey, configValue);
                    }
                }
            }
            it.remove();
        }

        // If we are at this code, there should be a login url from the database
        // passed into the test params.
        if (Text.isSet(getParam(SUBDOMAIN_URL)))
            setParam(BASEURL, getParam(SUBDOMAIN_URL));
        else if (Text.isSet(getParam(URL)))
            setParam(BASEURL, getParam(URL));

        // If the BASEURL is not set, do we throw an Exception?
        if (!Text.isSet(getParam(BASEURL)))
            throw new Exception("The BASEURL was not set.");

        // Set the chromeVersion from the DB entry into the map so it matches the docker
        // image and downloads the chrome chrome driver version.
        allSystemProperties.put(CHROME_DRIVER_VERSION, getParam("chromeVersion"));

        // Set the wait internal variables.
        sVal = Text.isSet(getParam(SHORT_WAIT)) ? Integer.valueOf(getParam(SHORT_WAIT)) : 2;
        mVal = Text.isSet(getParam(MED_WAIT)) ? Integer.valueOf(getParam(MED_WAIT)) : 10;
        lVal = Text.isSet(getParam(LONG_WAIT)) ? Integer.valueOf(getParam(LONG_WAIT)) : 20;
        xVal = Text.isSet(getParam(XLONG_WAIT)) ? Integer.valueOf(getParam(XLONG_WAIT)) : 300;
    }

    private static String valueToStringOrEmpty(Object object)
    {
        return object == null ? "" : object.toString();
    }

    /**
     * Get the value associated with the key passed into this method.
     *
     * @param key The key of the desired value.
     * @return The value associated with that key
     */
    public static String getParam(String key)
    {
        return allSystemProperties.containsKey(key) ? (String) allSystemProperties.get(key) : "";
    }

    /**
     * Get a custome parameter from the testParams, which would contain any
     * custom keys.
     *
     * @return The value of the key if found, or empty if not found.
     */
    public static Object getCustomParam(String key)
    {
        List otherValue = (List) allSystemProperties.get("other");
        for (Object row : otherValue)
        {
            Map pair = (Map) row;
            String configKey = (String) pair.get("key");
            if (configKey.equalsIgnoreCase(key))
                return pair.get("value");
        }

        log.warn("No value was found for the given key: {}", key);
        return "";
    }

    /**
     * Special accessor to return the selenium manager.
     *
     * @return {@link SeleniumManager}
     */
    public static SeleniumManager getSeleniumManager()
    {
        return (SeleniumManager) SystemProperties.allSystemProperties.get(SystemProperties.SELENIUM_MANAGER);
    }

    public static void setParam(String key, Object value)
    {
        allSystemProperties.put(key, value);
    }

    private static Ini getIni()
    {
        return ini;
    }

    public static WebDriverWait getLongWait()
    {
        return longWait;
    }

    public static WebDriverWait getMediumWait()
    {
        return mediumWait;
    }

    public static WebDriverWait getShortWait()
    {
        return shortWait;
    }

    public static WebDriverWait getxLongWait()
    {
        return xLongWait;
    }

    /**
     * Get access to all the mapped parameters.
     *
     * @return The Map containing all mapped parameters.
     */
    public static Map getMappedParams()
    {
        return allSystemProperties;
    }

    public static Map getTestParams()
    {
        return testParams;
    }

    /**
     * Is the operating system Windows.
     *
     * @return {@link Boolean}
     */
    public static boolean isWindows()
    {
        return operatingSystem.contains("windows");
    }

    /**
     * Is the operating system Mac.
     *
     * @return {@link Boolean}
     */
    public static boolean isMac()
    {
        return operatingSystem.contains("mac") || operatingSystem.contains("os x");
    }

    /**
     * Is the operating system Linux.
     *
     * @return {@link Boolean}
     */
    public static boolean isLinux()
    {
        return operatingSystem.contains("nix") || operatingSystem.contains("nux");
    }

    /**
     * Load the Properties INI file from the resource folder of the project as a temporary file.
     *
     * @return {@link Ini}
     */
    private static Ini loadPropertiesIni() throws Exception
    {
        Ini iniFile = null;
        File propertiesFile = null;
        InputStream propertiesInputStream =
            SystemProperties.class.getClassLoader().getResourceAsStream("properties.ini");
        try
        {
            // Open a temporary file.
            propertiesFile = File.createTempFile("properties", ".ini");
            propertiesFile.deleteOnExit();

            // Copy the temp file into the output stream.
            OutputStream outputStream = new FileOutputStream(propertiesFile);
            IOUtils.copy(propertiesInputStream, outputStream);
            outputStream.close();

            // Initialize the Ini file.
            iniFile = new Ini(propertiesFile);

        } catch (Exception fio)
        {
            throw new Exception("Unable to load the properties.ini files.");
        }

        return iniFile;
    }

    /**
     * Output a UTF-8 encoded string properly.
     *
     * @param args List of arguments to print.
     */
    public static void print(String args)
    {
        //  PrintStream stdout = new PrintStream(System.out, autoFlush, encoding);
        Assert.fail("Not yet implemented");
    }

    /**
     * Read the system properties, either from the command line or the properties.ini files.
     */
    private static void readProperties()
    {
        // Load the name of the operating system.
        operatingSystem = System.getProperty("os.name").toLowerCase();

        Properties osProperties = System.getProperties();
        try
        {
            // Open and load the properties.ini file.
            setIni(loadPropertiesIni());

            // Some basic default configurations.
            addDefaultsToMap();

            // The test is being run locally.
            log.info("Load INI values.");
            addIniValuesToMap();
        } catch (Exception e)
        {
            log.error("The stack trace: {}", e.getMessage());
            setParam(PROPERTIES_ERROR, "true");
            setParam(PROPERTIES_ERROR_MSG, e.getMessage());
        }
    }

    public static void setIni(Ini value)
    {
        ini = value;
    }

    public static void setLongWait(WebDriverWait value)
    {
        longWait = value;
    }

    public static void setMediumWait(WebDriverWait value)
    {
        mediumWait = value;
    }

    public static void setShortWait(WebDriverWait value)
    {
        shortWait = value;
    }

    public static void setXLongWait(WebDriverWait value)
    {
        xLongWait = value;
    }
}
