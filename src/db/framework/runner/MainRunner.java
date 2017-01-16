package db.framework.runner;

import com.github.mkolisnyk.cucumber.runner.ExtendedCucumber;
import com.github.mkolisnyk.cucumber.runner.ExtendedCucumberOptions;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import cucumber.api.CucumberOptions;
import cucumber.api.testng.AbstractTestNGCucumberTests;
import cucumber.api.testng.TestNGCucumberRunner;
import db.framework.interactions.Navigate;
import db.framework.utils.StepUtils;
import db.framework.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

import static db.framework.utils.EnvironmentVariableRetriever.*;
import static db.framework.utils.StepUtils.*;
import static java.lang.Runtime.getRuntime;

/**
 * This class handles the configuration and running of cucumber scenarios and features
 */
@RunWith(ExtendedCucumber.class)
@ExtendedCucumberOptions(
        jsonReport = "target/cucumber.json",
        retryCount = 1,
        detailedReport = true,
        detailedAggregatedReport = true,
        overviewReport = true,
        toPDF = true,
        outputFolder = "target"
)
@CucumberOptions(
        features = {"src/db/projects/sample/features/"},
        plugin = {"pretty", "html:target/site/cucumber-pretty",
                "json:target/cucumber.json", "pretty:target/cucumber-pretty.txt",
                "usage:target/cucumber-usage.json", "junit:target/cucumber-results.xml"},
        glue = {"db.shared.steps"},
        tags = {"@scenario1"}
)
public class MainRunner extends AbstractTestNGCucumberTests {

    /**
     * Contains OS to use when executing on saucelabs as given in "remote_os" env variable
     * <p>
     * Options: windows 7|8|8.1|10, OSX 10.10|10.11
     * </p>
     */
    public static String remoteOS;

    /**
     * Workspace path as given in "WORKSPACE" env variable
     */
    public static String workspace;

    /**
     * Path to logging folder
     */
    public static String logs;

    /**
     * Path to "temp" directory
     */
    public static String temp;

    /**
     * Map of current cucumber features
     */
    public static HashMap<String, Map> features = new HashMap<>();

    /**
     * Path to feature file to execute from
     */
    public static String scenarios = getEnvVar("scenarios") != null ? getEnvVar("scenarios") : SCENARIOS;

    /**
     * Whether we're running in debug mode
     */
    public static boolean debugMode = booleanParam("debug");

    /**
     * Will be true if executing through saucelabs. Checks for valid saucelabs URL in "saucelabs" env variable
     */

    public static boolean useSauceLabs;

    /**
     * The Sauce Labs username to use
     */
    public static String sauceUser = getEnvOrExParam("sauce_user") != null ? getEnvOrExParam("sauce_user") : SAUCE_USER;
    /**
     * The Sauce Labs API key for the user
     */
    public static String sauceKey = getEnvOrExParam("sauce_key") != null ? getEnvOrExParam("sauce_key") : SAUCE_KEY;

    /**
     * Path to active project files on file system
     */
    public static String projectDir = null;

    /**
     * Browser to use as given in "browser" env variable. Default firefox.
     */
    public static String browser = getEnvVar("browser") != null ? getEnvVar("browser") : BROWSER;

    /**
     * Version of browser to use as given in "browser_version" env variable
     */
    public static String browserVersion = getEnvOrExParam("browser_version");

    /**
     * Whether to close browser after testing is complete. False if "DEBUG" env variable is present
     */
    public static Boolean closeBrowserAtExit = true;


    /**
     * URL to start at and use as a base as given in "website" env variable
     */
    public static String url = getEnvVar("website") != null ? getEnvVar("website") : WEBSITE;


    /**
     * Current run status. 0 is good, anything else is bad
     */
    public static int runStatus = 0;

    /**
     * Time the tests were started
     */
    public static long startTime = System.currentTimeMillis();

    /**
     * Wait timeout as given in "timeout" env variable. Default 30 seconds
     */
    public static int timeout; // set the general default timeout to 30 seconds

    /**
     * List containing URL's that have been visited
     */
    public static ArrayList<String> URLStack = new ArrayList<>();


    /**
     * The current URL
     */
    public static String currentURL = "";

    private static WebDriver driver = null;
    private static long ieAuthenticationTs = System.currentTimeMillis() - 10000; // set authentication checking interval out of range

    /**
     * Main method to run tests
     *
     * @param argv run args. Ignored, use environment variables for all config
     * @throws Throwable if an exception or error gets here, we're done
     */
    public static void main(String[] argv) throws Throwable {
        getEnvVars();

        ArrayList<String> featureScenarios = getFeatureScenarios();
        if (featureScenarios == null) {
            throw new Exception("Error getting scenarios");
        }

        // add any tags
        String tags = getEnvOrExParam("tags");
        if (tags != null) {
            featureScenarios.add("--tags");
            featureScenarios.add(tags);
        }

        // attempt to use workspace as relative path to feature file (if needed)
        if (workspace != null) {
            for (int i = 0; i < featureScenarios.size(); i++) {
                String value = featureScenarios.get(i);
                if (value.equals("--tags")) {
                    break;
                }
                // remove windows drive to avoid incorrect matches on ":"
                String drive = "";
                if (value.matches("[A-Z]:.*?")) {
                    drive = value.substring(0, 2);
                    value = value.substring(2);
                }
                // remove any line number args
                value = value.split(":")[0];
                value = drive + value;
                // make sure file exists
                File featureFile = new File(value);
                if (!(featureFile.exists() || featureFile.getAbsoluteFile().exists())) {
                    featureScenarios.set(i, workspace + "/" + featureScenarios.get(i));
                }
            }
        }

        featureScenarios.add("--glue");
        featureScenarios.add("db.shared.steps");
        featureScenarios.add("--plugin");
        featureScenarios.add("db.framework.utils.DBFormatter");
        featureScenarios.add("--plugin");
        featureScenarios.add("html:logs");

        System.out.println("Browser Version:" + browserVersion);
        driver = getWebDriver();

        PageHangWatchDog.init();

        try {
            runStatus = cucumber.api.cli.Main.run(featureScenarios.toArray(new String[featureScenarios.size()]),
                    Thread.currentThread().getContextClassLoader());
        } catch (Throwable e) {
            e.printStackTrace();
            runStatus = 1;
        } finally {
            close();
            if (argv != null) {
                System.exit(runStatus);
            }
        }
    }

    public static void getEnvVars() {
        if (workspace == null) {
            workspace = ".";
        }
        workspace = workspace.replace('\\', '/');
        workspace = workspace.endsWith("/") ? workspace : workspace + "/";

        scenarios = scenarios.replace('\\', '/');

        Utils.createDirectory(logs = workspace + "logs/", true);
        Utils.createDirectory(temp = workspace + "temp/", true);

        if ((remoteOS == null || remoteOS.isEmpty()) && (OPERATING_SYSTEM == null || OPERATING_SYSTEM.isEmpty())) {
            System.out.println("Remote OS not specified. Using default (Windows 7)");
            remoteOS = "Windows 7";
        } else {
            remoteOS = OPERATING_SYSTEM;
            System.out.println("OS Selected: " + remoteOS);
        }

        // use saucelabs when valid "sauce_user" and "sauce_key" is provided
        useSauceLabs = (!sauceUser.isEmpty() && !sauceKey.isEmpty());
        if (useSauceLabs == true) {
            System.out.println("SauceLab Username: " + sauceUser);
            System.out.println("SauceLab Username: " + sauceUser);
        } else
            System.out.println("SauceLab not specified in the environment variables file");

        // close browser at exist unless debugMode is on
        closeBrowserAtExit = !debugMode;

        if ((url == null || url.isEmpty()) && (WEBSITE == null || WEBSITE.isEmpty())) {
            Assert.fail("\"website\" variable required to test a website");
        } else {
            url = WEBSITE;
            System.out.println("Website Given: " + url);
        }
        if ((browser == null || browser.isEmpty()) && (BROWSER == null || BROWSER.isEmpty())) {
            System.out.println("No browser given, using default (chrome)");
            browser = "chrome";
        } else {
            browser = BROWSER;
            System.out.println("Browser Given: " + BROWSER);
        }
        if (browserVersion == null) {
            browserVersion = WebDriverConfigurator.defaultBrowserVersion();
        }
        // close the test browser at scenario exit
        String envVal = getEnvOrExParam("timeout");
        if (envVal != null) {
            timeout = Integer.parseInt(envVal);
        } else {
            timeout = safari() ? 130 : 95;
        }

    }

    /**
     * Gets whether or not debug mode is on
     *
     * @return true if debug mode is on
     */
    public static boolean isDebug() {
        String debug = getExParam("DEBUG");
        return debug != null && debug.matches("t|true");
    }

    /**
     * Resets the driver
     *
     * @param quit whether to close the driver
     */
    public static void resetDriver(boolean quit) {
        try {
            if (quit) {
                driver.quit();
                System.out.println("driver quit");
                if (ie()) { // workaround for IE browser closing
                    driver.quit();
                }
            }
            driver = null;
            System.out.println("driver set to null");
        } catch (Exception e) {
            System.err.println("error in resetDriver : " + e.getMessage());
            driver = null;
            System.out.println("driver set to null in catch");
        }
    }

    /**
     * Checks if the web driver exists
     *
     * @return true if a valid web driver is active
     */
    public static Boolean driverInitialized() {
        return driver != null;
    }

    /**
     * Gets the current webDriver instance or tries to create one
     *
     * @return current webDriver instance
     */
    public static synchronized WebDriver getWebDriver() {
        if (URLStack.size() == 0) {
            URLStack.add(url);
        }
        if (driver != null) {
            if (!URLStack.get(URLStack.size() - 1).equals(currentURL)) {
                URLStack.add(currentURL);
            }
            return driver;
        }

        for (int i = 0; i < 2; i++) {
            driver = WebDriverConfigurator.initDriver(null);
            try {
                if (browser.equals("safari")) {
                    Dimension dimension = new Dimension(1280, 1024);
                    driver.manage().window().setSize(dimension);
                } else {
                    driver.manage().window().maximize();
                }
                String window_size = driver.manage().window().getSize().toString();
                System.out.println("Init driver: browser window size = " + window_size);
                return driver;
            } catch (Exception ex) {
                System.err.println("-->Failed initialized driver:retry" + i + ":" + ex.getMessage());
                Utils.threadSleep(2000, null);
            }
        }
        System.err.println("Cannot initialize driver: exiting test...");
        System.out.println("Quit the driver " + driver);
        if (driver != null) {
            driverQuit();
        }
        System.exit(-1);
        // return is unreachable but IDE doesn't realize, return non-null
        // to get rid of invalid lint errors
        return new ChromeDriver();
    }

    /**
     * Retrieves a parameter value from "ex_params" environment variable
     *
     * @param name name of the parameter to retrieve
     * @return value of parameter or null if not found
     */
    public static String getExParam(String name) {
        try {
            String exParams = URLDecoder.decode(System.getenv("ex_params"), "utf-8");
            if (exParams != null && !exParams.isEmpty()) {
                StringBuilder sb = new StringBuilder(exParams);
                for (int i = 0, quoteIndex = -1; i < sb.length(); i++) {
                    char c = sb.charAt(i);
                    if (c == '\"' && quoteIndex == -1) {
                        quoteIndex = i;
                    }
                    if (quoteIndex > -1) {
                        for (i = i + 1; i < sb.length(); i++) {
                            c = sb.charAt(i);
                            if (c == '\"') {
                                quoteIndex = -1;
                                break;
                            }
                            if (c == ' ') {
                                sb.setCharAt(i, '|');
                            }
                        }
                    }
                }
                exParams = sb.toString();
                String[] paramList = exParams.split(" ");
                for (String param : paramList) {
                    if (param.startsWith(name)) {
                        return param.split("=")[1].trim().replace('|', ' ').replace("\"", "");
                    }
                }
            }
        } catch (Exception ex) {
            // variable not found or malformed
        }
        return null;
    }

    /**
     * Retrieves an environment variable OR ex_param
     *
     * @param name name of parameter to retrieve
     * @return value of parameter or null if not found
     */
    public static String getEnvOrExParam(String name) {
        String val = getEnvVar(name);
        return val != null ? val : getExParam(name);
    }

    /**
     * Retrieves an environment variable
     *
     * @param name name of parameter to retrieve
     * @return value of parameter or null if not found
     */
    public static String getEnvVar(String name) {
        String value = System.getenv(name);
        value = value == null ? null : value.trim();
        if (value != null && !value.isEmpty()) {
            return value;
        }
        return null;
    }

    /**
     * Matches an ex_param against t|true and converts it to a boolean
     *
     * @param name name of parameter
     * @return true if parameter exists and matches "t|true"
     */
    public static boolean booleanParam(String name) {
        String param = getEnvOrExParam(name);
        return param != null && param.matches("t|true");
    }

    /**
     * Gets a list of all scenarios to be run
     *
     * @return the list of scenarios
     */
    public static ArrayList<String> getFeatureScenarios() {
        ArrayList<String> scenarioList = new ArrayList<>();
        if (scenarios == null)
            return scenarioList;
        scenarios = scenarios.trim();
        System.out.println("Scenario Path Selected: " + scenarios);
        String delimit = ".feature";
        int i = 0, end = scenarios.indexOf(delimit);
        while (i < scenarios.length()) {
            end = scenarios.indexOf(' ', end + 1);
            if (end == -1)
                end = scenarios.length();
            String scenarioPath = scenarios.substring(i, end).trim();
            scenarioList.add(scenarioPath);
            i = end;
        }

        Collections.sort(scenarioList);
        ArrayList<Map> featureScenarios = null;
        String workSpace = getExParam("WORKSPACE");
        if (workSpace == null)
            workSpace = "";
        for (String featureFilePath : scenarioList) {
            String[] featureInfo = featureFilePath.split(".feature:");
            String path = featureInfo[0];
            if (!path.endsWith(".feature"))
                path += ".feature";
            int line = 0;
            if (featureInfo.length == 2)
                line = Utils.parseInt(featureInfo[1], 0);
            if (!path.equals("")) {
                File featureFile = new File(path);
                if (!(featureFile.exists() || featureFile.getAbsoluteFile().exists())) {
                    System.out.println("File not found: " + path);
                    path = workSpace + "/" + path;
                }
                String json = Utils.gherkinToJson(false, path);
                try {
                    featureScenarios = new Gson().fromJson(json, ArrayList.class);
                } catch (JsonSyntaxException jex) {
                    System.err.println("--> Failed to parse : " + path);
                    System.err.println("--> json :\n\n" + json);
                    System.err.println();
                    throw jex;
                }
            }
            findScenario(featureScenarios, path, line);
        }

        // condense any duplicate feature files
        HashMap<String, ArrayList<String>> featureLines = new HashMap<>();
        // remove windows drive to avoid incorrect matches on ":"
        final String drive = scenarioList.get(0).matches("[A-Z]:.*?") ? scenarioList.get(0).substring(0, 2) : null;
        if (drive != null) {
            for (i = 0; i < scenarioList.size(); i++) {
                String scenario = scenarioList.remove(i);
                scenarioList.add(i, scenario.substring(2));
            }
        }
        for (String scenario : scenarioList) {
            int lineIndex = scenario.lastIndexOf(':');
            if (lineIndex == -1) {
                continue;
            }
            String scenarioPath = scenario.substring(0, lineIndex).trim();
            String lineNum = scenario.substring(lineIndex + 1);
            ArrayList<String> lines = featureLines.get(scenarioPath);
            if (lines == null) {
                lines = new ArrayList<>();
                featureLines.put(scenarioPath, lines);
            }
            lines.add(lineNum);
        }

        scenarioList.removeAll(scenarioList.stream()
                .filter(str -> str.contains(":"))
                .collect(Collectors.toList()));

        scenarioList.addAll(featureLines.keySet().stream()
                .map((key) -> key + ":" + StringUtils.join(featureLines.get(key), ":"))
                .collect(Collectors.toList()));
        if (drive != null) {
            for (i = 0; i < scenarioList.size(); i++) {
                String scenario = scenarioList.remove(i);
                scenarioList.add(i, drive + scenario);
            }
        }
        return scenarioList;
    }

    /**
     * Closes an alert if present
     */

    public static boolean isAlertPresent(){
        boolean foundAlert;
        WebDriverWait wait = new WebDriverWait(driver, 10);
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            foundAlert = true;
            driver.switchTo().defaultContent();
        } catch (TimeoutException eTO) {
            foundAlert = false;
        }
        return foundAlert;
    }

    public static void closeAlert() {
        if (driver != null) {
            driver.switchTo().alert().accept();
            Assert.assertFalse("Alert is still present", isAlertPresent());
        }
    }

    @Test(groups = "backend_testing", description = "Example of using TestNGCucumberRunner to invoke Cucumber")
    public void runCukes() {
        new TestNGCucumberRunner(getClass()).runCukes();
    }


    @AfterMethod
    public void takeScreenShotOnFailure(ITestResult testResult) throws IOException {
        if (testResult.getStatus() == ITestResult.FAILURE) {
            System.out.println(testResult.getStatus());
            File scrFile = ((TakesScreenshot) getWebDriver()).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(scrFile, new File("target/img_cucumber_jvm.jpg"));
        }
    }

    private static boolean findScenario(ArrayList<Map> featureScenarios, String scenarioPath, int line) {
        HashMap<Integer, Map> hscenario = new HashMap<>();
        for (Map scenario : featureScenarios) {
            ArrayList<Map> elements = (ArrayList<Map>) scenario.get("elements");
            for (Map element : elements) {
                element.put("uri", scenario.get("uri"));
                int l = Utils.parseInt(element.get("line"), 0);
                if (line == 0 || line == l) {
                    features.put(scenarioPath + ":" + l, element);
                    if (line == 0)
                        continue;
                    return true;
                }
                hscenario.put(l, element);
            }
        }
        int closest = 0;
        for (Integer l : hscenario.keySet()) {
            int dist = Math.abs(line - l);
            if (dist < line - closest)
                closest = l;
        }
        if (closest > 0) {
            features.put(scenarioPath + ":" + line, hscenario.get(closest));
            System.out.println("Load closest scenario with line:" + closest);
        }

        return false;
    }

    private static void close() {
        if (browser.equals("none"))
            return;
        if (useSauceLabs) {
            if (driver instanceof RemoteWebDriver) {
                System.out.println("Link to your job: https://saucelabs.com/jobs/" + ((RemoteWebDriver) driver).getSessionId());
            }
            driverQuit();
        } else if (closeBrowserAtExit) {
            System.out.println("Closing driver...");
            if (driver != null) {
                driverQuit();
            }
        }
    }

    private static void driverQuit() {
        try {
            driver.quit();
            if (ie()) {
                try {
                    driver.quit();
                } catch (Exception | Error e) {
                    // nothing we can do if this doesn't work
                }
            }
        } catch (Exception e) {
            // skip error message on saucelab remote driver
            if (!useSauceLabs) {
                System.err.println("Error closing driver. You may need to clean up execution machine. error: " + e);
            }
        }
        driver = null;
    }

    /**
     * Get the current URL from browser and/or URL param
     *
     * @return the current url the browser is on
     */
    public static String getCurrentUrl() {
        if (!driverInitialized()) {
            return url;
        }
        String curUrl = driver.getCurrentUrl();
        if (curUrl.matches(".*?data.*?")) {
            return url;
        }
        currentURL = curUrl;
        return curUrl;
    }

    /**
     * Get the current URL from browser and/or URL param
     */
    private static String getInternalCurrentUrl() {
        if (StepUtils.ie()) {
            // IE windows authentication popup disappears when driver.getCurrentUrl() executed
            // so need to hook the function and wait for 10 seconds to look for the IE window authentication popup
            // and repeat every 1 hour
            long cs = System.currentTimeMillis();
            // check first 10 seconds only
            if (cs - ieAuthenticationTs < 10000) {
                if (browser.equals("ie") && booleanParam("require_authentication")) {
                    // check IE window authentication popup
                    int exitValue = runIEMethod();
                    // IE authentication popup login successfully, no more checking for an hour
                    if (exitValue == 0) {
                        ieAuthenticationTs -= 10000;
                    }
                }
            } else {
                // after that check every hour
                if (cs - ieAuthenticationTs > 3600000) {
                    ieAuthenticationTs = cs;
                }
            }
        }
        return getCurrentUrl();
    }

    /**
     * Initialize IE authentication
     */
    public static void authenticationIeInit() {
        ieAuthenticationTs = System.currentTimeMillis();
    }

    public static int runIEMethod() {
        Process p;
        String file_path = "src/db/framework/authentication_popup/windows_authentication_ie.exe";
        if (!new File("src").exists())
            file_path = "db/framework/authentication_popup/windows_authentication_ie.exe";

        try {
            p = getRuntime().exec(file_path);
            p.waitFor();  // wait for process to complete
            return (p.exitValue());
        } catch (Exception e) {
            // ignore all errors
        }
        return 1;
    }

    public static int runChromeMethod() {
        Process p;
        String file_path = "src/db/framework/authentication_popup/windows_authentication_chrome.exe";
        if (!new File("src").exists())
            file_path = "db/framework/authentication_popup/windows_authentication_chrome.exe";

        try {
            p = getRuntime().exec(file_path);
            p.waitFor();  // wait for process to complete
            return (p.exitValue());
        } catch (Exception e) {
            // ignore all errors
        }
        return 1;
    }

    public static class PageHangWatchDog extends Thread {
        private final static long TIMEOUT = (StepUtils.safari() || StepUtils.ie() ? 130 : 95) * 1000;
        private final static int MAX_FAILURES = 5;
        public static boolean timedOut = false;
        private static PageHangWatchDog hangWatchDog;
        private static int failCount;
        private static boolean pause;
        private String m_url;
        private long ts;

        private PageHangWatchDog() {
            System.err.println("--> Start:PageHangWatchDog:" + new Date());
            this.reset(getWebDriver().getCurrentUrl());
            this.start();
        }

        public static void init() {
            if (hangWatchDog == null) {
                hangWatchDog = new PageHangWatchDog();
            }
        }

        public static void resetWatchDog() {
            hangWatchDog.reset(null);
        }

        public static void pause(boolean pause) {
            PageHangWatchDog.pause = pause;
            if (!pause) {
                timedOut = false;
                failCount = 0;
            }
        }

        private void reset(String url) {
            this.ts = System.currentTimeMillis();
            if (url != null) {
                this.m_url = url;
                failCount = 0;
            }
        }

        public void run() {
            while (true) {
                try {
                    if (pause || timedOut || !driverInitialized()) {
                        continue;
                    }
                    String url = currentURL;
                    if (url.contains("about:blank")) {
                        continue;
                    }
                    if (url.equals(this.m_url)) {
                        if (System.currentTimeMillis() - this.ts > TIMEOUT) {
                            System.err.println("--> PageHangWatchDog: timeout at " + this.m_url +
                                    ", " + (MAX_FAILURES - failCount) + " failures until exit");
                            failCount++;
                            new Thread(() -> {
                                try {
                                    stopPageLoad();
                                } catch (Exception e) {
                                    // sometimes IE fails to run js. Continue running.
                                } finally {
                                    if (browser.equalsIgnoreCase("ie")) {
                                        Navigate.browserRefresh();
                                    }
                                }
                            }).start();

                            this.reset(null);
                            if (failCount > MAX_FAILURES) {
                                timedOut = true;
                                System.err.println("PageHangWatchDog timeout! Test will fail after this step ends");
                            }
                        }
                    } else {
                        this.reset(url);
                    }
                } catch (Throwable ex) {
                    System.err.println("--> Error:PageHangWatchDog:" + ex.getMessage());
                    ex.printStackTrace();
                } finally {
                    Utils.threadSleep(5000, this.getClass().getSimpleName());
                }
            }
        }
    }
}
