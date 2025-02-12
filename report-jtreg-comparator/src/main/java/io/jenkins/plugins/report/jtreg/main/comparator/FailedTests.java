package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.BuildReportExtended;
import io.jenkins.plugins.report.jtreg.BuildSummaryParser;
import io.jenkins.plugins.report.jtreg.ConfigFinder;
import io.jenkins.plugins.report.jtreg.formatters.JtregPluginServicesCell;
import io.jenkins.plugins.report.jtreg.formatters.JtregPluginServicesLinkWithTooltip;
import io.jenkins.plugins.report.jtreg.model.*;
import io.jenkins.plugins.report.jtreg.wrappers.RunWrapperFromDir;

import java.io.File;
import java.util.*;

public class FailedTests {
    private Map<String, ArrayList<String>> cachedFailedMap;
    private final Options options;

    public FailedTests(Options options) {
        this.options = options;
    }

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
    public HashMap<String, ArrayList<String>> createFailedMap(ArrayList<File> buildsToCompare) throws Exception {

        HashMap<String, ArrayList<String>> failedMap = new HashMap<>();

        for (File build : buildsToCompare) {
            if (build != null) {
                failedMap.put(build.getAbsolutePath(), getBuildFailedTests(build, options.getExactTestsRegex()));
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

        this.cachedFailedMap = failedMap;
        return failedMap;
    }

    public void printFailedTable() throws Exception {
        if (cachedFailedMap == null) {
            throw new RuntimeException("To use cached failures map, you have to runn createFailedMap first");
        }
        printFailedTable(null);
    }

    // function for getting the HashMap of failed tests ready for printing to console
    public void printFailedTable(ArrayList<File> buildsToCompare) throws Exception {
        if (cachedFailedMap == null) {
            createFailedMap(buildsToCompare);
        }
        Map<String, ArrayList<String>> failedMap = cachedFailedMap;

        // by default, all builds are shown, this makes the table less "cluttery" by deleting builds with no failed tests
        if (options.isHidePasses()) {
            failedMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }

        // convert build paths in the map to a styled build description
        Map<String, ArrayList<String>> convertedMap = new HashMap<>();
        List<File> orderedBuilds = new ArrayList<>();
        for (Map.Entry<String, ArrayList<String>> entry : failedMap.entrySet()) {
            File build = new File(entry.getKey());
            List<String> otherLines = new ArrayList<>();

            for (Map.Entry<String, Options.Configuration> configEntry : options.getAllConfigurations().entrySet()) {
                String line = configEntry.getKey() + " : " +
                        new ConfigFinder(configEntry.getValue().findConfigFile(build), configEntry.getKey(), configEntry.getValue().getFindQuery()).findInConfig();
                otherLines.add(line);
            }
            orderedBuilds.add(build);
            convertedMap.put(options.getFormatter().generateTableHeaderItem(Builds.getJobName(build), Builds.getBuildNumber(build), otherLines, options.getJenkinsUrl()), entry.getValue());
        }

        // get all non-duplicate failed tests from the map
        Set<String> nonRepeatingValues = new HashSet<>();
        for (ArrayList<String> values : convertedMap.values()) {
            nonRepeatingValues.addAll(values);
        }

        // sort the failed tests set
        List<String> sortedValues = new ArrayList<>(nonRepeatingValues);
        Collections.sort(sortedValues);

        // get the key from the map (builds) and sort them
        List<String> keys = new ArrayList<>(convertedMap.keySet());
        Collections.sort(keys);

        // if the operation is "compare" (rows are failed tests and columns builds), switch the values and keys
        if (options.getOperation() == Options.Operations.Compare) {
            List<String> temp = keys;
            keys = sortedValues;
            sortedValues = temp;
        }

        // create the table itself (2D array), where [rows][columns]
        JtregPluginServicesCell[][] table = new JtregPluginServicesCell[keys.size() + 1][sortedValues.size() + 1];

        // first, put values into the table header (first row)
        for (int i = 1; i < sortedValues.size() + 1; i++) {
            table[0][i] = options.getFormatter().createCell(new JtregPluginServicesLinkWithTooltip(sortedValues.get(i - 1)));
        }

        // add the "X"s to the table where the tests fail
        int i = 1;
        for (String key : keys) {
            table[i][0] = options.getFormatter().createCell(new JtregPluginServicesLinkWithTooltip(key)); // put the key into first
            // column

            // now, create a list of values where to put the X for each column
            List<String> putXList = new ArrayList<>();
            if (options.getOperation() == Options.Operations.Compare) {
                // if the operation is compare, the values to put X are the builds (or the keys in the map),
                // so it has to go through the map and find them
                for (Map.Entry<String, ArrayList<String>> entry : convertedMap.entrySet()) {
                    if (entry.getValue().contains(key)) {
                        putXList.add(entry.getKey());
                    }
                }
            } else {
                // otherwise, just get the values from the map
                putXList = convertedMap.get(key);
            }

            // put the Xs itself
            for (String value : putXList) {
                int column = sortedValues.indexOf(value) + 1;
                String buildName = Builds.getJobName(orderedBuilds.get(column - 1));
                String jobId = Builds.getBuildNumber(orderedBuilds.get(column - 1));
                String id = "failed-" + key + "-" + buildName + "-" + jobId;
                table[i][column] =
                        options.getFormatter().createCell(new JtregPluginServicesLinkWithTooltip("X", null, id, VirtualJobsResults.createTooltip(key, buildName, jobId, column, id, options.getJenkinsUrl()), true));
            }

            i++;
        }

        // print the table into stdout
        options.getFormatter().printTable(table, keys.size() + 1, sortedValues.size() + 1);
    }


    public void printColumns(ArrayList<File> buildsToCompare) throws Exception {
        if (cachedFailedMap == null) {
            createFailedMap(buildsToCompare);
        }
        Map<String, Integer> failuresPerBuild = new TreeMap<>();//the builds should be sorted already
        for (File build : buildsToCompare) {
            if (build != null) {
                failuresPerBuild.put(build.getAbsolutePath(), 0);
            }
        }
        for (Map.Entry<String, ArrayList<String>> testInBuilds : cachedFailedMap.entrySet()) {
            failuresPerBuild.put(testInBuilds.getKey(), testInBuilds.getValue().size());
        }
        List<String> allPased = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        for (Map.Entry<String, Integer> buildAndFailures : failuresPerBuild.entrySet()) {
            String nice = Builds.getJobName(new File(buildAndFailures.getKey())) + " - build:" + Builds.getBuildNumber(new File(buildAndFailures.getKey())) + " (" + buildAndFailures.getValue() + ")";
            if (buildAndFailures.getValue() == 0) {
                allPased.add(nice);
            } else {
                failures.add(nice);
            }
        }
        options.getFormatter().printColumns(new String[]{"Failed at(" + failures.size() + "):", "No failures at(" + allPased.size() + "):"}, failures, allPased);
    }
}