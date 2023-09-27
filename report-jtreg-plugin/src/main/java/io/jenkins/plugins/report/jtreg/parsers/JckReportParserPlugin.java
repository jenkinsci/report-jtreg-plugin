package io.jenkins.plugins.report.jtreg.parsers;

import io.jenkins.plugins.report.jtreg.model.Test;
import io.jenkins.plugins.report.jtreg.model.TestOutput;
import io.jenkins.plugins.report.jtreg.model.TestPlugin;
import io.jenkins.plugins.report.jtreg.model.TestStatus;

import java.util.Collections;
import java.util.List;
import javax.xml.stream.XMLStreamReader;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

public class JckReportParserPlugin extends JckReportParser {
    @Override
    protected Test parseTest(XMLStreamReader in) throws Exception {
        String url = findAttributeValue(in, "url");
        String status = findAttributeValue(in, "status");
        List<TestOutput> testOutputs = Collections.emptyList();
        String statusLine = null;
        while (in.hasNext()) {
            int event = in.next();
            if (event == END_ELEMENT && "TestResult".equals(in.getLocalName())) {
                break;
            }
            if (event != START_ELEMENT) {
                continue;
            }
            if ("ResultProperties".equals(in.getLocalName())) {
                statusLine = processStatusLine(in);
            }
            if ("Sections".equals(in.getLocalName())) {
                testOutputs = processTestOutputs(in);
            }
        }
        return new TestPlugin(url, TestStatus.valueOf(status.toUpperCase()), statusLine, testOutputs);
    }
}
