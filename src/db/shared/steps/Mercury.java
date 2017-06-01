package db.shared.steps;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.PendingException;
import db.framework.interactions.*;
import db.framework.utils.StepUtils;
import db.shared.utils.CommonUtils;

import org.junit.Assert;
import static db.framework.interactions.Elements.element;
import static db.shared.utils.CommonUtils.getTestDataFromExcelSheet;
import static db.shared.utils.CommonUtils.new_account_details;
import static db.shared.utils.CommonUtils.writeToAExcelSheet;

/**
 * Created by dasunh on 6/1/2017.
 */
public class Mercury extends StepUtils {

    @Given("^I visit the mercury home page$")
    public void I_visit_the_mercury_home_page() throws Throwable {
        Navigate.visit();
        Assert.assertTrue("ERROR-APP: Mercury home page is not loaded properly", title().equalsIgnoreCase("Welcome: Mercury Tours"));
        shouldBeOnPage("mercury_home");
        System.out.print("User successfully landing into the mercury home page");
    }

    @When("^I navigate to the user registration page$")
    public void I_navigate_to_the_user_registration_page() throws Throwable {
        Clicks.clickIfPresent("mercury_home.lnk_register");
        shouldBeOnPage("mercury_register");
        System.out.print("User successfully navigated into the user registration page");
    }

    @And("^I registered as a new user$")
    public void I_registered_as_a_new_user() throws Throwable {
        TextBoxes.typeTextbox(element("mercury_register.txt_f_name"), CommonUtils.generateRandomFirstName());
        TextBoxes.typeTextbox(element("mercury_register.txt_l_name"), CommonUtils.generateRandomLastName());
        TextBoxes.typeTextbox(element("mercury_register.txt_phone"), CommonUtils.getPhoneNumber());
        TextBoxes.typeTextbox(element("mercury_register.txt_email"), CommonUtils.getEmailAddress());
        TextBoxes.typeTextbox(element("mercury_register.txt_address"), CommonUtils.randomString(15));
        TextBoxes.typeTextbox(element("mercury_register.txt_city"), CommonUtils.randomString(8));
        TextBoxes.typeTextbox(element("mercury_register.txt_state"), CommonUtils.randomString(5));
        TextBoxes.typeTextbox(element("mercury_register.txt_postal_code"), String.valueOf(CommonUtils.genRandomNumber()));
        DropDowns.selectByText(element("mercury_register.drp_country"), "SRI LANKA");
        TextBoxes.typeTextbox(element("mercury_register.txt_user_name"), CommonUtils.generatedUsrName(9));
        TextBoxes.typeTextbox(element("mercury_register.txt_password"), CommonUtils.generatedPassword(10));
        TextBoxes.typeTextbox(element("mercury_register.txt_c_password"), CommonUtils.password);
        Clicks.clickIfPresent("mercury_register.btn_submit");
        Wait.untilElementPresent("mercury_reg_confirmation.lbl_user_name");
        shouldBeOnPage("mercury_reg_confirmation");
    }

    @Then("^I verify my registration success state$")
    public void I_verify_my_registration_success_state() throws Throwable {
        shouldBeOnPage("mercury_reg_confirmation");
        String actual_user_name = Elements.getText("mercury_reg_confirmation.lbl_user_name").split(" ")[5].replace(".", "").toString();
        Assert.assertTrue("ERROR-APP: User name you have created is not matching with the registration confirmation page user name", CommonUtils.userName.equalsIgnoreCase(actual_user_name));
        System.out.print(CommonUtils.userName + ": User New Account Successfully Created");
        //Save user name and password  into a excel sheet
        writeToAExcelSheet();
    }

    @And("^I logout from my account$")
    public void I_logout_from_my_account() throws Throwable {
        Clicks.clickIfPresent("mercury_reg_confirmation.lnk_signOff");
        Wait.untilElementPresent("mercury_login.txt_user_name");
        shouldBeOnPage("mercury_login");
        System.out.print(CommonUtils.userName + ": User successfully Logout");
    }

    @When("^I login to my new account$")
    public void I_login_to_my_new_account() throws Throwable {
        shouldBeOnPage("mercury_login");
        //Retrieve my new account details
        getTestDataFromExcelSheet();
        TextBoxes.typeTextbox(element("mercury_login.txt_user_name"), new_account_details.get(0));
        TextBoxes.typeTextbox(element("mercury_login.txt_password"), new_account_details.get(1));
        Clicks.clickIfPresent("mercury_login.btn_login");
        System.out.print(CommonUtils.userName + ": Successfully Logged in");
    }
}
