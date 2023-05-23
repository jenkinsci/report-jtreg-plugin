package io.jenkins.plugins.report.jtreg.main.comparator.jobs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class JobsByQuery implements JobsProvider {
    private final ArrayList<String[]> queryList;
    private final ArrayList<File> matchedJobs;
    public JobsByQuery(String queryString, ArrayList<File> jobsInDir, int exactLength) {
        this.queryList = parseToList(queryString);

        this.matchedJobs = new ArrayList<>();
        for (File job : jobsInDir) {
            if (checkJobWithQuery(job, exactLength)) {
                this.matchedJobs.add(job);
            }
        }
    }

    public ArrayList<File> getJobs() {
        return matchedJobs;
    }

    // parses the given query string into a list of string arrays
    private ArrayList<String[]> parseToList(String queryString) {
        String[] queryArray = queryString.split("\\s+");
        ArrayList<String[]> queryList = new ArrayList<>();

        for (String s : queryArray) {
            boolean reversed = false;
            if (s.charAt(0) == '!') {
                reversed = true;
                s = s.substring(1);
            }
            // checks for array in the query
            if (s.charAt(0) == '{') {
                if (s.charAt(s.length() - 1) != '}') {
                    throw new RuntimeException("Expected closing }.");
                }
                String[] array = s.substring(1, s.length() - 1).split(",");
                if (reversed) {
                    for (int i = 0; i < array.length; i++) {
                        array[i] = "!" + array[i];
                    }
                    queryList.add(array);
                } else {
                    queryList.add(array);
                }
            } else {
                String[] arrayOfOne = new String[1];
                if (reversed) {
                    arrayOfOne[0] = "!" + s;
                } else {
                    arrayOfOne[0] = s;
                }
                queryList.add(arrayOfOne);
            }
        }
        return queryList;
    }

    // checks if the given job matches the query string
    private boolean checkJobWithQuery(File job, int exactLength) {
        String[] jobArray = job.getName().split("[.-]");

        if (exactLength != -1 && exactLength != jobArray.length) {
            return false;
        }

        for (int i = 0; i < queryList.size(); i++) {
            if (jobArray.length <= i) {
                break;
            }

            String[] qA = queryList.get(i); // get a single array from the queryList

            boolean reversed = qA[0].charAt(0) == '!'; // check for reversed query

            if (qA.length == 1) {
                if ( (!reversed && !qA[0].equals("*") && !qA[0].equals(jobArray[i])) ||
                        (reversed && !qA[0].equals("*") && qA[0].equals("!" + jobArray[i])) ) {
                    return false;
                }
            } else {
                if ( (!reversed && !Arrays.asList(qA).contains(jobArray[i])) ||
                        (reversed && Arrays.asList(qA).contains("!" + jobArray[i])) ) {
                    return false;
                }
            }
        }
        return true;
    }
}
