package hudson.plugins.report.jck.model;

import java.util.List;
import java.util.Objects;

import static hudson.plugins.report.jck.utils.MoreStrings.compareStrings;

import hudson.plugins.report.jck.JenkinsReportJckGlobalConfig;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Test test = (Test) o;
        return Objects.equals(name, test.name) && status == test.status && Objects.equals(statusLine, test.statusLine) && Objects.equals(outputs, test.outputs);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, status, statusLine, outputs);
    }

    @Override
    public String toString() {
        return "Test{" + "name='" + name + '\'' + ", status=" + status + ", statusLine='" + statusLine + '\'' + ", " +
                "outputs=" + outputs + '}';
    }

    private static String createDiffUrl() {
        return SuiteTestsWithResults.getDiffServer() + "?generated-part=+-view%3Dall-tests+++-output%3Dhtml++-fill++";
    }


    public String getTrackingUrl(String job) {
        return (createDiffUrl()+"&custom-part=-track%3D"+name+"++"+job+"++0+-365").replaceAll("#", "%23");
    }

}
