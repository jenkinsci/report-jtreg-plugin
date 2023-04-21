package io.jenkins.plugins.report.jtreg.main.comparator;

import java.io.File;
import java.util.ArrayList;

public interface IJobs {
    ArrayList<File> getJobs();
    void printJobs(boolean skipFailed, String nvrQuery, int numberOfBuilds);
    void printVariants();
}
