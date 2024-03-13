package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.main.comparator.arguments.ArgumentsParsing;
import io.jenkins.plugins.report.jtreg.main.comparator.listing.DirListing;
import io.jenkins.plugins.report.jtreg.main.comparator.listing.FsDirListing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VariantComparator {
    public static void main(String[] args) throws Exception {
        Options options = ArgumentsParsing.parse(args);
        if(options.isDie()) {
            return;
        }

        DirListing dl = new FsDirListing(options.getJobsPath());

        // add the jobs to the provider and filter them
        options.getJobsProvider().addJobs(dl.getJobsInDir());
        options.getJobsProvider().filterJobs();

        JobConfigFilter jfbc = new JobConfigFilter(options.getJobsProvider().getJobs(), options.getAllConfigurations());
        jfbc.filterJobs();

        ArrayList<File> buildsToCompare = new ArrayList<>();
        for (File job : jfbc.getJobs()) {
            List<File> builds = Builds.getBuilds(job, options);
            buildsToCompare.addAll(builds);
        }

        if (!buildsToCompare.isEmpty()) {
            // do the chosen operation
            if (options.getOperation() == Options.Operations.List || options.getOperation() == Options.Operations.Compare) {
                FailedTests.printFailedTable(
                        FailedTests.createFailedMap(buildsToCompare, options),
                        options.getOperation(), options.getFormatter());
            } else if (options.getOperation() == Options.Operations.Enumerate) {
                JobsPrinting.printVariants(options.getJobsProvider().getJobs(), options.getFormatter());
            } else if (options.getOperation() == Options.Operations.Print) {
                JobsPrinting.printJobs(options.getJobsProvider().getJobs(), options);
            }

            // print virtual table
            if (options.isPrintVirtual()) {
                options.getFormatter().println();
                VirtualJobsResults.printVirtualTable(buildsToCompare, options);
            }
        }
    }
}
