package db.framework.utils;

import com.google.gson.internal.LinkedTreeMap;
import cucumber.api.Scenario;
import cucumber.runtime.ScenarioImpl;
import db.framework.runner.MainRunner;
import gherkin.formatter.model.Result;
import net.lightbody.bmp.core.har.Har;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.Point;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class contains page interaction and information methods to help write test steps.
 */
public abstract class StepUtils {
    private static Runnable beforeNavigate;
    private static Runnable afterNavigate;
    protected ScenarioImpl scenario = null;

    /**
     * Sets a method (or lambda) to run before any clicks or other navigation
     *
     * @param toRun method or lambda to run
     */
    public static void setBeforeNavigation(Runnable toRun) {
        beforeNavigate = toRun;
    }

    /**
     * Sets a method (or lambda) to run after any clicks or other navigation
     *
     * @param toRun method or lambda to run
     */
    public static void setAfterNavigation(Runnable toRun) {
        afterNavigate = toRun;
    }

    /**
     * Removes any saved before or after navigation methods
     */
    public static void clear_navigation_methods() {
        beforeNavigate = null;
        afterNavigate = null;
    }

    /**
     * Checks if using chrome
     *
     * @return true if using chrome
     */
    public static boolean chrome() {
        return MainRunner.browser.equals("chrome");
    }

    /**
     * Checks if using firefox
     *
     * @return true if using firefox
     */
    public static boolean firefox() {
        return MainRunner.browser.equals("firefox");
    }

    /**
     * Checks if using Internet Explorer
     *
     * @return true if using Internet Explorer
     */
    public static boolean ie() {
        return MainRunner.browser.equals("ie");
    }

    /**
     * Checks if using safari
     *
     * @return true if using safari
     */
    public static boolean safari() {
        return MainRunner.browser.equals("safari");
    }

    /**
     * Navigates the browser back a page
     */
    public static void browserBack() {
        if (safari()) {
            try {
                int urlStackSize = MainRunner.URLStack.size();
                if (urlStackSize <= 1)
                    visit("home");
                else
                    MainRunner.getWebDriver().get(MainRunner.URLStack.get(urlStackSize - 2));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            MainRunner.getWebDriver().navigate().back();
        }
        waitForPageReady();
    }

    /**
     * Refreshes the current browser page
     */
    public static void browserRefresh() {
        if (safari()) {
            StepUtils.execJavascript("document.location.reload()");
        } else {
            MainRunner.getWebDriver().navigate().refresh();
        }
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
                MainRunner.getWebDriver().switchTo().frame(findElement(element(frame)));
            }
        } catch (NullPointerException e) {
            System.out.println("Frame " + frame + " does not exist.");
        }
    }

    /**
     * Closes a firefox alert if present
     */
    public static void closeFFAlert() {
        if (firefox())
            MainRunner.closeAlert();
    }

    //=======================================================================
    // UI steps
    //=======================================================================

    /**
     * Types text into a text box
     *
     * @param selector String selector in format "page_name.element_name"
     * @param text     text to type in
     */
    public static void typeTextbox(String selector, String text) {
        typeTextbox(element(selector), text);
    }

    /**
     * Types text into a text box
     *
     * @param selector By selector to use
     * @param text     text to type in
     */
    public static void typeTextbox(By selector, String text) {
        WebElement element = new WebDriverWait(MainRunner.getWebDriver(), MainRunner.timeout).until(ExpectedConditions.elementToBeClickable(selector));
        element.clear();
        element.sendKeys(text);
        waitForPageReady();
    }

    /**
     * Sends an enter key to an element
     *
     * @param selector String selector in format "page_name.element_name"
     */
    public static void sendEnter(String selector) {
        sendEnter(element(selector));
    }

    /**
     * Sends an enter key to an element
     *
     * @param selector By selector to use
     */
    public static void sendEnter(By selector) {
        StepUtils.findElement(selector).sendKeys(Keys.ENTER);
    }

    /**
     * Sends an enter key to an element
     *
     * @param el element to use
     */
    public static void sendEnter(WebElement el) {
        el.sendKeys(Keys.ENTER);
    }

    /**
     * Sends an enter key to an element, then runs exit condition lambda
     *
     * @param selector      By selector to use
     * @param exitCondition lambda expression to run after sending enter
     */
    public static void sendEnter(By selector, Runnable exitCondition) {
        StepUtils.findElement(selector).sendKeys(Keys.ENTER);
        exitCondition.run();
    }

    /**
     * Selects a value from a drop down menu based on text
     *
     * @param selector String selector in format "page_name.element_name"
     * @param text     text to select
     */
    public static void selectDropDownText(String selector, String text) {
        selectDropDownText(findElement(element(selector)), text);
    }

    /**
     * Selects a value from a drop down menu based on text
     *
     * @param selector By selector to use
     * @param text     text to select
     */
    public static void selectDropDownText(By selector, String text) {
        selectDropDownText(findElement(selector), text);
    }

    /**
     * Selects a value from a drop down menu based on text
     *
     * @param el   drop down menu element
     * @param text text to select
     */
    public static void selectDropDownText(WebElement el, String text) {
        if (safari()) {
            click(el);
            click(el.findElement(By.linkText(text)));
        } else {
            Select select = new Select(el);
            select.selectByVisibleText(text);
            waitForPageReady();
        }
    }

    public static String getSelectedValueFromDropDown(By el) {
        Select select = new Select(StepUtils.findElement(el));
        return select.getFirstSelectedOption().getText();
    }

    /**
     * Checks a checkbox if it is not already checked
     *
     * @param selector String selector in format "page_name.element_name"
     */
    public static void selectCheckbox(String selector) {
        selectCheckbox(element(selector));
    }

    /**
     * Checks a checkbox if it is not already checked
     *
     * @param selector By selector to use
     */
    public static void selectCheckbox(By selector) {
        WebElement checkBox = StepUtils.findElement(selector);
        if (checkBox != null && !checkBox.isSelected()) {
            click(checkBox);
        }
    }

    /**
     * Selects the value at an index from the top of a drop down menu
     *
     * @param selector String selector in format "page_name.element_name"
     * @param index    location to select
     */
    public static void selectDropDownValue(String selector, int index) {
        selectDropDownValue(element(selector), index);
    }

    /**
     * Selects the value at an index from the top of a drop down menu
     *
     * @param selector By selector to use
     * @param index    location to select
     */
    public static void selectDropDownValue(By selector, int index) {
        WebElement selectEl = StepUtils.findElement(selector);
        Assert.assertNotNull(selectEl);
        if (safari()) {
            click(selectEl);
            click(selectEl.findElement(By.cssSelector("nth-child(" + (index + 1) + ")")));
        } else {
            Select select = new Select(selectEl);
            select.selectByIndex(index);
        }
    }

    /**
     * Selects a random value from drop down menu
     *
     * @param selector By selector to use
     * @return the value that was selected as a String
     */
    public static String selectRandomValueFromDropDown(String selector) {
        return selectRandomValueFromDropDown(element(selector));
    }

    /**
     * Selects a random value from drop down menu
     *
     * @param selector By selector to use
     * @return the value that was selected as a String
     */
    public static String selectRandomValueFromDropDown(By selector) {
        Select select = new Select(StepUtils.findElement(selector));
        List<WebElement> elements = select.getOptions();
        int size = elements.size();
        if (size == 0) {
            throw new org.openqa.selenium.NoSuchElementException("No values found with drop down: " + selector);
        }
        int randomIndex = new Random().nextInt(elements.size());
        selectDropDownValue(selector, randomIndex);
        return elements.get(randomIndex).getText();
    }

    /**
     * Gets all values in a drop down menu
     *
     * @param selector String selector in format "page_name.element_name"
     * @return list of values as Strings
     */
    public static List<String> getAllDropDownValues(String selector) {
        return getAllDropDownValues(element(selector));
    }

    /**
     * Gets all values in a drop down menu
     *
     * @param selector By selector to use
     * @return list of values as Strings
     */
    public static List<String> getAllDropDownValues(By selector) {
        Select select = new Select(StepUtils.findElement(selector));
        List<WebElement> elements = select.getOptions();
        return elements.stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    /**
     * Method to select text from a drop down list
     * Note: Using this for BCOM as in Account pages the select elements are hidden in UI
     *
     * @param ele    drop down list element
     * @param listId list of elements
     * @param text   to select
     */
    public static void selectCustomDropDownText(String ele, String listId, String text) {
        click(ele);
        clickElementByText(listId, text);
    }

    /**
     * Method to select value from a drop down list based on index
     * Note: Using this for BCOM as in Account pages the select elements are hidden in UI
     *
     * @param ele    drop down list element
     * @param listId list of elements
     * @param index  of text to be selected
     */
    public static void selectCustomDropDownValue(String ele, String listId, int index) {
        click(ele);
        clickElementByText(listId, findElements(listId).get(index).getText());
    }

    /**
     * Gets the available values from a custom dropdown list
     *
     * @param ele    list expander element as locator string in format "page_name.element_name"
     * @param listId list element as locator string in format "page_name.element_name"
     * @return a list of all available options
     */
    public static List<String> getAllCustomDropDownValues(String ele, String listId) {
        click(ele);
        List<String> dropDownValues = findElements(listId).stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
        click(ele);
        return dropDownValues;
    }


    /**
     * Types text into element and sends an enter key
     *
     * @param selector String selector in format "page_name.element_name"
     * @param text     text to type in
     */
    public static void typeTextNEnter(String selector, String text) {
        typeTextNEnter(element(selector), text);
    }

    /**
     * Types text into element and sends an enter key
     *
     * @param selector By selector to use
     * @param text     text to type in
     */
    public static void typeTextNEnter(By selector, String text) {
        WebElement element = StepUtils.findElement(selector);
        if (element == null)
            return;

        element.click();
        element.clear();
        element.sendKeys(text);

        if (safari()) {
            try {
                element.submit();
            } catch (Exception e) {
                element.sendKeys(Keys.RETURN);
            }
        } else {
            element.sendKeys(Keys.RETURN);
        }
    }

    /**
     * Retrieves the first element
     *
     * @param selector String selector in format "page_name.element_name"
     * @return first element selected by el
     */
    public static WebElement findElement(String selector) {
        return findElement(element(selector));
    }

    /**
     * Retrieves the first element
     *
     * @param selector By selector to use
     * @return first element selected by el
     */
    public static WebElement findElement(By selector) {
        try {
            return MainRunner.getWebDriver().findElement(selector);
        } catch (Exception ex) {
            System.err.println("-->StepUtils.findElement() no element found with selector: " + selector);
        }
        return null;
    }

    /**
     * Retrieves all elements
     *
     * @param selector String selector in format "page_name.element_name"
     * @return list of WebElements selected by el
     */
    public static List<WebElement> findElements(String selector) {
        return findElements(element(selector));
    }

    /**
     * Retrieves all elements using a selector & filters them with the given Predicate if provided
     *
     * @param selector String selector in format "page_name.element_name"
     * @param filter   Predicate to filter results with
     * @return list of WebElements selected by el after filter is applied
     */
    public static List<WebElement> findElements(String selector, Predicate<WebElement> filter) {
        return findElements(element(selector), filter);
    }

    /**
     * Retrieves all visible elements using a given selector
     * <p>
     * This will return null if no elements are found, or an empty list if elements are found but not
     * currently displayed.
     * </p>
     *
     * @param selector By selector to use
     * @return list of WebElements selected by el
     */
    public static List<WebElement> findElements(By selector) {
        return findElements(selector, null);
    }

    /**
     * Retrieves all elements using a selector & filters them with the given Predicate if provided
     *
     * @param selector By selector to use
     * @param filter   Predicate to filter results with
     * @return list of WebElements selected by el after filter is applied
     */
    public static List<WebElement> findElements(By selector, Predicate<WebElement> filter) {
        String msg = "-->StepUtils.findElements(): " + selector;
        for (int i = 0; i < 3; i++) {
            try {
                if (filter != null)
                    return MainRunner.getWebDriver().findElements(selector).stream()
                            .filter(WebElement::isDisplayed)
                            .filter(filter)
                            .collect(Collectors.toList());

                return MainRunner.getWebDriver().findElements(selector).stream()
                        .filter(WebElement::isDisplayed)
                        .collect(Collectors.toList());
            } catch (Exception ex) {
                msg += ":" + i;
                Utils.threadSleep(100, null);
            }
        }
        System.err.println(msg);
        return null;
    }

    /**
     * Gets an attribute value "attr" from element
     *
     * @param selector String selector in format "page_name.element_name"
     * @param attr     attribute to retrieve
     * @return requested attribute value if it exists, otherwise empty string
     */
    public static String getElementAttribute(String selector, String attr) {
        return getElementAttribute(element(selector), attr);
    }

    /**
     * Gets an attribute value "attr" from element
     *
     * @param selector By selector to use
     * @param attr     attribute to retrieve
     * @return requested attribute value if it exists, otherwise empty string
     */
    public static String getElementAttribute(By selector, String attr) {
        try {
            return StepUtils.findElement(selector).getAttribute(attr);
        } catch (NullPointerException e) {
            return "";
        }
    }

    /**
     * Hovers over an element
     *
     * @param selector String selector in format "page_name.element_name"
     */
    public static void hoverForSelection(String selector) {
        hoverForSelection(findElement(selector));
    }

    /**
     * Hovers over an element
     *
     * @param selector By selector to use
     */
    public static void hoverForSelection(By selector) {
        hoverForSelection(findElement(selector));
    }

    /**
     * Hovers over an element
     *
     * @param el element to hover over
     */
    public static void hoverForSelection(WebElement el) {
        if (safari()) {
            hover(el);
        } else {
            Actions action = new Actions(MainRunner.getWebDriver());
            action.moveToElement(el).build().perform();
            Utils.threadSleep(1000, null);
        }
    }

    /**
     * Hovers over an element
     *
     * @param selector String selector in format "page_name.element_name"
     */
    public static void hover(String selector) {
        hover(element(selector));
    }

    /**
     * Hovers over an element
     *
     * @param selector By selector to use
     */
    public static void hover(By selector) {
        new WebDriverWait(MainRunner.getWebDriver(), MainRunner.timeout).until(
                ExpectedConditions.visibilityOfElementLocated(selector)
        );
    }

    /**
     * Hovers over an element using javascript
     *
     * @param element By selector to use
     */
    public static void hover(WebElement element) {
        waitForPageReady();
        execJavascript("if(document.createEvent){"
                + "var evObj = document.createEvent('MouseEvents');"
                + "evObj.initEvent('mouseover', true, false); "
                + "arguments[0].focus();arguments[0].dispatchEvent(evObj);"
                + "}else if(document.createEventObject){"
                + "arguments[0].focus();arguments[0].fireEvent('onmouseover');"
                + "}", element);
    }

    /**
     * Clicks an element using javascript
     *
     * @param selector By selector to use
     */
    public static void javascriptClick(String selector) {
        javascriptClick(findElement(element(selector)));
    }

    /**
     * Clicks an element using javascript
     *
     * @param selector By selector to use
     */
    public static void javascriptClick(By selector) {
        javascriptClick(findElement(selector));
    }

    /**
     * Clicks an element e using javascript
     *
     * @param e element to be clicked
     */
    public static void javascriptClick(WebElement e) {
        waitForPageReady();
        if (beforeNavigate != null)
            beforeNavigate.run();

        execJavascript("arguments[0].focus();arguments[0].click();", e);

        waitForPageReady();
        if (afterNavigate != null)
            afterNavigate.run();
    }

    /**
     * Clicks a random element from a group of elements using javascript
     *
     * @param selector String selector in format "page_name.element_name"
     * @throws NoSuchElementException
     */
    public static void randomJavascriptClick(String selector) throws NoSuchElementException {
        javascriptClick(getRandomElement(element(selector)));
    }

    /**
     * Clicks a random element from a group of elements using javascript
     *
     * @param selector By selector to use
     * @throws NoSuchElementException
     */
    public static void randomJavascriptClick(By selector) throws NoSuchElementException {
        javascriptClick(getRandomElement(selector));
    }

    /**
     * Clicks an element that may be lazily loaded
     *
     * @param selector string locator in format "page_name.element_name"
     */
    public static void clickLazyElement(String selector) {
        scrollToLazyLoadElement(selector);
        click(selector);
    }

    /**
     * Clicks an element
     *
     * @param selector string locator in format "page_name.element_name"
     */
    public static void click(String selector) {
        click(element(selector));
    }

    /**
     * Clicks an element
     *
     * @param selector By selector to use
     */
    public static void click(By selector) {
        click(findElement(selector), 0);
    }

    /**
     * Click an element
     *
     * @param e By selector to use
     */
    public static void click(WebElement e) {
        click(e, 0);
    }

    /**
     * Hovers over and then clicks an element
     *
     * @param selector By selector to use
     * @param hover    time to hover (in millis)
     */
    public static void click(By selector, int hover) {
        click(findElement(selector), hover);
    }

    /**
     * Runs a pre-condition lambda, then clicks an element
     *
     * @param pre_condition code to run before element is clicked
     * @param selector      By selector to use
     */
    public static void click(Runnable pre_condition, By selector) {
        pre_condition.run();
        click(findElement(selector), 0);
    }

    /**
     * Clicks an element, then runs an exit condition lambda
     *
     * @param selector       By selector to use
     * @param exit_condition code to run after element is clicked
     */
    public static void click(By selector, Runnable exit_condition) {
        click(findElement(selector), 0);
        exit_condition.run();
    }

    /**
     * Hovers over and then clicks an element e
     *
     * @param e     By selector to use
     * @param hover time to hover (in millis)
     */
    public static void click(WebElement e, int hover) {
        waitForPageReady();
        if (beforeNavigate != null)
            beforeNavigate.run();
        WebDriver driver = MainRunner.getWebDriver();
        Actions actions = new Actions(driver);
        try {
            e = new WebDriverWait(driver, MainRunner.timeout).until(ExpectedConditions.elementToBeClickable(e));
        } catch (Exception ex) {
            if (e == null) {
                if (MainRunner.isDebug())
                    System.out.println("-->StepUtils.click(): element null");
                return;
            } else {
                try {
                    if (MainRunner.isDebug())
                        System.out.println("-->StepUtils.click(): element not clickable: " + e.getTagName() + ":" + e.getText() + ":" + ex.getMessage());
                    return;
                } catch (org.openqa.selenium.StaleElementReferenceException exc) {
                    if (MainRunner.isDebug())
                        System.out.println("-->StepUtils.click(): element not clickable: " + exc.getMessage());
                    return;
                }
            }
        }

        actions.moveToElement(e).perform();
        if (hover > 0)
            Utils.threadSleep(hover, null);
        try {
            actions.click().perform();
        } catch (WebDriverException ex) {
            if (MainRunner.isDebug())
                System.err.println("Error while clicking, trying JS");
            javascriptClick(e);
        }
        closeFFAlert();
        waitForPageReady();

        if (ie() || firefox()) {
            // IE & firefox like to leave the mouse over dropdown menus
            if (elementPresent("home.open_flyout") || elementPresent("home.my_account_menu")
                    || elementPresent("home.quickbag_items_list")) {
                try {
                    actions.moveToElement(findElement(element("home.search_field"))).perform();
                } catch (Exception ex) {
                    // ignore
                }
            }
        }
        if (afterNavigate != null)
            afterNavigate.run();
    }

    /**
     * Clicks a random element from a group of elements
     *
     * @param selector By selector to use
     * @throws NoSuchElementException
     */
    public static void clickRandomElement(String selector) throws NoSuchElementException {
        clickRandomElement(element(selector), 0);
    }

    /**
     * Clicks a random element from a group of elements
     *
     * @param selector  By selector to use
     * @param predicate predicate to use to filter elements
     * @throws NoSuchElementException
     */
    public static void clickRandomElement(String selector, Predicate<WebElement> predicate) throws NoSuchElementException {
        click(getRandomElement(element(selector), predicate), 0);
    }

    /**
     * Clicks a random element from a group of elements
     *
     * @param selector By selector to use
     * @throws NoSuchElementException
     */
    public static void clickRandomElement(By selector) throws NoSuchElementException {
        clickRandomElement(selector, 0);
    }

    /**
     * Hovers over and then click a random element from a group of elements
     *
     * @param selector By selector to use
     * @param hover    time to hover over element before clicking (in millis)
     * @throws NoSuchElementException
     */
    public static void clickRandomElement(By selector, int hover) throws NoSuchElementException {
        click(getRandomElement(selector), hover);
    }

    /**
     * Sends an enter key to a random element from a group of elements
     *
     * @param selector String selector in format "page_name.element_name"
     * @throws NoSuchElementException
     */
    public static void sendRandomEnter(String selector) throws NoSuchElementException {
        sendRandomEnter(element(selector));
    }

    /**
     * Sends an enter key to a random element from a group of elements
     *
     * @param selector By selector to use
     * @throws NoSuchElementException
     */
    public static void sendRandomEnter(By selector) throws NoSuchElementException {
        sendEnter(getRandomElement(selector));
    }

    /**
     * Gets a random element from a group of elements
     *
     * @param selector String selector in format "page_name.element_name"
     * @return random element from list found using el
     * @throws Exception
     */
    public static WebElement getRandomElement(String selector) throws NoSuchElementException {
        return getRandomElement(element(selector), null);
    }

    /**
     * Gets a random element from a group of elements
     *
     * @param selector String selector in format "page_name.element_name"
     * @return random element from list found using el
     * @throws Exception
     */
    public static WebElement getRandomElement(By selector) throws NoSuchElementException {
        return getRandomElement(selector, null);
    }

    /**
     * Gets a random element from a group of elements
     *
     * @param selector By selector to use
     * @return random element from list found using el
     * @throws Exception
     */
    public static WebElement getRandomElement(By selector, Predicate<WebElement> predicate) throws NoSuchElementException {
        List<WebElement> elements = findElements(selector);
        if (elements == null || elements.size() == 0) {
            throw new NoSuchElementException("No elements found with selector: " + selector);
        }
        if (predicate != null)
            elements = elements.stream()
                    .filter(predicate)
                    .collect(Collectors.toList());

        WebElement e = null;
        int trials = 10;
        Random rand = new Random();
        while ((e == null || !e.isDisplayed()) && trials-- > 0) {
            e = elements.get(rand.nextInt(elements.size()));
        }
        if (e == null) {
            throw new NoSuchElementException("Cannot select random element.");
        }
        return e;
    }

    /**
     * Checks if an error panel is visible
     *
     * @return true if error panel is visible
     */
    public static boolean isErrorPaneVisible() {
        try {
            WebElement errPane = StepUtils.findElement(By.className("errorPageMessagePanelHd"));
            if (errPane != null && errPane.isDisplayed() && errPane.getText().contains("Oops!"))
                return true;
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * Waits until an element is no longer present
     *
     * @param selector String selector in format "page_name.element_name"
     * @return true if element disappears/was not present
     */
    public static boolean waitUntilElementNotPresent(String selector) {
        return waitUntilElementNotPresent(element(selector));
    }

    /**
     * Waits until an element is no longer present
     *
     * @param selector By selector to use
     * @return true if element disappears/was not present
     */
    public static boolean waitUntilElementNotPresent(By selector) {
        try {
            WebDriverWait wait = new WebDriverWait(MainRunner.getWebDriver(), MainRunner.timeout);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(selector));
            return true;
        } catch (Exception ex) {
            if (MainRunner.isDebug())
                System.err.println("-->Error:waitUntilElementNotPresent():" + selector.toString() + ":" + ex.getMessage());
            return false;
        }
    }

    /**
     * Waits until an element is present and click it.
     *
     * @param selector    By selector to use
     * @param waitSeconds time to wait in seconds
     */
    public static void waitSecondsUntilElementPresentAndClick(String selector, int waitSeconds) {
        By elClick = element(selector);
        waitSecondsUntilElementPresent(elClick, waitSeconds);
        click(elClick);
    }

    /**
     * Waits until an element is present
     *
     * @param selector String selector in format "page_name.element_name"
     * @return true if element is present
     */
    public static boolean waitUntilElementPresent(String selector) {
        return waitSecondsUntilElementPresent(element(selector), 2);
    }

    /**
     * Waits until an element is present
     *
     * @param selector By selector to use
     * @return true if element is present
     */
    public static boolean waitUntilElementPresent(By selector) {
        return waitSecondsUntilElementPresent(selector, 2);
    }

    /**
     * Waits until an element is present
     *
     * @param selector String selector in format "page_name.element_name"
     * @param seconds  time to wait in seconds
     * @return true if element appeared/was already present
     */
    public static boolean waitSecondsUntilElementPresent(String selector, int seconds) {
        return waitSecondsUntilElementPresent(element(selector), seconds);
    }

    /**
     * Waits until an element is present
     *
     * @param selector By selector to use
     * @param seconds  time to wait in seconds
     * @return true if element appeared/was already present
     */
    public static boolean waitSecondsUntilElementPresent(By selector, int seconds) {
        try {
            WebDriverWait wait = new WebDriverWait(MainRunner.getWebDriver(), seconds);
            wait.until(ExpectedConditions.visibilityOfElementLocated(selector));
            return true;
        } catch (Exception ex) {
            if (MainRunner.isDebug())
                System.err.println("-->Error:waitSecondsUntilElementPresent():" +
                        Utils.listToString(Utils.getCallFromFunction("waitSecondsUntilElementPresent"), "\n\t ", null) +
                        ":" + selector == null ? selector.toString() : "" + ":" + ex.getMessage());

            return false;
        }
    }

    /**
     * Waits for an element to not be present
     *
     * @param selector String selector in format "page_name.element_name"
     * @param seconds  timeout in seconds
     * @return true if element went away/was not present
     */
    public static boolean waitSecondsUntilElementNotPresent(String selector, int seconds) {
        return waitSecondsUntilElementNotPresent(element(selector), seconds);
    }

    /**
     * Waits for an element to not be present
     *
     * @param selector By selector to use
     * @param seconds  timeout in seconds
     * @return true if element went away/was not present
     */
    public static boolean waitSecondsUntilElementNotPresent(By selector, int seconds) {
        try {
            WebDriverWait wait = new WebDriverWait(MainRunner.getWebDriver(), seconds);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(selector));
            return true;
        } catch (Exception ex) {
            if (MainRunner.isDebug())
                System.err.println("-->Error:waitSecondsUntilElementNotPresent():" + selector.toString() + ":" + ex.getMessage());
            return false;
        }
    }

    /**
     * Wait for an element to be present, refresh the page if it does not appear and check again
     *
     * @param selector By selector to use
     */
    public static void waitUntilElementPresentWithRefresh(By selector) {
        try {
            int cnt = 0;
            while (!elementPresent(selector) && cnt++ < 1) {
                try {
                    waitSecondsUntilElementPresent(selector, 3);
                } catch (Exception e) {
                }
                if (elementPresent(selector))
                    return;
                browserRefresh();
                waitForPageReady();
            }
        } catch (Exception ex) {
            if (MainRunner.isDebug())
                System.err.println("-->Error:waitUntilElementPresentWithRefresh():" + selector.toString() + ":" + ex.getMessage());
        }
    }

    /**
     * Try to wait for el1 to be present. If it is not present, wait for el2 to be present.
     *
     * @param el1 first By selector to use
     * @param el2 second By selector to use
     */
    public static void waitUntilElementPresentWithRefresh(By el1, By el2) {
        try {
            int cnt = 0;
            while (!elementPresent(el1) && cnt++ < 1) {
                try {
                    waitSecondsUntilElementPresent(el1, 3);
                } catch (Exception e) {
                }
                if (elementPresent(el1))
                    return;
                browserRefresh();
                waitForPageReady();
            }

            while (!elementPresent(el2) && cnt++ < 1) {
                try {
                    waitSecondsUntilElementPresent(el2, 3);
                } catch (Exception e) {
                }
                if (elementPresent(el2))
                    return;
                browserRefresh();
                waitForPageReady();
            }
        } catch (Exception ex) {
            if (MainRunner.isDebug())
                System.err.println("-->Error:waitUntilElementPresentWithRefresh():" + el1.toString() + ":" + el2.toString() + ":" + ex.getMessage());
        }
    }

    /**
     * Wait for the first element. If it is not present, refresh browser and click the second element.
     *
     * @param el_wait  By selector to use
     * @param el_click By selector to use if el_wait does not appear
     */
    public static void waitUntilElementPresentWithRefreshAndClick(By el_wait, By el_click) {
        try {
            if (elementPresent(el_wait)) {
                click(el_click);
                return;
            }
            for (int i = 0; i < 2; i++) {
                try {
                    waitSecondsUntilElementPresent(el_wait, 3);
                } catch (Exception e) {
                }
                if (elementPresent(el_wait)) {
                    click(el_click);
                    return;
                }
            }
        } catch (Exception ex) {
            if (MainRunner.isDebug())
                System.err.println("-->Error:waitUntilElementPresentWithRefreshAndClick():" + el_wait.toString() + ":" + el_click.toString() + ":" + ex.getMessage());
        }
    }

    /**
     * Wait for the first element. If it is not present, refresh browser and hover/click the second element.
     *
     * @param el_wait  By selector to use
     * @param el_click By selector to use if el_wait does not appear
     */
    public static void waitUntilElementPresentWithRefreshAndHoverclick(By el_wait, By el_click) {
        try {
            int cnt = 0;
            while (!elementPresent(el_wait) && cnt++ < 3) {
                try {
                    waitSecondsUntilElementPresent(el_wait, 3);
                } catch (Exception e) {
                }
                if (elementPresent(el_wait))
                    return;
                browserRefresh();
                waitForPageReady();
                click(el_click);
            }
        } catch (Exception ex) {
            if (MainRunner.isDebug())
                System.err.println("-->Error:waitUntilElementPresentWithRefreshAndHoverclick():" + el_wait.toString() + ":" + el_click.toString() + ":" + ex.getMessage());
        }
    }

    /**
     * Clicks the element if it is displayed on the page
     *
     * @param selector String selector in format "page_name.element_name"
     * @return true if element was clicked
     */
    public static boolean clickIfPresent(String selector) {
        return clickIfPresent(selector, 0);
    }

    /**
     * Clicks the element if it is displayed on the page
     *
     * @param selector String selector in format "page_name.element_name"
     * @param hover    amount of time to hover over element before clicking (in millis)
     * @return true if element was clicked
     */
    public static boolean clickIfPresent(String selector, int hover) {
        return clickIfPresent(element(selector), hover);
    }

    /**
     * Clicks the element if it is displayed on the page
     *
     * @param selector By selector to use
     * @param hover    amount of time to hover over element before clicking (in millis)
     * @return true if element was clicked
     */
    public static boolean clickIfPresent(By selector, int hover) {
        waitForPageReady();
        if (elementPresent(selector)) {
            click(selector, hover);
            return true;
        }
        return false;
    }

    /**
     * Checks if element is displayed on the page
     *
     * @param selector String selector in format "page_name.element_name"
     * @return true if element is displayed on the page
     */
    public static boolean elementPresent(String selector) {
        return elementPresent(element(selector));
    }

    /**
     * Checks if element is displayed on the page
     *
     * @param selector By selector to use
     * @return true if element is displayed on the page
     */
    public static boolean elementPresent(By selector) {
        try {
            return MainRunner.getWebDriver().findElement(selector).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if any elements are displayed on the page
     *
     * @param selector String selector in format "page_name.element_name"
     * @return true if any elements selected by "by" are displayed on the page
     */
    public static boolean anyPresent(String selector) {
        return anyPresent(element(selector));
    }

    /**
     * Checks if any elements are displayed on the page
     *
     * @param selector By selector to use
     * @return true if any elements selected by "by" are displayed on the page
     */
    public static boolean anyPresent(By selector) {
        List<WebElement> items = findElements(selector);
        return items != null && items.stream().anyMatch(WebElement::isDisplayed);
    }

    /**
     * Checks if browser is on a specific page.
     *
     * @param name name of expected page
     * @return true if on page "name," otherwise false
     */
    public static boolean onPage(String name) {
        if (!(name.matches("^page\\..*$")))
            name = "page." + name;
        String pString = pageString(name + ".url");
        String curl = url();
        if (pString != null && curl.contains(pString)) {
            return true;
        }

        String verifyElementKey = name + ".verify_page";
        String verifyElement = getElementValue(verifyElementKey);
        if (verifyElement != null) {
            if (elementPresent(verifyElementKey)) {
                return true;
            } else {
                if (MainRunner.isDebug())
                    System.err.println("-->Error StepUtils.onPage().verify_element:" + verifyElement + ":" + verifyElementKey);
                return false;
            }
        }
        return false;
    }

    /**
     * Checks if browser is on any of a list of pages
     *
     * @param names list of pages to check for
     * @return true if on one of the listed pages
     */
    public static boolean onPage(String... names) {
        for (String name : names) {
            if (onPage(name))
                return true;
        }
        return false;
    }

    /**
     * Throws an exception if not on one of the listed pages
     *
     * @param names names of all allowed pages
     * @throws Exception
     */
    public static void shouldBeOnPage(String... names) throws Exception {
        waitForPageReady();
        for (String name : names)
            if (onPage(name))
                return;

        String pages = "";
        for (String name : names)
            pages += " " + name.replace("_", " ") + ", ";
        pages = pages.substring(0, pages.length() - 2);
        throw new Exception("ERROR - ENV: Not on pages:" + pages);
    }

    /**
     * Checks if element(s) is/are present. If not, fails the current test
     *
     * @param selector String selector in format "page_name.element_name"
     */
    public static void elementShouldBePresent(String selector) {
        List<WebElement> elements = findElements(element(selector));
        Assert.assertTrue("Element " + selector + " is not displayed", !(elements == null || elements.isEmpty()));
    }

    /**
     * Checks if elements are present. If not, fails the current test
     *
     * @param elements list of elements to check
     */
    public static void elementShouldBePresent(List<WebElement> elements) {
        elements.forEach(el -> Assert.assertTrue("Element " + el.getText() + " is not displayed", el.isDisplayed()));
    }

    /**
     * Checks if element is present. If not, fails the current test
     *
     * @param el element to check
     */
    public static void elementShouldBePresent(WebElement el) {
        Assert.assertTrue("Element " + el.getText() + " not visible", el.isDisplayed());
    }

    /**
     * Checks if an element is currently on the screen
     *
     * @param selector String selector in format "page_name.element_name"
     * @return true if element is on the screen
     */
    public static boolean elementInView(String selector) {
        return elementInView(findElement(selector));
    }

    /**
     * Checks if an element is currently on the screen
     *
     * @param selector String selector in format "page_name.element_name"
     * @return true if element is on the screen
     */
    public static boolean elementInView(By selector) {
        return elementInView(findElement(selector));
    }

    /**
     * Checks if an element is currently on the screen using javascript
     * <p>
     * NOTE: Not thoroughly tested, may be buggy. Use at your own risk.
     * </p>
     *
     * @param el element to check
     * @return true if element is on the screen
     */
    public static boolean elementInView(WebElement el) {
        return (boolean) execJavascript("arguments[0].scrollIntoView(false);" +
                "       var position = arguments[0].getBoundingClientRect();" +
                "       var x = position.left + (position.width / 2);" +
                "       var y = position.top + (position.height / 2);" +
                "       var actual = document.elementFromPoint(x, y);" +
                "       do { if(actual === arguments[0]) { return true; } } while(actual = actual.parentNode);" +
                "       return false;", el);
    }

    /**
     * Scrolls until a lazily loaded element is present
     *
     * @param selector String selector in format "page_name.element_name"
     */
    public static void scrollToLazyLoadElement(String selector) {
        execJavascript("window.scrollTo(0, document.body.scrollHeight)");
        waitForPageReady();
    }

    /**
     * Scrolls the web page by a specified amount
     *
     * @param horizontal distance to scroll horizontally (right is positive, left is negative)
     * @param vertical   distance to scroll vertically (down is positive, up is negative)
     */
    public static void scrollPage(int horizontal, int vertical) {
        execJavascript("window.scrollBy(" + horizontal + ", " + vertical + ")");
    }

    /**
     * Waits until an element's attribute has changed
     *
     * @param selector     String selector in format "page_name.element_name"
     * @param attr         attribute to use
     * @param initialValue value that should change
     */
    public static void waitAttributeChanged(String selector, String attr, String initialValue) {
        waitAttributeChanged(element(selector), attr, initialValue);
    }

    /**
     * Waits until an element's attribute has changed
     *
     * @param selector     By selector to use
     * @param attr         attribute to use
     * @param initialValue value that should change
     */
    public static void waitAttributeChanged(By selector, String attr, String initialValue) {
        waitAttributeChanged(findElement(selector), attr, initialValue);
    }

    /**
     * Waits until an element's attribute has changed
     *
     * @param element       element to use
     * @param attr          attribute to use
     * @param expectedValue value to wait for attribute to become
     */
    public static void waitAttributeChanged(WebElement element, String attr, String expectedValue) {
        WebDriverWait wait = new WebDriverWait(MainRunner.getWebDriver(), MainRunner.timeout);

        wait.until(new ExpectedCondition<Boolean>() {
            private WebElement element;
            private String attr;
            private String ExpectedValue;

            private ExpectedCondition<Boolean> init(WebElement element, String attr, String ExpectedValue) {
                this.element = element;
                this.attr = attr;
                this.ExpectedValue = ExpectedValue;
                return this;
            }

            public Boolean apply(WebDriver driver) {
                String enabled = element.getAttribute(this.attr);
                if (MainRunner.isDebug())
                    System.out.println("wait: init = (" + expectedValue + "), enabled = (" + enabled + ")");
                return enabled.matches(this.ExpectedValue);
            }
        }.init(element, attr, expectedValue));
    }

    /**
     * Navigates to a page
     * <p>
     * Navigates to a url. If pageURL is not a valid url, it assumes it's a page json file.
     * It the uses "url" element in json file as relative path from "website" environment variable.
     * </p>
     *
     * @param pageURL either valid url or String selector in format "page_name.element_name"
     */
    public static void visit(String pageURL) {
        if (beforeNavigate != null)
            beforeNavigate.run();
        try {
            String link = MainRunner.url;
            if (!pageURL.startsWith("http")) {
                if (!(pageURL.matches(".*\\.url$")))
                    pageURL = pageURL + ".url";

                String url = PageUtils.findPageData(pageURL);

                if (link == null) {
                    if (url == null)
                        Assert.fail("No URL is given");
                    else
                        link = url;
                } else {
                    if (url != null)
                        link = link + url;
                }
            } else {
                link = pageURL;
            }

            System.out.println("...Loading " + link);
            Thread.sleep(30);
            // start checking IE windows authentication popup
            if (ie() && MainRunner.getExParams("require_authentication") != null
                    && MainRunner.getExParams("require_authentication").equals("true"))
                MainRunner.authenticationIeInit();
            //utils.ThreadWatchDog twd = new utils.ThreadWatchDog(null, 60000, "ThreadWatchDog:visit(" + link + ")", () -> stopPageLoad());
            MainRunner.getWebDriver().get(link);
            //twd.interrupt();
            waitForPageReady();
        } catch (Exception ex) {
            if (MainRunner.isDebug())
                System.err.println("-->Error:StepUtils.visit():" + pageURL + ":" + ex.getMessage());
        }
        if (afterNavigate != null)
            afterNavigate.run();
    }

    /**
     * Retrieves value from page json file and strips any selector type information
     *
     * @param key String selector in format "page_name.element_name"
     * @return element value from json file
     */
    public static String pageString(String key) {
        return PageUtils.findPageElement(new PageElement(key));
    }

    /**
     * Retrieves value from page json file
     *
     * @param key String selector in format "page_name.element_name"
     * @return element value from json file
     */
    public static String getElementValue(String key) {
        return PageUtils.findPageElementValue(new PageElement(key));
    }

    /**
     * Creates a By selector based on data in a json file
     *
     * @param elementKey String selector in format "page_name.element_name"
     * @return By selector retrieved from json file
     */
    public static By element(String elementKey) {
        waitForPageReady();
        PageElement elementData = new PageElement(elementKey);
        String element_value = PageUtils.findPageElementValue(elementData);
        if (element_value == null)
            return null;

        By first = null;
        String[] values = element_value.split("\\|\\|");
        for (String retval : values) {
            elementData.parseResult(retval);
            if (elementData.elementValue == null)
                Assert.fail("ERROR - UI: element '" + elementKey + "' is not defined.");
            if (elementData.elementLocator == null)
                Assert.fail("ERROR - UI: element locator is not recognizable.");
            By el = findLocatorMethod(elementData.elementLocator, elementData.elementValue);
            if (values.length == 1)
                return el;

            if (first == null)
                first = el;
            try {
                if (StepUtils.findElement(el).isDisplayed())
                    return el;
            } catch (Exception e) {
                // try the next one (if it exists)
            }
        }
        return first;
    }

    /**
     * Creates a By selector based on data in a json file with embedded params
     * <p>
     * To use embedded parameters, put args in json file string in the format
     * {arg1} {arg2} etc. They will be matched and replaced by params[0],
     * params[1], etc.
     * <br>json example:
     * <br><br>"element": "id, row_{arg1}_{arg2}"<br><br>
     * <p>
     * if you call: <br><br>paramElement("page.element", "2", "3");<br><br>
     * the end result will be: <br><br>"id, row_2_3"
     * </p>
     *
     * @param elementKey String selector in format "page_name.element_name"
     * @param params     values to fill embedded parameters
     * @return By selector built using json file and parameters
     */
    public static By paramElement(String elementKey, String... params) {
        waitForPageReady();
        PageElement elementData = new PageElement(elementKey);
        String element_value = PageUtils.findPageElementValue(elementData);
        if (element_value == null)
            return null;

        By first = null;

        for (String retval : element_value.split("\\|\\|")) {
            elementData.parseResult(retval);
            if (elementData.elementValue == null)
                Assert.fail("ERROR - UI: element '" + elementKey + "' is not defined.");
            if (elementData.elementLocator == null)
                Assert.fail("ERROR - UI: element locator is not recognizable.");
            String param_value = elementData.elementValue;
            int index = 1;
            for (String param : params) {
                param_value = param_value.replace("{arg" + Integer.toString(index) + "}", param);
                // System.out.println("Param element: (" + param + "): " + param_value);
                index++;
            }
            By el = findLocatorMethod(elementData.elementLocator, param_value);
            if (first == null)
                first = el;
            try {
                if (StepUtils.findElement(el).isDisplayed()) {
                    return el;
                }
            } catch (Exception e) {
                if (MainRunner.isDebug())
                    System.err.println("-->StepUtils.paramElement():tried " + el.toString());
            }
        }
        return first;
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
     * Gets the index of the browser window with a given title
     *
     * @param title the title to look for
     * @return the index of the window, -1 if not found
     */
    public static int findIndexOfWindow(String title) {
        WebDriver driver = MainRunner.getWebDriver();
        ArrayList<String> newTabs = new ArrayList<>(driver.getWindowHandles());
        int index = -1;

        for (int i = 0; i < newTabs.size(); i++) {
            driver.switchTo().window(newTabs.get(i));
            waitForPageReady();
            if (driver.getTitle().contains(title)) {
                index = i;
                break;
            }
        }
        driver.switchTo().window(newTabs.get(0));
        return index;
    }

    /**
     * Switches browser to window at given index
     *
     * @param index index of window to switch to
     * @return WebDriver which can control the window
     */
    public static WebDriver switchWindow(int index) {
        ArrayList<String> newTab = new ArrayList<>(MainRunner.getWebDriver().getWindowHandles());
        if (newTab.size() > index) {
            MainRunner.getWebDriver().switchTo().window(newTab.get(index));
        }
        return MainRunner.getWebDriver();
    }

    /**
     * Closes the current window and switches to the base window
     */
    public static void switchWindowClose() {
        WebDriver driver = MainRunner.getWebDriver();
        ArrayList<String> newTab = new ArrayList<>(driver.getWindowHandles());
        if (newTab.size() > 1) {
            driver.close();
            driver.switchTo().window(newTab.get(0));
        }
    }

    /**
     * Finds the url of the current page
     *
     * @return the url of the current page
     */
    public static String url() {
        return MainRunner.getWebDriver().getCurrentUrl();
    }

    /**
     * Waits for an element to appear and then disappear again
     *
     * @param selector By selector to use
     */
    public static void waitUntilLoading(String selector) {
        waitUntilLoading(element(selector));
    }

    /**
     * Waits for an element to appear and then disappear again
     *
     * @param selector By selector to use
     */
    public static void waitUntilLoading(By selector) {
        try {
            waitUntilElementPresent(selector);
            waitUntilElementNotPresent(selector);
        } catch (Exception e) {
            // ignore any wait exceptions
        }
    }

    /**
     * Selects an element from a list using the given text
     *
     * @param selector String selector in format "page_name.element_name"
     * @param find     text to look for
     */
    public static void clickElementByText(String selector, String find) {
        clickElementByText(element(selector), find);
    }

    /**
     * Selects an element from a list using the given text
     *
     * @param selector By selector to use
     * @param find     text to look for
     */
    public static void clickElementByText(By selector, String find) {
        List<WebElement> list = findElements(selector);
        if (list == null || find == null || list.size() == 0) {
            Assert.fail((list == null || list.size() == 0 ? "List to find from is empty " : "") + (find == null ? "String to find is null" : ""));
        }
        Optional<WebElement> element = list.stream()
                .filter(el -> el.isDisplayed() && el.getText().equalsIgnoreCase(find))
                .findFirst();

        if (element.isPresent())
            click(element.get());
        else
            Assert.fail("element with text \"" + find + "\" not found");
    }

    /**
     * Gets the text of an element or "null" if element does not exist
     *
     * @param selector String selector in format "page_name.element_name"
     * @return text of the element or "null" if element does not exist
     */
    public static String getElementText(String selector) {
        return getElementText(element(selector));
    }

    public static String getElementsText(By e, int index) {
        try {
            return StepUtils.findElements(e).get(index).getText().toString();
        } catch (NullPointerException ex) {
            return "null";
        }
    }

    /**
     * Gets the text of an element or "null" if element does not exist
     *
     * @param e By selector to use
     * @return text of the element or "null" if element does not exist
     */
    public static String getElementText(By e) {
        try {
            return StepUtils.findElement(e).getText();
        } catch (NullPointerException ex) {
            // return string "null" to avoid accidentally matching checks for empty strings while also avoiding
            // 99.99% of realistic match expectations and ALSO not forcing callers to deal with null returns
            return "null";
        }

    }

    //=======================================================================
    // non-ui public methods
    //=======================================================================

    /**
     * Stops any active loading on the page
     *
     * @return true if stop was successful
     */
    public static boolean stopPageLoad() {
        System.out.print("--> stopPageLoad():");
        try {
            execJavascript("window.stop()");
            Utils.threadSleep(500, null);
            String res = execJavascript("return document.readyState").toString();
            System.out.print(res);
            return res != null && res.equals("complete");
        } catch (Exception ex) {
            System.out.print(ex.getMessage());
        }
        return false;
    }

    /**
     * Waits for any loading activities on the page to complete
     */
    public static void waitForPageReady() {
        waitForPageReady(null);
    }

    /**
     * Waits for any loading activities and checks if the page html contains some text
     *
     * @param containText text to look for in page html
     * @return true if page is loaded and contains specified text
     */
    public static boolean waitForPageReady(final String containText) {
        int waitTime = MainRunner.timeout;
        try {
            new WebDriverWait(MainRunner.getWebDriver(), waitTime).until((WebDriver wDriver) -> {
                return ajaxInactive() && isPageLoaded(containText);
            });
        } catch (Exception ex) {
            System.out.println("page has exceeded wait time: " + waitTime);
        }

        try {
            String pageText = getPageText().toLowerCase();
            return !(pageText.contains("not found") || pageText.contains("bad gateway:"));
        } finally {
            closeJQueryPopup();
            //stopPageLoad();
            //System.out.println("." + (System.currentTimeMillis() - ts) + ".exit");
        }
    }

    /**
     * Checks if the page is loaded
     *
     * @param containText test to check for on page. Leave null to skip check
     * @return true if page is loaded
     */
    public static boolean isPageLoaded(String containText) {
        String ret = (String) execJavascript("return document.readyState");
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
    public static boolean ajaxInactive() {
        Long queries = (Long) execJavascript("return jQuery.active;");
        //System.out.print("." + queries + " AJAX");
        String url = MainRunner.getWebDriver().getCurrentUrl();
        return queries == 0;
    }

    /**
     * Closes any open jquery popups
     *
     * @return true if a popup was closed
     */
    public static boolean closeJQueryPopup() {
        if (safari())
            return true;

        String[] texts = new String[]{"some technical issues"};
        for (String text : texts) {
            try {
                boolean res = (boolean) execJavascript(
                        "return $('div.rc-overlay-visible').text().contains('" + text + "')"
                );
                if (res) {
                    execJavascript(
                            "$('div.rc-overlay-visible').find('button').click()"
                    );
                }
                return true;
            } catch (Exception ex) {
                //ignore failed
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
        File fimg = new File(MainRunner.logs + fileName);
        try {
            File scrFile = ((TakesScreenshot) MainRunner.getWebDriver()).getScreenshotAs(OutputType.FILE);
            scrFile.renameTo(fimg);
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                Utils.desktopCapture(new FileOutputStream(fimg));
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
     * @throws Exception
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

    /**
     * Executes javascript code using given arguments
     *
     * @param script String containing JS code
     * @param args   Any arguments to JS code in script
     * @return returned value of JS code (if any)
     */
    public static synchronized Object execJavascript(String script, Object... args) {
        WebDriver wd = MainRunner.getWebDriver();
        JavascriptExecutor scriptExe = ((JavascriptExecutor) wd);
        try {
            //System.err.print("$");
            //System.out.print("StepUtils.execJavascript():" + script + ":");
            //utils.ThreadWatchDog twd = new utils.ThreadWatchDog(Thread.currentThread(), 5*1000l, "StepUtils.execJavascript()", () -> new DBRunnable(new Object[]{wd}){
            //    public void run(){
            //        System.err.println("StepUtils.execJavascript() has timeout:");
            //        try{
            //            ((WebDriver)this.m_params[0]).close();
            //        }catch(Exception ex){}
            //    }
            //});
            Object ret = scriptExe.executeScript(script, args);
            //twd.interrupt();
            return ret;
        } catch (Exception ex) {
            return "";
        } //finally {
        //System.err.print("^");
        //}
    }

    private static By findLocatorMethod(String locator, String value) {
        switch (locator) {
            case "id":
                return By.id(value);
            case "linkText":
                return By.linkText(value);
            case "name":
                return By.name(value);
            case "partialLinkText":
                return By.partialLinkText(value);
            case "tagName":
                return By.tagName(value);
            case "xpath":
                return By.xpath(value);
            case "className":
                return By.className(value);
            case "cssSelector":
                return By.cssSelector(value);
            default:
                return null;
        }
    }

    private static String getPageSource() {
        try {
            return execJavascript("return document.documentElement.outerHTML").toString();
        } catch (Exception ex) {
            return "";
        }
    }

    private static String getPageText() {
        try {
            return execJavascript("return document.body.textContent").toString();
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * Initializes a scenario
     *
     * @param s scenario to initialize
     */
    public void init(Scenario s) {
        this.scenario = (ScenarioImpl) s;
        MainRunner.URLStack = new ArrayList<>();
    }

    /**
     * Gets the browsermob har
     *
     * @return browsermob har
     */
    public Har getHar() {
        try {
            Har har = MainRunner.browsermobServer.getHar();
            har.writeTo(new File(this.scenario.getName() + "." + getScenarioIndex() + ".har"));
            return har;
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the index of the current step in the scenario
     *
     * @return the index of the current step in the scenario
     */
    public int getScenarioIndex() {
        return this.scenario.getStepResuls().size() - 1;
    }

    /**
     * Gets the name of the step at an index
     *
     * @param stepIndex the index to find
     * @return the name of the step at stepIndex
     */
    public String getScenarioStepName(int stepIndex) {
        for (Object o : MainRunner.features.values()) {
            if (o instanceof LinkedTreeMap) {
                LinkedTreeMap s = (LinkedTreeMap) o;
                if (this.scenario.getName().equals(s.get("name"))) {
                    ArrayList list = (ArrayList) s.get("steps");
                    // ignore any error after scenario complete, specially with tag collection option
                    try {
                        s = (LinkedTreeMap) list.get(stepIndex);
                    } catch (Exception e) {
                    }
                    return stepIndex + ":" + Utils.parseInt(s.get("line"), -1) + " - " + s.get("name");
                }
            }
        }
        return null;
    }

    /**
     * Gets information about the current scenario
     *
     * @return scenario information
     */
    public LinkedTreeMap getScenarioInfo() {
        for (Object o : MainRunner.features.values()) {
            if (o instanceof LinkedTreeMap) {
                LinkedTreeMap s = (LinkedTreeMap) o;
                if (this.scenario.getName().equals(s.get("name"))) {
                    return s;
                }
            }
        }
        return null;
    }

    /**
     * Checks if the last scenario passed
     *
     * @return true if last scenario passed
     */
    public boolean isScenarioPassed() {
        List<Result> results = this.scenario.getStepResuls();
        for (Result result : results) {
            if (!result.getStatus().equals("passed"))
                return false;
        }
        return true;
    }

    /**
     * Gets the result of the last step
     *
     * @return last step result
     */
    public Result getLastStepResult() {
        return getStepResult(-1);
    }

    /**
     * Clears the result of a step
     *
     * @param step index of step to clear
     * @return cleared step result
     */
    public Result clearStepResult(int step) {
        if (step == -1)
            step = getScenarioIndex();
        List<Result> steps = this.scenario.getStepResuls();
        return steps.remove(step);
    }

    /**
     * Gets the result of a step
     *
     * @param step index of step to get
     * @return last step result
     */
    public Result getStepResult(int step) {
        if (step == -1)
            step = getScenarioIndex();
        return this.scenario.getStepResuls().get(step);
    }

    /**
     * Checks if a step has passed
     *
     * @param step index of step to check
     * @return true if step passed
     */
    public boolean isStepPassed(int step) {
        return getStepResult(step).getStatus().equals("passed");
    }

    //=======================================================================
    // private methods
    //=======================================================================

    /**
     * Exception to throw to skip a step
     */
    public static class SkipException extends Exception {
        private static final long serialVersionUID = 3719597044976490615L;

        public SkipException(String msg) {
            super(msg);
        }
    }

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
            if (firefox() ||
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
