package db.framework.utils;

import db.framework.runner.MainRunner;
import org.testng.IExecutionListener;

public class TestNGExecutionListener implements IExecutionListener {

    @Override
    public void onExecutionStart() {
        System.out.println("TestNG is staring the execution");
        MainRunner.getEnvVars();
        MainRunner.PageHangWatchDog.init();
    }

    @Override
    public void onExecutionFinish() {
        System.out.println("Generating the Cucumber JVM Report");
        GenerateReport.GenerateMasterthoughtReport();
        MainRunner.getWebDriver().close();
    }
}
