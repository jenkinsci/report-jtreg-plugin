package io.jenkins.plugins.report.jtreg.model;

import java.util.List;
import java.util.Objects;

public class ReportFull extends Report {

    private final List<String> testsList;

    public ReportFull(int testsPassed, int testsNotRun, int testsFailed, int testsError, int testsTotal, List<? extends Test> testProblems, List<String> testsList) {
        super(testsPassed, testsNotRun, testsFailed, testsError, testsTotal, testProblems);
        this.testsList = testsList;
    }

    public List<String> getTestsList() {
        return testsList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ReportFull that = (ReportFull) o;
        return Objects.equals(testsList, that.testsList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), testsList);
    }

    @Override
    public String toString() {
        return "ReportFull{" + "testsList=" + testsList + "} " + super.toString();
    }
}
