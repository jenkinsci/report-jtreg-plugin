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

class ReportProjectActionUtilsCollectRegressionsAgainstTest {

    @Test
    void testCollectRegressionsAgainst_emptyReportsList() {
        BuildReport build = createBuildReport(5, "Build5",
            Arrays.asList(createFailedTest("Suite1", "Test1")));
        
        List<Integer> regressions = ReportProjectActionUtils.collectRegressionsAgainst(
            build, Collections.emptyList());
        
        assertEquals(0, regressions.size());
    }

    @Test
    void testCollectRegressionsAgainst_buildWithNoFailures() {
        BuildReport build = createBuildReport(5, "Build5", Collections.emptyList());
        
        BuildReport report1 = createBuildReport(1, "Build1",
            Arrays.asList(createFailedTest("Suite1", "Test1")));
        BuildReport report2 = createBuildReport(2, "Build2",
            Arrays.asList(createFailedTest("Suite1", "Test2")));
        
        List<Integer> regressions = ReportProjectActionUtils.collectRegressionsAgainst(
            build, Arrays.asList(report1, report2));
        
        assertEquals(2, regressions.size());
        assertEquals(0, regressions.get(0)); // No failures in build, so no regressions
        assertEquals(0, regressions.get(1)); // No failures in build, so no regressions
    }

    @Test
    void testCollectRegressionsAgainst_buildWithNewFailures() {
        BuildReport build = createBuildReport(5, "Build5",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2")
            ));
        
        BuildReport report1 = createBuildReport(1, "Build1", Collections.emptyList());
        BuildReport report2 = createBuildReport(2, "Build2",
            Arrays.asList(createFailedTest("Suite1", "Test1")));
        
        List<Integer> regressions = ReportProjectActionUtils.collectRegressionsAgainst(
            build, Arrays.asList(report1, report2));
        
        assertEquals(2, regressions.size());
        assertEquals(2, regressions.get(0)); // Both Test1 and Test2 are new in build compared to report1
        assertEquals(1, regressions.get(1)); // Test2 is new in build compared to report2
    }

    @Test
    void testCollectRegressionsAgainst_noRegressions() {
        BuildReport build = createBuildReport(5, "Build5",
            Arrays.asList(createFailedTest("Suite1", "Test1")));
        
        BuildReport report1 = createBuildReport(1, "Build1",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2")
            ));
        BuildReport report2 = createBuildReport(2, "Build2",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test3")
            ));
        
        List<Integer> regressions = ReportProjectActionUtils.collectRegressionsAgainst(
            build, Arrays.asList(report1, report2));
        
        assertEquals(2, regressions.size());
        assertEquals(0, regressions.get(0)); // Test1 was already failing in report1
        assertEquals(0, regressions.get(1)); // Test1 was already failing in report2
    }

    @Test
    void testCollectRegressionsAgainst_multipleRegressionsPerReport() {
        BuildReport build = createBuildReport(5, "Build5",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2"),
                createFailedTest("Suite1", "Test3"),
                createFailedTest("Suite2", "Test4")
            ));
        
        BuildReport report1 = createBuildReport(1, "Build1", Collections.emptyList());
        BuildReport report2 = createBuildReport(2, "Build2",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2")
            ));
        
        List<Integer> regressions = ReportProjectActionUtils.collectRegressionsAgainst(
            build, Arrays.asList(report1, report2));
        
        assertEquals(2, regressions.size());
        assertEquals(4, regressions.get(0)); // All 4 tests are new compared to report1
        assertEquals(2, regressions.get(1)); // Test3 and Test4 are new compared to report2
    }

    @Test
    void testCollectRegressionsAgainst_singleReport() {
        BuildReport build = createBuildReport(5, "Build5",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2"),
                createFailedTest("Suite1", "Test3")
            ));
        
        BuildReport report1 = createBuildReport(1, "Build1",
            Arrays.asList(createFailedTest("Suite1", "Test2")));
        
        List<Integer> regressions = ReportProjectActionUtils.collectRegressionsAgainst(
            build, Arrays.asList(report1));
        
        assertEquals(1, regressions.size());
        assertEquals(2, regressions.get(0)); // Test1 and Test3 are new (Test2 was already failing)
    }

    @Test
    void testCollectRegressionsAgainst_differentSuites() {
        BuildReport build = createBuildReport(5, "Build5",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite2", "Test2"),
                createFailedTest("Suite3", "Test3")
            ));
        
        BuildReport report1 = createBuildReport(1, "Build1",
            Arrays.asList(createFailedTest("Suite1", "Test1")));
        BuildReport report2 = createBuildReport(2, "Build2",
            Arrays.asList(createFailedTest("Suite2", "Test2")));
        
        List<Integer> regressions = ReportProjectActionUtils.collectRegressionsAgainst(
            build, Arrays.asList(report1, report2));
        
        assertEquals(2, regressions.size());
        assertEquals(2, regressions.get(0)); // Suite2/Test2 and Suite3/Test3 are new
        assertEquals(2, regressions.get(1)); // Suite1/Test1 and Suite3/Test3 are new
    }

    @Test
    void testCollectRegressionsAgainst_allReportsHaveSameFailures() {
        BuildReport build = createBuildReport(5, "Build5",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2")
            ));
        
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
        
        List<Integer> regressions = ReportProjectActionUtils.collectRegressionsAgainst(
            build, Arrays.asList(report1, report2));
        
        assertEquals(2, regressions.size());
        assertEquals(0, regressions.get(0)); // Same failures, no regressions
        assertEquals(0, regressions.get(1)); // Same failures, no regressions
    }

    @Test
    void testCollectRegressionsAgainst_comparisonIsIndependent() {
        // Each report is compared independently against build
        BuildReport build = createBuildReport(5, "Build5",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2"),
                createFailedTest("Suite1", "Test3")
            ));
        
        BuildReport report1 = createBuildReport(1, "Build1", Collections.emptyList());
        BuildReport report2 = createBuildReport(2, "Build2",
            Arrays.asList(createFailedTest("Suite1", "Test1")));
        BuildReport report3 = createBuildReport(3, "Build3",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2")
            ));
        
        List<Integer> regressions = ReportProjectActionUtils.collectRegressionsAgainst(
            build, Arrays.asList(report1, report2, report3));
        
        assertEquals(3, regressions.size());
        assertEquals(3, regressions.get(0)); // All 3 tests are new compared to report1
        assertEquals(2, regressions.get(1)); // Test2 and Test3 are new compared to report2
        assertEquals(1, regressions.get(2)); // Test3 is new compared to report3
    }

    @Test
    void testCollectRegressionsAgainst_mixedScenario() {
        BuildReport build = createBuildReport(5, "Build5",
            Arrays.asList(
                createFailedTest("Suite1", "Test2"),
                createFailedTest("Suite1", "Test3")
            ));
        
        BuildReport report1 = createBuildReport(1, "Build1",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2")
            ));
        BuildReport report2 = createBuildReport(2, "Build2",
            Arrays.asList(createFailedTest("Suite1", "Test3")));
        
        List<Integer> regressions = ReportProjectActionUtils.collectRegressionsAgainst(
            build, Arrays.asList(report1, report2));
        
        assertEquals(2, regressions.size());
        assertEquals(1, regressions.get(0)); // Test3 is new (Test2 was already failing, Test1 was fixed)
        assertEquals(1, regressions.get(1)); // Test2 is new (Test3 was already failing)
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
