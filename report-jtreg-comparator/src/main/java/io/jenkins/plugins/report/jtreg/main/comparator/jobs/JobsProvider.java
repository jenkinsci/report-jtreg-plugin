package io.jenkins.plugins.report.jtreg.main.comparator.jobs;

import java.io.File;
import java.util.ArrayList;

public interface JobsProvider {

    // returns a list of arguments that the job provider takes/supports
    ArrayList<String> getSupportedArgs();

    // takes an argument name and value (e.g. argument:"--history" value:"10") and parses it
    void parseArguments(String argument, String value);

    // adds a list of unfiltered jobs to the provider
    void addJobs(ArrayList<File> jobsInDir);

    // filters the jobs from addJobs() by query, regex etc.
    void filterJobs();

    // returns a list of filtered jobs
    ArrayList<File> getJobs();
}
