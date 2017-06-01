package db.shared.utils.database.models;

import db.framework.utils.StepUtils;
import db.shared.resources.actions.page_object.HomePage;
import db.shared.utils.CommonUtils;
import db.shared.utils.database.utils.DBUtilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class CustomerService extends StepUtils {

    /**
     * Method to get customer information
     *
     * @return customer information
     * @throws Throwable
     */
    public static String[] getAddedCustomerInformation() throws Throwable {
        String[] customerInformation = null;
        String sqlQuery = CommonUtils.getSqlQueries().get("retrieve_customer_info").toString();
        Connection con = DBUtilities.setupMySqlDBConnection();
        PreparedStatement preparedStatement = con.prepareStatement(sqlQuery);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            String FirstName = rs.getString("first_name");
            String LastName = rs.getString("last_name");
            String Email = rs.getString("email_id");
            String ContactNumber = rs.getString("contact_no");
            customerInformation = new String[]{FirstName, LastName, Email, ContactNumber};
        }
        return customerInformation;
    }

    public static String[] getUpdatedCustomerInformation() throws Throwable {
        String[] updatedCustomerInformation = null;
        String sqlQuery = CommonUtils.getSqlQueries().get("retrieve_updated_customer_info").toString().replaceAll("= \\?", "= " + HomePage.record_id + "");
        Connection con = DBUtilities.setupMySqlDBConnection();
        PreparedStatement preparedStatement = con.prepareStatement(sqlQuery);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            String UpdatedFirstName = rs.getString("first_name");
            String UpdatedLastName = rs.getString("last_name");
            String UpdatedEmail = rs.getString("email_id");
            String UpdatedContactNumber = rs.getString("contact_no");
            updatedCustomerInformation = new String[]{UpdatedFirstName, UpdatedLastName, UpdatedEmail, UpdatedContactNumber};
        }
        return updatedCustomerInformation;
    }

    public static String[] verifyRecordDeleted() throws Throwable {
        String[] deletedCustomerInformation = null;
        String sqlQuery = CommonUtils.getSqlQueries().get("retrieve_deleted_customer_info").toString().replaceAll("= \\?", "= " + HomePage.record_id + "");
        Connection con = DBUtilities.setupMySqlDBConnection();
        PreparedStatement preparedStatement = con.prepareStatement(sqlQuery);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            String UpdatedFirstName = rs.getString("first_name");
            String UpdatedLastName = rs.getString("last_name");
            String UpdatedEmail = rs.getString("email_id");
            String UpdatedContactNumber = rs.getString("contact_no");
            deletedCustomerInformation = new String[]{UpdatedFirstName, UpdatedLastName, UpdatedEmail, UpdatedContactNumber};
        }
        return deletedCustomerInformation;
    }

    public static String[] getClientInformation() throws Throwable {
        String[] clientInformation = null;
        String sqlQuery = CommonUtils.getSqlQueries().get("retrieve_employee_type_info").toString();
        Connection con = DBUtilities.setupMSSqlDBConnection();
        PreparedStatement preparedStatement = con.prepareStatement(sqlQuery);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            String TypeCode = rs.getString("EmployeeTypeCode");
            String TypeDesc = rs.getString("EmployeeTypeDesc");
            String ColorCode = rs.getString("ColorCode");
            clientInformation = new String[]{TypeCode, TypeDesc, ColorCode};
        }
        return clientInformation;
    }

    /**
     * Method to get excel information
     *
     * @return excel information
     * @throws Throwable
     */
    public static List<String> getExcelFileData() throws Throwable {
        List<String> excelData = CommonUtils.getTestDataExcel();
        return  excelData;
    }

}
