package io.jenkins.plugins.report.jtreg.main.comparator.jobs;

import java.io.File;
import java.util.ArrayList;

public class DefaultProvider implements JobsProvider {
    private ArrayList<File> jobsInDir;

    public DefaultProvider() {
        this.jobsInDir = new ArrayList<>();
    }

    public static ArrayList<String> getSupportedArgsStatic() {
        return new ArrayList<>();
    }

    public ArrayList<String> getSupportedArgs() {
        return getSupportedArgsStatic();
    }

    public void parseArguments(String argument, String value) {
        // no operation
    }

    public void addJobs(ArrayList<File> jobsInDir) {
        this.jobsInDir = jobsInDir;
    }

    public void filterJobs() {
        // no operation
    }

    public ArrayList<File> getJobs() {
        return jobsInDir;
    }

}
