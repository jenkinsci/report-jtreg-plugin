package io.jenkins.plugins.report.jtreg.model;

import java.util.List;

public class TestPluginFactory extends TestFactory {
    public Test createTest(String name, TestStatus status, String statusLine, List<TestOutput> outputs) {
        return new TestPlugin(name, status, statusLine, outputs);
    }
}
