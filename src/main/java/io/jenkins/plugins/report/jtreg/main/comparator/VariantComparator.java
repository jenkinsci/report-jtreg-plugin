package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.main.comparator.arguments.ArgumentsParsing;
import io.jenkins.plugins.report.jtreg.main.comparator.jobs.JobsByQuery;
import io.jenkins.plugins.report.jtreg.main.comparator.jobs.JobsProvider;
import io.jenkins.plugins.report.jtreg.main.comparator.listing.DirListing;
import io.jenkins.plugins.report.jtreg.main.comparator.listing.FsDirListing;

import java.io.File;
import java.util.ArrayList;

public class VariantComparator {
    public static void main(String[] args) throws Exception {
        Options options = ArgumentsParsing.parse(args);

        DirListing dl = new FsDirListing(options.getJobsPath());

        JobsProvider jobs = new JobsByQuery(options.getQueryString(), dl.getJobsInDir(), options.getExactJobLength());

        ArrayList<File> buildsToCompare = new ArrayList<>();
        for (File job : jobs.getJobs()) {
            ArrayList<File> builds = Builds.getBuilds(job, options.isSkipFailed(), options.getNvrQuery(), options.getNumberOfBuilds());
            buildsToCompare.addAll(builds);
        }

        if (options.getOperation() == Options.Operations.List || options.getOperation() == Options.Operations.Compare) {
            FailedTests.printFailedTable(
                    FailedTests.createFailedMap(buildsToCompare, options.isOnlyVolatile(), options.getExactTestsRegex()),
                    options.getOperation(), options.getFormatter());
        } else if (options.getOperation() == Options.Operations.Enumerate) {
            jobs.printVariants(options.getFormatter());
        } else if (options.getOperation() == Options.Operations.Print) {
            jobs.printJobs(options.isSkipFailed(), options.getNvrQuery(), options.getNumberOfBuilds(), options.getFormatter());
        }
    }
}
