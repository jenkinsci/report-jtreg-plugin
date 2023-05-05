package io.jenkins.plugins.report.jtreg.main.comparator;

import java.io.File;
import java.util.ArrayList;

public class FsDirListing implements DirListing {
    private final ArrayList<File> jobsInDir;

    public FsDirListing(String pathToDirWithJobs) {
        File[] filesInDir = new File(pathToDirWithJobs).listFiles();
        if (filesInDir == null) {
            throw new RuntimeException("The job directory is empty.");
        }

        ArrayList<File> foundJobs = new ArrayList<>();
        for (File f : filesInDir) {
            if (f.isDirectory()) {
                foundJobs.add(f);
            }
        }

        this.jobsInDir = foundJobs;
    }

    public ArrayList<File> getJobsInDir() {
        return jobsInDir;
    }
}
