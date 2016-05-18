package db.shared.utils;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import db.framework.runner.MainRunner;
import db.framework.utils.RunFeature;
import db.framework.utils.StepUtils;
import db.framework.utils.Utils;
import gherkin.formatter.model.Result;
import java.util.Map;

public class Hooks extends StepUtils {

    private SingletonScenario singletonScenario;
    private String resetBrowser = MainRunner.getExParams("reset_browser");
    private long scenarioStartTime;
    private long stepStartTime;

    private Map getScenarioInfo(Scenario scenario) {
        for (Object key : MainRunner.features.keySet()) {
            String scenarioKey = key.toString();
            if (MainRunner.features.get(scenarioKey) instanceof Map) {
                Map scenarioInfo = (Map) MainRunner.features.get(scenarioKey);
                if (scenarioInfo.get("name").equals(scenario.getName()))
                    return scenarioInfo;
            }
        }
        return null;
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        if (RunFeature.checkAborted()) {
            System.exit(-1);
        }
        // make sure driver is initialized
        MainRunner.getWebDriver();

        scenarioStartTime = System.currentTimeMillis();
        this.init(scenario);
        Map sinfo = getScenarioInfo(scenario);
        String line = "";
        if (sinfo != null)
            line = Utils.parseInt(sinfo.get("line"), -1) + " ";
        System.out.println("\n\nScenario:" + line + scenario.getSourceTagNames() + " - " + scenario.getName());
        try {
            singletonScenario = new SingletonScenario(scenario);
        } catch (Exception ex) {
            System.out.println("--> Error Common.beforeScenario(): " + ex.getMessage());
            singletonScenario = null;
        }
    }

    @After
    public void afterScenario() {
        try {
            Result result = this.getLastStepResult();
            if (!this.isScenarioPassed()) {
                String errorMsg = result.getErrorMessage();
                if (errorMsg != null)
                    errorMsg = errorMsg.trim();
                else
                    errorMsg = "Unknown";

                System.err.println("<--------------------->" +
                        "\nFAILED SCENARIO: " + this.getScenarioInfo().get("name").toString().trim() +
                        "\nFAILED STEP: " + this.getScenarioStepName(getScenarioIndex() - 1).trim() +
                        "\nERROR: " + errorMsg +
                        "\n<--------------------->\n\n");
                if (errorMsg.startsWith("sdt.utils.StepUtils$ProductionException:") ||
                        errorMsg.startsWith("sdt.utils.StepUtils$SkipException:"))
                    this.clearStepResult(-1);
            }
            System.out.println("\n--> DURATION: " + Utils.toDuration(System.currentTimeMillis() - scenarioStartTime) + "\n\n\n\n");

        } finally {
            if (resetBrowser != null && resetBrowser.matches("true|t")) {
                if (MainRunner.isDebug())
                    MainRunner.resetDriver(this.isScenarioPassed());
                else
                    MainRunner.resetDriver(true);
            }

            if (singletonScenario != null) {
                singletonScenario.release();
                singletonScenario = null;
            }
        }
    }

    @Before("@Step")
    public void before_step() {
        stepStartTime = System.currentTimeMillis();
        String stepName = this.getScenarioStepName(this.getScenarioIndex());
        System.out.println("\n--->Step " + stepName);
    }

    @After("@Step")
    public void after_step() {
        System.out.println("-->Step duration: " + Utils.toDuration(System.currentTimeMillis() - stepStartTime) + "\n");
    }

}
