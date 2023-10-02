package io.jenkins.plugins.report.jtreg.parsers;

import io.jenkins.plugins.report.jtreg.model.TestPluginFactory;

public class JckReportParserPlugin extends JckReportParser {
    public JckReportParserPlugin() {
        testFactory = new TestPluginFactory();
    }
}
