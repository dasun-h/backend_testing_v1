package db.shared.steps;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.PendingException;
import db.framework.interactions.Clicks;
import db.framework.interactions.Navigate;
import db.framework.interactions.TextBoxes;
import org.junit.Assert;
import db.framework.utils.StepUtils;

import static db.framework.interactions.Clicks.click;
import static db.framework.interactions.Clicks.javascriptClick;
import static db.framework.interactions.Elements.*;
import static db.framework.interactions.Wait.untilElementPresent;

/**
 * Created by dasunh on 1/3/2017.
 */
public class Sample extends StepUtils{
    @Given("^I visit the yahoo home page$")
    public void I_visit_the_yahoo_home_page() throws Throwable {
        Navigate.visit();
        Assert.assertTrue("ERROR-APP: Google home page is not loaded properly", title().equalsIgnoreCase("Yahoo"));
    }

    @And("^I verify yahoo home page$")
    public void I_verify_yahoo_home_page() throws Throwable {
        onPage("yahoo_home");
        Assert.assertTrue("ERROR-APP: Yahoo logo is not displayed", elementPresent("yahoo_home.verify_page"));
        Assert.assertTrue("ERROR-APP: Yahoo logo text is incorrect", getText("yahoo_home.logo_text").equalsIgnoreCase("Yahoo"));
        Assert.assertTrue("ERROR-APP: Yahoo left navigation bar is not displayed", elementPresent("yahoo_home.left_navigation_bar"));
        Assert.assertTrue("ERROR-APP: Yahoo header panel tool bar is not displayed", elementPresent("yahoo_home.header_tool_bar"));
    }

    @And("^I search \"([^\"]*)\" keyword using yahoo$")
    public void search_Keyword_using_yahoo(String keyword) throws Throwable {
        TextBoxes.typeTextbox("yahoo_home.search_text_box", keyword);
        Clicks.clickIfPresent("yahoo_home.search_btn");
        shouldBeOnPage("yahoo_search_result");
    }
}
