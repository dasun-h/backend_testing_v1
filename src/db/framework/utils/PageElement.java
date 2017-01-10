package db.framework.utils;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.regex.Pattern;

import static db.framework.utils.PageUtils.getPageFullPath;

public class PageElement {
    public String elementKey = null;      // home.logo => home.logo or panel.home.logo
    public String pageName = null;        // home.logo => home
    public String pagePath = null;        // home.logo => website.page.home
    public String elementName = null;     // home.logo => logo
    public ArrayList<String> elementLocators = new ArrayList<>(); // home.logo => id
    public ArrayList<String> elementValues = new ArrayList<>();   // home.logo => logoImage

    /**
     * setup and read page element data
     *
     * @param stringName in format home.logo = home.logo or panel.home.logo
     */
    public PageElement(String stringName) {
        elementKey = stringName;

        parseKey();
        parseValue(PageUtils.findPageElement(this));
    }

    /**
     * This return the JSON page name
     *
     * @return page name home.logo = home
     */
    public String getPageName() {
        return pageName;
    }

    /**
     * This return element name in the JSON page
     *
     * @return element name home.logo = logo
     */
    public String getElementName() {
        return elementName;
    }

    /**
     * This return path of the page
     *
     * @return page path home.logo = website.page.home
     */
    public String getPagePath() {
        return pagePath;
    }

    /**
     * parse the locators and values for a page element
     *
     * @param values values in format 'id, b_id || class,  b_class'
     * @return list of values (b_id, b_class)
     */
    public ArrayList<String> parseValue(String values) {
        if (values == null) {
            return elementValues;
        }

        // parse element String
        for (String value : values.split("\\|\\|")) {
            if (value.contains(",")) {
                String[] parts = value.split(Pattern.quote(","));

                String locator = parts[0].trim();

                // add only valid locator and corresponding value
                if (isValidLocatorStrategy(locator)) {
                    elementLocators.add(locator);
                    value = value.replace(locator + ",", "").trim();
                    elementValues.add(value);
                } else {
                    System.err.println("wrong locator : " + locator);
                }
            } else {    // else use case example : url where no locator present
                elementValues.add(value);
            }
        }
        return elementValues;
    }

    private boolean isValidLocatorStrategy(String strategy) {
        switch (strategy) {
            case "id":
            case "linkText":
            case "name":
            case "partialLinkText":
            case "tagName":
            case "xpath":
            case "className":
            case "class":
            case "cssSelector":
                return true;
            default:
                return false;
        }
    }

    // separate page name and element name from the element key
    private void parseKey() {
        // page process
        if (!elementKey.matches("(.*?).(.*?)")) {
            System.err.println("-->Error - UI: element name format is not correct:" + elementKey);
            Assert.fail();
        }

        String[] parts = elementKey.split(Pattern.quote("."));
        if (parts.length >= 2) {
            pageName = parts[parts.length - 2];
            elementName = parts[parts.length - 1];
            pagePath = getPageFullPath(pageName);
        } else {
            System.err.println("-->Error - UI: element name format is not correct:" + elementKey);
            Assert.fail();
        }
    }
}
