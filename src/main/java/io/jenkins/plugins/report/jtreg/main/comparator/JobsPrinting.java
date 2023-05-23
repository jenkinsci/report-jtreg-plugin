package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.main.comparator.formatters.Formatters;

import java.io.File;
import java.util.ArrayList;

public class JobsPrinting {
    // print all matched jobs and their matched builds
    public static void printJobs(ArrayList<File> matchedJobs, boolean skipFailed, String nvrQuery, int numberOfBuilds, Formatters formatter) {
        for (File job : matchedJobs) {
            formatter.printBold(job.getName());
            formatter.println(":");
            ArrayList<File> jobBuilds = Builds.getBuilds(job, skipFailed, nvrQuery, numberOfBuilds);
            for (File build : jobBuilds) {
                formatter.print("\t");
                formatter.printItalics("build: ");
                formatter.print(Builds.getBuildNumber(build));
                formatter.print(" - ");
                formatter.printItalics("nvr: ");
                formatter.println(Builds.getNvr(build));
            }
        }
    }

    // print the available variants of all jobs (meant to be used with query)
    public static void printVariants(ArrayList<File> matchedJobs, Formatters formatter) {
        int maxLength = 0;
        ArrayList<String[]> splitJobs = new ArrayList<>();

        for (File job : matchedJobs) {
            String[] splitJob = job.getName().split("[.-]");
            splitJobs.add(splitJob);
            if (splitJob.length > maxLength) {
                maxLength = splitJob.length;
            }
        }

        for (int i = 0; i < maxLength; i++) {
            ArrayList<String> variantList = new ArrayList<>();
            for (String[] job : splitJobs) {

                // checks the variant with query string and adds only non-duplicate
                if (job.length > i && !variantList.contains(job[i])) {
                    variantList.add(job[i]);
                }
            }
            formatter.printBold(i + 1 + ") ");
            for (String variant : variantList) {
                formatter.print(variant + ", ");
            }
            formatter.println("");
        }
    }
}
