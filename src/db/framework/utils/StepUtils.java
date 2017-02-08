package db.framework.utils;

import cucumber.api.Scenario;
import cucumber.runtime.ScenarioImpl;
import db.framework.interactions.Elements;
import db.framework.interactions.Navigate;
import db.framework.interactions.Wait;
import db.framework.runner.MainRunner;
import org.junit.Assert;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import static db.framework.interactions.Elements.element;
import static db.framework.runner.MainRunner.isAlertPresent;

/**
 * This class contains page interaction and information methods to help write test steps.
 */
public abstract class StepUtils {
    protected ScenarioImpl scenario = null;

    /**
     * to track ajax check
     */
    public static boolean ajaxCheck = false;

    public static ArrayList<String> expectedURLs;

    /**
     * Checks if using chrome
     *
     * @return true if using chrome
     */
    public static boolean chrome() {
        return MainRunner.browser.equalsIgnoreCase("chrome");
    }

    /**
     * Checks if using firefox
     *
     * @return true if using firefox
     */
    public static boolean firefox() {
        return MainRunner.browser.equalsIgnoreCase("firefox");
    }

    /**
     * Checks if using Internet Explorer
     *
     * @return true if using Internet Explorer
     */
    public static boolean ie() {
        return MainRunner.browser.equalsIgnoreCase("ie");
    }

    /**
     * Checks if using safari
     *
     * @return true if using safari
     */
    public static boolean safari() {
        return MainRunner.browser.equalsIgnoreCase("safari");
    }


    /**
     * Pauses the PageHangWatchDog
     */
    public static void pausePageHangWatchDog() {
        MainRunner.PageHangWatchDog.pause(true);
    }

    /**
     * Resumes the PageHangWatchDog
     */
    public static void resumePageHangWatchDog() {
        MainRunner.PageHangWatchDog.pause(false);
    }

    /**
     * Switches to frame using frame
     *
     * @param frame either "default" for default frame or selector in format "page_name.element_name"
     */
    public static void switchToFrame(String frame) {
        try {
            if (frame.equalsIgnoreCase("default")) {
                MainRunner.getWebDriver().switchTo().defaultContent();
            } else {
                MainRunner.getWebDriver().switchTo().frame(Elements.findElement(element(frame)));
            }
        } catch (NullPointerException e) {
            System.out.println("Frame " + frame + " does not exist.");
        }
    }

    /**
     * Closes an alert if present - if no alert, nothing happens
     */
    public static void closeAlert() {
        if (isAlertPresent())
            MainRunner.closeAlert();
    }

    /**
     * Checks if browser is on a specific page by URL and element if given
     * <p>
     * Uses special page elements "url" and "verify_page" to check if currently on given page.
     * url is required. verify_page may be left out, but should be included whenever possible.
     * </p>
     *
     * @param name name of expected page
     * @return true if on page "name," otherwise false
     */
    public static boolean onPage(String name) {

        expectedURLs = Elements.getValues(name + ".url");

        String currentURL = MainRunner.getCurrentUrl();
        if (MainRunner.debugMode) {
            System.err.println("---> OnPage call: " + name + "\nfound url: " + currentURL);
        }

        String verifyElementKey = name + ".verify_page";
        List<String> verifyElement = Elements.getValues(verifyElementKey);
        for (String expectedURL : expectedURLs) {
            if (!verifyElement.isEmpty() && expectedURL != null) {
                if (Elements.elementPresent(verifyElementKey) && currentURL.contains(expectedURL)) {
                    return true;
                }
            } else if (expectedURL != null && currentURL.contains(expectedURL)) {
                return true;
            }
        }
        if (MainRunner.debugMode) {
            if (verifyElement == null) {
                System.err.println("-->Error StepUtils.onPage(): No verify_page element defined in page: " + name);
            } else if (!Elements.elementPresent(verifyElementKey)) {
                System.err.println("-->Error StepUtils.onPage(): verify_page element for page " + name + " not present");
            }

            if (expectedURLs.size() == 0) {
                System.err.println("-->Error StepUtils.onPage(): No url element defined in page: " + name);
            } else {
                expectedURLs.forEach(expectedURL -> {
                    if (!currentURL.contains(expectedURL)) {
                        System.err.println("-->Error StepUtils.onPage(): Could not match expected url: " + expectedURL);
                    }
                });
            }
        }
        return false;
    }

    /**
     * Checks if browser is on any of a list of pages
     * <p>
     * Uses special page elements "url" and "verify_page" to check if currently on any of the pages listed
     * </p>
     *
     * @param names list of pages to check for
     * @return true if on one of the listed pages
     */
    public static boolean onPage(String... names) {
        for (String name : names) {
            if (onPage(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Throws an exception if not on one of the listed pages
     * <p>
     * Uses special page elements "url" and "verify_page" to check if currently on any of the pages listed
     * </p>
     *
     * @param names names of all allowed pages
     */
    public static void shouldBeOnPage(String... names) {
        Wait.forPageReady();
        // check each allowed page - short timeout to avoid waiting forever
        for (String name : names) {
            Wait.secondsUntilElementPresent(name + ".verify_page", 10);
            if (onPage(name)) {
                return;
            }
        }

        // give the first option some more time just to be sure
        Wait.secondsUntilElementPresent(names[0] + ".verify_page", 10);
        if (onPage(names[0])) {
            return;
        }

        String pages = "";
        for (String name : names)
            pages += " " + name.replace("_", " ") + ", ";
        pages = pages.substring(0, pages.length() - 2);
        Assert.fail("ERROR - ENV: Not on pages: " + pages);
    }

    /**
     * Scrolls until a lazily loaded element is present
     *
     * @param selector String selector in format "page_name.element_name"
     */
    public static void scrollToLazyLoadElement(String selector) {
        Navigate.execJavascript("window.scrollTo(0, document.body.scrollHeight)");
        Wait.secondsUntilElementPresent(selector, 10);
    }


    /**
     * Gets the title of the current page
     *
     * @return title of the current page
     */
    public static String title() {
        return MainRunner.getWebDriver().getTitle();
    }

    /**
     * Finds the url of the current page
     *
     * @return the url of the current page
     */
    public static String url() {
        return MainRunner.currentURL;
    }

    /**
     * Stops any active loading on the page
     *
     * @return true if stop was successful
     */
    public static boolean stopPageLoad() {
        System.out.print("--> stopPageLoad(): ");
        try {
            Navigate.execJavascript("window.stop()");
            Utils.threadSleep(500, null);
            String res = Navigate.execJavascript("return document.readyState").toString();
            return res != null && res.equals("complete") && Wait.ajaxDone();
        } catch (Exception ex) {
            System.out.print(ex.getMessage());
        }
        return false;
    }

    /**
     * Closes any open jquery popups
     *
     * @return true if a popup was closed
     */
    public static boolean closeJQueryPopup() {
        if (safari()) {
            return true;
        }

        String[] texts = new String[]{"some technical issues"};
        for (String text : texts) {
            try {
                boolean res = (boolean) Navigate.execJavascript(
                        "return $('div.rc-overlay-visible').text().contains('" + text + "')"
                );
                if (res) {
                    Navigate.execJavascript(
                            "$('div.rc-overlay-visible').find('button').click()"
                    );
                }
                return true;
            } catch (Exception ex) {
                //ignore failure, will return false anyway
            }
        }
        return false;
    }

    /**
     * Captures the browser window and saves to a specified file name
     *
     * @param fileName file name to save screenshot as
     */
    public static void browserScreenCapture(String fileName) {
        File imgFile = new File(MainRunner.logs + fileName);
        try {
            File scrFile = ((TakesScreenshot) MainRunner.getWebDriver()).getScreenshotAs(OutputType.FILE);
            boolean success = scrFile.renameTo(imgFile);
            if (!success) {
                System.err.println("Failed to rename screenshot file");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                Utils.desktopCapture(new FileOutputStream(imgFile));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Cannot desktop capture.");
            }
        }
    }

    /**
     * Takes a screenshot and saves to a specified file
     *
     * @param fout file to save to
     * @throws Exception thrown if there's an error creating the screenshot
     */
    public static void desktopScreenCapture(File fout) throws Exception {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle screenRectangle = new Rectangle(screenSize);
        Robot robot = new Robot();
        BufferedImage image = robot.createScreenCapture(screenRectangle);
        ImageIO.write(image, "jpg", fout);
    }

    /**
     * Maximises the browser window
     */
    public static void maximizeWindow() {
        MainRunner.getWebDriver().manage().window().setPosition(new Point(0, 0));
    }

    /**
     * Minimizes the browser window
     */
    public static void minimizeWindow() {
        MainRunner.getWebDriver().manage().window().setPosition(new Point(-2000, 0));
    }

    //=======================================================================
    // private methods
    //=======================================================================

    /**
     * A class for creating and managing singleton scenarios and steps
     */
    public static class SingletonScenario extends Thread {
        private final static String TAG_SINGLETON = "@singleton";
        private static final long TIMEOUT_DURATION = 20 * 50 * 60 * 1000;
        private static final int PORT_SCENARIO = 9001;
        private static final int PORT_STEP = 9002;
        private static SingletonScenario m_singletonStep;
        private Scenario scenario;
        private String lockName;
        private int lockType = PORT_SCENARIO;
        private ServerSocket lockSocket;

        /**
         * Creates a singleton scenario
         *
         * @param scenario scenario to make singleton
         * @throws Exception
         */
        public SingletonScenario(Scenario scenario) throws Exception {
            this.scenario = scenario;
            this.lockName = this.scenario.getName();
            if (MainRunner.useSauceLabs || firefox() ||
                    !this.scenario.getSourceTagNames().contains(TAG_SINGLETON))
                return;

            this.start();
            this.join();
        }

        /**
         * Creates a singleton scenario
         *
         * @param stepName name of scenario to make singleton
         * @throws Exception
         */
        public SingletonScenario(String stepName) throws Exception {
            this.lockName = stepName;
            this.lockType = PORT_STEP;
            this.start();
            this.join();
        }

        /**
         * Creates a lock for a single step
         *
         * @param stepName name of step to lock
         */
        public static void createSteplock(String stepName) {
            try {
                m_singletonStep = new SingletonScenario(stepName);
            } catch (Exception e) {
                System.err.println("-->Cannot create step singleton");
            }
        }

        /**
         * Releases the current step lock
         */
        public static void releaseSteplock() {
            if (m_singletonStep != null)
                m_singletonStep.release();
        }

        /**
         * Runs the current singleton scenario
         */
        public void run() {
            long ts = System.currentTimeMillis();
            boolean running;
            while (running = (System.currentTimeMillis() - ts < TIMEOUT_DURATION)) {
                try {
                    lockSocket = new ServerSocket(lockType);
                    if (lockType == PORT_SCENARIO)
                        System.err.println("...SingletonScenario: SCENARIO locked: " + this.lockName);
                    else
                        System.err.println("...SingletonScenario: STEP locked: " + this.lockName);
                    break;
                } catch (Exception ex) {
                    //ignore
                }
                Utils.threadSleep(10 * 1000, "...SingletonScenario:waiting for lock:" + this.lockName + "...");
            }
            if (!running)
                System.err.println("-->Exhausted SingletonScenario:waiting for lock:" + Utils.toDuration(TIMEOUT_DURATION));
        }

        /**
         * Releases the current singleton scenario
         */
        public void release() {
            try {
                if (this.lockSocket != null)
                    this.lockSocket.close();
                else
                    return;

                if (this.isAlive())
                    this.interrupt();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.err.println("...SingletonScenario: lock is released: " + this.lockName);
        }
    }

    public static class DBRunnable implements Runnable {
        protected Object[] m_params;

        public DBRunnable(Object[] params) {
            this.m_params = params;
        }

        @Override
        public void run() {
        }
    }
}
