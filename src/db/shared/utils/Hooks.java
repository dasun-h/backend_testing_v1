package db.shared.utils;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import db.framework.runner.MainRunner;
import db.framework.utils.*;
import gherkin.formatter.model.Result;
import org.junit.Assert;

import java.util.Map;

import static db.framework.utils.ScenarioHelper.*;

public class Hooks extends StepUtils {

    private SingletonScenario singletonScenario;
    private boolean keepBrowser = MainRunner.booleanParam("keep_browser");
    private long scenarioStartTime;
    private long stepStartTime;
    private boolean scenarioSetupComplete = false;
    private static boolean isBackground = false;

    private Map getScenarioInfo(Scenario scenario) {
        for (Object key : MainRunner.features.keySet()) {
            String scenarioKey = key.toString();
            if (MainRunner.features.get(scenarioKey) != null) {
                Map scenarioInfo = MainRunner.features.get(scenarioKey);
                if (scenarioInfo.get("name").equals(scenario.getName())) {
                    return scenarioInfo;
                }
            }
        }
        return null;
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        if (RunFeature.checkAborted()) {
            Assert.fail("Run has been aborted");
        }
        // make sure driver is initialized
        MainRunner.getWebDriver();

        scenarioStartTime = System.currentTimeMillis();
        init(scenario);
        Map sinfo = getScenarioInfo(scenario);
        String line = "";
        if (sinfo != null) {
            line = Utils.parseInt(sinfo.get("line"), -1) + " ";
        }
        System.out.println("\n\nScenario:" + line + scenario.getSourceTagNames() + " - " + scenario.getName());
        if (isScenarioOutline()) {
            System.out.println("Examples: " + getScenarioExamples());
        }
        try {
            singletonScenario = new SingletonScenario(scenario);
        } catch (Exception ex) {
            System.out.println("--> Error Common.beforeScenario(): " + ex.getMessage());
            singletonScenario = null;
        }

        if (!MainRunner.browser.equals("none")) {
            Cookies.deleteAllCookies();
        }
    }

    @After
    public void afterScenario(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                Result result = getFailedStepResult();
                String errorMsg = "Unknown";
                if (result != null) {
                    String error = result.getErrorMessage();
                    if (error != null) {
                        errorMsg = error.trim();
                    }
                }

                System.err.println("<--------------------->" + "\nFAILED SCENARIO: " + scenario.getName().trim());
                if (isScenarioOutline()) {
                    System.err.println("FAILED EXAMPLES: " + getScenarioExamples());
                }
                String stepName = getScenarioStepName(getScenarioIndex() - 1);

                System.err.println("FAILED STEP: " + (stepName == null ? null : stepName.trim()) + "\nERROR: " + errorMsg + "\n<--------------------->\n\n");
                if (errorMsg.startsWith("db.utils.StepUtils$SkipException:")) {
                    clearStepResult(-1);
                }
            }
            System.out.println("\n--> DURATION: " + Utils.toDuration(System.currentTimeMillis() - scenarioStartTime) + "\n\n\n\n");

        } finally {
            if (singletonScenario != null) {
                singletonScenario.release();
                singletonScenario = null;
            }

            MainRunner.PageHangWatchDog.pause(false);
            scenarioSetupComplete = false;

            if (!keepBrowser) {
                if (MainRunner.debugMode) {
                    MainRunner.resetDriver(isScenarioPassed());
                } else {
                    MainRunner.resetDriver(true);
                }
            }
        }
    }

    @Before("@Step")
    public void before_step() {
        stepStartTime = System.currentTimeMillis();
        isBackground = ScenarioHelper.isBackground();
        if (isBackground) {
            ScenarioHelper.incrementBackgroundStepCount();
            System.out.println("--->Running background step...");
            return;
        }
        // an extra result is added by every @Before hook. Need to adjust for this.
        if (!scenarioSetupComplete) {
            resetScenarioOffset();
            try {
                String stepName = getScenarioStepName(getScenarioIndex());
                // the first step will be step 0 and will start with "0:[lineNum] - [step name]"
                while (stepName != null && !stepName.startsWith("0")) {
                    incrementStepIndexOffset();
                    stepName = getScenarioStepName(getScenarioIndex());
                }
            } catch (NullPointerException e) {
                // not a problem
            }
            scenarioSetupComplete = true;
        }
        String stepName = getScenarioStepName(getScenarioIndex());
        System.out.println("\n--->Step " + stepName);
    }

    @After("@Step")
    public void after_step(Scenario scenario) {
        if (scenario.isFailed()) {
            Assert.fail("Scenario unexpectedly failed");
        }
        if (MainRunner.PageHangWatchDog.timedOut) {
            resumePageHangWatchDog();
            Assert.fail("PageHangWatchDog timed out, failing test");
        }
        System.out.println("-->Step duration: " + Utils.toDuration(System.currentTimeMillis() - stepStartTime) + "\n");
    }
}
