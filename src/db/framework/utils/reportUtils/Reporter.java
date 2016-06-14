package db.framework.utils.reportUtils;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import cucumber.api.testng.AbstractTestNGCucumberTests;
import cucumber.api.testng.TestNGCucumberRunner;
import org.junit.runner.RunWith;
import org.testng.annotations.Test;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/db/projects/BackendTesting/features/", format = { "pretty", "html:target/site/cucumber-pretty", "json:target/cucumber.json" }, glue = {"db.shared.steps"})
public class Reporter extends AbstractTestNGCucumberTests {

    @Test(groups = "examples-testng", description = "Example of using TestNGCucumberRunner to invoke Cucumber")
    public void runCukes() {
        new TestNGCucumberRunner(getClass()).runCukes();
    }
}
