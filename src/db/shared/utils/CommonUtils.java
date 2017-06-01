package db.shared.utils;

import com.github.javafaker.Faker;
import db.framework.utils.StepUtils;
import db.framework.utils.Utils;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class CommonUtils extends StepUtils {

    public static String userName;
    public static String password;
    public static String random_string;
    public static String email;
    public static String phoneNo;
    public static String firstName;
    public static String lastName;
    public static int employeeID;

    /**
     * Util: Return Random Email Address
     *
     * @return String
     */
    public static String getEmailAddress() {
        String name = new Faker().name().firstName();
        email = name + "@malinator.com";
        return email;
    }

    public static String randomString(int length) {
        SecureRandom random = new SecureRandom();
        char[] chars = new char[length];
        for (int i = 0; i < chars.length; i++) {
            int v = random.nextInt(10 + 26 + 26);
            char c;
            if (v < 10) {
                c = (char) ('0' + v);
            } else if (v < 36) {
                c = (char) ('a' - 10 + v);
            } else {
                c = (char) ('A' - 36 + v);
            }
            chars[i] = c;
        }
        random_string = new String(chars);
        return random_string;
    }

    /**
     * Util: Return Random Phone Number
     *
     * @return static
     */
    public static String getPhoneNumber() {
        phoneNo = new Faker().phoneNumber().phoneNumber();
        return phoneNo;
    }

    public static String generateRandomFirstName() {
        Faker faker = new Faker();
        firstName = faker.name().firstName();
        return firstName;
    }

    public static String generateRandomLastName() {
        Faker faker = new Faker();
        lastName = faker.name().lastName();
        return lastName;
    }

    public static int genRandomNumber() {
        Random r = new Random(System.currentTimeMillis());
        employeeID = 10000 + r.nextInt(20000);
        return employeeID;
    }

    public static String generateRandomEmail(int length) {
        String allowedChars = "abcdefghijklmnopqrstuvwxyz" + "1234567890";
        String email = RandomStringUtils.random(length, allowedChars);
        email = email.substring(0, email.length()) + "@gmail.com";
        return email;
    }

    public static String generateRandomMonth() {
        String[] monthArray = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        return monthArray[new Random().nextInt(monthArray.length)];
    }

    public static int generateRandomDateIndex() {
        int min = 1;
        int max = 31;
        Random dates = new Random();
        return dates.nextInt(max - min) + min;
    }

    public static int generateRandomYearIndex() {
        int min = 17;
        int max = 87;
        Random years = new Random();
        return years.nextInt(max - min) + min;
    }

    public static String generateRandomGender() {
        String[] genderArray = {"Female", "Male"};
        return genderArray[new Random().nextInt(genderArray.length)];
    }

    public static String generateRandomSecurityAnswer() {
        String[] security_answerArray = {"Metallica", "Billy Joel", "The Beatles", "Michael Jackson", "Justin Bieber"};
        return security_answerArray[new Random().nextInt(security_answerArray.length)];
    }

    public static String generateRandomPhoneNumber() {
        int length = 10;
        char[] digits = new char[length];
        // Make sure the leading digit isn't 0.
        Random random_no = new Random();
        digits[0] = (char) ('1' + random_no.nextInt(9));
        for (int i = 1; i < length; i++) {
            digits[i] = (char) ('0' + random_no.nextInt(10));
        }
        return new String(digits);
    }

    public static String generatedUsrName(int length) {
        char[] CHARSET_AZ_09 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        Random randomm = new SecureRandom();
        char[] result = new char[length];
        for (int i = 0; i < result.length; i++) {
            int randomCharIndex = randomm.nextInt(CHARSET_AZ_09.length);
            result[i] = CHARSET_AZ_09[randomCharIndex];
        }
        userName = new String(result);
        return userName;

    }

    public static String generatedPassword(int length) {
        String allowedChars = "0123456789abcdefghijklmnopqrstuvwABCDEFGHIJKLMNOP!§$%&?*+#";
        SecureRandom random = new SecureRandom();
        StringBuilder pass = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            pass.append(allowedChars.charAt(random.nextInt(allowedChars.length())));
        }
        password = pass.toString();
        return password;
    }

    /**
     * Gets a resource file with a given name
     *
     * @param fname file name
     * @return resulting File
     */

    private static String getResourceFile(String fname) {
        File file = new File(fname);//full file path URL
        String absolutePath = file.getAbsolutePath();
        String dirPath = absolutePath.replace(fname, "");
        String fullPath = dirPath + "src/db/shared/resources/data/" + fname;
        return fullPath;
    }

    /**
     * Method to return SQL Queries
     *
     * @return SQL queries as json object
     */
    public static JSONObject getSqlQueries() {
        File queries = new File(getResourceFile("queries.json"));
        JSONObject jsonObject = null;
        try {
            String jsonTxt = Utils.readTextFile(queries);
            jsonObject = new JSONObject(jsonTxt);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * Method to read excel file
     *
     * @return data from the excel sheet using jxl library
     */
    public static List<String> getTestDataExcel() throws IOException, BiffException {
        List<String> employee_names_list = new ArrayList<>();
        File test_data = new File(getResourceFile("test_data.xls"));
        Workbook wb = Workbook.getWorkbook(test_data);
        int num_of_sheets = wb.getNumberOfSheets();
        for (int i = 0; i < num_of_sheets; i++) {
            int column_count = wb.getSheet(i).getColumns();
            int row_count = wb.getSheet(i).getRows();
            for (int j = 0; j < column_count; j++) {
                for (int k = 0; k < row_count; k++) {
                    String sh = wb.getSheet(i).getCell(j, k).getContents();
                    employee_names_list.add(sh);
                }
            }
        }
        return employee_names_list;
    }

}