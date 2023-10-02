package io.jenkins.plugins.report.jtreg.model;

import java.io.Serializable;
import java.util.List;

public class TestFactory implements Serializable {
    public Test createTest(String name, TestStatus status, String statusLine, List<TestOutput> outputs) {
        return new Test(name, status, statusLine, outputs);
    }
}
