package io.jenkins.plugins.report.jtreg.main.comparator;

import java.io.File;
import java.util.ArrayList;

public class VariantComparator {
    public static void main(String[] args) throws Exception {
        Options options = Arguments.parse(args);
        IJobs jobs = new JobsByQuery(options.getQueryString(), options.getJobsPath(), options.getExactJobLength());

        ArrayList<File> buildsToCompare = new ArrayList<>();
        for (File job : jobs.getJobs()) {
            ArrayList<File> builds = Builds.getBuilds(job, options.isSkipFailed(), options.getNvrQuery(), options.getNumberOfBuilds());
            buildsToCompare.addAll(builds);
        }

        if (options.getOperation() == Options.Operations.List || options.getOperation() == Options.Operations.Compare) {
            FailedTests.printFailedTable(FailedTests.createFailedMap(buildsToCompare), options.getOperation());
        } else if (options.getOperation() == Options.Operations.Enumerate) {
            jobs.printVariants();
        } else if (options.getOperation() == Options.Operations.Print) {
            jobs.printJobs(options.isSkipFailed(), options.getNvrQuery(), options.getNumberOfBuilds());
        }
    }
}
