package io.jenkins.plugins.report.jtreg.main.comparator.jobs;

import java.io.File;
import java.util.ArrayList;

public interface JobsProvider {
    ArrayList<File> getJobs();
    void printJobs(boolean skipFailed, String nvrQuery, int numberOfBuilds);
    void printVariants();
}
