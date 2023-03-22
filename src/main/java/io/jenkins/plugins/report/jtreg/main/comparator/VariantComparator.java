package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.main.diff.CompareBuilds;

import java.io.File;
import java.util.ArrayList;

public class VariantComparator {
    public static void main(String[] args) {
        Options options = Arguments.parse(args);
        Jobs jobs = new Jobs(options.getJobsPath());

        if (options.getOperation() == Options.Operations.List) {
            ArrayList<File> jobsToCompare = jobs.getJobsByQuery(options.getQueryString());
            ArrayList<File> buildsToCompare = new ArrayList<>();
            for (File job : jobsToCompare) {
                ArrayList<File> builds = Builds.getBuilds(job, options.isSkipFailed(), options.getNvrQuery());
                buildsToCompare.addAll(builds);
            }

            Tests.printFailedTable(Tests.createFailedMap(buildsToCompare));
        } else if (options.getOperation() == Options.Operations.Enumerate) {
            jobs.printVariants(options.getQueryString());
        } else if (options.getOperation() == Options.Operations.Compare) {
            ArrayList<String> commandArguments = new ArrayList<>();

            ArrayList<File> jobsToCompare = jobs.getJobsByQuery(options.getQueryString());
            for (File job : jobsToCompare) {
                ArrayList<File> builds = Builds.getBuilds(job, options.isSkipFailed(), options.getNvrQuery());
                for (File build : builds) {
                    commandArguments.add(build.getAbsolutePath());
                }
            }

            // add more arguments
            commandArguments.add("--view=diff-list");
            commandArguments.add("--view=info");
            commandArguments.add("--view=info-hide-details");

            // call the main method from jenkins-report-jck
            try {
                CompareBuilds.main(commandArguments.toArray(new String[0]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

