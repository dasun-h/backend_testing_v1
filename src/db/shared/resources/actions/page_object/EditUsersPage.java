package db.shared.resources.actions.page_object;

import org.junit.Assert;
import db.shared.utils.CommonUtils;
import db.framework.utils.StepUtils;

public class EditUsersPage extends StepUtils {
    public static String edit_firstName = CommonUtils.generateRandomFirstName();
    public static String edit_lastName = CommonUtils.generateRandomLastName();
    public static String edit_Email = CommonUtils.generateRandomEmail(9);
    public static String edit_contactID = CommonUtils.generateRandomPhoneNumber();

    public static void EditUsers() {
        typeTextbox(element("edit_users.edit_first_name"), edit_firstName);
        typeTextbox(element("edit_users.edit_last_name"), edit_lastName);
        typeTextbox(element("edit_users.edit_email"), edit_Email);
        typeTextbox(element("edit_users.edit_contact_id"), edit_contactID);
        javascriptClick(element("edit_users.edit_record_btn"));
        Assert.assertTrue("ERROR: Record was not edited in to the database", waitUntilElementPresent(element("edit_users.edit_success_msg")));
        String success_msg = getElementText(element("edit_users.edit_success_msg"));
        if (success_msg.equals("WOW! Record was updated successfully HOME!"))
            System.out.print("WOW! Record was updated successfully");
        else{
            Assert.fail("ERROR-Application: Record was not updated");
        }
    }

    public static String[] editedRecordInfo() {
        String[] Record = new String[]{edit_firstName, edit_lastName, edit_Email, edit_contactID};
        return Record;
    }
}
