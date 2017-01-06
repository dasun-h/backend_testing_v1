package db.framework.interactions;

import db.framework.runner.MainRunner;
import db.framework.utils.StepUtils;
import db.framework.utils.Utils;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A collection of ways to click elements on the page
 */
public class Clicks {
    /**
     * Sends an enter key to an element
     *
     * @param selector String selector in format "page_name.element_name"
     */
    public static void sendEnter(String selector) {
        sendEnter(Elements.element(selector));
    }

    /**
     * Sends an enter key to an element
     *
     * @param selector By selector to use
     */
    public static void sendEnter(By selector) {
        Elements.findElement(selector).sendKeys(Keys.ENTER);
    }

    /**
     * Sends an enter key to an element
     *
     * @param el element to use
     */
    public static void sendEnter(WebElement el) {
        Navigate.runBeforeNavigation();
        el.sendKeys(Keys.ENTER);
        Navigate.runAfterNavigation();
    }

    /**
     * Checks a checkbox if it is not already checked
     *
     * @param selector String selector in format "page_name.element_name"
     */
    public static void selectCheckbox(String selector) {
        selectCheckbox(Elements.element(selector));
    }

    /**
     * Checks a checkbox if it is not already checked
     *
     * @param selector By selector to use
     */
    public static void selectCheckbox(By selector) {
        WebElement checkBox = Elements.findElement(selector);
        if (checkBox != null && !checkBox.isSelected()) {
            click(checkBox);
        }
    }

    /**
     * Unchecks a checkbox if it is already checked
     *
     * @param selector By selector to use
     */
    public static void unSelectCheckbox(By selector) {
        WebElement checkBox = Elements.findElement(selector);
        if (checkBox != null && checkBox.isSelected()) {
            Clicks.click(checkBox);
        }
    }

    /**
     * Unchecks a checkbox if it is already checked
     *
     * @param selector String selector in format "page_name.element_name"
     */
    public static void unSelectCheckbox(String selector) {
        unSelectCheckbox(Elements.element(selector));
    }

    /**
     * Hovers over an element
     *
     * @param selector String selector in format "page_name.element_name"
     */
    public static void hoverForSelection(String selector) {
        hoverForSelection(Elements.element(selector));
    }

    /**
     * Hovers over an element
     *
     * @param selector By selector to use
     */
    public static void hoverForSelection(By selector) {
        hoverForSelection(Elements.findElement(selector));
    }

    /**
     * Hovers over an element
     *
     * @param el element to hover over
     */
    public static void hoverForSelection(WebElement el) {
        if (StepUtils.safari()) {
            javascriptHover(el);
        } else {
            Actions action = new Actions(MainRunner.getWebDriver());
            action.moveToElement(el).build().perform();
        }
        Wait.forPageReady();
    }

    /**
     * Hovers over an element
     *
     * @param selector String selector in format "page_name.element_name"
     */
    public static void hover(String selector) {
        hover(Elements.element(selector));
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
     * @param element Element to hover over
     */
    public static void javascriptHover(WebElement element) {
        Wait.forPageReady();
        Navigate.execJavascript("if(document.createEvent){"
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
        javascriptClick(Elements.element(selector));
    }

    /**
     * Clicks an element using javascript
     *
     * @param selector By selector to use
     */
    public static void javascriptClick(By selector) {
        javascriptClick(Elements.findElement(selector));
    }

    /**
     * Clicks an element e using javascript
     *
     * @param e element to be clicked
     */
    public static void javascriptClick(WebElement e) {
        Wait.forPageReady();
        Navigate.runBeforeNavigation();

        Navigate.execJavascript("arguments[0].focus();arguments[0].click();", e);

        Wait.forPageReady();
        Navigate.runAfterNavigation();
    }

    /**
     * Clicks a random element from a group of elements using javascript
     *
     * @param selector String selector in format "page_name.element_name"
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void randomJavascriptClick(String selector) throws NoSuchElementException {
        randomJavascriptClick(Elements.element(selector));
    }

    /**
     * Clicks a random element from a group of elements using javascript
     *
     * @param selector By selector to use
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void randomJavascriptClick(By selector) throws NoSuchElementException {
        javascriptClick(Elements.getRandomElement(selector));
    }

    /**
     * Clicks an element that may be lazily loaded
     *
     * @param selector string locator in format "page_name.element_name"
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void clickLazyElement(String selector) throws NoSuchElementException {
        StepUtils.scrollToLazyLoadElement(selector);
        click(selector);
    }

    /**
     * Clicks an element
     *
     * @param selector string locator in format "page_name.element_name"
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void click(String selector) throws NoSuchElementException {
        click(Elements.element(selector));
    }

    /**
     * Clicks an element
     *
     * @param selector By selector to use
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void click(By selector) throws NoSuchElementException {
        click(Elements.findElement(selector));
    }

    /**
     * Runs a pre-condition lambda, then clicks an element
     *
     * @param preCondition code to run before element is clicked
     * @param selector     By selector to use
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void click(Runnable preCondition, By selector) throws NoSuchElementException {
        preCondition.run();
        click(Elements.findElement(selector));
    }

    /**
     * Clicks an element, then runs an exit condition lambda
     *
     * @param selector      By selector to use
     * @param exitCondition code to run after element is clicked
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void click(By selector, Runnable exitCondition) throws NoSuchElementException {
        click(Elements.findElement(selector));
        exitCondition.run();
    }

    /**
     * clicks a WebElement e
     *
     * @param el By selector to use
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void click(WebElement el) throws NoSuchElementException {
        if (el == null) {
            if (MainRunner.debugMode) {
                System.out.println("-->StepUtils.click(): element null");
            }
            throw new NoSuchElementException("Unable to click element");
        }

        Wait.forPageReady();

        Navigate.runBeforeNavigation();
        WebDriver driver = MainRunner.getWebDriver();
        Actions actions = new Actions(driver);
        try {
            el = new WebDriverWait(driver, MainRunner.timeout).until(ExpectedConditions.elementToBeClickable(el));
        } catch (Exception ex) {
            try {
                if (MainRunner.debugMode) {
                    System.out.println("-->StepUtils.click(): element not clickable: " + el.getTagName() + ": " + el.getText() + ": " + ex.getMessage());
                }
                throw new NoSuchElementException("Unable to click element");
            } catch (StaleElementReferenceException exc) {
                if (MainRunner.debugMode) {
                    System.out.println("-->StepUtils.click(): element not clickable: " + exc.getMessage());
                }
                throw new NoSuchElementException("Unable to click element");
            }
        }
        try {
            if (StepUtils.safari()) {  // actions not supported in safari
                javascriptHover(el);
                el.click();
            } else {
                actions.moveToElement(el).perform();
                actions.click().perform();
            }
        } catch (WebDriverException ex) {
            if (MainRunner.debugMode) {
                System.err.println("Error while clicking, trying JS: " + ex);
            }
            javascriptClick(el);
        }
        StepUtils.closeAlert();
        Wait.forPageReady();

        if (StepUtils.ie() || StepUtils.firefox()) {
            // IE & firefox like to leave the mouse over dropdown menus
            Utils.redirectSErr();
            if (Elements.elementPresent("home.open_flyout") || Elements.elementPresent("home.my_account_menu")
                    || Elements.elementPresent("home.quickbag_items_list")) {
                try {
                    actions.moveToElement(Elements.findElement(Elements.element("home.verify_page"))).perform();
                } catch (Exception | Error ex) {
                    // ignore
                }
            }
            Utils.resetSErr();
        }
        Navigate.runAfterNavigation();
    }

    /**
     * Clicks a random element from a group of elements
     *
     * @param selector By selector to use
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void clickRandomElement(String selector) throws NoSuchElementException {
        clickRandomElement(Elements.element(selector));
    }

    /**
     * Clicks a random element from a group of elements
     *
     * @param selector  By selector to use
     * @param predicate predicate to use to filter elements
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void clickRandomElement(String selector, Predicate<WebElement> predicate) throws NoSuchElementException {
        click(Elements.getRandomElement(Elements.element(selector), predicate));
    }

    /**
     * Clicks a random element from a group of elements
     *
     * @param selector By selector to use
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void clickRandomElement(By selector) throws NoSuchElementException {
        click(Elements.getRandomElement(selector));
    }

    /**
     * Clicks a point in a map area as a workaround of Selenium on Firefox/IE/Safari
     *
     * @param attribute   attribute to look for
     * @param searchValue expected value of attribute given by "attribute" arg
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void clickArea(String attribute, String searchValue) throws NoSuchElementException {
        List<WebElement> allOptions = Elements.findElements(By.tagName("area"), WebElement::isDisplayed);
        if (allOptions == null) {
            throw new NoSuchElementException("Unable to find any areas on this page");
        }
        for (WebElement option : allOptions)
            if (searchValue.equals(option.getAttribute(attribute))) {
                option.click();
                return;
            }
    }

    /**
     * Sends an enter key to a random element from a group of elements
     *
     * @param selector String selector in format "page_name.element_name"
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void sendRandomEnter(String selector) throws NoSuchElementException {
        sendRandomEnter(Elements.element(selector));
    }

    /**
     * Sends an enter key to a random element from a group of elements
     *
     * @param selector By selector to use
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void sendRandomEnter(By selector) throws NoSuchElementException {
        sendEnter(Elements.getRandomElement(selector));
    }

    /**
     * Clicks the element if it is displayed on the page
     *
     * @param selector String selector in format "page_name.element_name"
     * @return true if element was clicked
     */
    public static boolean clickIfPresent(String selector) {
        return clickIfPresent(Elements.element(selector));
    }

    /**
     * Clicks the element if it is displayed on the page
     *
     * @param selector By selector to use
     * @return true if element was clicked
     */
    public static boolean clickIfPresent(By selector) {
        Wait.forPageReady();
        try {
            if (Elements.elementPresent(selector)) {
                click(selector);
                return true;
            } else {
                throw new NoSuchElementException("Element not present");
            }
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Wait until an element is present then clicks it
     *
     * @param selector String selector in format "page_name.element_name"
     * @return true if element was clicked
     */
    public static boolean clickWhenPresent(String selector) {
        return clickWhenPresent(Elements.element(selector));
    }

    /**
     * Wait until an element is present then clicks it
     *
     * @param selector By selector to use
     * @return true if element was clicked
     */
    public static boolean clickWhenPresent(By selector) {
        if (Wait.secondsUntilElementPresent(selector, 5)) {
            click(selector);
            return true;
        }
        return false;
    }

    /**
     * Selects an element from a list using the given text
     *
     * @param selector String selector in format "page_name.element_name"
     * @param find     text to look for
     */
    public static void clickElementByText(String selector, String find) {
        clickElementByText(Elements.element(selector), find);
    }

    /**
     * Selects an element from a list using the given text
     *
     * @param selector By selector to use
     * @param find     text to look for
     */
    public static void clickElementByText(By selector, String find) {
        List<WebElement> list = Elements.findElements(selector);
        if (list == null || find == null || list.size() == 0) {
            Assert.fail((list == null || list.size() == 0 ? "List to find from is empty " : "") + (find == null ? "String to find is null" : ""));
        }
        Optional<WebElement> element = list.stream()
                .filter(WebElement::isDisplayed)
                .filter(el -> el.isDisplayed() && el.getText().equalsIgnoreCase(find))
                .findFirst();

        if (element.isPresent()) {
            click(element.get());
        } else {
            Assert.fail("element with text \"" + find + "\" not found");
        }
    }
}
