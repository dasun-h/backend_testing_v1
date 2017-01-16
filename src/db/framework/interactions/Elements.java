package db.framework.interactions;

import db.framework.runner.MainRunner;
import db.framework.utils.PageElement;
import db.framework.utils.StepUtils;
import db.framework.utils.Utils;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByAll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static db.framework.utils.Utils.errLog;

/**
 * A collection of ways to get elements and other information about them
 */
public class Elements {
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
            Wait.forPageReady();
            Wait.untilElementPresent(selector);
            List<WebElement> elements = MainRunner.getWebDriver().findElements(selector);
            if (elements.size() == 0) {
                throw new NoSuchElementException("Unable to locate an element using: " + selector);
            }
            if (elements.size() > 1) {
                List<WebElement> visible = elements.stream().filter(WebElement::isDisplayed).collect(Collectors.toList());
                if (!visible.isEmpty()) {
                    elements = visible;
                }
            }
            return elements.get(0);
        } catch (NoSuchElementException ex) {
            System.err.println("-->StepUtils.findElement() no element found with selector: " + selector);
            errLog.println(ex);
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
     * Retrieves all elements using a selector and filters them with the given Predicate if provided
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
        if (StepUtils.safari()) {
            Wait.untilElementPresent(selector);
        }
        return findElements(selector, null);
    }

    /**
     * Retrieves all elements using a selector and filters them with the given Predicate if provided
     *
     * @param selector By selector to use
     * @param filter   Predicate to filter results with
     * @return list of WebElements selected by el after filter is applied
     */
    public static List<WebElement> findElements(By selector, Predicate<WebElement> filter) {
        String msg = "-->StepUtils.findElements(): " + selector;
        for (int i = 0; i < 3; i++) {
            try {
                if (filter != null) {
                    return MainRunner.getWebDriver().findElements(selector).stream()
                            .filter(filter)
                            .collect(Collectors.toList());
                } else {
                    return MainRunner.getWebDriver().findElements(selector);
                }
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
            String attribute = findElement(selector).getAttribute(attr);
            if (attribute == null) {
                throw new NullPointerException();
            }
            return attribute;
        } catch (NullPointerException e) {
            return "";
        }
    }

    /**
     * Gets a random element from a group of elements
     *
     * @param selector String selector in format "page_name.element_name"
     * @return random element from list found using el
     * @throws NoSuchElementException thrown if no element is found
     */
    public static WebElement getRandomElement(String selector) throws NoSuchElementException {
        return getRandomElement(element(selector), null);
    }

    /**
     * Gets a random element from a group of elements
     *
     * @param selector String selector in format "page_name.element_name"
     * @return random element from list found using el
     * @throws NoSuchElementException thrown if no element is found
     */
    public static WebElement getRandomElement(By selector) throws NoSuchElementException {
        return getRandomElement(selector, null);
    }

    /**
     * Gets a random element from a group of elements
     *
     * @param selector  By selector to use
     * @param predicate predicate to use to filter elements
     * @return random element from list found using el
     * @throws NoSuchElementException thrown if no element is found
     */
    public static WebElement getRandomElement(By selector, Predicate<WebElement> predicate) throws NoSuchElementException {
        List<WebElement> elements = findElements(selector);
        if (elements == null || elements.isEmpty()) {
            throw new NoSuchElementException("No elements found with selector: " + selector);
        }
        Collections.shuffle(elements);
        Optional<WebElement> selected;
        if (predicate != null) {
            selected = elements.stream()
                    .filter(predicate)
                    .findFirst();
        } else {
            selected = Optional.of(elements.get(0));
        }

        if (!selected.isPresent()) {
            throw new NoSuchElementException("Cannot select random element with selector: " + selector);
        }
        return selected.get();
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
     * Checks if an element is present. If not, throws env exception
     *
     * @param selector String selector in format "page_name.element_name"
     */
    public static void elementShouldBePresent(String selector) {
        if (!Wait.untilElementPresent(selector)) {
            throw new TimeoutException("Element " + selector + " is not displayed");
        }
    }

    /**
     * Checks if ALL elements are present. If not, fails the current test
     *
     * @param elements list of elements to check
     */
    public static void elementShouldBePresent(List<WebElement> elements) {
        elements.forEach(el -> Assert.assertTrue("ERROR - ENV: Element " + el.getText() + " is not displayed", el.isDisplayed()));
    }

    /**
     * Checks if element is present. If not, fails the current test
     *
     * @param el element to check
     */
    public static void elementShouldBePresent(WebElement el) {
        Assert.assertTrue("ERROR - ENV: Element " + el.getText() + " not visible", el.isDisplayed());
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
        return (boolean) Navigate.execJavascript("arguments[0].scrollIntoView(false);" +
                "       var position = arguments[0].getBoundingClientRect();" +
                "       var x = position.left + (position.width / 2);" +
                "       var y = position.top + (position.height / 2);" +
                "       var actual = document.elementFromPoint(x, y);" +
                "       do { if(actual === arguments[0]) { return true; } } while(actual = actual.parentNode);" +
                "       return false;", el);
    }

    /**
     * Checks if an element is currently visible on the screen using javascript
     *
     * <p>
     * NOTE: This method will not scroll to the element. It will just return true or false.
     * </p>
     *
     * @param el element to check
     * @return true if element is on the screen and false if element is not in view
     */
    public static boolean isElementInView(WebElement el) {
        return (boolean) Navigate.execJavascript("var position = arguments[0].getBoundingClientRect();" +
                "var actual = document.elementFromPoint(position.x, position.y);" +
                "if(actual === arguments[0]) { return true; } else { return false; }", el);
    }

    /**
     * Retrieves values from page json file and strips any selector type information
     *
     * @param key String selector in format "page_name.element_name"
     * @return element value from json file
     */
    public static ArrayList<String> getValues(String key) {
        return new PageElement(key).elementValues;
    }

    /**
     * Creates a By selector based on data in a json file
     *
     * @param elementKey String selector in format "page_name.element_name"
     * @return By selector retrieved from json file
     */
    public static By element(String elementKey) {
        PageElement elementData = new PageElement(elementKey);
        if (elementData.elementValues.isEmpty()) {
            System.err.println("ERROR - UI: element '" + elementKey + "' is not defined.");
        }
        if (elementData.elementLocators.isEmpty()) {
            System.err.println("ERROR - UI: element locator is not recognizable.");
        }
        if (elementData.elementLocators.isEmpty() || elementData.elementValues.isEmpty()) {
            return null;
        }

        By[] bys = new By[elementData.elementLocators.size()];
        for (int i = 0; i < elementData.elementLocators.size(); i++) {
            bys[i] = Elements.findLocatorMethod(elementData.elementLocators.get(i), elementData.elementValues.get(i));
        }

        return new ByAll(bys);
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
        PageElement elementData = new PageElement(elementKey);
        if (elementData.elementValues.isEmpty()) {
            System.err.println("ERROR - UI: element '" + elementKey + "' is not defined.");
        }
        if (elementData.elementLocators.isEmpty()) {
            System.err.println("ERROR - UI: element locator is not recognizable.");
        }
        if (elementData.elementLocators.isEmpty() || elementData.elementValues.isEmpty()) {
            return null;
        }

        By[] bys = new By[elementData.elementLocators.size()];

        for (int i = 0; i < elementData.elementLocators.size(); i++) {
            String paramValue = elementData.elementValues.get(i);
            int index = 1;
            for (String param : params) {
                paramValue = paramValue.replace("{arg" + index + "}", param);
                // System.out.println("Param element: (" + param + "): " + paramValue);
                index++;
            }

            bys[i] = Elements.findLocatorMethod(elementData.elementLocators.get(i), paramValue);
        }

        return new ByAll(bys);
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
            case "class":
                return By.className(value);
            case "cssSelector":
            case "css":
                return By.cssSelector(value);
            default:
                return null;
        }
    }

    /**
     * Gets the text of an element or "null" if element does not exist
     * <p>
     * return string "null" to avoid accidentally matching checks for empty strings while also avoiding
     * 99.99% of realistic match expectations and without forcing callers to deal with null returns
     * </p>
     *
     * @param selector String selector in format "page_name.element_name"
     * @return text of the element or "null" if element does not exist
     */
    public static String getText(String selector) {
    return getText(element(selector));
}

    /**
     * Gets the text of an element or "null" if element does not exist
     * <p>
     * return string "null" to avoid accidentally matching checks for empty strings while also avoiding
     * 99.99% of realistic match expectations and without forcing callers to deal with null returns
     * </p>
     *
     * @param e By selector to use
     * @return text of the element or "null" if element does not exist
     */
    public static String getText(By e) {
        try {
            return findElement(e).getText().toString();
        } catch (NullPointerException ex) {
            return "null";
        }
    }

    public static String getIndexedText(String selector, int index) {
        return getIndexedText(element(selector),index);
    }

    public static String getIndexedText(By e, int index) {
        try {
            return findElements(e).get(index).getText().toString();
        } catch (NullPointerException ex) {
            return "null";
        }
    }
}
