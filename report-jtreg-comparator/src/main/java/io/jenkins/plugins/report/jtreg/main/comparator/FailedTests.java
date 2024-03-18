package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.BuildReportExtended;
import io.jenkins.plugins.report.jtreg.BuildSummaryParser;
import io.jenkins.plugins.report.jtreg.ConfigFinder;
import io.jenkins.plugins.report.jtreg.formatters.Formatter;
import io.jenkins.plugins.report.jtreg.model.*;
import io.jenkins.plugins.report.jtreg.wrappers.RunWrapperFromDir;

import java.io.File;
import java.util.*;

public class FailedTests {
    // function for getting failed tests into an ArrayList
    private static ArrayList<String> getBuildFailedTests(File build, String exactTestsRegex) throws Exception {
        ArrayList<String> failedTests = new ArrayList<>();

        BuildSummaryParser bs = new BuildSummaryParser(Arrays.asList("jck", "jtreg"), null);

        BuildReportExtended bex = bs.parseBuildReportExtended(new RunWrapperFromDir(build), null);
        SuitesWithResults swr = bex.getAllTests();

        // since the exception is already handled elsewhere, it checks for it by this and prints the info message
        if (swr == null) {
            System.err.println("The " + Builds.getJobName(build) + " - build:" + Builds.getBuildNumber(build) + " is probably missing some files (may be ABORTED).");
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
    public static HashMap<String, ArrayList<String>> createFailedMap(ArrayList<File> buildsToCompare, Options options) throws Exception {

        HashMap<String, ArrayList<String>> failedMap = new HashMap<>();

        for (File build : buildsToCompare) {
            if (build != null) {
                String mainLine = Builds.getJobName(build) + " - build:" + Builds.getBuildNumber(build);
                List<String> otherLines = new ArrayList<>();

                for (Map.Entry<String, Options.Configuration> entry : options.getAllConfigurations().entrySet()) {
                    String line = entry.getKey() + " : " +
                            new ConfigFinder(entry.getValue().findConfigFile(build), entry.getKey(), entry.getValue().getFindQuery()).findInConfig();
                    otherLines.add(line);
                }

                failedMap.put(options.getFormatter().generateTableHeaderItem(mainLine, otherLines), getBuildFailedTests(build, options.getExactTestsRegex()));
            }
        }

        if (options.isOnlyVolatile()) {
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

    // function for getting the HashMap of failed tests ready for printing to console
    public static void printFailedTable(HashMap<String, ArrayList<String>> failedMap, Options options) {
        // by default, all builds are shown, this makes the table less "cluttery" by deleting builds with no failed tests
        if (options.isHidePasses()) {
            failedMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }

        // get all non-duplicate failed tests from the map
        Set<String> nonRepeatingValues = new HashSet<>();
        for (ArrayList<String> values : failedMap.values()) {
            nonRepeatingValues.addAll(values);
        }

        // sort the failed tests set
        List<String> sortedValues = new ArrayList<>(nonRepeatingValues);
        Collections.sort(sortedValues);

        // get the key from the map (builds) and sort them
        List<String> keys = new ArrayList<>(failedMap.keySet());
        Collections.sort(keys);

        // if the operation is "compare" (rows are failed tests and columns builds), switch the values and keys
        if (options.getOperation() == Options.Operations.Compare) {
            List<String> temp = keys;
            keys = sortedValues;
            sortedValues = temp;
        }

        // create the table itself (2D array), where [rows][columns]
        String[][] table = new String[keys.size() + 1][sortedValues.size() + 1];

        // first, put values into the table header (first row)
        for (int i = 1; i < sortedValues.size() + 1; i++) {
            table[0][i] = sortedValues.get(i - 1);
        }

        // add the "X"s to the table where the tests fail
        int i = 1;
        for (String key : keys) {
            table[i][0] = key; // put the key into first column

            // now, create a list of values where to put the X for each column
            List<String> putXList = new ArrayList<>();
            if (options.getOperation() == Options.Operations.Compare) {
                // if the operation is compare, the values to put X are the builds (or the keys in the map),
                // so it has to go through the map and find them
                for (Map.Entry<String, ArrayList<String>> entry : failedMap.entrySet()) {
                    if (entry.getValue().contains(key)) {
                        putXList.add(entry.getKey());
                    }
                }
            } else {
                // otherwise, just get the values from the map
                putXList = failedMap.get(key);
            }

            // put the Xs itself
            for (String value : putXList) {
                table[i][sortedValues.indexOf(value) + 1] = "X";
            }

            i++;
        }

        // print the table into stdout
        options.getFormatter().printTable(table, keys.size() + 1, sortedValues.size() + 1);
    }
}