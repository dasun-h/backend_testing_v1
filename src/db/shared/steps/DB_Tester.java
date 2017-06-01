package db.shared.steps;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import db.framework.interactions.Navigate;
import db.framework.utils.StepUtils;
import db.shared.resources.actions.page_object.AddUsersPage;
import db.shared.resources.actions.page_object.EditUsersPage;
import db.shared.resources.actions.page_object.HomePage;
import db.shared.utils.database.models.CustomerService;
import org.junit.Assert;

import java.util.Arrays;
import java.util.List;

import static db.framework.interactions.Clicks.click;
import static db.framework.interactions.Clicks.javascriptClick;
import static db.framework.interactions.Elements.*;
import static db.framework.interactions.Wait.untilElementPresent;

public class DB_Tester extends StepUtils {

    public static List<String> customerInformationDB;
    public static List<String> updatedCustomerInformationDB;

    @Given("^I visit the crud site home page$")
    public void I_visit_the_crud_site_home_page() throws Throwable {
        Navigate.visit("home");
        Assert.assertTrue("ERROR: CRUD Home page is not loaded properly", untilElementPresent(element("home.add_users_btn")));
    }

    @When("^I navigate to the \"([^\"]*)\" page$")
    public void I_navigate_to_the_page(String transaction_type_btn) throws Throwable {
        HomePage.transactions(transaction_type_btn);
    }

    @And("^I \"([^\"]*)\" a record to my database$")
    public void I_a_record_to_my_database(String transaction_type) throws Throwable {
        switch (transaction_type.toLowerCase()) {
            case "add":
                AddUsersPage.AddUsers();
                javascriptClick(element("add_users.back_to_index_btn"));
                break;
            case "update":
                EditUsersPage.EditUsers();
                javascriptClick(element("edit_users.cancel_btn"));
                break;
            case "delete":
                click(element("delete_users.remove_confirmation_btn"));
                String success_msg = getText(element("delete_users.remove_success_message"));
                if (success_msg.equals("Success! record was deleted..."))
                    System.out.print("Success! record was deleted...");
                else {
                    Assert.fail("ERROR-Application: Record was not deleted");
                }
                click(element("delete_users.back_to_index_btn"));
                Assert.assertTrue("ERROR: Still you have not deleted records left in the data base", getIndexedText(element("home.table_row"), 0).contains("Nothing here..."));
                break;
            default:
                Assert.fail("Unable to find your action type");
        }
    }

    @Then("^I verify added record display in the home page$")
    public void I_verify_added_record_display_in_the_home_page() throws Throwable {
        HomePage.verifyAddedRecordsWithUI();
    }

    @And("^I remove all the initial test data from database$")
    public void I_remove_all_the_initial_test_data_from_database() throws Throwable {
        if (getText(element("home.table_row")).contains("Nothing here...")) {
            System.out.print("No Records found for delete");
        } else {
            HomePage.removeRecordsFromDB();
        }
    }

    @When("^I retrieve added record details from database$")
    public void I_retrieve_added_record_details_from_database() throws Throwable {
        String[] customerInformation = CustomerService.getAddedCustomerInformation();
        if (customerInformation == null)
            Assert.fail("ERROR-DATA: Customer information not available in database!!");
        customerInformationDB = Arrays.asList(customerInformation);
    }

    @Then("^I verify added record values with backend record values$")
    public void I_verify_added_record_values_with_backend_record_values() throws Throwable {
        HomePage.verifyAddedRecordsWithDB();
    }

    @Then("^I verify updated record display in the home page$")
    public void I_verify_updated_record_display_in_the_home_page() throws Throwable {
        HomePage.verifyUpdatedRecordsWithUI();
    }

    @When("^I retrieve updated record details from database$")
    public void I_retrieve_updated_record_details_from_database() throws Throwable {
        String[] updatedCustomerInformation = CustomerService.getUpdatedCustomerInformation();
        if (updatedCustomerInformation == null)
            Assert.fail("ERROR-DATA: Updated customer information not available in database!!");
        updatedCustomerInformationDB = Arrays.asList(updatedCustomerInformation);
    }

    @Then("^I verify updated record values with backend record values$")
    public void I_verify_updated_record_values_with_backend_record_values() throws Throwable {
        HomePage.verifyEditedRecordsWithDB();
    }

    @Then("^I verify whether the record deleted from backend$")
    public void I_verify_whether_the_record_deleted_from_backend() throws Throwable {
        String[] deletedCustomerInformation = CustomerService.verifyRecordDeleted();
        if (deletedCustomerInformation == null) {
            System.out.print("" + HomePage.inputted_user_info + " Record successfully deleted from the database:");
        } else {
            Assert.fail("ERROR-DATA: Record not deleted from the database!!");
        }
    }

    @When("^I retrieve test data from excel sheet$")
    public void I_retrieve_test_data_from_excel_sheet() throws Throwable {
        String excelInfo = CustomerService.getExcelFileData().get(0);

    }
}
