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
        assertEquals(0, improvements.get(0));
        assertEquals(0, improvements.get(1));
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
        assertEquals(0, improvements.get(0));
        assertEquals(0, improvements.get(1));
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
        assertEquals(1, improvements.get(0));
        assertEquals(1, improvements.get(1));
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
        assertEquals(0, improvements.get(0));
        assertEquals(0, improvements.get(1));
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
        assertEquals(0, improvements.get(0));
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
        assertEquals(0, improvements.get(0));
        assertEquals(1, improvements.get(1));
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
        assertEquals(1, improvements.get(0));
        assertEquals(1, improvements.get(1));
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
        assertEquals(0, improvements.get(0));
        assertEquals(0, improvements.get(1));
        assertEquals(0, improvements.get(2));
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
