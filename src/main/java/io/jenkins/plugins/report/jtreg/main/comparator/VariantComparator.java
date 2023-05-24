package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.main.comparator.arguments.ArgumentsParsing;
import io.jenkins.plugins.report.jtreg.main.comparator.jobs.JobsByQuery;
import io.jenkins.plugins.report.jtreg.main.comparator.jobs.JobsByRegex;
import io.jenkins.plugins.report.jtreg.main.comparator.jobs.JobsProvider;
import io.jenkins.plugins.report.jtreg.main.comparator.listing.DirListing;
import io.jenkins.plugins.report.jtreg.main.comparator.listing.FsDirListing;

import java.io.File;
import java.util.ArrayList;

public class VariantComparator {
    public static void main(String[] args) throws Exception {
        Options options = ArgumentsParsing.parse(args);

        DirListing dl = new FsDirListing(options.getJobsPath());

        // filter jobs by name (either using the query or regex)
        JobsProvider jobs;
        if (!options.getQueryString().equals("")) {
            jobs = new JobsByQuery(options.getQueryString(), dl.getJobsInDir(), options.getExactJobLength());
        }
        else if (!options.getRegexString().equals("")) {
            jobs = new JobsByRegex(options.getRegexString(), dl.getJobsInDir());
        } else {
            throw new RuntimeException("No jobs filtering specified.");
        }

        ArrayList<File> buildsToCompare = new ArrayList<>();
        for (File job : jobs.getJobs()) {
            ArrayList<File> builds = Builds.getBuilds(job, options.isSkipFailed(), options.getNvrQuery(), options.getNumberOfBuilds());
            buildsToCompare.addAll(builds);
        }

        if (options.getOperation() == Options.Operations.List || options.getOperation() == Options.Operations.Compare) {
            FailedTests.printFailedTable(
                    FailedTests.createFailedMap(buildsToCompare, options.isOnlyVolatile(), options.getExactTestsRegex()),
                    options.getOperation(), options.getTablePrinter());
        } else if (options.getOperation() == Options.Operations.Enumerate) {
            JobsPrinting.printVariants(jobs.getJobs(), options.getFormatter());
        } else if (options.getOperation() == Options.Operations.Print) {
            JobsPrinting.printJobs(jobs.getJobs(), options.isSkipFailed(), options.getNvrQuery(), options.getNumberOfBuilds(), options.getFormatter());
        } else if (options.getOperation() == Options.Operations.Virtual) {
            VirtualJobsResults.printVirtualTable(buildsToCompare, options.getTablePrinter());
        }
    }
}
