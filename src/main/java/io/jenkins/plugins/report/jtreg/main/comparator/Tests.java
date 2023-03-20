package hudson.plugins.report.jck.main.comparator;

import hudson.plugins.report.jck.BuildSummaryParser;
import hudson.plugins.report.jck.JckReportPublisher;
import hudson.plugins.report.jck.model.BuildReport;
import hudson.plugins.report.jck.model.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class Tests {
    public static ArrayList<String> getFailedTests(File buildDir) {
        ArrayList<String> failedTests = new ArrayList<>();

        JckReportPublisher jcp = new JckReportPublisher("report-{runtime,devtools,compiler}.xml.gz");
        BuildSummaryParser bs = new BuildSummaryParser(Arrays.asList("jck", "jtreg"), jcp);

        BuildReport br = bs.parseJobReports(buildDir);

        for (Test t : br.getSuites().get(0).getReport().getTestProblems()) {
            failedTests.add(t.getName());
        }
        return failedTests;
    }

    public static void printFailedTable(ArrayList<ArrayList<String>> failedTestsMatrix, ArrayList<File> builds) {
        ArrayList<String> totalFailedTests = new ArrayList<>();
        for (ArrayList<String> l : failedTestsMatrix) {
            for (String s : l) {
                if (!totalFailedTests.contains(s)) {
                    totalFailedTests.add(s);
                }
            }
        }

        for (int i = 0; i < totalFailedTests.size(); i++) {
            System.out.println(i + ") " + totalFailedTests.get(i));
        }

        int longestLength = 0;
        for (File build : builds) {
            if (build.getAbsolutePath().length() > longestLength) {
                longestLength = build.getAbsolutePath().length();
            }
        }
        for (int i = 0; i < longestLength; i++) {
            System.out.print(" ");
        }
        for (int i = 0; i < totalFailedTests.size(); i++) {
            System.out.print(" | " + i);
        }
        System.out.print(" |\n");

        for (int i = 0; i < builds.size(); i++) {
            System.out.printf("%s", builds.get(i));
            for (long j = builds.get(i).getAbsolutePath().length(); j < longestLength; j++) {
                System.out.print(" ");
            }
            for (String test : totalFailedTests) {
                if(failedTestsMatrix.get(i).contains(test)) {
                    System.out.print(" | X");
                } else {
                    System.out.print(" |  ");
                }
            }
            System.out.print(" |\n");
        }
    }
}
