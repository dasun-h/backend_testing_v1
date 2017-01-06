package db.framework.interactions;

import db.framework.runner.MainRunner;
import db.framework.utils.StepUtils;
import db.framework.utils.Utils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import static com.macys.sdt.framework.utils.Utils.errLog;

/**
 * A collection of ways to wait for expected conditions
 */
public class Wait {

    public static boolean until(BooleanSupplier condition) {
        return until(condition, null);
    }

    public static boolean until(BooleanSupplier condition, Integer seconds) {
        try {
            WebDriverWait wait = new WebDriverWait(MainRunner.getWebDriver(), seconds != null ? seconds : 5);
            wait.until((WebDriver driver) -> condition.getAsBoolean());
            return true;
        } catch (Exception ex) {
            if (MainRunner.debugMode) {
                System.err.println("-->Error:until: " + condition + ": " + ex.getMessage());
            }
            return false;
        }
    }

    /**
     * Wait until an element is no longer present
     *
     * @param selector String selector in format "page_name.element_name"
     * @return true if element disappears/was not present
     */
    public static boolean untilElementNotPresent(String selector) {
        return untilElementNotPresent(Elements.element(selector));
    }

    /**
     * Wait until an element is no longer present
     *
     * @param selector By selector to use
     * @return true if element disappears/was not present
     */
    public static boolean untilElementNotPresent(By selector) {
        try {
            WebDriverWait wait = new WebDriverWait(MainRunner.getWebDriver(), MainRunner.timeout);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(selector));
            return true;
        } catch (Exception ex) {
            if (MainRunner.debugMode) {
                System.err.println("-->Error:untilElementNotPresent(): " + selector.toString() + ": " + ex.getMessage());
            }
            return false;
        }
    }

    /**
     * Wait until all elements are no longer present
     *
     * @param list WebElements to watch for
     * @return true if elements disappear/were not present
     */
    public static boolean untilElementNotPresent(List<WebElement> list) {
        try {
            WebDriverWait wait = new WebDriverWait(MainRunner.getWebDriver(), MainRunner.timeout);
            wait.until(ExpectedConditions.invisibilityOfAllElements(list));
            return true;
        } catch (Exception ex) {
            if (MainRunner.debugMode) {
                System.err.println("-->Error:untilElementNotPresent(): " + ex.getMessage());
            }
            return false;
        }
    }

    /**
     * Wait until an element is no longer present
     *
     * @param el WebElement to watch for
     * @return true if element disappears/was not present
     */
    public static boolean untilElementNotPresent(WebElement el) {
        ArrayList<WebElement> list = new ArrayList<>();
        list.add(el);
        return untilElementNotPresent(list);
    }

    /**
     * Wait until an element is present and click it.
     *
     * @param selector    By selector to use
     * @param waitSeconds time to wait in seconds
     */
    public static void secondsUntilElementPresentAndClick(String selector, int waitSeconds) {
        By elClick = Elements.element(selector);
        secondsUntilElementPresent(elClick, waitSeconds);
        Clicks.click(elClick);
    }

    /**
     * Wait until an element is present
     *
     * @param selector String selector in format "page_name.element_name"
     * @return true if element is present
     */
    public static boolean untilElementPresent(String selector) {
        return secondsUntilElementPresent(Elements.element(selector), 5);
    }

    /**
     * Wait until an element is present
     *
     * @param selector By selector to use
     * @return true if element is present
     */
    public static boolean untilElementPresent(By selector) {
        return secondsUntilElementPresent(selector, 3);
    }

    /**
     * Wait until an element is present
     *
     * @param selector String selector in format "page_name.element_name"
     * @param seconds  time to wait in seconds
     * @return true if element appeared/was already present
     */
    public static boolean secondsUntilElementPresent(String selector, int seconds) {
        return secondsUntilElementPresent(Elements.element(selector), seconds);
    }

    /**
     * Wait until an element is present
     *
     * @param selector By selector to use
     * @param seconds  time to wait in seconds
     * @return true if element appeared/was already present
     */
    public static boolean secondsUntilElementPresent(By selector, int seconds) {
        if (selector == null) {
            return false;
        }
        try {
            WebDriverWait wait = new WebDriverWait(MainRunner.getWebDriver(), seconds);
            wait.until(ExpectedConditions.visibilityOfElementLocated(selector));
            return true;
        } catch (Exception ex) {
            if (MainRunner.debugMode) {
                System.err.println("-->Error:secondsUntilElementPresent(): " +
                        Utils.listToString(Utils.getCallFromFunction("secondsUntilElementPresent"), "\n\t ", null) +
                        ": " + selector.toString());
            }
            errLog.println(ex.getMessage());
            return false;
        }
    }

    /**
     * Wait for an element to not be present
     *
     * @param selector String selector in format "page_name.element_name"
     * @param seconds  timeout in seconds
     * @return true if element went away/was not present
     */
    public static boolean secondsUntilElementNotPresent(String selector, int seconds) {
        return secondsUntilElementNotPresent(Elements.element(selector), seconds);
    }

    /**
     * Wait for an element to not be present
     *
     * @param selector By selector to use
     * @param seconds  timeout in seconds
     * @return true if element went away/was not present
     */
    public static boolean secondsUntilElementNotPresent(By selector, int seconds) {
        if (selector == null) {
            return false;
        }
        try {
            WebDriverWait wait = new WebDriverWait(MainRunner.getWebDriver(), seconds);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(selector));
            return true;
        } catch (Exception ex) {
            if (MainRunner.debugMode) {
                System.err.println("-->Error:secondsUntilElementNotPresent(): " + selector.toString());
            }
            errLog.println(ex.getMessage());
            return false;
        }
    }

    /**
     * Wait for an element to be present, refresh the page if it does not appear and check again
     *
     * @param selector By selector to use
     */
    public static void untilElementPresentWithRefresh(By selector) {
        try {
            for (int i = 0; i < 2; i++) {
                if (secondsUntilElementPresent(selector, 3)) {
                    return;
                }
                Navigate.browserRefresh();
            }
        } catch (Exception ex) {
            if (MainRunner.debugMode) {
                System.err.println("-->Error:untilElementPresentWithRefresh(): " + selector.toString() + ": " + ex.getMessage());
            }
        }
    }

    /**
     * Try to wait for el1 to be present. If it is not present, wait for el2 to be present.
     *
     * @param el1 first By selector to use
     * @param el2 second By selector to use
     */
    public static void untilElementPresentWithRefresh(By el1, By el2) {
        try {
            int cnt = 0;
            while (!untilElementPresent(el1) && cnt++ < 1) {
                Navigate.browserRefresh();
            }
            cnt = 0;
            while (!untilElementPresent(el2) && cnt++ < 1) {
                Navigate.browserRefresh();
            }
        } catch (Exception ex) {
            if (MainRunner.debugMode) {
                System.err.println("-->Error:untilElementPresentWithRefresh(): " + el1.toString() + ": " + el2.toString() + ": " + ex.getMessage());
            }
        }
    }

    /**
     * Wait for the first element. If it is not present, refresh browser and click the second element.
     *
     * @param waitFor By selector to use
     * @param toClick By selector to use if waitFor does not appear
     */
    public static void untilElementPresentWithRefreshAndClick(By waitFor, By toClick) {
        try {
            for (int i = 0; i < 2; i++) {
                if (secondsUntilElementPresent(waitFor, 3)) {
                    Clicks.click(toClick);
                    return;
                }
            }
        } catch (Exception ex) {
            if (MainRunner.debugMode) {
                System.err.println("-->Error:untilElementPresentWithRefreshAndClick(): " + waitFor.toString() + ": " + toClick.toString());
            }
            errLog.println(ex.getMessage());
        }
    }

    /**
     * Wait until an element's attribute has changed
     *
     * @param selector     String selector in format "page_name.element_name"
     * @param attr         attribute to use
     * @param initialValue value that should change
     */
    public static void attributeChanged(String selector, String attr, String initialValue) {
        attributeChanged(Elements.element(selector), attr, initialValue);
    }

    /**
     * Wait until an element's attribute has changed
     *
     * @param selector     By selector to use
     * @param attr         attribute to use
     * @param initialValue value that should change
     */
    public static void attributeChanged(By selector, String attr, String initialValue) {
        attributeChanged(Elements.findElement(selector), attr, initialValue);
    }

    /**
     * Wait until an element's attribute has changed
     *
     * @param element       element to use
     * @param attr          attribute to use
     * @param expectedValue value to wait for attribute to become
     */
    public static void attributeChanged(WebElement element, String attr, String expectedValue) {
        WebDriverWait wait = new WebDriverWait(MainRunner.getWebDriver(), MainRunner.timeout);

        wait.until(new ExpectedCondition<Boolean>() {
            private WebElement element;
            private String attr;
            private String expectedValue;

            private ExpectedCondition<Boolean> init(WebElement element, String attr, String expectedValue) {
                this.element = element;
                this.attr = attr;
                this.expectedValue = expectedValue;
                return this;
            }

            public Boolean apply(WebDriver driver) {
                String enabled = element.getAttribute(this.attr);
                if (MainRunner.debugMode) {
                    System.out.println("wait: init = (" + expectedValue + "), enabled = (" + enabled + ")");
                }
                return enabled.matches(this.expectedValue);
            }
        }.init(element, attr, expectedValue));
    }

    /**
     * Waits for any loading activities on the page to complete
     */
    public static void forPageReady() {
        forPageReady(null);
    }

    /**
     * Waits for any loading activities and checks if the page html contains some text
     *
     * @param pageName text to look for in page html
     * @return true if page is loaded and contains specified text
     */
    public static boolean forPageReady(final String pageName) {
        int waitTime = MainRunner.timeout;
        try {
            new WebDriverWait(MainRunner.getWebDriver(), waitTime).until((WebDriver wDriver) -> {
                if (StepUtils.safari()) {
                    Utils.threadSleep(100, null);
                }
                return animationDone() && ajaxDone() && isPageLoaded(pageName);
            });
        } catch (Exception e) {
            // IE likes to throw a lot of garbage exceptions, don't bother printing them out
            if (MainRunner.debugMode && !StepUtils.ie() && !StepUtils.safari()) {
                System.out.println("Exception in forPageReady: ");
                System.err.println(e.getMessage());
            }
            return false;
        }

        if (pageName != null) {
            By verifyElement = Elements.element(pageName + ".verify_page");
            if (verifyElement != null) {
                untilElementPresent(verifyElement);
            }
        }
        StepUtils.closeJQueryPopup();
        return true;
    }

    /**
     * Checks if any JQuery animations are currently running
     *
     * @return true if an animation is running
     */
    public static boolean animationDone() {
        if (StepUtils.safari()) {
            return true;
        }
        Object done = Navigate.execJavascript("return $(\":animated\").length == 0;");
        return done instanceof Boolean ? (Boolean) done : true;
    }

    /**
     * Checks if the page is loaded
     *
     * @param containText test to check for on page. Leave null to skip check
     * @return true if page is loaded
     */
    public static boolean isPageLoaded(String containText) {
        String ret = (String) Navigate.execJavascript("return document.readyState");
        //System.out.print("." + ret);
        boolean isReady = ret.matches("complete|loaded|interactive");
        if (containText != null)
            isReady &= getPageText().contains(containText);
        return isReady;
    }

    /**
     * Checks if all ajax calls are complete
     *
     * @return true if no active ajax calls
     */
    public static boolean ajaxDone() {
        Long queries = (Long) Navigate.execJavascript("return jQuery.active;");
        //System.out.print("." + queries + " AJAX");
        String url = MainRunner.getWebDriver().getCurrentUrl();
        return queries == 0;
    }

    /**
     * Wait for an element to appear and then disappear again (such as a loading symbol)
     *
     * @param selector By selector to use
     */
    public static void forLoading(String selector) {
        forLoading(Elements.element(selector));
    }

    /**
     * Wait for an element to appear and then disappear again (such as a loading symbol)
     *
     * @param selector By selector to use
     */
    public static void forLoading(By selector) {
        untilElementPresent(selector);
        untilElementNotPresent(selector);
    }

    private static String getPageText() {
        try {
            return Navigate.execJavascript("return document.body.textContent").toString();
        } catch (Exception ex) {
            return "";
        }
    }
}
