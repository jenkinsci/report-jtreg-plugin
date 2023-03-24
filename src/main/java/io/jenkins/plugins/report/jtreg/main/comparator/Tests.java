package hudson.plugins.report.jck.main.comparator;

import hudson.plugins.report.jck.BuildSummaryParser;
import hudson.plugins.report.jck.JckReportPublisher;
import hudson.plugins.report.jck.model.BuildReport;
import hudson.plugins.report.jck.model.Test;

import java.io.File;
import java.util.*;

public class Tests {
    private static ArrayList<String> getBuildFailedTests(File build) {
        ArrayList<String> failedTests = new ArrayList<>();

        JckReportPublisher jcp = new JckReportPublisher("report-{runtime,devtools,compiler}.xml.gz");
        BuildSummaryParser bs = new BuildSummaryParser(Arrays.asList("jck", "jtreg"), jcp);

        BuildReport br = bs.parseJobReports(build);

        for (Test t : br.getSuites().get(0).getReport().getTestProblems()) {
            failedTests.add(t.getName());
        }
        return failedTests;
    }

    public static HashMap<String, ArrayList<String>> createFailedMap(ArrayList<File> buildsToCompare) {
        HashMap<String, ArrayList<String>> failedMap = new HashMap<>();

        for (File build : buildsToCompare) {
            failedMap.put(Builds.getJobName(build) + " -=- " + Builds.getBuildNumber(build) + " -=- " + Builds.getNvr(build), getBuildFailedTests(build));
        }

        return failedMap;
    }

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

        Set<String> builds = failedMap.keySet();
        for (String test : allFailed) {
            ArrayList<String> testBuilds = new ArrayList<>();
            for (String build : builds) {
                if (failedMap.get(build).contains(test)) {
                    testBuilds.add(build);
                }
            }
            reversedMap.put(test, testBuilds);
        }

        return reversedMap;
    }

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

        String[][] table = new String[failedMap.size() + 1][allFailedTests.size() + 1];
        // add first line (header)
        for (int i = 1; i < allFailedTests.size() + 1; i++) {
            table[0][i] = allFailedTests.get(i - 1);
        }
        // add the builds and tests
        Set<String> keys = failedMap.keySet();
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

    public static void printQueryForTest(String testName, HashMap<String, ArrayList<String>> failedMap) {
        int size = failedMap.size();
        failedMap = reverseFailedMap(failedMap);

        ArrayList<String> builds = failedMap.get(testName);
        ArrayList<String[]> splitBuilds = new ArrayList<>();

        for (String build : builds) {
            splitBuilds.add(build.split(" -=- ")[0].split("[.-]"));
        }

        ArrayList<ArrayList<String>> queryList = new ArrayList<>();
        for (int i = 0; i < splitBuilds.get(0).length; i++) {
            ArrayList<String> variants = new ArrayList<>();
            for (String[] build : splitBuilds) {
                if (!variants.contains(build[i])) {
                    variants.add(build[i]);
                }
            }
            queryList.add(variants);
        }

        StringBuilder queryString = new StringBuilder();
        for (ArrayList<String> v : queryList) {
            if (v.size() == 1) {
                queryString.append(v.get(0)).append(" ");
            } else if(v.size() == size) {
                queryString.append("* ");
            } else {
                queryString.append("{");
                for (String s : v) {
                    queryString.append(s);
                    if(v.indexOf(s) != v.size() - 1) {
                        queryString.append(",");
                    }
                }
                queryString.append("}").append(" ");
            }
        }

        System.out.println(queryString);
    }
}
