package io.jenkins.plugins.report.jtreg.parsers;

import io.jenkins.plugins.report.jtreg.model.Test;
import io.jenkins.plugins.report.jtreg.model.TestOutput;
import io.jenkins.plugins.report.jtreg.model.TestStatus;
import io.jenkins.plugins.report.jtreg.model.ReportFull;
import io.jenkins.plugins.report.jtreg.model.Suite;
import org.apache.commons.io.input.ReaderInputStream;
import org.junit.Assert;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class JckReportParserTest {

    private final static String reportCompilerFileName = "report-compiler.xml";
    private final static String reportDevtoolsFileName = "report-devtools.xml";
    private final static String reportRuntimeFileName = "report-runtime.xml";

    private final static String WSIMPORT = "wsimport";
    private final static String OUT1 = "out1";
    private final static String OUT2 = "out2";
    private final static String MESSAGES = "messages";
    private final static String SCRIPT_MESSAGES = "script_messages";
    private final static String TEST_ANNO_PROC_SRC = "testAnnoProcSrc.java";
    private final static String COMPILE = "compile.java";
    private final static String TEST_EXECUTE = "testExecute";

    private Document createDocument(String xmlFileName) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/" + xmlFileName), "UTF-8"));
            reader.readLine();
            return documentBuilder.parse(new ReaderInputStream(reader, StandardCharsets.UTF_8));
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new RuntimeException();
        }
    }

    private String getText(String xPathExpr, Document document) {
        String content;
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            content = (String) xPath.evaluate(xPathExpr, document, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            throw new RuntimeException();
        }
        return content.trim();
    }

    private InputStream getReportCompilerStream() throws IOException {
        return this.getClass().getResourceAsStream("/" + reportCompilerFileName);
    }

    private InputStream getReportDevtoolsStream() throws IOException {
        return this.getClass().getResourceAsStream("/" + reportDevtoolsFileName);
    }

    private InputStream getReportRuntimeStream() throws IOException {
        return this.getClass().getResourceAsStream("/" + reportRuntimeFileName);
    }

    public void checkCompilerReport(ReportFull actualReport) {
        Document document = createDocument(reportCompilerFileName);

        final List<String> testsList = new ArrayList<>();
        final List<Test> testProblems = new ArrayList<>();
        final List<TestOutput> outputs = new ArrayList<>();

        // passed testresults
        testsList.add("api/java_rmi/Naming/index.html#Bind");
        testsList.add("api/javax_annotation/processing/Messager/index.html#PM1_NEG");

        // url of failed testresult
        String testUrl;

        testUrl = "api/javax_lang/model/element/ModuleElement/ModEleDirVisit.html";
        testsList.add(testUrl);

        outputs.add(createTestOutput(document, testUrl, SCRIPT_MESSAGES, MESSAGES));
        outputs.addAll(createTestOutputList(document, testUrl, TEST_ANNO_PROC_SRC));

        testProblems.add(new Test(
                testUrl,
                TestStatus.FAILED,
                getText(getExecStatusExpr(testUrl), document),
                outputs
        ));

        final ReportFull expectedReport = new ReportFull(
                2,
                0,
                1,
                0,
                3,
                testProblems,
                testsList
        );

        Assert.assertEquals("Expected report doesn\'t match the actual report", expectedReport, actualReport);
    }

    @org.junit.Test
    public void parseCompilerReportTest() throws Exception {
        try (InputStream reportRuntimeStream = getReportCompilerStream()) {
            final JckReportParser parser = new JckReportParser();
            ReportFull actualReport = parser.parseReport(reportRuntimeStream);
            checkCompilerReport(actualReport);
        }
    }

    @org.junit.Test
    public void parseCompilerReportTestGz() throws Exception {
        Path path = Files.createTempFile("report-compiler", ".xml.gz");
        try (InputStream is = getReportCompilerStream();
            FileOutputStream fos = new FileOutputStream(path.toFile());
            OutputStream xzos = new GZIPOutputStream(fos)) {
            copyStream(is, xzos);
            xzos.close();
            final JckReportParser parser = new JckReportParser();
            Suite s = parser.parsePath(path);
            Assert.assertNotNull("Suite in not null", s);
            ReportFull actualReport = (ReportFull) s.getReport();
            checkCompilerReport(actualReport);
        } finally {
            Files.delete(path);
        }
    }

    @org.junit.Test
    public void parseCompilerReportTestXz() throws Exception {
        Path path = Files.createTempFile("report-compiler", ".xml.xz");
        try (InputStream is = getReportCompilerStream();
            FileOutputStream fos = new FileOutputStream(path.toFile());
            OutputStream xzos = new XZOutputStream(fos, new LZMA2Options())) {
            copyStream(is, xzos);
            xzos.close();
            final JckReportParser parser = new JckReportParser();
            Suite s = parser.parsePath(path);
            Assert.assertNotNull("Suite in not null", s);
            ReportFull actualReport = (ReportFull) s.getReport();
            checkCompilerReport(actualReport);
        } finally {
            Files.delete(path);
        }
    }

    private void checkDevtoolsReport(ReportFull actualReport) {
        Document document = createDocument(reportDevtoolsFileName);

        final List<String> testsList = new ArrayList<>();
        final List<Test> testProblems = new ArrayList<>();
        final List<TestOutput> outputs0 = new ArrayList<>();
        final List<TestOutput> outputs1 = new ArrayList<>();

        testsList.add("java2schema/CustomizedMapping/classes/XmlTransient/XmlTransient002.html#testCase0002");
        testsList.add("java2schema/CustomizedMapping/classes/XmlType/constraints/Constraint001.html");

        String testUrl;

        testUrl = "jaxws/mapping/w2jmapping/document/literal/annotations/HandlerChainAnnotationsTest.html#HandlerChainAnnotationsTest";
        testsList.add(testUrl);

        outputs0.addAll(createTestOutputList(document, testUrl, COMPILE));
        outputs0.add(createTestOutput(document, testUrl, SCRIPT_MESSAGES, MESSAGES));
        outputs0.addAll(createTestOutputList(document, testUrl, WSIMPORT));

        testProblems.add(new Test(
                testUrl,
                TestStatus.FAILED,
                getText(getExecStatusExpr(testUrl), document),
                outputs0
        ));

        testUrl = "jaxws/mapping/w2jmapping/document/literal/annotations/HelloOperationAnnotationsTest.html#HelloOperationAnnotationsTest";
        testsList.add(testUrl);

        outputs1.addAll(createTestOutputList(document, testUrl, COMPILE));
        outputs1.add(createTestOutput(document, testUrl, SCRIPT_MESSAGES, MESSAGES));
        outputs1.addAll(createTestOutputList(document, testUrl, WSIMPORT));

        testProblems.add(new Test(
                testUrl,
                TestStatus.FAILED,
                getText(getExecStatusExpr(testUrl), document),
                outputs1
        ));

        final ReportFull expectedReport = new ReportFull(
                2,
                0,
                2,
                0,
                4,
                testProblems,
                testsList
        );

        Assert.assertEquals("Expected report doesn\'t match the actual report", expectedReport, actualReport);
    }

    @org.junit.Test
    public void parseDevtoolsReportTest() throws Exception {
        try (InputStream reportRuntimeStream = getReportDevtoolsStream()) {
            final JckReportParser parser = new JckReportParser();
            ReportFull actualReport = parser.parseReport(reportRuntimeStream);
            checkDevtoolsReport(actualReport);
        }
    }

    @org.junit.Test
    public void parseDevtoolsReportTestGz() throws Exception {
        Path path = Files.createTempFile("report-devtools", ".xml.gz");
        try (InputStream is = getReportDevtoolsStream();
            FileOutputStream fos = new FileOutputStream(path.toFile());
            OutputStream xzos = new GZIPOutputStream(fos)) {
            copyStream(is, xzos);
            xzos.close();
            final JckReportParser parser = new JckReportParser();
            Suite s = parser.parsePath(path);
            Assert.assertNotNull("Suite in not null", s);
            ReportFull actualReport = (ReportFull) s.getReport();
            checkDevtoolsReport(actualReport);
        } finally {
            Files.delete(path);
        }
    }

    @org.junit.Test
    public void parseDevtoolsReportTestXz() throws Exception {
        Path path = Files.createTempFile("report-devtools", ".xml.xz");
        try (InputStream is = getReportDevtoolsStream();
            FileOutputStream fos = new FileOutputStream(path.toFile());
            OutputStream xzos = new XZOutputStream(fos, new LZMA2Options())) {
            copyStream(is, xzos);
            xzos.close();
            final JckReportParser parser = new JckReportParser();
            Suite s = parser.parsePath(path);
            Assert.assertNotNull("Suite in not null", s);
            ReportFull actualReport = (ReportFull) s.getReport();
            checkDevtoolsReport(actualReport);
        } finally {
            Files.delete(path);
        }
    }

    private void checkRuntimeReport(ReportFull actualReport) {
        Document document = createDocument(reportRuntimeFileName);

        final List<String> testsList = new ArrayList<>();
        final List<Test> testProblems = new ArrayList<>();
        final List<TestOutput> outputs0 = new ArrayList<>();
        final List<TestOutput> outputs1 = new ArrayList<>();

        testsList.add("api/java_applet/Applet/AccessibleApplet/serial/index.html#Constructor");

        String testUrl = "api/java_awt/Image_pack/ComponentColorModel/index.html#ConstructorTesttestCase1";
        testsList.add(testUrl);

        outputs0.add(createTestOutput(document, testUrl, SCRIPT_MESSAGES, MESSAGES));
        outputs0.addAll(createTestOutputList(document, testUrl, TEST_EXECUTE));

        testProblems.add(new Test(
                testUrl,
                TestStatus.FAILED,
                getText(getExecStatusExpr(testUrl), document),
                outputs0
        ));

        testUrl = "api/java_awt/awt_focus_subsystem/focus_vetoablechangelistener/index.html#VetoableChangeListener";
        testsList.add(testUrl);

        outputs1.add(createTestOutput(document, testUrl, SCRIPT_MESSAGES, MESSAGES));
        outputs1.addAll(createTestOutputList(document, testUrl, TEST_EXECUTE));

        testProblems.add(new Test(
                testUrl,
                TestStatus.FAILED,
                getText(getExecStatusExpr(testUrl), document),
                outputs1
        ));

        final ReportFull expectedReport = new ReportFull(
                1,
                0,
                2,
                0,
                3,
                testProblems,
                testsList
        );

        Assert.assertEquals("Expected suite doesn\'t match the actual suite", expectedReport, actualReport);
    }

    @org.junit.Test
    public void parseRuntimeReportTest() throws Exception {
        try (InputStream reportRuntimeStream = getReportRuntimeStream()) {
            final JckReportParser parser = new JckReportParser();
            ReportFull actualReport = parser.parseReport(reportRuntimeStream);
            checkRuntimeReport(actualReport);
        }
    }

    @org.junit.Test
    public void parseRuntimeReportTestGz() throws Exception {
        Path path = Files.createTempFile("report-runtime", ".xml.gz");
        try (InputStream is = getReportRuntimeStream();
            FileOutputStream fos = new FileOutputStream(path.toFile());
            OutputStream xzos = new GZIPOutputStream(fos)) {
            copyStream(is, xzos);
            xzos.close();
            final JckReportParser parser = new JckReportParser();
            Suite s = parser.parsePath(path);
            Assert.assertNotNull("Suite in not null", s);
            ReportFull actualReport = (ReportFull) s.getReport();
            checkRuntimeReport(actualReport);
        } finally {
            Files.delete(path);
        }
    }

    @org.junit.Test
    public void parseRuntimeReportTestXz() throws Exception {
        Path path = Files.createTempFile("report-runtime", ".xml.xz");
        try (InputStream is = getReportRuntimeStream();
            FileOutputStream fos = new FileOutputStream(path.toFile());
            OutputStream xzos = new XZOutputStream(fos, new LZMA2Options())) {
            copyStream(is, xzos);
            xzos.close();
            final JckReportParser parser = new JckReportParser();
            Suite s = parser.parsePath(path);
            Assert.assertNotNull("Suite in not null", s);
            ReportFull actualReport = (ReportFull) s.getReport();
            checkRuntimeReport(actualReport);
        } finally {
            Files.delete(path);
        }
    }

    private void copyStream(InputStream is, OutputStream os) throws IOException {
        int b;
        while ((b = is.read()) >= 0) {
            os.write(b);
        }
        is.close();
    }

    private String getExecStatusExpr(String testUrl) {
        return "/TestResults/TestResult[@url='" + testUrl + "']/ResultProperties/Property[@name='execStatus']/@value";
    }

    private String getResultOutputExpr(String testUrl, String sectionTitle, String outputTitle) {
        return "/TestResults/TestResult[@url='" + testUrl + "']/Sections/Section[@title='" + sectionTitle + "']/Output[@title='" + outputTitle + "']/text()";
    }

    private TestOutput createTestOutput(Document document, String testUrl, String title, String outputType) {
        return new TestOutput(title + " / " + outputType, getText(getResultOutputExpr(testUrl, title, outputType), document));
    }

    private List<TestOutput> createTestOutputList(Document document, String testUrl, String sectionTitle) {
        List<TestOutput> outputs = new ArrayList<>();
        outputs.add(createTestOutput(document, testUrl, sectionTitle, MESSAGES));
        outputs.add(createTestOutput(document, testUrl, sectionTitle, OUT1));
        outputs.add(createTestOutput(document, testUrl, sectionTitle, OUT2));
        return outputs;
    }
}
