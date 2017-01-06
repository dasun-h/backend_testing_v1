package db.shared.resources.actions.page_object;

import org.junit.Assert;
import db.shared.steps.DB_Tester;
import db.framework.utils.StepUtils;

import java.util.*;

import static db.framework.interactions.Clicks.click;
import static db.framework.interactions.Clicks.clickIfPresent;
import static db.framework.interactions.Elements.element;
import static db.framework.interactions.Elements.findElements;
import static db.framework.interactions.Elements.getIndexedText;
import static db.framework.interactions.Wait.untilElementPresent;


public class HomePage extends StepUtils {

    public static List<String> inputted_user_info = new ArrayList<>(Arrays.asList(AddUsersPage.addedRecordInfo()));
    public static List<String> updated_user_info = new ArrayList<>(Arrays.asList(EditUsersPage.editedRecordInfo()));
    public static String record_id;

    public static void transactions(String transaction_type_btn) throws Throwable {
        switch (transaction_type_btn.toLowerCase()) {
            case "add users":
                clickIfPresent(element("home.add_users_btn"));
                Assert.assertTrue("ERROR: Page is not loaded successfully", untilElementPresent(element("add_users.create_new_record_btn")));
                break;
            case "update users":
                record_id = getIndexedText(element("home.table_row"), 0);
                clickIfPresent(element("home.update_link"));
                Assert.assertTrue("ERROR: Page is not loaded successfully", untilElementPresent(element("edit_users.edit_record_btn")));
                break;
            case "delete users":
                record_id = getIndexedText(element("home.table_row"), 0);
                clickIfPresent(element("home.remove_link"));
                Assert.assertTrue("ERROR: Page is not loaded successfully", untilElementPresent(element("delete_users.remove_confirmation_btn")));
                break;
            default:
                Assert.fail("Unable to find your button action type");
        }
    }

    public static void verifyAddedRecordsWithUI() throws Throwable {
        List<String> ui_user_info = new ArrayList<String>(Arrays.asList());
        int recordCount = findElements(element("home.table_row")).size();
        for (int i = 1; i < recordCount; i++) {
            String row_value = getIndexedText(element("home.table_row"), i);
            ui_user_info.add(row_value);
        }
        ui_user_info.removeAll(Collections.singleton(""));
        Iterator<String> ui_it = ui_user_info.iterator();
        Iterator<String> inputted_it = inputted_user_info.iterator();
        while (ui_it.hasNext()) {
            String ui_record_value = ui_it.next();
            while (inputted_it.hasNext()) {
                String inputted_record_value = inputted_it.next();
                if (ui_record_value.equals(inputted_record_value)) {
                    System.out.print("UI verification successfully passed with inputted data values: [ " + ui_record_value + ", " + inputted_record_value + " ]" + "\n" + "");
                    break;
                } else {
                    Assert.fail("UI verification failed for: [ " + ui_record_value + ", " + inputted_record_value + " ]" + "\n" + "");
                }
            }
        }
    }

    public static void removeRecordsFromDB() throws Throwable {
        for (int i = 0; i < findElements(element("home.remove_link")).size(); ) {
            click(findElements(element("home.remove_link")).get(i));
            click(element("delete_users.remove_confirmation_btn"));
            click(element("delete_users.back_to_index_btn"));
        }
        Assert.assertTrue("ERROR: Still you have not deleted records left in the data base", getIndexedText(element("home.table_row"), 0).contains("Nothing here..."));
    }

    public static void verifyAddedRecordsWithDB() throws Throwable {
        List<String> customerInformationUI = HomePage.inputted_user_info;
        Iterator<String> ui_it = customerInformationUI.iterator();
        Iterator<String> db_it = DB_Tester.customerInformationDB.iterator();
        while (ui_it.hasNext()) {
            String ui_record_value = ui_it.next();
            while (db_it.hasNext()) {
                String db_record_value = db_it.next();
                if (ui_record_value.equals(db_record_value)) {
                    System.out.print("Backend verification successfully passed for data values: [ " + ui_record_value + ", " + db_record_value + " ]" + "\n" + "");
                    break;
                } else {
                    Assert.fail("Backend verification failed for: [ " + ui_record_value + ", " + db_record_value + " ]" + "\n" + "");
                }
            }
        }
    }

    public static void verifyUpdatedRecordsWithUI() throws Throwable {
        List<String> ui_user_info = new ArrayList<String>(Arrays.asList());
        int recordCount = findElements(element("home.table_row")).size();
        for (int i = 1; i < recordCount; i++) {
            String row_value = getIndexedText(element("home.table_row"), i);
            ui_user_info.add(row_value);
        }
        ui_user_info.removeAll(Collections.singleton(""));
        Iterator<String> ui_it = ui_user_info.iterator();
        Iterator<String> updated_it = updated_user_info.iterator();
        while (ui_it.hasNext()) {
            String ui_record_value = ui_it.next();
            while (updated_it.hasNext()) {
                String updated_record_value = updated_it.next();
                if (ui_record_value.equals(updated_record_value)) {
                    System.out.print("UI verification successfully passed with updated data values: [ " + ui_record_value + ", " + updated_record_value + " ]" + "\n" + "");
                    break;
                } else {
                    Assert.fail("UI verification failed for: [ " + ui_record_value + ", " + updated_record_value + " ]" + "\n" + "");
                }
            }
        }
    }

    public static void verifyEditedRecordsWithDB() throws Throwable {
        List<String> editedCustomerInformationUI = HomePage.updated_user_info;
        Iterator<String> edited_ui_it = editedCustomerInformationUI.iterator();
        Iterator<String> edited_db_it = DB_Tester.updatedCustomerInformationDB.iterator();
        while (edited_ui_it.hasNext()) {
            String edited_ui_record_value = edited_ui_it.next();
            while (edited_db_it.hasNext()) {
                String edited_db_record_value = edited_db_it.next();
                if (edited_ui_record_value.equals(edited_db_record_value)) {
                    System.out.print("Backend verification successfully passed for edited data values: [ " + edited_ui_record_value + ", " + edited_db_record_value + " ]" + "\n" + "");
                    break;
                } else {
                    Assert.fail("Backend verification failed for: [ " + edited_ui_record_value + ", " + edited_db_record_value + " ]" + "\n" + "");
                }
            }
        }
    }

}
