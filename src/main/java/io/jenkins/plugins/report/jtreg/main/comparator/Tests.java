package hudson.plugins.report.jck.main.comparator;

import hudson.plugins.report.jck.BuildSummaryParser;
import hudson.plugins.report.jck.JckReportPublisher;
import hudson.plugins.report.jck.model.BuildReport;
import hudson.plugins.report.jck.model.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

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

    public static HashMap<File, ArrayList<String>> createFailedMap(ArrayList<File> buildsToCompare) {
        HashMap<File, ArrayList<String>> failedMap = new HashMap<>();

        for (File build : buildsToCompare) {
            failedMap.put(build, getBuildFailedTests(build));
        }

        return failedMap;
    }

    public static void printFailedTable(HashMap<File, ArrayList<String>> failedMap) {
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
        Set<File> keys = failedMap.keySet();
        int i = 1;
        for (File key : keys) {
            table[i][0] = Builds.getJobName(key) + " - " + Builds.getBuildNumber(key) + " - " + Builds.getNvr(key);
            for (String test : failedMap.get(key)) {
                table[i][allFailedTests.indexOf(test) + 1] = "X";
            }
            i++;
        }

        PrintTable.print(table, failedMap.size() + 1, allFailedTests.size() + 1);
    }
}
