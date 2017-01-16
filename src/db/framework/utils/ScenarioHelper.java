package db.framework.utils;

import com.google.gson.internal.LinkedTreeMap;
import cucumber.api.Scenario;
import cucumber.runtime.ScenarioImpl;
import db.framework.runner.MainRunner;
import gherkin.formatter.model.Result;

import java.util.ArrayList;
import java.util.List;

public class ScenarioHelper {

    public static int outlineCount = 1;
    protected static ScenarioImpl scenario = null;
    private static LinkedTreeMap scenarioInfo = new LinkedTreeMap();
    private static int stepOffset = 1;
    private static int backgroundStepCount = 0;


    /**
     * Initializes a scenario
     *
     * @param s scenario to initialize
     */
    public static void init(Scenario s) {
        if (scenario != null && s.getName().equals(scenario.getName())) {
            outlineCount++;
        } else {
            outlineCount = 1;
        }
        scenario = (ScenarioImpl) s;
        for (Object o : MainRunner.features.values()) {
            if (o instanceof LinkedTreeMap) {
                LinkedTreeMap savedScenario = (LinkedTreeMap) o;
                if (scenario.getName().equals(savedScenario.get("name"))) {
                    scenarioInfo = savedScenario;
                    break;
                }
            }
        }
        MainRunner.URLStack = new ArrayList<>();
    }

    public static void incrementBackgroundStepCount() {
        backgroundStepCount++;
    }

    public static boolean isBackground() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stack) {
            if (element.toString().contains("runBackground")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the index of the current step in the scenario
     *
     * @return the index of the current step in the scenario
     */
    public static int getScenarioIndex() {
        return scenario.getStepResuls().size() - stepOffset - backgroundStepCount;
    }

    /**
     * Resets the offset of the step index
     * <p>
     * The list of results includes not only steps and scenario names,
     * but also a result for each pre- and post-run hook. We need to adjust for
     * this offset.
     * </p>
     */
    public static void resetScenarioOffset() {
        stepOffset = 1;
    }

    /**
     * Increments the offset of the step index
     * <p>
     * The list of results includes not only steps and scenario names,
     * but also a result for each pre-run hook. We need to adjust for
     * this offset.
     * </p>
     */
    public static void incrementStepIndexOffset() {
        stepOffset++;
    }

    /**
     * Checks if current scenario is a scenario outline
     *
     * @return true if current scenario is a scenario outline
     */
    public static boolean isScenarioOutline() {
        ArrayList examples = (ArrayList) scenarioInfo.get("examples");
        return examples != null;
    }

    /**
     * Get the examples for the current scenario outline
     *
     * @return A string with the current examples or null if current scenario is not an outline.
     */
    public static String getScenarioExamples() {
        ArrayList examples = (ArrayList) scenarioInfo.get("examples");
        if (examples == null) {
            return null;
        }
        ArrayList rows = (ArrayList) ((LinkedTreeMap) examples.get(0)).get("rows");
        LinkedTreeMap row = (LinkedTreeMap) rows.get(outlineCount);
        ArrayList values = (ArrayList) row.get("cells");
        return Utils.listToString(values, " | ", null);
    }

    /**
     * Gets the name of the step at an index
     *
     * @param stepIndex the index to find
     * @return the name of the step at stepIndex
     */
    public static String getScenarioStepName(int stepIndex) {
        if (scenarioInfo == null) {
            System.err.println("Can't get scenario step name - scenario not initialized");
            return null;
        }
        ArrayList list = (ArrayList) scenarioInfo.get("steps");
        LinkedTreeMap currentStep = (LinkedTreeMap) list.get(stepIndex);
        return stepIndex + ": " + Utils.parseInt(currentStep.get("line"), -1) + " - " + currentStep.get("name");
    }

    /**
     * Gets information about the current scenario
     *
     * @return scenario information
     */
    public static LinkedTreeMap getScenarioInfo() {
        return scenarioInfo;
    }

    /**
     * Gets the result of a step
     *
     * @param step index of step to get
     * @return last step result
     */
    public static Result getStepResult(int step) {
        if (step == -1) {
            step = getScenarioIndex();
        }
        return scenario.getStepResuls().get(step);
    }

    /**
     * Checks if a step has passed
     *
     * @param step index of step to check
     * @return true if step passed
     */
    public boolean isStepPassed(int step) {
        return getStepResult(step).getStatus().equals("passed");
    }

    /**
     * Clears the result of a step
     *
     * @param step index of step to clear
     * @return cleared step result
     */
    public static Result clearStepResult(int step) {
        if (step == -1) {
            step = getScenarioIndex();
        }
        List<Result> steps = scenario.getStepResuls();
        return steps.remove(step);
    }

    /**
     * Gets the result of the last step
     *
     * @return last step result
     */
    public static Result getLastStepResult() {
        return getStepResult(-1);
    }

    /**
     * Gets the Result of a failed step if there is one, otherwise null
     *
     * @return the failed step Result
     */
    public static Result getFailedStepResult() {
        List<Result> results = scenario.getStepResuls();
        for (Result result : results) {
            if (!result.getStatus().equals("passed")) {
                return result;
            }
        }
        return null;
    }

    /**
     * Checks if the last scenario passed
     *
     * @return true if last scenario passed
     */
    public static boolean isScenarioPassed() {
        List<Result> results = scenario.getStepResuls();
        for (Result result : results) {
            if (!result.getStatus().equals("passed")) {
                return false;
            }
        }
        return true;
    }
}
