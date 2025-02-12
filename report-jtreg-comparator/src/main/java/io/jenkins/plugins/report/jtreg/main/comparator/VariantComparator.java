package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.main.comparator.arguments.ComparatorArgParser;
import io.jenkins.plugins.report.jtreg.main.comparator.listing.DirListing;
import io.jenkins.plugins.report.jtreg.main.comparator.listing.FsDirListing;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VariantComparator {

    static List<String> copyOfArgs;

    public static void main(String[] args) throws Exception {
        //IF we do not make a copy of original arguments, we would need to reconstruct the it back from (generic)options and from ComapratorOption, which would be a lot of work
        copyOfArgs = Collections.unmodifiableList(Arrays.asList(args));

        ComparatorArgParser comparatorArgParser = new ComparatorArgParser(new Options(), args);
        Options options = comparatorArgParser.parseAndGetOptions();

        if (options.isDie()) {
            System.out.print(HelpMessage.HELP_MESSAGE);
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
                FailedTests failedTests = new FailedTests(options);
                failedTests.printFailedTable(buildsToCompare);
                if (options.isPrintFinalColumns()) {
                    options.getFormatter().println();
                    failedTests.printColumns(buildsToCompare);
                }
            } else if (options.getOperation() == Options.Operations.Enumerate) {
                JobsPrinting.printVariants(options.getJobsProvider().getJobs(), options.getFormatter());
            } else if (options.getOperation() == Options.Operations.Print) {
                JobsPrinting.printJobs(options.getJobsProvider().getJobs(), options);
            } else if (options.getOperation() == Options.Operations.TraceCompare) {
                StackTraceCompare.compareTraces(new FailedTests(options).createFailedMap(buildsToCompare), options);
            }

            // print virtual table
            if (options.isPrintVirtual()) {
                options.getFormatter().println();
                VirtualJobsResults.printVirtualTable(buildsToCompare, options);
            }
        }
    }
}
