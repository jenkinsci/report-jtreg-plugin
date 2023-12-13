package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.BuildReportExtended;
import io.jenkins.plugins.report.jtreg.BuildSummaryParser;
import io.jenkins.plugins.report.jtreg.formatters.Formatter;
import io.jenkins.plugins.report.jtreg.model.*;
import io.jenkins.plugins.report.jtreg.wrappers.RunWrapperFromDir;

import java.io.File;
import java.util.*;

public class FailedTests {
    // function for getting failed tests into an ArrayList
    private static ArrayList<String> getBuildFailedTests(File build, String exactTestsRegex, Formatter formatter) throws Exception {
        ArrayList<String> failedTests = new ArrayList<>();

        BuildSummaryParser bs = new BuildSummaryParser(Arrays.asList("jck", "jtreg"), null);

        BuildReportExtended bex = bs.parseBuildReportExtended(new RunWrapperFromDir(build), null);
        SuitesWithResults swr = bex.getAllTests();

        // since the exception is already handled elsewhere, it checks for it by this and prints the info message
        if (swr == null) {
            formatter.startColor(Formatter.SupportedColors.Yellow);
            formatter.println("The " + Builds.getJobName(build) + " - build:" + Builds.getBuildNumber(build) + " is probably missing some files (may be ABORTED).");
            formatter.reset();
            return failedTests;
        }

        for (SuiteTestsWithResults t : swr.getAllTestsAndSuites()) {
            for (SuiteTestsWithResults.StringWithResult s : t.getTests()) {
                if (s.getStatus().isFailed() && s.getTestName().matches(exactTestsRegex)) {
                    failedTests.add(s.getTestName());
                }
            }
        }

        return failedTests;
    }

    // function for creating a HashMap of "build info - list of its failed tests" pair
    public static HashMap<String, ArrayList<String>> createFailedMap(
            ArrayList<File> buildsToCompare, boolean onlyVolatile, String exactTestsRegex, Formatter formatter, Options.Configuration nvrConfig) throws Exception {

        HashMap<String, ArrayList<String>> failedMap = new HashMap<>();

        for (File build : buildsToCompare) {
            if (build != null) {
                failedMap.put(Builds.getJobName(build)
                                + " - build:" + Builds.getBuildNumber(build)
                                + " - nvr:" + Builds.getNvr(build, nvrConfig),
                        getBuildFailedTests(build, exactTestsRegex, formatter));
            }
        }

        if (onlyVolatile) {
            // get all different tests
            Set<String> failedTests = new HashSet<>();
            for (ArrayList<String> tests : failedMap.values()) {
                failedTests.addAll(tests);
            }

            ArrayList<String> keysFromMap = new ArrayList<>(failedMap.keySet());

            for (String failedTest : failedTests) {
                boolean isEverywhere = true;
                for (String key : keysFromMap) {
                    if (!failedMap.get(key).contains(failedTest)) {
                        isEverywhere = false;
                        break;
                    }
                }

                if (isEverywhere) {
                    for (String key : keysFromMap) {
                        failedMap.get(key).remove(failedTest);
                    }
                }
            }
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
    public static void printFailedTable(HashMap<String, ArrayList<String>> failedMap, Options.Operations operation, Formatter formatter) {
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

        formatter.printTable(table, failedMap.size() + 1, allFailedTests.size() + 1);
    }
}