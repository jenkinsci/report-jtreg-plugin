package io.jenkins.plugins.report.jtreg.main.comparator;

import java.io.File;
import java.util.ArrayList;

public class VariantComparator {
    public static void main(String[] args) throws Exception {
        Options options = Arguments.parse(args);
        Jobs jobs = new Jobs(options.getJobsPath());

        ArrayList<File> jobsToCompare = jobs.getJobsByQuery(options.getQueryString());
        ArrayList<File> buildsToCompare = new ArrayList<>();
        for (File job : jobsToCompare) {
            ArrayList<File> builds = Builds.getBuilds(job, options.isSkipFailed(), options.getNvrQuery(), options.getNumberOfBuilds());
            buildsToCompare.addAll(builds);
        }

        if (options.getOperation() == Options.Operations.List || options.getOperation() == Options.Operations.Compare) {
            FailedTests.printFailedTable(FailedTests.createFailedMap(buildsToCompare), options.getOperation());
        } else if (options.getOperation() == Options.Operations.Enumerate) {
            jobs.printVariants(options.getQueryString());
        }
    }
}

