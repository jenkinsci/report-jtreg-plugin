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

class ReportProjectActionUtilsCollectImprovementsTest {

    @Test
    void testCollectImprovements_emptyList() {
        List<BuildReport> reports = Collections.emptyList();
        List<Integer> improvements = ReportProjectActionUtils.collectImprovements(reports);
        assertEquals(0, improvements.size());
    }

    @Test
    void testCollectImprovements_singleBuild() {
        BuildReport report = createBuildReport(1, "Build1", 
            Arrays.asList(createFailedTest("Suite1", "Test1")));
        
        List<Integer> improvements = ReportProjectActionUtils.collectImprovements(Arrays.asList(report));
        
        assertEquals(1, improvements.size());
        assertEquals(0, improvements.get(0)); // First build has no improvements
    }

    @Test
    void testCollectImprovements_twoBuilds_oneTestFixed() {
        BuildReport report1 = createBuildReport(1, "Build1",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2")
            ));
        
        BuildReport report2 = createBuildReport(2, "Build2",
            Arrays.asList(
                createFailedTest("Suite1", "Test2")
            ));
        
        List<Integer> improvements = ReportProjectActionUtils.collectImprovements(
            Arrays.asList(report1, report2));
        
        assertEquals(2, improvements.size());
        assertEquals(0, improvements.get(0)); // First build
        assertEquals(1, improvements.get(1)); // Test1 was fixed
    }

    @Test
    void testCollectImprovements_threeBuilds_progressiveImprovements() {
        BuildReport report1 = createBuildReport(1, "Build1",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2"),
                createFailedTest("Suite2", "Test3")
            ));
        
        BuildReport report2 = createBuildReport(2, "Build2",
            Arrays.asList(
                createFailedTest("Suite1", "Test2"),
                createFailedTest("Suite2", "Test3")
            ));
        
        BuildReport report3 = createBuildReport(3, "Build3",
            Arrays.asList(
                createFailedTest("Suite2", "Test3")
            ));
        
        List<Integer> improvements = ReportProjectActionUtils.collectImprovements(
            Arrays.asList(report1, report2, report3));
        
        assertEquals(3, improvements.size());
        assertEquals(0, improvements.get(0)); // First build
        assertEquals(1, improvements.get(1)); // Test1 fixed
        assertEquals(1, improvements.get(2)); // Test2 fixed
    }

    @Test
    void testCollectImprovements_allTestsFixed() {
        BuildReport report1 = createBuildReport(1, "Build1",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2")
            ));
        
        BuildReport report2 = createBuildReport(2, "Build2", Collections.emptyList());
        
        List<Integer> improvements = ReportProjectActionUtils.collectImprovements(
            Arrays.asList(report1, report2));
        
        assertEquals(2, improvements.size());
        assertEquals(0, improvements.get(0)); // First build
        assertEquals(2, improvements.get(1)); // Both tests fixed
    }

    @Test
    void testCollectImprovements_noImprovements() {
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
        
        List<Integer> improvements = ReportProjectActionUtils.collectImprovements(
            Arrays.asList(report1, report2));
        
        assertEquals(2, improvements.size());
        assertEquals(0, improvements.get(0)); // First build
        assertEquals(0, improvements.get(1)); // No tests fixed
    }

    @Test
    void testCollectImprovements_multipleSuites() {
        BuildReport report1 = createBuildReport(1, "Build1",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite2", "Test2"),
                createFailedTest("Suite3", "Test3")
            ));
        
        BuildReport report2 = createBuildReport(2, "Build2",
            Arrays.asList(
                createFailedTest("Suite2", "Test2")
            ));
        
        List<Integer> improvements = ReportProjectActionUtils.collectImprovements(
            Arrays.asList(report1, report2));
        
        assertEquals(2, improvements.size());
        assertEquals(0, improvements.get(0)); // First build
        assertEquals(2, improvements.get(1)); // Test1 and Test3 fixed
    }

    @Test
    void testCollectImprovements_withNewFailures() {
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
        
        List<Integer> improvements = ReportProjectActionUtils.collectImprovements(
            Arrays.asList(report1, report2));
        
        assertEquals(2, improvements.size());
        assertEquals(0, improvements.get(0)); // First build
        assertEquals(1, improvements.get(1)); // Test1 fixed (Test3 is new, not an improvement)
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
