package io.jenkins.plugins.report.jtreg.main.comparator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Jobs {
    private final File[] jobsInDir;

    // constructor that gets jobs from the given directory and splits them by . or -
    public Jobs(String jobsDirectory) {
        File directory = new File(jobsDirectory);

        File[] filesInDir = directory.listFiles();
        if (filesInDir == null) {
            throw new RuntimeException("The job directory is empty.");
        }
        this.jobsInDir = Arrays
                .stream(filesInDir)
                .filter(File::isDirectory)
                .toArray(File[]::new);
    }

    // prints all jobs that match the query string
    public void printJobs(String queryString, boolean skipFailed, String nvrQuery, int numberOfBuilds) {
        ArrayList<File> jobs = getJobsByQuery(queryString);
        for (File job : jobs) {
            System.out.println(job.getName());
            ArrayList<File> jobBuilds = Builds.getBuilds(job, skipFailed, nvrQuery, numberOfBuilds);
            for (File build : jobBuilds) {
                System.out.println("\t" + "build:" + Builds.getBuildNumber(build) + " nvr:" + Builds.getNvr(build));
            }
        }
    }

    // gets the "length - and how many jobs has this length" pairs
    private HashMap<Integer, Integer> getJobsLengths(String queryString) {
        HashMap<Integer, Integer> jobLengths = new HashMap<>();
        ArrayList<File> jobList = getJobsByQuery(queryString);
        for (File job : jobList) {
            Integer length = job.getName().split("[.-]").length;
            Integer count = jobLengths.get(length) + 1;
            jobLengths.put(length, count);
        }

        return jobLengths;
    }

    // gets all different variants from the jobs into 2D list
    private ArrayList<ArrayList<String>> getVariantsList(String queryString, HashMap<Integer, Integer> jobsLengths) {
        ArrayList<ArrayList<String>> variantsLists = new ArrayList<>();
        // splits a job to "variants" by . or - and goes through all of them
        for (int i = 0; i < jobsInDir[0].getName().split("[.-]").length; i++) {
            ArrayList<String> variantList = new ArrayList<>();
            for (File job : jobsInDir) {
                String[] jobArray = job.getName().split("[.-]");
                // checks the variant with query string and adds only non-duplicate
                if (QueryString.checkJobWithQuery(job, queryString) && !variantList.contains(jobArray[i])) {
                    variantList.add(jobArray[i]);
                }
            }
            variantsLists.add(variantList);
        }
        return variantsLists;
    }

    // prints all the variants of jobs
    public void printVariants(String queryString) {
        ArrayList<ArrayList<String>> variantsLists = getVariantsList(queryString, null);
        for (int i = 0; i < variantsLists.size(); i++) {
            System.out.printf("%d) ", i + 1);
            for (String variant : variantsLists.get(i)) {
                System.out.print(variant + ", ");
            }
            System.out.print("\n");
        }
    }

    // returns matched jobs with query string
    public ArrayList<File> getJobsByQuery(String queryString) {
        ArrayList<File> matchedJobsList = new ArrayList<>();
        for (File job : jobsInDir) {
            if (QueryString.checkJobWithQuery(job, queryString)) {
                matchedJobsList.add(job);
            }
        }
        return matchedJobsList;
    }
}
