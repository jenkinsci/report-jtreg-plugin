package io.jenkins.plugins.report.jtreg;

import io.jenkins.plugins.report.jtreg.model.BuildReport;
import io.jenkins.plugins.report.jtreg.model.Report;
import io.jenkins.plugins.report.jtreg.model.Suite;
import io.jenkins.plugins.report.jtreg.model.TestStatus;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportProjectActionUtilsCollectRegressionsTest {

    @Test
    void testCollectRegressions_emptyList() {
        List<BuildReport> reports = Collections.emptyList();
        List<Integer> regressions = ReportProjectActionUtils.collectRegressions(reports);
        assertEquals(0, regressions.size());
    }

    @Test
    void testCollectRegressions_singleBuild() {
        BuildReport report = createBuildReport(1, "Build1",
            Arrays.asList(createFailedTest("Suite1", "Test1")));
        
        List<Integer> regressions = ReportProjectActionUtils.collectRegressions(Arrays.asList(report));
        
        assertEquals(1, regressions.size());
        assertEquals(0, regressions.get(0)); // First build has no regressions
    }

    @Test
    void testCollectRegressions_twoBuilds_oneNewFailure() {
        BuildReport report1 = createBuildReport(1, "Build1",
            Arrays.asList(
                createFailedTest("Suite1", "Test1")
            ));
        
        BuildReport report2 = createBuildReport(2, "Build2",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2")
            ));
        
        List<Integer> regressions = ReportProjectActionUtils.collectRegressions(
            Arrays.asList(report1, report2));
        
        assertEquals(2, regressions.size());
        assertEquals(0, regressions.get(0)); // First build
        assertEquals(1, regressions.get(1)); // Test2 is new failure
    }

    @Test
    void testCollectRegressions_threeBuilds_progressiveRegressions() {
        BuildReport report1 = createBuildReport(1, "Build1",
            Arrays.asList(
                createFailedTest("Suite1", "Test1")
            ));
        
        BuildReport report2 = createBuildReport(2, "Build2",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2")
            ));
        
        BuildReport report3 = createBuildReport(3, "Build3",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2"),
                createFailedTest("Suite2", "Test3"),
                createFailedTest("Suite2", "Test4")
            ));
        
        List<Integer> regressions = ReportProjectActionUtils.collectRegressions(
            Arrays.asList(report1, report2, report3));
        
        assertEquals(3, regressions.size());
        assertEquals(0, regressions.get(0)); // First build
        assertEquals(1, regressions.get(1)); // Test2 is new
        assertEquals(2, regressions.get(2)); // Test3 and Test4 are new
    }

    @Test
    void testCollectRegressions_noNewFailures() {
        BuildReport report1 = createBuildReport(1, "Build1",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2")
            ));
        
        BuildReport report2 = createBuildReport(2, "Build2",
            Arrays.asList(
                createFailedTest("Suite1", "Test1")
            ));
        
        List<Integer> regressions = ReportProjectActionUtils.collectRegressions(
            Arrays.asList(report1, report2));
        
        assertEquals(2, regressions.size());
        assertEquals(0, regressions.get(0)); // First build
        assertEquals(0, regressions.get(1)); // No new failures (Test2 was fixed)
    }

    @Test
    void testCollectRegressions_allNewFailures() {
        BuildReport report1 = createBuildReport(1, "Build1", Collections.emptyList());
        
        BuildReport report2 = createBuildReport(2, "Build2",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2")
            ));
        
        List<Integer> regressions = ReportProjectActionUtils.collectRegressions(
            Arrays.asList(report1, report2));
        
        assertEquals(2, regressions.size());
        assertEquals(0, regressions.get(0)); // First build
        assertEquals(2, regressions.get(1)); // Both tests are new failures
    }

    @Test
    void testCollectRegressions_sameFailures() {
        BuildReport report1 = createBuildReport(1, "Build1",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2")
            ));
        
        BuildReport report2 = createBuildReport(2, "Build2",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2")
            ));
        
        List<Integer> regressions = ReportProjectActionUtils.collectRegressions(
            Arrays.asList(report1, report2));
        
        assertEquals(2, regressions.size());
        assertEquals(0, regressions.get(0)); // First build
        assertEquals(0, regressions.get(1)); // Same failures, no new ones
    }

    @Test
    void testCollectRegressions_multipleSuites() {
        BuildReport report1 = createBuildReport(1, "Build1",
            Arrays.asList(
                createFailedTest("Suite1", "Test1")
            ));
        
        BuildReport report2 = createBuildReport(2, "Build2",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite2", "Test2"),
                createFailedTest("Suite3", "Test3")
            ));
        
        List<Integer> regressions = ReportProjectActionUtils.collectRegressions(
            Arrays.asList(report1, report2));
        
        assertEquals(2, regressions.size());
        assertEquals(0, regressions.get(0)); // First build
        assertEquals(2, regressions.get(1)); // Test2 and Test3 are new failures
    }

    @Test
    void testCollectRegressions_withFixedTests() {
        // Build 1: Test1, Test2 fail
        BuildReport report1 = createBuildReport(1, "Build1",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2")
            ));
        
        // Build 2: Test2, Test3 fail (Test1 fixed, Test3 new)
        BuildReport report2 = createBuildReport(2, "Build2",
            Arrays.asList(
                createFailedTest("Suite1", "Test2"),
                createFailedTest("Suite1", "Test3")
            ));
        
        List<Integer> regressions = ReportProjectActionUtils.collectRegressions(
            Arrays.asList(report1, report2));
        
        assertEquals(2, regressions.size());
        assertEquals(0, regressions.get(0)); // First build
        assertEquals(1, regressions.get(1)); // Test3 is new (Test1 fixed is not a regression)
    }

    @Test
    void testCollectRegressions_allTestsFixed() {
        BuildReport report1 = createBuildReport(1, "Build1",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2")
            ));
        
        BuildReport report2 = createBuildReport(2, "Build2", Collections.emptyList());
        
        List<Integer> regressions = ReportProjectActionUtils.collectRegressions(
            Arrays.asList(report1, report2));
        
        assertEquals(2, regressions.size());
        assertEquals(0, regressions.get(0)); // First build
        assertEquals(0, regressions.get(1)); // No new failures (all tests fixed)
    }

    // Helper methods to create test data

    private BuildReport createBuildReport(int buildNumber, String buildName, List<Suite> suites) {
        return new BuildReport(buildNumber, buildName, 0, 0, 0, suites, 0, 0, 
            System.currentTimeMillis(), 1000L);
    }

    private Suite createFailedTest(String suiteName, String testName) {
        io.jenkins.plugins.report.jtreg.model.Test test = new io.jenkins.plugins.report.jtreg.model.Test(testName, TestStatus.FAILED, "Failed", Collections.emptyList());
        Report report = new Report(0, 0, 1, 0, 1, Arrays.asList(test));
        return new Suite(suiteName, report);
    }
}

// Made with Bob
