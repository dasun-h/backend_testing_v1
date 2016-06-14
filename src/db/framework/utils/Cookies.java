package db.framework.utils;

import db.framework.runner.MainRunner;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import java.util.Calendar;
import java.util.Date;

/**
 * This class provides an easy way to safely manage cookies either through the web driver or javascript.
 */
public class Cookies {

    private static String domain = MainRunner.url.replace("www1", "").replace("www", "").replace("http://", "");

    /**
     * Adds or replaces a cookie using javascript
     *
     * @param name  name of the cookie
     * @param value value of the cookie
     */
    public static void addCookieJavascript(String name, String value) {
        StepUtils.execJavascript("document.cookie = '" + name + "=" + value + "'");
    }

    /**
     * Adds or replaces a cookie using javascript
     *
     * @param name   name of the cookie
     * @param value  value of the cookie
     * @param path   path value of the cookie (usually '/')
     * @param expiry expiration date of the cookie
     */
    public static void addCookieJavascript(String name, String value, String path, Date expiry) {
        if (expiry == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 5);
            expiry = calendar.getTime();
        }
        StepUtils.execJavascript("document.cookie = '" + name + "=" + value + "; path=" + path + "; expires=" + expiry.toGMTString() + "; domain=" + domain + "'");
    }

    /**
     * Deletes a cookie using javascript
     *
     * @param name name of cookie to delete
     */
    public static void deleteCookieJavascript(String name) {
        StepUtils.execJavascript("document.cookie = '" + name + "=; expires=Thu, 18 Dec 2013 12:00:00 UTC'");
    }

    /**
     * Edits a cookie to replace part of its value with another
     *
     * @param name    name of the cookie to be edited
     * @param replace value to replace
     * @param with    value to insert
     */
    public static void editCookie(String name, String replace, String with) {
        Cookie oldCookie = MainRunner.getWebDriver().manage().getCookieNamed(name);
        addCookie(name, oldCookie.getValue().replace(replace, with), "/", null);
    }

    /**
     * Gets the value of a cookie
     *
     * @param name name of the cookie you're looking for
     * @return String representation of cookie value. Empty string if cookie not found.
     */
    public static String getCookieValue(String name) {
        try {
            return MainRunner.getWebDriver().manage().getCookieNamed(name).getValue();
        } catch (Exception e) {
            System.err.println("Unable to get " + name + " cookie value");
            return "";
        }
    }

    /**
     * Prints out the value of a cookie (for debugging purposes)
     *
     * @param name name of cookie to print out
     */
    public static void printCookie(String name) {
        try {
            System.out.println(java.net.URLDecoder.decode(MainRunner.getWebDriver().manage().getCookieNamed(name).toString(), "UTF-8"));
        } catch (Exception e) {
            System.out.println("Unable to print cookie \"" + name + "\"");
        }
    }

    /**
     * Sets the ISHIP cookie value to "US"
     */
    public static void resetIshipCookie() {
        addCookieJavascript("shippingCountry", "US", "/", null);
    }

    /**
     * Delete all cookies using javascript
     */
    public static void deleteAllCookiesJavascript() {
        resetIshipCookie();
        StepUtils.execJavascript(
                "var cookies=document.cookie.split(';');for (var i=0; i<cookies.length; i++){var spcook=cookies[i].split('=');console.log(spcook[0]);document.cookie=spcook[0]+'='+spcook[1]+'; expires=Thu, 21 Sep 1979 00:00:01 UTC; domain=" + domain + "; path=/';}");
    }

    /**
     * Adds a cookie to the browser
     *
     * @param name  name of the cookie
     * @param value value of the cookie
     */
    public static boolean addCookie(String name, String value) {
        return addCookie(name, value, "/", getExpiry());
    }

    /**
     * Adds a cookie to the browser
     *
     * @param name   name of the cookie
     * @param value  value of the cookie
     * @param path   path value of the cookie (usually '/')
     * @param expiry expiration date of the cookie
     */
    public static boolean addCookie(String name, String value, String path, Date expiry) {
        if (expiry == null) {
            expiry = getExpiry();
        }

            try {
                WebDriver.Options options = MainRunner.getWebDriver().manage();
                options.deleteCookieNamed(name);
                options.addCookie(new Cookie(name, value, domain, path, expiry));
            } catch (Exception e) {
                System.out.println("Unable to set " + name + " cookie value");
                return false;
            }
        return true;
    }

    /**
     * Deletes all cookies
     */
    public static void deleteAllCookies() {
        MainRunner.getWebDriver().manage().deleteAllCookies();
    }

    /**
     * Replaces the "SEGMENT" cookie with one with only this value
     *
     * @param value value to insert into the segment cookie
     */

    /**
     * Forces responsive checkout behavior when available
     */


    /**
     * Disables the foresee survey popup
     */
    public static void disableForeseeSurvey() {
        String fsr_r = "{\"d\":365,\"i\":\"d036702-53369766-67bf-6dea-4b996\",\"e\":1408990569653}";
        String fsr_s = "{\"v2\":-2,\"v1\":1,\"rid\":\"d036702-53369766-67bf-6dea-4b996\",\"cp\":{\"SignedIn\":\"0\"},\"to\":3,\"pv\":1,\"lc\":{\"d0\":{\"v\":1,\"s\":false}},\"cd\":0}";
        addCookie("fsr.r", fsr_r, "/", null);
        addCookie("fsr.s", fsr_s, "/", null);
        addCookie("fsr.o", "365");
    }

    private static Date getExpiry() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 5);
        return calendar.getTime();
    }

}
