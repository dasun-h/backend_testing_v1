package db.shared.steps;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.PendingException;
import db.framework.interactions.Navigate;
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
    @Given("^I visit the google home page$")
    public void I_visit_the_google_home_page() throws Throwable {
        Navigate.visit();
        Assert.assertTrue("ERROR-APP: Google home page is not loaded properly", title().equalsIgnoreCase("Google"));
    }

    @And("^I verify google home page$")
    public void I_verify_google_home_page() throws Throwable {
        elementPresent("google_home.verify_page");
        Assert.assertTrue("ERROR-APP: Google sub logo text incorrect", getText("google_home.logo_sub_txt").equalsIgnoreCase("Sri Lanka"));
    }
}
