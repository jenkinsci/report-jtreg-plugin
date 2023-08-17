package io.jenkins.plugins.report.jtreg.main.comparator.listing;

import java.io.File;
import java.util.ArrayList;

// a class used in tests - instead of getting the jobs from directory, it simulates it by converting ArrayList of strings to files
public class ListDirListing implements DirListing {
    private final ArrayList<File> jobs;

    public ListDirListing(ArrayList<String> jobsList) {
        ArrayList<File> convertedJobs = new ArrayList<>();
        for (String s : jobsList) {
            convertedJobs.add(new File(s));
        }

        this.jobs = convertedJobs;
    }
    public ArrayList<File> getJobsInDir() {
        return jobs;
    }
}
