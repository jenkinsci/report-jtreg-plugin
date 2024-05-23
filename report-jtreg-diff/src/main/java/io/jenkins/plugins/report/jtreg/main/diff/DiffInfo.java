package io.jenkins.plugins.report.jtreg.main.diff;

import io.jenkins.plugins.report.jtreg.CommonOptions;
import io.jenkins.plugins.report.jtreg.formatters.BasicFormatter;

import java.io.File;

public class DiffInfo extends CommonOptions {
    private String jobOne;
    private String buildOne;
    private String jobTwo;
    private String buildTwo;
    private BasicFormatter.TypeOfDiff typeOfDiff;

    public DiffInfo() {
        typeOfDiff = BasicFormatter.TypeOfDiff.PATCH;
    }

    public void setBuildOne(String jobOne, String buildOne) {
        this.jobOne = jobOne;
        this.buildOne = buildOne;
    }

    public void setBuildTwo(String jobTwo, String buildTwo) {
        this.jobTwo = jobTwo;
        this.buildTwo = buildTwo;
    }

    public File getBuildOne(String pathToJobsDir) {
        if (jobOne == null || buildOne == null) {
            throw new RuntimeException("Please specify the first build to compare the stack trace.");
        }

        File jobsDir = new File(pathToJobsDir);
        File firstBuild = new File(jobsDir, jobOne + "/builds/" + buildOne);

        if (firstBuild.exists()) {
            return firstBuild;
        } else {
            throw new RuntimeException("The specified " + buildOne + " of job " + jobOne + " does not exist.");
        }
    }

    public String getBuildOneName() {
        return jobOne + " : build " + buildOne;
    }

    public File getBuildTwo(String pathToJobsDir) {
        if (jobTwo == null || buildTwo == null) {
            throw new RuntimeException("Please specify the second build to compare the stack trace.");
        }

        File jobsDir = new File(pathToJobsDir);
        File secondBuild = new File(jobsDir, jobTwo + "/builds/" + buildTwo);

        if (secondBuild.exists()) {
            return secondBuild;
        } else {
            throw new RuntimeException("The specified " + buildTwo + " of job " + jobTwo + " does not exist.");
        }
    }

    public String getBuildTwoName() {
        return jobTwo + " : build " + buildTwo;
    }

    public BasicFormatter.TypeOfDiff getTypeOfDiff() {
        return typeOfDiff;
    }

    public void setTypeOfDiff(BasicFormatter.TypeOfDiff typeOfDiff) {
        this.typeOfDiff = typeOfDiff;
    }
}