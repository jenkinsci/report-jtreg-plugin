package io.jenkins.plugins.report.jtreg.parsers;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.plugins.report.jtreg.model.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

public class JckReportParser implements ReportParser {

    private final boolean fixing;

    public JckReportParser() {
        super();
        fixing = true;
    }

    public JckReportParser(boolean fixing) {
        super();
        this.fixing = fixing;
    }

    @Override
    public Suite parsePath(Path path) {
        try (InputStream in = streamPath(path)) {
            ReportFull report = parseReport(in);
            return new Suite(suiteName(path), report);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private InputStream streamPath(Path path) throws IOException {
        InputStream stream = new BufferedInputStream(Files.newInputStream(path));
        if (path.toString().endsWith(".gz")) {
            return new GZIPInputStream(stream);
        }
        if (path.toString().endsWith(".xz")) {
            return new org.tukaani.xz.XZInputStream(stream);
        }
        return stream;
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "We deserve to die")
    private String suiteName(Path path) {
        String fullName = path.getFileName().toString();
        if (fullName.endsWith(".xml.xz")) {
            return fullName.substring(0, fullName.length() - 7);
        }
        if (fullName.endsWith(".xml.gz")) {
            return fullName.substring(0, fullName.length() - 7);
        }
        if (fullName.endsWith(".xml")) {
            return fullName.substring(0, fullName.length() - 4);
        }
        throw new IllegalArgumentException("file name does not end with either .xml or .xml.gz extension: " + fullName);
    }

    /**
     * Unluckkily JCK generates invalid xml which have invalid header of"
     * <?xml....?><Report>...
     * It should be
     * <?xml....?>
     * <Report>...
     * Jdk parser then fails to read it. This parser is fixing first and second buffer,
     * and if it founds ?> it inserts new line
     */
    static class FixingReader extends InputStreamReader {

        private boolean fixed = false;
        private boolean buffered = false;
        private int prevChar = 0;
        //private int maxCharsToReadInBadMode = 10000;

        public FixingReader(InputStream is, String charsetName) throws UnsupportedEncodingException {
            super(is, charsetName);
        }

        public int read() throws IOException {
            if (buffered) {
                buffered = false;
                return prevChar;
            }
            int c = super.read();
            if (!fixed && prevChar == '>') {
                fixed = true;
                buffered = true;
                prevChar = c;
                return '\n';
            }
            prevChar = c;
            return c;
        }

        public int read(char[] cbuf, int off, int len) throws IOException {
            //if (maxCharsToReadInBadMode <= 0) {
            //    return super.read(cbuf, off, len);
            //} else {
            //    maxCharsToReadInBadMode--;
            {
                if (prevChar < 0) {
                    return -1;
                }
                if (len == 0) {
                    return 0;
                }
                int c = read();
                if (c < 0) {
                    return -1;
                }
                cbuf[off] = (char) c;
                return 1;
            }
        }
    }

    ReportFull parseReport(InputStream reportStream) throws Exception {
        return parseReport(reportStream, fixing);
    }

    private ReportFull parseReport(InputStream reportStream, boolean fix) throws Exception {
        if (fixing) {
            try (Reader reader = new FixingReader(reportStream, "UTF-8")) {
                ReportFull report = parseReport(reader);
                return report;
            }
        } else {
            try (Reader reader = new InputStreamReader(reportStream, "UTF-8")) {
                ReportFull report = parseReport(reader);
                return report;
            }
        }
    }

    private ReportFull parseReport(Reader reader) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty("http://java.sun.com/xml/stream/properties/report-cdata-event", Boolean.TRUE);
        factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        XMLStreamReader in = factory.createXMLStreamReader(reader);
        if (!fastForwardToElement(in, "TestResults")) {
            throw new Exception("TestResults element was not found in provided XML stream");
        }
        return processTestResults(in);
    }

    @SuppressWarnings("empty-statement")
    private boolean fastForwardToElement(XMLStreamReader reader, String element) throws Exception {
        while (reader.hasNext()) {
            if (reader.next() == START_ELEMENT && element.equals(reader.getLocalName())) {
                return true;
            }
        }
        return false;
    }

    private ReportFull processTestResults(XMLStreamReader in) throws Exception {
        Map<String, AtomicInteger> countersMap = createCountersMap();
        List<Test> testProblemsList = new ArrayList<>();
        List<String> fullTestsList = new ArrayList<>();
        while (in.hasNext()) {
            int event = in.next();
            if (event == START_ELEMENT && "TestResult".equals(in.getLocalName())) {
                String testStatus = findAttributeValue(in, "status");
                incrementCounters(testStatus, countersMap);
                Test test = parseTest(in);
                if (test.getStatus() != TestStatus.NOT_RUN) {
                    fullTestsList.add(test.getName());
                }
                if (isProblematic(testStatus)) {
                    testProblemsList.add(test);
                }
            }
        }
        Collections.sort(testProblemsList);
        Collections.sort(fullTestsList);
        return new ReportFull(
                countersMap.get("PASSED").get(),
                countersMap.get("NOT_RUN").get(),
                countersMap.get("FAILED").get(),
                countersMap.get("ERROR").get(),
                countersMap.get("TOTAL").get(),
                testProblemsList,
                fullTestsList);
    }

    private Test parseTest(XMLStreamReader in) throws Exception {
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
        return new Test(url, TestStatus.valueOf(status.toUpperCase()), statusLine, testOutputs);
    }

    private String processStatusLine(XMLStreamReader in) throws Exception {
        String line = "";
        while (in.hasNext()) {
            int event = in.next();
            if (event == END_ELEMENT && "ResultProperties".equals(in.getLocalName())) {
                break;
            }
            if (event == START_ELEMENT && "Property".equals(in.getLocalName())) {
                if ("execStatus".equals(findAttributeValue(in, "name"))) {
                    line = findAttributeValue(in, "value");
                }
            }
        }
        return line;
    }

    private List<TestOutput> processTestOutputs(XMLStreamReader in) throws Exception {
        List<TestOutput> list = new ArrayList<>();
        while (in.hasNext()) {
            int event = in.next();
            if (event == END_ELEMENT && "Sections".equals(in.getLocalName())) {
                break;
            }
            if (event == START_ELEMENT && "Section".equals(in.getLocalName())) {
                list.addAll(processOutputSection(in));
            }
        }
        Collections.sort(list);
        return list;
    }

    private List<TestOutput> processOutputSection(XMLStreamReader in) throws Exception {
        String title = findAttributeValue(in, "title");
        List<TestOutput> list = new ArrayList<>();
        while (in.hasNext()) {
            int event = in.next();
            if (event == END_ELEMENT && "Section".equals(in.getLocalName())) {
                break;
            }
            if (event == START_ELEMENT && "Output".equals(in.getLocalName())) {
                list.add(processTestOutput(title, in));
            }
        }
        return list;
    }

    private TestOutput processTestOutput(String sectionTitle, XMLStreamReader in) throws Exception {
        String title = findAttributeValue(in, "title");
        String resultTitle = sectionTitle + " / " + title;
        while (in.hasNext()) {
            int event = in.next();
            if (event == END_ELEMENT && "Output".equals(in.getLocalName())) {
                break;
            }
            if (event == CDATA || event == CHARACTERS) {
                StringBuilder outputString = new StringBuilder();
                do {
                    outputString.append(in.getText());
                    event = in.next();
                } while (event == CDATA || event == CHARACTERS);
                return new TestOutput(resultTitle, outputString.toString().trim());
            }
        }
        return new TestOutput(resultTitle, "");
    }

    private void incrementCounters(String testStatus, Map<String, AtomicInteger> countersMap) throws Exception {
        AtomicInteger aInt = countersMap.get(testStatus);
        if (aInt == null) {
            throw new Exception("Invalid 'status' attribute value: " + testStatus);
        }
        aInt.incrementAndGet();
        countersMap.get("TOTAL").incrementAndGet();
    }

    private boolean isProblematic(String testStatus) {
        switch (testStatus) {
            case "FAILED":
            case "ERROR":
                return true;
            default:
                return false;
        }
    }

    private String findAttributeValue(XMLStreamReader in, String name) {
        int count = in.getAttributeCount();
        for (int i = 0; i < count; i++) {
            if (name.equals(in.getAttributeLocalName(i))) {
                return in.getAttributeValue(i);
            }
        }
        return null;
    }

    private Map<String, AtomicInteger> createCountersMap() {
        Map<String, AtomicInteger> map = new HashMap<>();
        map.put("NOT_RUN", new AtomicInteger());
        map.put("PASSED", new AtomicInteger());
        map.put("FAILED", new AtomicInteger());
        map.put("ERROR", new AtomicInteger());
        map.put("TOTAL", new AtomicInteger());
        return map;
    }

}
