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

class ReportProjectActionUtilsCollectImprovementsAgainstTest {

    @Test
    void testCollectImprovementsAgainst_emptyReportsList() {
        BuildReport build = createBuildReport(5, "Build5",
            Arrays.asList(createFailedTest("Suite1", "Test1")));
        
        List<Integer> improvements = ReportProjectActionUtils.collectImprovementsAgainst(
            build, Collections.emptyList());
        
        assertEquals(0, improvements.size());
    }

    @Test
    void testCollectImprovementsAgainst_buildWithNoFailures() {
        BuildReport build = createBuildReport(5, "Build5", Collections.emptyList());
        
        BuildReport report1 = createBuildReport(1, "Build1",
            Arrays.asList(createFailedTest("Suite1", "Test1")));
        BuildReport report2 = createBuildReport(2, "Build2",
            Arrays.asList(createFailedTest("Suite1", "Test2")));
        
        List<Integer> improvements = ReportProjectActionUtils.collectImprovementsAgainst(
            build, Arrays.asList(report1, report2));
        
        assertEquals(2, improvements.size());
        assertEquals(1, improvements.get(0)); // Test1 from report1 is fixed in build
        assertEquals(1, improvements.get(1)); // Test2 from report2 is fixed in build
    }

    @Test
    void testCollectImprovementsAgainst_buildWithSomeFailures() {
        BuildReport build = createBuildReport(5, "Build5",
            Arrays.asList(createFailedTest("Suite1", "Test2")));
        
        BuildReport report1 = createBuildReport(1, "Build1",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2")
            ));
        BuildReport report2 = createBuildReport(2, "Build2",
            Arrays.asList(
                createFailedTest("Suite1", "Test2"),
                createFailedTest("Suite1", "Test3")
            ));
        
        List<Integer> improvements = ReportProjectActionUtils.collectImprovementsAgainst(
            build, Arrays.asList(report1, report2));
        
        assertEquals(2, improvements.size());
        assertEquals(1, improvements.get(0)); // Test1 from report1 is fixed (Test2 still fails)
        assertEquals(1, improvements.get(1)); // Test3 from report2 is fixed (Test2 still fails)
    }

    @Test
    void testCollectImprovementsAgainst_noImprovements() {
        BuildReport build = createBuildReport(5, "Build5",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2")
            ));
        
        BuildReport report1 = createBuildReport(1, "Build1",
            Arrays.asList(createFailedTest("Suite1", "Test1")));
        BuildReport report2 = createBuildReport(2, "Build2",
            Arrays.asList(createFailedTest("Suite1", "Test2")));
        
        List<Integer> improvements = ReportProjectActionUtils.collectImprovementsAgainst(
            build, Arrays.asList(report1, report2));
        
        assertEquals(2, improvements.size());
        assertEquals(0, improvements.get(0)); // Test1 still fails in build
        assertEquals(0, improvements.get(1)); // Test2 still fails in build
    }

    @Test
    void testCollectImprovementsAgainst_multipleImprovementsPerReport() {
        BuildReport build = createBuildReport(5, "Build5", Collections.emptyList());
        
        BuildReport report1 = createBuildReport(1, "Build1",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2"),
                createFailedTest("Suite1", "Test3")
            ));
        BuildReport report2 = createBuildReport(2, "Build2",
            Arrays.asList(
                createFailedTest("Suite2", "Test4"),
                createFailedTest("Suite2", "Test5")
            ));
        
        List<Integer> improvements = ReportProjectActionUtils.collectImprovementsAgainst(
            build, Arrays.asList(report1, report2));
        
        assertEquals(2, improvements.size());
        assertEquals(3, improvements.get(0)); // All 3 tests from report1 are fixed
        assertEquals(2, improvements.get(1)); // Both tests from report2 are fixed
    }

    @Test
    void testCollectImprovementsAgainst_singleReport() {
        BuildReport build = createBuildReport(5, "Build5",
            Arrays.asList(createFailedTest("Suite1", "Test2")));
        
        BuildReport report1 = createBuildReport(1, "Build1",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2"),
                createFailedTest("Suite1", "Test3")
            ));
        
        List<Integer> improvements = ReportProjectActionUtils.collectImprovementsAgainst(
            build, Arrays.asList(report1));
        
        assertEquals(1, improvements.size());
        assertEquals(2, improvements.get(0)); // Test1 and Test3 are fixed (Test2 still fails)
    }

    @Test
    void testCollectImprovementsAgainst_differentSuites() {
        BuildReport build = createBuildReport(5, "Build5",
            Arrays.asList(createFailedTest("Suite2", "Test2")));
        
        BuildReport report1 = createBuildReport(1, "Build1",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite2", "Test2")
            ));
        BuildReport report2 = createBuildReport(2, "Build2",
            Arrays.asList(
                createFailedTest("Suite3", "Test3")
            ));
        
        List<Integer> improvements = ReportProjectActionUtils.collectImprovementsAgainst(
            build, Arrays.asList(report1, report2));
        
        assertEquals(2, improvements.size());
        assertEquals(1, improvements.get(0)); // Suite1/Test1 is fixed (Suite2/Test2 still fails)
        assertEquals(1, improvements.get(1)); // Suite3/Test3 is fixed
    }

    @Test
    void testCollectImprovementsAgainst_allReportsHaveNoFailures() {
        BuildReport build = createBuildReport(5, "Build5",
            Arrays.asList(createFailedTest("Suite1", "Test1")));
        
        BuildReport report1 = createBuildReport(1, "Build1", Collections.emptyList());
        BuildReport report2 = createBuildReport(2, "Build2", Collections.emptyList());
        
        List<Integer> improvements = ReportProjectActionUtils.collectImprovementsAgainst(
            build, Arrays.asList(report1, report2));
        
        assertEquals(2, improvements.size());
        assertEquals(0, improvements.get(0)); // No failures in report1 to fix
        assertEquals(0, improvements.get(1)); // No failures in report2 to fix
    }

    @Test
    void testCollectImprovementsAgainst_comparisonIsIndependent() {
        // Each report is compared independently against build
        BuildReport build = createBuildReport(5, "Build5",
            Arrays.asList(createFailedTest("Suite1", "Test3")));
        
        BuildReport report1 = createBuildReport(1, "Build1",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test3")
            ));
        BuildReport report2 = createBuildReport(2, "Build2",
            Arrays.asList(
                createFailedTest("Suite1", "Test2"),
                createFailedTest("Suite1", "Test3")
            ));
        BuildReport report3 = createBuildReport(3, "Build3",
            Arrays.asList(
                createFailedTest("Suite1", "Test1"),
                createFailedTest("Suite1", "Test2"),
                createFailedTest("Suite1", "Test3")
            ));
        
        List<Integer> improvements = ReportProjectActionUtils.collectImprovementsAgainst(
            build, Arrays.asList(report1, report2, report3));
        
        assertEquals(3, improvements.size());
        assertEquals(1, improvements.get(0)); // Test1 fixed (Test3 still fails)
        assertEquals(1, improvements.get(1)); // Test2 fixed (Test3 still fails)
        assertEquals(2, improvements.get(2)); // Test1 and Test2 fixed (Test3 still fails)
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
