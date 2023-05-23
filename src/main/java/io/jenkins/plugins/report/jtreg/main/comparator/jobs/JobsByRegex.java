package io.jenkins.plugins.report.jtreg.main.comparator.jobs;

import java.io.File;
import java.util.ArrayList;

public class JobsByRegex implements JobsProvider {
    private final ArrayList<File> matchedJobs;
    public JobsByRegex(String regexString, ArrayList<File> jobsInDir) {
        this.matchedJobs = new ArrayList<>();
        for (File job : jobsInDir) {
            if (job.getName().matches(regexString)) {
                matchedJobs.add(job);
            }
        }
    }

    public ArrayList<File> getJobs() {
        return matchedJobs;
    }
}
