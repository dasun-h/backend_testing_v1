package db.framework.utils.reportUtils;

import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GenerateReport {

    public static void GenerateMasterthoughtReport(){
        try{
            File reportOutputDirectory = new File("target");
            List<String> jsonFiles = new ArrayList<>();
            jsonFiles.add("cucumber.json");

            String jenkinsBasePath = "";
            String buildNumber = "75";
            String projectName = "cucumber-jvm";
            boolean skippedFails = true;
            boolean pendingFails = false;
            boolean undefinedFails = true;
            boolean missingFails = true;
            boolean runWithJenkins = false;
            boolean parallelTesting = false;

            Configuration configuration = new Configuration(reportOutputDirectory, projectName);

            configuration.setStatusFlags(skippedFails, pendingFails, undefinedFails, missingFails);
            configuration.setParallelTesting(parallelTesting);
            configuration.setJenkinsBasePath(jenkinsBasePath);
            configuration.setRunWithJenkins(runWithJenkins);
            configuration.setBuildNumber(buildNumber);
            ReportBuilder reportBuilder = new ReportBuilder(jsonFiles, configuration);
            reportBuilder.generateReports();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void main(String args[]) {
        try {
            File reportOutputDirectory = new File("target");
            List<String> jsonFiles = new ArrayList<>();
            jsonFiles.add("cucumber.json");

            String jenkinsBasePath = "";
            String buildNumber = "75";
            String projectName = "cucumber-jvm";
            boolean skippedFails = true;
            boolean pendingFails = false;
            boolean undefinedFails = true;
            boolean missingFails = true;
            boolean runWithJenkins = false;
            boolean parallelTesting = false;

            Configuration configuration = new Configuration(reportOutputDirectory, projectName);

            configuration.setStatusFlags(skippedFails, pendingFails, undefinedFails, missingFails);
            configuration.setParallelTesting(parallelTesting);
            configuration.setJenkinsBasePath(jenkinsBasePath);
            configuration.setRunWithJenkins(runWithJenkins);
            configuration.setBuildNumber(buildNumber);
            ReportBuilder reportBuilder = new ReportBuilder(jsonFiles, configuration);
            reportBuilder.generateReports();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
