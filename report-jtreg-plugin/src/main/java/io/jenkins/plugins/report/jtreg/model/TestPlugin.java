package io.jenkins.plugins.report.jtreg.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;
import java.util.Objects;

// TODO
@SuppressFBWarnings
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

    private static String createDiffUrl() {
        return SuiteTestsWithResultsPlugin.getDiffServer() + "?generated-part=+-view%3Dall-tests+++-output%3Dhtml++-fill++";
    }

    @Override
    public String getTrackingUrl(String job) {
        return (createDiffUrl()+"&custom-part=-track%3D"+name+"++"+job+"++0+-365").replaceAll("#", "%23");
    }

}