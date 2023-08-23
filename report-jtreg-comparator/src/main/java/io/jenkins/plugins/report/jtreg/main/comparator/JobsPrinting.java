package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.formatters.Formatter;

import java.io.File;
import java.util.ArrayList;

public class JobsPrinting {
    // print all matched jobs and their matched builds
    public static void printJobs(ArrayList<File> matchedJobs, boolean skipFailed, String nvrQuery, int numberOfBuilds, Formatter formatter, boolean useDefaultBuild) {
        for (File job : matchedJobs) {
            formatter.startBold();
            formatter.println(job.getName() + ":");
            formatter.reset();
            ArrayList<File> jobBuilds = Builds.getBuilds(job, skipFailed, nvrQuery, numberOfBuilds, useDefaultBuild, formatter);
            for (File build : jobBuilds) {
                formatter.print("\t");
                formatter.startColor(Formatter.SupportedColors.LightBlue);
                formatter.print("build:");
                formatter.reset();
                formatter.print(Builds.getBuildNumber(build));
                formatter.print(" - ");
                formatter.startColor(Formatter.SupportedColors.LightBlue);
                formatter.print("nvr: ");
                formatter.reset();
                formatter.println(Builds.getNvr(build));
            }
        }
    }

    // print the available variants of all jobs (meant to be used with query)
    public static void printVariants(ArrayList<File> matchedJobs, Formatter formatter) {
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
            formatter.startBold();
            formatter.print(i + 1 + ") ");
            formatter.reset();
            for (String variant : variantList) {
                formatter.print(variant + ", ");
            }
            formatter.println("");
        }
    }
}
