package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.BuildReportExtended;
import io.jenkins.plugins.report.jtreg.BuildSummaryParser;
import io.jenkins.plugins.report.jtreg.Constants;
import io.jenkins.plugins.report.jtreg.JckReportPublisher;
import io.jenkins.plugins.report.jtreg.model.*;
import io.jenkins.plugins.report.jtreg.wrappers.RunWrapperFromDir;

import java.io.File;
import java.util.*;

public class Tests {
    // function for getting failed tests into an ArrayList
    private static ArrayList<String> getBuildFailedTests(File build) throws Exception {
        ArrayList<String> failedTests = new ArrayList<>();

        JckReportPublisher jcp = new JckReportPublisher(Constants.IRRELEVANT_GLOB_STRING); // completely irrelevant string
        BuildSummaryParser bs = new BuildSummaryParser(Arrays.asList("jck", "jtreg"), jcp);

        BuildReportExtended bex = bs.parseBuildReportExtended(new RunWrapperFromDir(build), null);
        SuitesWithResults swr = bex.getAllTests();

        if (swr != null) {
            for (SuiteTestsWithResults t : swr.getAllTestsAndSuites()) {
                for (SuiteTestsWithResults.StringWithResult s : t.getTests()) {
                    if (s.getStatus().isFailed()) {
                        failedTests.add(s.getTestName());
                    }
                }
            }
        }

        return failedTests;
    }

    // function for creating a HashMap of "build info - list of its failed tests" pair
    public static HashMap<String, ArrayList<String>> createFailedMap(ArrayList<File> buildsToCompare) throws Exception {
        HashMap<String, ArrayList<String>> failedMap = new HashMap<>();

        for (File build : buildsToCompare) {
            failedMap.put(Builds.getJobName(build) + " - " + Builds.getBuildNumber(build) + " - " + Builds.getNvr(build), getBuildFailedTests(build));
        }

        return failedMap;
    }

    // function for reversing the HashMap from reverseFailedMap() method
    // Keys: Failed test names, Values: list of builds where the test failed
    private static HashMap<String, ArrayList<String>> reverseFailedMap(HashMap<String, ArrayList<String>> failedMap) {
        HashMap<String, ArrayList<String>> reversedMap = new HashMap<>();

        ArrayList<String> allFailed = new ArrayList<>();
        for (ArrayList<String> tests : failedMap.values()) {
            for (String test : tests) {
                if(!allFailed.contains(test)) {
                    allFailed.add(test);
                }
            }
        }

        for (String test : allFailed) {
            ArrayList<String> testBuilds = new ArrayList<>();
            for (Map.Entry<String, ArrayList<String>> entry : failedMap.entrySet()) {
                if (entry.getValue().contains(test)) {
                    testBuilds.add(entry.getKey());
                }
            }
            reversedMap.put(test, testBuilds);
        }

        return reversedMap;
    }

    // function for getting the HashMap of failed tests ready for printing to console
    public static void printFailedTable(HashMap<String, ArrayList<String>> failedMap, Options.Operations operation) {
        if (operation == Options.Operations.Compare) {
            failedMap = reverseFailedMap(failedMap);
        }

        ArrayList<String> allFailedTests = new ArrayList<>();
        for (ArrayList<String> tests : failedMap.values()) {
            for (String test : tests) {
                if(!allFailedTests.contains(test)) {
                    allFailedTests.add(test);
                }
            }
        }
        Collections.sort(allFailedTests);

        String[][] table = new String[failedMap.size() + 1][allFailedTests.size() + 1];
        // add first line (header)
        for (int i = 1; i < allFailedTests.size() + 1; i++) {
            table[0][i] = allFailedTests.get(i - 1);
        }
        // add the builds and tests
        ArrayList<String> keys = new ArrayList<>(failedMap.keySet());
        Collections.sort(keys);

        // add the "X"s to the table
        int i = 1;
        for (String key : keys) {
            table[i][0] = key;
            for (String test : failedMap.get(key)) {
                table[i][allFailedTests.indexOf(test) + 1] = "X";
            }
            i++;
        }

        PrintTable.print(table, failedMap.size() + 1, allFailedTests.size() + 1);
    }
}