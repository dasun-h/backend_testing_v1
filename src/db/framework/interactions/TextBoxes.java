package db.framework.interactions;

import db.framework.runner.MainRunner;
import db.framework.utils.StepUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * A collection of ways to interact with text boxes
 */
public class TextBoxes {
    /**
     * Types text into a text box
     *
     * @param selector String selector in format "page_name.element_name"
     * @param text     text to type in
     */
    public static void typeTextbox(String selector, String text) {
        typeTextbox(Elements.element(selector), text);
    }

    /**
     * Types text into a text box
     *
     * @param selector By selector to use
     * @param text     text to type in
     */
    public static void typeTextbox(By selector, String text) {
        Navigate.runBeforeNavigation();
        Wait.forPageReady();
        new WebDriverWait(MainRunner.getWebDriver(), MainRunner.timeout).until(ExpectedConditions.elementToBeClickable(selector));
        WebElement element = Elements.findElement(selector);
        if (element != null) {
            element.clear();
            element.sendKeys(text);
            Wait.forPageReady();
        } else {
            System.err.println("Could not type text \"" + text +
                    "\"\n into text box " + selector + "\nbecause no element was found");
        }
        Navigate.runAfterNavigation();
    }

    /**
     * Types text into element and sends an enter key
     *
     * @param selector String selector in format "page_name.element_name"
     * @param text     text to type in
     */
    public static void typeTextNEnter(String selector, String text) {
        typeTextNEnter(Elements.element(selector), text);
    }

    /**
     * Types text into element and sends an enter key
     *
     * @param selector By selector to use
     * @param text     text to type in
     */
    public static void typeTextNEnter(By selector, String text) {
        Navigate.runBeforeNavigation();
        WebElement element = Elements.findElement(selector);
        if (element == null) {
            return;
        }
            element.click();
            element.clear();
            element.sendKeys(text);

            if (StepUtils.safari()) {
                try {
                    element.submit();
                } catch (Exception e) {
                    element.sendKeys(Keys.RETURN);
                }
            } else {
                element.sendKeys(Keys.RETURN);
            }
        Navigate.runAfterNavigation();
    }
}
