package io.jenkins.plugins.report.jtreg.model;

import java.util.List;
import java.util.Objects;

public class TestPlugin extends Test {

    private final String name;
    private final TestStatus status;
    private final String statusLine;
    private final List<TestOutput> outputs;

    public TestPlugin(String name, TestStatus status, String statusLine, List<TestOutput> outputs) {
        super(name, status, statusLine, outputs);
        this.name = name;
        this.status = status;
        this.statusLine = statusLine;
        this.outputs = outputs;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TestPlugin test = (TestPlugin) o;
        return Objects.equals(name, test.name) && status == test.status && Objects.equals(statusLine, test.statusLine) && Objects.equals(outputs, test.outputs);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, status, statusLine, outputs);
    }

    private static String createDiffUrl() {
        return SuiteTestsWithResultsPlugin.getDiffServer() + "?generated-part=+-view%3Dall-tests+++-output%3Dhtml++-fill++";
    }

    public String getTrackingUrl(String job) {
        return (createDiffUrl()+"&custom-part=-track%3D"+name+"++"+job+"++0+-365").replaceAll("#", "%23");
    }

}