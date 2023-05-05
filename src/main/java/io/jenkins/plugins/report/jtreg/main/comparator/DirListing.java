package io.jenkins.plugins.report.jtreg.main.comparator;

import java.io.File;
import java.util.ArrayList;

// an interface used for getting jobs from a directory
public interface DirListing {
    ArrayList<File> getJobsInDir();
}
