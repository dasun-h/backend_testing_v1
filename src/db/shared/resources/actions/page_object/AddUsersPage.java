package db.shared.resources.actions.page_object;

import org.junit.Assert;
import db.shared.utils.CommonUtils;
import db.framework.utils.StepUtils;

public class AddUsersPage extends StepUtils {

    public static String firstName = CommonUtils.generateRandomFirstName();
    public static String lastName = CommonUtils.generateRandomLastName();
    public static String Email = CommonUtils.generateRandomEmail(9);
    public static String contactID = CommonUtils.generateRandomPhoneNumber();

    public static void AddUsers() {
        typeTextbox(element("add_users.first_name"), firstName);
        typeTextbox(element("add_users.last_name"), lastName);
        typeTextbox(element("add_users.email"), Email);
        typeTextbox(element("add_users.contact_id"), contactID);
        javascriptClick(element("add_users.create_new_record_btn"));
        Assert.assertTrue("ERROR: Record not added to the database", waitUntilElementPresent(element("add_users.success_msg")));
        String success_msg = getElementText(element("add_users.success_msg"));
        if (success_msg.equals("WOW! Record was inserted successfully HOME!"))
            System.out.print("WOW! Record was inserted successfully");
        else {
            Assert.fail("ERROR-Application: Record was not inserted");
        }
    }

    public static String[] addedRecordInfo() {
        String[] Record = new String[]{firstName, lastName, Email, contactID};
        return Record;
    }

}
