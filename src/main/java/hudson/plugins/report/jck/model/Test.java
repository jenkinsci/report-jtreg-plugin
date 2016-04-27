package hudson.plugins.report.jck.model;

import java.util.List;

import static hudson.plugins.report.jck.utils.MoreStrings.compareStrings;

public class Test implements Comparable<Test>, java.io.Serializable {

    private final String name;
    private final TestStatus status;
    private final String statusLine;
    private final List<TestOutput> outputs;

    public Test(String name, TestStatus status, String statusLine, List<TestOutput> outputs) {
        this.name = name;
        this.status = status;
        this.statusLine = statusLine;
        this.outputs = outputs;
    }

    public String getName() {
        return name;
    }

    public TestStatus getStatus() {
        return status;
    }

    public String getStatusLine() {
        return statusLine;
    }

    public List<TestOutput> getOutputs() {
        return outputs;
    }

    @Override
    public int compareTo(Test o) {
        return compareStrings(name, o.getName());
    }

}
