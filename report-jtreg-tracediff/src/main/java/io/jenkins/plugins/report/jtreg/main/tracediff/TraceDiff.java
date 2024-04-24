package io.jenkins.plugins.report.jtreg.main.tracediff;

import io.jenkins.plugins.report.jtreg.BuildReportExtended;
import io.jenkins.plugins.report.jtreg.BuildSummaryParser;
import io.jenkins.plugins.report.jtreg.utils.StackTraceTools;
import io.jenkins.plugins.report.jtreg.model.SuiteTestsWithResults;
import io.jenkins.plugins.report.jtreg.model.SuitesWithResults;
import io.jenkins.plugins.report.jtreg.wrappers.RunWrapperFromDir;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TraceDiff {
    public static void main(String[] args) {
        TraceDiffArgParser comparatorArgParser = new TraceDiffArgParser(new DiffInfo(), args);
        DiffInfo diffInfo = comparatorArgParser.parseAndGetOptions();

        if(diffInfo.isDie()) {
            System.out.print(DiffHelp.HELP_MESSAGE);
            return;
        }

        printTraceDiff(diffInfo);
    }

    private static void printTraceDiff(DiffInfo diffInfo) {
        File buildOne = diffInfo.getBuildOne(diffInfo.getJobsPath());
        File buildTwo = diffInfo.getBuildTwo(diffInfo.getJobsPath());
        String testRegex = diffInfo.getExactTestsRegex();
        List<String> matchedFailedTests = new ArrayList<>();

        try {
            BuildSummaryParser bs = new BuildSummaryParser(Arrays.asList("jck", "jtreg"), null);

            BuildReportExtended bex1 = bs.parseBuildReportExtended(new RunWrapperFromDir(buildOne), null);
            SuitesWithResults swr1 = bex1.getAllTests();

            BuildReportExtended bex2 = bs.parseBuildReportExtended(new RunWrapperFromDir(buildTwo), null);
            SuitesWithResults swr2 = bex2.getAllTests();

            List<String> buildTwoFailedTests = new ArrayList<>();
            swr2.getAllTestsAndSuites().forEach(stwr -> stwr.getTests().stream().filter(t -> t.getStatus().isFailed()).forEach(s -> buildTwoFailedTests.add(s.getTestName())));

            for (SuiteTestsWithResults stwr : swr1.getAllTestsAndSuites()) {
                for (SuiteTestsWithResults.StringWithResult t : stwr.getTests()) {
                    if (t.getStatus().isFailed() && t.getTestName().matches(testRegex) && buildTwoFailedTests.contains(t.getTestName())) {
                        matchedFailedTests.add(t.getTestName());
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("An exception was caught when trying to get the failed tests of " + diffInfo.getBuildOneName());
            e.printStackTrace();
        }

        for (String test : matchedFailedTests) {
            String traceOne = StackTraceTools.getTestTrace(buildOne, test, diffInfo.getSubstringSide(), diffInfo.getSubstringLength());
            String traceTwo = StackTraceTools.getTestTrace(buildTwo, test, diffInfo.getSubstringSide(), diffInfo.getSubstringLength());

            diffInfo.getFormatter().printDiff(traceOne, diffInfo.getBuildOneName() + " : test " + test, traceTwo,
                    diffInfo.getBuildTwoName() + " : test " + test, diffInfo.getTypeOfDiff());
        }
    }
}
