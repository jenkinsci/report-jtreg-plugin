package io.jenkins.plugins.report.jtreg;

import io.jenkins.plugins.report.jtreg.model.Suite;
import io.jenkins.plugins.report.jtreg.model.SuiteTestChanges;
import io.jenkins.plugins.report.jtreg.model.SuitesWithResults;

import java.util.List;

public class BuildReportExtendedFactory {
    public BuildReportExtended createBuildReportExtended(int buildNumber, String buildName, int passed, int failed,
                                                         int error, List<Suite> suites, List<String> addedSuites,
                                                         List<String> removedSuites, List<SuiteTestChanges> testChanges,
                                                         int total, int notRun, SuitesWithResults allTests, String job) {
        return new BuildReportExtended(buildNumber, buildName, passed, failed, error, suites, addedSuites, removedSuites, testChanges, total, notRun, allTests, job);
    }
}
