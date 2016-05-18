package db.framework.utils;

import org.junit.Assert;

import java.util.regex.Pattern;

public class PageElement {
    protected String elementKey = null;      // home.logo => home.logo or panel.home.logo
    protected String pageName = null;        // home.logo => home
    protected String pagePath = null;        // home.logo => website.mcom.page.home
    protected String elementName = null;     // home.logo => logo
    protected String elementLocator = null;  // home.logo => id
    protected String elementValue = null;    // home.logo => logoImage
    protected int elementIndex = -1;

    PageElement(String stringName) {
        elementKey = stringName;

        parseKey();
    }

    PageElement(String stringName, int index) {
        elementKey = stringName;
        elementIndex = index;

        parseKey();
    }

    PageElement(String stringName, String passingValue) {
        elementKey = stringName;
        elementValue = passingValue;

        parseKey();
    }

    public String getPageName() {
        return pageName;
    }

    public String getElementName() {
        return elementName;
    }

    public String getPagePath() {
        return pagePath;
    }

    public String parseResult(String resultString) {
        // parse element String
        String[] parts = resultString.split(Pattern.quote(","));
        int count = parts.length;

        elementLocator = parts[0].trim();
        // System.out.println("elementLocator = " + elementLocator);

        // no element locator, just return the result
        if (!isValidElementelementLocator())
            return resultString;

        String removeString = elementLocator + ",";
        elementValue = resultString.replace(removeString, "").trim();
        // System.out.println("elementValue = " + elementValue);
        return elementValue;
    }

    private boolean isValidElementelementLocator() {
        switch (elementLocator) {
            case "id":
            case "linkText":
            case "name":
            case "partialLinkText":
            case "tagName":
            case "xpath":
            case "className":
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
        if (2 <= parts.length) {
            pageName = parts[parts.length - 2];
            elementName = parts[parts.length - 1];
            pagePath = "other.";
            if (elementKey.contains("panel."))
                pagePath = pagePath + ".panel." + pageName;
            else
                pagePath = pagePath + ".page." + pageName;
        } else {
            System.err.println("-->Error - UI: element name format is not correct:" + elementKey);
            Assert.fail();
        }
    }
}
