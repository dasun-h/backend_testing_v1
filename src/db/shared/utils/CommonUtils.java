package db.shared.utils;

import db.framework.utils.StepUtils;
import db.framework.utils.Utils;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public abstract class CommonUtils extends StepUtils {
    public static String generateRandomEmail(int length) {
        String allowedChars = "abcdefghijklmnopqrstuvwxyz" + "1234567890";
        String email = RandomStringUtils.random(length, allowedChars);
        email = email.substring(0, email.length()) + "@gmail.com";
        return email;
    }

    public static String generateRandomFirstName() {
        String[] first_name_list = {"JAMES", "ALEXANDER", "ROBERT", "MICHAEL", "WILLIAM", "DAVID", "RICHARD", "CHARLES", "JOSEPH", "THOMAS", "CHRISTOPHER", "KASUN", "DASUN", "CHANDIKA", "DINUKA", "PAVITHRA", "KOSHILA", "RAJITHA"};
        return first_name_list[new Random().nextInt(first_name_list.length)];
    }

    public static String generateRandomLastName() {
        String[] last_name_list = {"SMITH", "JOHNSON", "BROWN", "JONES", "MILLER", "GARCIA", "RODRIGUEZ", "ANDERSON", "TAYLOR", "JUNG", "MOORE", "ALWIS", "HETTIARACHCHI", "PERERA", "HERATH", "JAYAWEERA", "RATHNAYAKE"};
        return last_name_list[new Random().nextInt(last_name_list.length)];
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
}