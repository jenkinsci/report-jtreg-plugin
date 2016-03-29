package hudson.plugins.report.jck.model;

import java.util.List;

public class ReportFull extends Report {

    private final List<String> testsList;

    public ReportFull(int testsPassed, int testsNotRun, int testsFailed, int testsError, int testsTotal, List<Test> testProblems, List<String> testsList) {
        super(testsPassed, testsNotRun, testsFailed, testsError, testsTotal, testProblems);
        this.testsList = testsList;
    }

    public List<String> getTestsList() {
        return testsList;
    }

}
