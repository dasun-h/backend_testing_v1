package db.framework.interactions;

import db.framework.utils.StepUtils;
import db.framework.utils.Utils;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * A collection of ways to interact with drop down menus
 */
public class DropDowns {
    /**
     * Selects a value from a drop down menu based on text
     *
     * @param selector String selector in format "page_name.element_name"
     * @param text     text to select
     */
    public static void selectByText(String selector, String text) {
        selectByText(Elements.findElement(Elements.element(selector)), text);
    }

    /**
     * Selects a value from a drop down menu based on text
     *
     * @param selector By selector to use
     * @param text     text to select
     */
    public static void selectByText(By selector, String text) {
        selectByText(Elements.findElement(selector), text);
    }

    /**
     * Selects a value from a drop down menu based on text
     *
     * @param el   drop down menu element
     * @param text text to select
     */
    public static void selectByText(WebElement el, String text) {
        Navigate.runBeforeNavigation();
        Select select = new Select(el);
        select.selectByVisibleText(text);
        Wait.forPageReady();
        Navigate.runAfterNavigation();
    }

    /**
     * Gets the value currently selected in a dropdown menu
     *
     * @param el By selector to use
     * @return String containing selected value
     */
    public static String getSelectedValue(By el) {
        WebElement element = Elements.findElement(el);
        if (element == null) {
            return null;
        }

        Select select = new Select(element);
        return select.getFirstSelectedOption().getText();
    }

    /**
     * Selects the value at an index from the top of a drop down menu
     *
     * @param selector String selector in format "page_name.element_name"
     * @param index    location to select
     */
    public static void selectByIndex(String selector, int index) {
        selectByIndex(Elements.element(selector), index);
    }

    /**
     * Selects the value at an index from the top of a drop down menu
     *
     * @param selector By selector to use
     * @param index    location to select
     */
    public static void selectByIndex(By selector, int index) {
        Navigate.runBeforeNavigation();
        WebElement selectEl = Elements.findElement(selector);
        Assert.assertNotNull(selectEl);
        Select select = new Select(selectEl);
        select.selectByIndex(index);
        Navigate.runAfterNavigation();
    }

    /**
     * Selects the value that matches the given string
     *
     * @param selector String selector in format "page_name.element_name"
     * @param value    value to select
     */
    public static void selectByValue(String selector, String value) {
        selectByValue(Elements.element(selector), value);
    }

    /**
     * Selects the value that matches the given string
     *
     * @param selector By selector to use
     * @param value    value to select
     */
    public static void selectByValue(By selector, String value) {
        Navigate.runBeforeNavigation();
        WebElement selectEl = Elements.findElement(selector);
        Assert.assertNotNull(selectEl);
        Select select = new Select(selectEl);
        select.selectByValue(value);
        Navigate.runAfterNavigation();
    }

    /**
     * Selects a random value from drop down menu
     *
     * @param selector By selector to use
     * @return the value that was selected as a String
     */
    public static String selectRandomValue(String selector) {
        return selectRandomValue(Elements.element(selector));
    }

    /**
     * Selects a random value from drop down menu
     *
     * @param selector By selector to use
     * @return the value that was selected as a String
     */
    public static String selectRandomValue(By selector) {
        Navigate.runBeforeNavigation();
        WebElement el = Elements.findElement(selector);
        if (el == null) {
            return null;
        }

        Select select = new Select(el);
        List<WebElement> elements = select.getOptions();
        int size = elements.size();
        if (size == 0) {
            throw new org.openqa.selenium.NoSuchElementException("No values found with drop down: " + selector);
        }
        int randomIndex = new Random().nextInt(elements.size());
        // need to grab text before selecting or we get a stale reference
        String elText = elements.get(randomIndex).getText();
        selectByIndex(selector, randomIndex);
        Navigate.runAfterNavigation();
        return elText;
    }

    /**
     * Gets all values in a drop down menu
     *
     * @param selector String selector in format "page_name.element_name"
     * @return list of values as Strings
     */
    public static List<String> getAllValues(String selector) {
        return getAllValues(Elements.element(selector));
    }

    /**
     * Gets all values in a drop down menu
     *
     * @param selector By selector to use
     * @return list of values as Strings
     */
    public static List<String> getAllValues(By selector) {
        WebElement el = Elements.findElement(selector);
        if (el == null) {
            return null;
        }

        Select select = new Select(el);
        List<WebElement> elements = select.getOptions();
        return elements.stream()
                .filter(WebElement::isDisplayed)
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
    public static void selectCustomText(String ele, String listId, String text) {
        Clicks.click(ele);
        if (StepUtils.chrome()) {
            Utils.threadSleep(500, null);
        }
        Clicks.clickElementByText(listId, text);
    }

    /**
     * Method to select value from a drop down list based on index
     * Note: Using this for BCOM as in Account pages the select elements are hidden in UI
     *
     * @param ele    drop down list element
     * @param listId list of elements
     * @param index  of text to be selected
     */
    public static void selectCustomValue(String ele, String listId, int index) {
        Clicks.click(ele);
        Clicks.clickElementByText(listId, Elements.findElements(listId, WebElement::isDisplayed).get(index).getText());
    }

    /**
     * Gets the available values from a custom dropdown list
     *
     * @param ele    list expander element as locator string in format "page_name.element_name"
     * @param listId list element as locator string in format "page_name.element_name"
     * @return a list of all available options
     */
    public static List<String> getAllCustomValues(String ele, String listId) {
        Clicks.click(ele);
        if (StepUtils.chrome()) {
            Utils.threadSleep(500, null);
        }
        List<String> dropDownValues = Elements.findElements(listId).stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .collect(Collectors.toList());
        Clicks.click(ele);
        return dropDownValues;
    }
}
