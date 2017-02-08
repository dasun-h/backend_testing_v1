package db.framework.runner;

import db.framework.utils.StepUtils;
import db.framework.utils.Utils;
import org.junit.Assert;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static db.framework.runner.MainRunner.*;

/**
 * Created by dasunh on 12/22/2016.
 */
class WebDriverConfigurator {
    /**
     * This method initiate specific driver with customized configurations
     *
     * @param capabilities preferred configurations in UI client
     * @return driver
     */
    static RemoteWebDriver initDriver(DesiredCapabilities capabilities) {
        if (capabilities == null) {
            capabilities = initBrowserCapabilities();
        }
        RemoteWebDriver driver;
        if (useSauceLabs) {
            driver = initSauceLabs(capabilities);

            // print the session id of saucelabs for tracking job on sauceLabs
            if (driver instanceof RemoteWebDriver) {
                System.out.println("Link to your job: https://saucelabs.com/jobs/" + driver.getSessionId());
            } else {
                System.out.println("no RemoteWebDriver instance : " + driver);
            }
        } else {
            driver = initBrowser(capabilities);
        }
        Assert.assertNotNull("Driver should have been initialized by now", driver);

        if (!remoteOS.equals("Linux")) {
            RemoteWebDriver.Timeouts to = driver.manage().timeouts();
            to.pageLoadTimeout(timeout, TimeUnit.SECONDS);
            to.setScriptTimeout(timeout, TimeUnit.SECONDS);
        }
        return driver;
    }

    /*
 * initiate browser driver with given capabilities based on browser asked
 *
 * @param capabilities preferred configurations for browser driver
 * @return instance of browser driver with preferred capabilities
 */
    private static RemoteWebDriver initBrowser(DesiredCapabilities capabilities) {
        RemoteWebDriver driver = null;
        switch (MainRunner.browser.toLowerCase()) {
            case "ie":
            case "internetexplorer":
                return new InternetExplorerDriver(capabilities);
            case "chrome":
                return new ChromeDriver(capabilities);
            case "safari":
                int count = 0;
                while (driver == null && count++ < 3)
                    try {
                        driver = new SafariDriver(capabilities);
                    } catch (Exception e) {
                        System.err.println("Failed to open safari driver: " + e);
                        System.err.println("Retrying: " + count);
                        Utils.threadSleep(5000, null);
                    }
                return driver;
            case "edge":
                return new EdgeDriver(capabilities);
            default:
                try {
                    return new FirefoxDriver(capabilities);
                } catch (IllegalStateException e) {
                    capabilities.setCapability("marionette", false);
                    return new FirefoxDriver(capabilities);
                }
        }
    }

    /*
 * This method set up capabilities based on browser asked mainly for desktop execution
 *
 * @return desiredCapabilities customized configurations as per browser
 */
    private static DesiredCapabilities initBrowserCapabilities() {
        DesiredCapabilities capabilities;
        switch (MainRunner.browser.toLowerCase()) {
            case "ie":
            case "internetexplorer":
                capabilities = DesiredCapabilities.internetExplorer();
                String path = "src/db/framework/selenium_drivers/IEDriverServer.exe";
                File file = new File(path);
                if (file.exists()) {
                    System.setProperty("webdriver.ie.driver", file.getAbsolutePath());
                } else {
                    System.out.println("Unable to use built-in IE driver, will use machine's IE driver if it exists");
                }
                capabilities.setCapability(InternetExplorerDriver.INITIAL_BROWSER_URL, true);
                capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
                capabilities.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, false);
                // changing requireWindowFocus to default value 'false' to avoid window or
                // page freeze issue when the focus is not on the window
                capabilities.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, false);
                capabilities.setCapability(InternetExplorerDriver.NATIVE_EVENTS, false);
                return capabilities;
            case "chrome":
                capabilities = DesiredCapabilities.chrome();
                setChromeDriverLocation();
                ChromeOptions chrome = new ChromeOptions();
                chrome.addArguments("test-type");
                chrome.addArguments("--disable-extensions");
                capabilities.setCapability(ChromeOptions.CAPABILITY, chrome);
                capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
                return capabilities;
            case "safari":
                capabilities = DesiredCapabilities.safari();
                capabilities.setCapability("unexpectedAlertBehaviour", "accept");
                return capabilities;
            case "edge":
                System.err.println("WARNING: Microsoft's Edge Driver is not fully implemented yet. There may" +
                        " be strange or unexpected errors.");
                capabilities = DesiredCapabilities.edge();
                return capabilities;
            default:
                capabilities = DesiredCapabilities.firefox();
                FirefoxProfile firefoxProfile = new FirefoxProfile();
                firefoxProfile.setAcceptUntrustedCertificates(true);
                firefoxProfile.setAssumeUntrustedCertificateIssuer(false);
                firefoxProfile.setEnableNativeEvents(true);

                firefoxProfile.setPreference("browser.download.folderList", 2);
                firefoxProfile.setPreference("browser.download.manager.alertOnEXEOpen", false);
                firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/msword, application/csv, application/ris, text/csv, image/png, application/pdf, text/html, text/plain, application/zip, application/x-zip, application/x-zip-compressed, application/download, application/octet-stream");
                firefoxProfile.setPreference("browser.download.manager.showWhenStarting", false);
                firefoxProfile.setPreference("browser.download.manager.focusWhenStarting", false);
                firefoxProfile.setPreference("browser.download.useDownloadDir", true);
                firefoxProfile.setPreference("browser.helperApps.alwaysAsk.force", false);
                firefoxProfile.setPreference("browser.download.manager.alertOnEXEOpen", false);
                firefoxProfile.setPreference("browser.download.manager.closeWhenDone", true);
                firefoxProfile.setPreference("browser.download.manager.showAlertOnComplete", false);
                firefoxProfile.setPreference("browser.download.manager.useWindow", false);
                firefoxProfile.setPreference("services.sync.prefs.sync.browser.download.manager.showWhenStarting", false);
                firefoxProfile.setPreference("pdfjs.disabled", true);
                ArrayList<File> extensions = new ArrayList<>();
                for (File f : extensions) {
                    try {
                        firefoxProfile.addExtension(f);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Assert.fail("Cannot load extension");
                    }
                }
                capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
                capabilities.setCapability(FirefoxDriver.PROFILE, firefoxProfile);
                capabilities.setCapability("marionette", false);
                return capabilities;
        }
    }

    /*
 * This method set chromeDriver from the repo in the running machine for execution
 */
    private static void setChromeDriverLocation() {
        if (StepUtils.chrome()) {
            String fileName = "chromedriver2_25.exe";
            String path = "src/db/framework/selenium_drivers/" + fileName;
            File file = new File(MainRunner.workspace + path);
            if (file.exists()) {
                System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());
            } else {
                System.out.println("Unable to use built-in chrome driver, will use machine's chrome driver if it exists");
            }
        }
    }

    private static RemoteWebDriver initSauceLabs(DesiredCapabilities capabilities) {
        try {
            // remove quoted chars
            remoteOS = remoteOS.replace("\"", "");
            remoteOS = remoteOS.replace("'", "");
            capabilities.setCapability("platform", remoteOS);
            capabilities.setCapability("version", browserVersion);
            capabilities.setCapability("idleTimeout", 240);
            capabilities.setCapability("maxDuration", 3600);
            // need to increase res or we get tablet layout
            // not supported on win10 and mac OSX El Capitan (10.11)
            if (!remoteOS.matches("^Windows 10|(.*?)10.11$")) {
                capabilities.setCapability("screenResolution", "1280x1024");
            }
            if (browser.equals("safari")) {
                // safari driver is not stable, retry 3 times
                int count = 0;
                while (count++ < 3)
                    try {
                        return new RemoteWebDriver(new URL("http://" + sauceUser + ":" + sauceKey + "@ondemand.saucelabs.com:80/wd/hub"), capabilities);
                    } catch (Error | Exception e) {
                        Utils.threadSleep(5000, null);
                    }
            } else
                return new RemoteWebDriver(new URL("http://" + sauceUser + ":" + sauceKey + "@ondemand.saucelabs.com:80/wd/hub"), capabilities);

        } catch (Exception e) {
            System.err.println("Could not create remove web driver: " + e);
        }
        return null;
    }

    /**
     * This method sets default browser version based on browser asked
     *
     * @return default version of browser asked
     */
    static String defaultBrowserVersion() {
        switch (browser) {
            case "ie":
                return "11.0";
            case "edge":
                return "25.10586";
            case "safari":
                String version;
                if (remoteOS == null) {
                    version = "9.0";
                } else if (remoteOS.contains("10.12")) {
                    version = "10.0";
                } else if (remoteOS.contains("10.11")) {
                    version = "9.0";
                } else if (remoteOS.contains("10.10")) {
                    version = "8.0";
                } else if (remoteOS.contains("10.9")) {
                    version = "7.0";
                } else if (remoteOS.contains("10.8")) {
                    version = "6.0";
                } else {
                    version = "0";
                }
                return version;
            case "chrome":
            default:
                return "54.0";
        }
    }
}
