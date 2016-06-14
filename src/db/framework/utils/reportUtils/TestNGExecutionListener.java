package db.framework.utils.reportUtils;
import org.testng.IExecutionListener;

public class TestNGExecutionListener implements IExecutionListener {

    @Override
    public void onExecutionStart() {
        System.out.println("TestNG is staring the execution");
    }

    @Override
    public void onExecutionFinish() {
        System.out.println("Generating the Masterthought Report");
        GenerateReport.GenerateMasterthoughtReport();
    }
}
