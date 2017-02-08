package db.shared.steps;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.PendingException;
import db.framework.interactions.*;
import db.framework.utils.StepUtils;
import org.apache.logging.log4j.core.Logger;
import org.junit.Assert;

import static db.framework.interactions.Elements.*;
import static db.framework.interactions.Elements.findElement;

/**
 * Created by dasunh on 1/3/2017.
 */
public class Sample extends StepUtils {

    @Given("^I visit the yahoo home page$")
    public void I_visit_the_yahoo_home_page() throws Throwable {
        Navigate.visit();
        Assert.assertTrue("ERROR-APP: Google home page is not loaded properly", title().equalsIgnoreCase("Yahoo"));
    }

    @Given("^I visit the canopy labs login page$")
    public void I_visit_the_canopy_labs_login_page() throws Throwable {
        Navigate.visit();
        shouldBeOnPage("canopy_labs_login");
    }

    @Given("^I visit the drag and drop html5demo app$")
    public void I_visit_the_drag_and_drop_html5demo_app() throws Throwable {
        Navigate.visit();
        shouldBeOnPage("html5demo");
    }

    @Given("^I visit the drag and drop herokuapp app$")
    public void I_visit_the_drag_and_drop_herokuapp_app() throws Throwable {
        Navigate.visit();
        shouldBeOnPage("herokuapp");
    }

    @And("^I verify yahoo home page$")
    public void I_verify_yahoo_home_page() throws Throwable {
        onPage("yahoo_home");
        Assert.assertTrue("ERROR-APP: Yahoo logo is not displayed", elementPresent("yahoo_home.verify_page"));
        Assert.assertTrue("ERROR-APP: Yahoo logo text is incorrect", getText("yahoo_home.logo_text").equalsIgnoreCase("Yahoo"));
        //Assert.assertTrue("ERROR-APP: Yahoo left navigation bar is not displayed", elementPresent("yahoo_home.left_navigation_bar"));
        Assert.assertTrue("ERROR-APP: Yahoo header panel tool bar is not displayed", elementPresent("yahoo_home.header_tool_bar"));
    }

    @And("^I search \"([^\"]*)\" keyword using yahoo$")
    public void search_Keyword_using_yahoo(String keyword) throws Throwable {
        TextBoxes.typeTextbox("yahoo_home.search_text_box", keyword);
        Clicks.clickIfPresent("yahoo_home.search_btn");
        shouldBeOnPage("yahoo_search_result");
    }

    @And("^I login to the canopy labs using \"([^\"]*)\" and \"([^\"]*)\"$")
    public void I_Login_to_the_canopy_labs_using_and(String user_name, String password) throws Throwable {
        TextBoxes.typeTextbox(element("canopy_labs_login.user_name"), user_name);
        TextBoxes.typeTextbox(element("canopy_labs_login.password"), password);
        Clicks.clickIfPresent(element("canopy_labs_login.login_btn"));
    }

    @When("^I navigate to the canopy labs funnel generator page$")
    public void I_navigate_to_the_canopy_labs_funnel_generator_page() throws Throwable {
        Clicks.clickIfPresent(element("canopy_labs_home.create_btn"));
    }

    @And("^I drag and drop canopy labs sequence")
    public void I_drag_and_drop_canopy_labs_sequence() throws Throwable {
        dragAndDropForNormalApps(element("canopy_labs_create_funnel.draggable"), element("canopy_labs_create_funnel.droppable"));
        Assert.assertFalse("Error - Drag and Drop Failed", elementPresent(element("canopy_labs_create_funnel.droppable")));
        System.out.print("Drag and Drop Successfully Triggered");
    }

    @And("^I drag and drop heroku app elements$")
    public void I_drag_and_drop_heroku_app_elements() throws Throwable {
        dragAndDropForHTML5(element("herokuapp.draggable"), element("herokuapp.droppable"));
        Assert.assertFalse("Error - Drag and Drop Failed", findElement(element("herokuapp.droppable")).getText().equalsIgnoreCase("B"));
        System.out.print("Drag and Drop Successfully Triggered");
    }

    @And("^I drag and drop html5demo app elements$")
    public void I_drag_and_drop_html5demo_app_elements() throws Throwable {
        dragAndDropForHTML5(element("html5demo.draggable"), element("html5demo.droppable"));
        Assert.assertFalse("Error - Drag and Drop Failed", elementPresent(element("html5demo.draggable")));
        System.out.print("Drag and Drop Successfully Triggered");
    }

    @Then("^I should navigate to the canopy labs home page$")
    public void I_should_navigate_to_the_canopy_labs_home_page() throws Throwable {
        Elements.elementPresent(element("canopy_labs_home.create_btn"));
    }

}
