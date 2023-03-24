package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.main.diff.CompareBuilds;

import java.io.File;
import java.util.ArrayList;

public class VariantComparator {
    public static void main(String[] args) {
        Options options = Arguments.parse(args);
        Jobs jobs = new Jobs(options.getJobsPath());

        ArrayList<File> jobsToCompare = jobs.getJobsByQuery(options.getQueryString());
        ArrayList<File> buildsToCompare = new ArrayList<>();
        for (File job : jobsToCompare) {
            ArrayList<File> builds = Builds.getBuilds(job, options.isSkipFailed(), options.getNvrQuery());
            buildsToCompare.addAll(builds);
        }

        if (options.getOperation() == Options.Operations.List || options.getOperation() == Options.Operations.Compare) {
            Tests.printFailedTable(Tests.createFailedMap(buildsToCompare), options.getOperation());
        } else if (options.getOperation() == Options.Operations.Enumerate) {
            jobs.printVariants(options.getQueryString());
        } else if (options.getOperation() == Options.Operations.Slim) {
            Tests.printQueryForTest(options.getTest(), Tests.createFailedMap(buildsToCompare));
        }
    }
}

