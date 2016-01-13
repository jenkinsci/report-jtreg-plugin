package hudson.plugins.report.jck.model;

import java.util.List;
import lombok.Value;

@Value
public class Report {

    int testsPassed;
    int testsNotRun;
    int testsFailed;
    int testsError;
    int testsTotal;
    List<Test> testProblems;

}
