package db.shared.utils;

/**
 * Created by dasunh on 5/30/2017.
 */
public class DataProvider {

    @org.testng.annotations.DataProvider
    public static Object[][] RegisterNewUser() {
        return new Object[][]{
                {"calum", "scott", "12345678912"}
        };
    }
}
