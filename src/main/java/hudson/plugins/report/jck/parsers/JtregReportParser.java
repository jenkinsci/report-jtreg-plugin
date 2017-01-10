/*
 * The MIT License
 *
 * Copyright 2016 user.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, mergeOutputs, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.report.jck.parsers;

import hudson.plugins.report.jck.model.ReportFull;
import hudson.plugins.report.jck.model.Suite;
import hudson.plugins.report.jck.model.Test;
import hudson.plugins.report.jck.model.TestOutput;
import hudson.plugins.report.jck.model.TestStatus;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.io.input.CloseShieldInputStream;

import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;

public class JtregReportParser implements ReportParser {

    private static final Map<String, ArchiveFactory> SUPPORTED_ARCHIVE_TYPES_MAP = createSupportedArchiveTypesMap();

    @Override
    public Suite parsePath(Path path) {
        List<Test> testsList = new ArrayList<>();
        try (ArchiveInputStream in = streamPath(path)) {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            ArchiveEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                String entryName = entry.getName();
                if (entryName == null || !entryName.endsWith(".jtr.xml")) {
                    continue;
                }
                try {
                    XMLStreamReader reader = inputFactory.createXMLStreamReader(new CloseShieldInputStream(in), "UTF-8");
                    testsList.addAll(parseTestsuites(reader));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (testsList.isEmpty()) {
            return null;
        }
        List<String> testNames = testsList.stream()
                .sequential()
                .map(t -> t.getName())
                .sorted()
                .collect(Collectors.toList());
        List<Test> testProblems = testsList.stream()
                .sequential()
                .filter(t -> t.getStatus() == TestStatus.ERROR || t.getStatus() == TestStatus.FAILED)
                .sorted()
                .collect(Collectors.toList());
        ReportFull fullReport = new ReportFull(
                (int) testsList.stream().sequential().filter(t -> t.getStatus() == TestStatus.PASSED).count(),
                0,
                (int) testsList.stream().sequential().filter(t -> t.getStatus() == TestStatus.FAILED).count(),
                (int) testsList.stream().sequential().filter(t -> t.getStatus() == TestStatus.ERROR).count(),
                testsList.size(), testProblems, testNames);
        return new Suite(suiteName(path), fullReport);
    }

    private String suiteName(Path path) {
        for (Entry<String, ArchiveFactory> factory : SUPPORTED_ARCHIVE_TYPES_MAP.entrySet()) {
            String pathName = path.getFileName().toString();
            if (pathName.toLowerCase().endsWith(factory.getKey())) {
                return pathName.substring(0, pathName.length() - factory.getKey().length());
            }
        }
        return path.getFileName().toString();
    }

    private List<Test> parseTestsuites(XMLStreamReader in) throws Exception {
        List<Test> r = new ArrayList<>();
        String ignored = "com.google.security.wycheproof.OpenJDKAllTests";
        while (in.hasNext()) {
            int event = in.next();
            if (event == START_ELEMENT && TESTSUITE.equals(in.getLocalName())) {
                JtregBackwardCompatibileSuite suite = parseTestSuite(in);
                if (ignored.equals(suite.name)) {
                    System.out.println("Skipping ignored suite : " + ignored);
                } else {
                    r.addAll(suite.getTests());
                }
            }
        }
        return r;
    }
    private static final String SYSTEMOUT = "system-out";
    private static final String SYSTEMERR = "system-err";
    private static final String TESTSUITE = "testsuite";
    private static final String TESTCASE = "testcase";
    private static final String PROPERTIES = "properties";
    private static final String FAILURE = "failure";

    private String findAttributeValue(XMLStreamReader in, String name) {
        int count = in.getAttributeCount();
        for (int i = 0; i < count; i++) {
            if (name.equals(in.getAttributeLocalName(i))) {
                return in.getAttributeValue(i);
            }
        }
        return null;
    }

    private int tryParseString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
        }
        return 1;
    }

    private String findStatusLine(XMLStreamReader in) throws Exception {
        while (in.hasNext()) {
            int event = in.next();
            if (event == END_ELEMENT && PROPERTIES.equals(in.getLocalName())) {
                break;
            }
            if (event == START_ELEMENT && "property".equals(in.getLocalName())) {
                if ("execStatus".equals(findAttributeValue(in, "name"))) {
                    return findAttributeValue(in, "value");
                }
            }
        }
        return "";
    }

    private String captureCharacters(XMLStreamReader in, String element) throws Exception {
        while (in.hasNext()) {
            int event = in.next();
            if (event == END_ELEMENT && element.equals(in.getLocalName())) {
                break;
            }
            if (event == CHARACTERS) {
                return in.getText();
            }
        }
        return "";
    }

    private ArchiveInputStream streamPath(Path path) throws IOException {
        for (Entry<String, ArchiveFactory> factory : SUPPORTED_ARCHIVE_TYPES_MAP.entrySet()) {
            String pathName = path.getFileName().toString().toLowerCase();
            if (pathName.endsWith(factory.getKey())) {
                InputStream stream = new BufferedInputStream(Files.newInputStream(path));
                return factory.getValue().create(stream);
            }
        }
        throw new IOException("Unsupported archive format: " + path.getFileName());
    }

    private static Map<String, ArchiveFactory> createSupportedArchiveTypesMap() {
        Map<String, ArchiveFactory> map = new HashMap<>();

        map.put(".zip", in -> new ZipArchiveInputStream(in));
        map.put(".tar", in -> new TarArchiveInputStream(in));
        map.put(".tar.gz", in -> new TarArchiveInputStream(new GzipCompressorInputStream(in)));
        map.put(".tar.bz2", in -> new TarArchiveInputStream(new BZip2CompressorInputStream(in)));
        map.put(".tar.xz", in -> new TarArchiveInputStream(new XZCompressorInputStream(in)));

        return Collections.unmodifiableMap(map);
    }

    private JtregBackwardCompatibileSuite parseTestSuite(XMLStreamReader in) throws XMLStreamException, Exception {

        final String name = findAttributeValue(in, "name");
        final String failuresStr = findAttributeValue(in, "failures");
        final String errorsStr = findAttributeValue(in, "errors");
        final String totalStr = findAttributeValue(in, "tests");

        final int failures = tryParseString(failuresStr);
        final int errors = tryParseString(errorsStr);
        final int total = tryParseString(totalStr);

        JtregBackwardCompatibileSuite suite = new JtregBackwardCompatibileSuite(name, failures, errors, total);

        String statusLine = "";
        String stdOutput = "";
        String errOutput = "";

        while (in.hasNext()) {
            int event = in.next();
            if (event == START_ELEMENT && TESTCASE.equals(in.getLocalName())) {
                JtregBackwardCompatibileTest test = parseTestcase(in);
                suite.add(test);
                continue;
            }

            if (event == START_ELEMENT && PROPERTIES.equals(in.getLocalName())) {
                statusLine = findStatusLine(in);
                continue;
            }
            if (event == START_ELEMENT && SYSTEMOUT.equals(in.getLocalName())) {
                stdOutput = captureCharacters(in, SYSTEMOUT);
                continue;
            }
            if (event == START_ELEMENT && SYSTEMERR.equals(in.getLocalName())) {
                errOutput = captureCharacters(in, SYSTEMERR);
                continue;
            }

            if (event == END_ELEMENT && TESTSUITE.equals(in.getLocalName())) {
                break;
            }
        }

        TestStatus status;
        if (errors > 0) {
            status = TestStatus.ERROR;
        } else if (failures > 0) {
            status = TestStatus.FAILED;
        } else {
            status = TestStatus.PASSED;
        }

        //order imortant! see revalidateTests
        List<TestOutput> outputs = Arrays.asList(
                new TestOutput(SYSTEMOUT, stdOutput),
                new TestOutput(SYSTEMERR, errOutput)
        );

        suite.setStatusLine(statusLine);
        suite.setOutputs(outputs);
        suite.setStatus(status);
        return suite;

    }

    private JtregBackwardCompatibileTest parseTestcase(XMLStreamReader in) throws Exception {

        final String testName = findAttributeValue(in, "name");
        final String className = findAttributeValue(in, "classname");

        boolean failureFound = false;

        String failureOutput = "";
        String stdOutput = "";
        String errOutput = "";
        String message = "";
        String type = "";

        while (in.hasNext()) {
            int event = in.next();
            if (event == START_ELEMENT && FAILURE.equals(in.getLocalName())) {
                final String lEmessage = findAttributeValue(in, "message");
                final String lEtype = findAttributeValue(in, "type");
                if (lEmessage != null) {
                    message = lEmessage;
                }
                if (lEtype != null) {
                    type = lEtype;
                }
                failureOutput = captureCharacters(in, FAILURE);
                failureFound = true;
                continue;
            }
            if (event == START_ELEMENT && SYSTEMOUT.equals(in.getLocalName())) {
                stdOutput = captureCharacters(in, SYSTEMOUT);
                continue;
            }
            if (event == START_ELEMENT && SYSTEMERR.equals(in.getLocalName())) {
                errOutput = captureCharacters(in, SYSTEMERR);
                continue;
            }

            if (event == END_ELEMENT && TESTCASE.equals(in.getLocalName())) {
                break;
            }
        }

        //order imortant! see revalidateTests
        List<TestOutput> outputs = Arrays.asList(
                new TestOutput(SYSTEMOUT, stdOutput),
                new TestOutput(SYSTEMERR, errOutput)
        );

        return new JtregBackwardCompatibileTest(className, null, message, outputs, testName, failureOutput, type, failureFound);
    }

    @FunctionalInterface
    private static interface ArchiveFactory {

        ArchiveInputStream create(InputStream in) throws IOException;
    }

    private static class JtregBackwardCompatibileTest extends Test {

        private final String testName;
        private final String failureOutput;
        private final String failureType;
        private final boolean failed;

        public JtregBackwardCompatibileTest(String className, TestStatus status, String statusLine, List<TestOutput> outputs, String testName, String failureOutput, String failureType, boolean failed) {
            super(className, status, statusLine, outputs);
            this.testName = testName;
            this.failureOutput = failureOutput;
            this.failureType = failureType;
            this.failed = failed;
        }

    }

    private static class JtregBackwardCompatibileSuite {

        private final String name;
        private final int failures;
        private final int errors;
        private final int total;
        private List<TestOutput> outputs;
        private final List<JtregBackwardCompatibileTest> settedTests = new ArrayList<>();
        private List<Test> revalidatedCopyOfTests;
        private TestStatus status;
        private String statusLine;
        private boolean validated = false;

        /**
         * This method is handling backward compatibility. Is setting error
         * based on suite info, and is setting backward compatible name if
         * applicable.
         *
         * eg: sout/err should be testsuite's only, but we have to distribute
         * them to individual tests eg: error seems to be indetectbale from test
         * itself, so it needs to be set from suite too
         */
        private void revalidateTests() {
            revalidatedCopyOfTests = new ArrayList<>(settedTests.size());
            //control summs
            int cPass = 0;
            int cFail = 0;
            int cErr = 0;
            int cSkipp = 0;
            int cTotoal = 0;
            for (JtregBackwardCompatibileTest testcase : settedTests) {
                Test t = null;
                //99% of legacy suites is mapped 1 testsuite == 1 testcase
                //if there is only one test in suite, consider it like this
                if (settedTests.size() == 1) {
                    //TODO - remove this backward compaatibility after few runs
                    //this is causing bad baehavior, when :
                    //  we have testite with one test, whichch is because of this swithc shown in legacy mode
                    //  hhowever, seconf test is added. That measn the instead of test added, we eill see test rmeoved and two "new" tests added
                    //  and vice versa
                    //TODO maybe add date > 2017 to above == 1 condition? :P
                    t = mergeOutputs(name, testcase, status);

                } else {
                    TestStatus st;
                    //we are loosing error status, as 
                    //type att. seems to be unused.
                    //also not run  is lot, but that was probably never used
                    //the ony correct error is when whole testsuite really error
                    if (testcase.failed) {
                        st = TestStatus.FAILED;
                    } else {
                        st = TestStatus.PASSED;
                    }
                    if (testcase.failureType.toUpperCase().startsWith("ERROR") || errors == total) {
                        st = TestStatus.ERROR;
                    }
                    t = mergeOutputs(name + "#" + testcase.testName, testcase, st);

                }
                switch (t.getStatus()) {
                    case ERROR:
                        cErr++;
                        break;
                    case FAILED:
                        cFail++;
                        break;
                    case NOT_RUN:
                        cSkipp++;
                        break;
                    case PASSED:
                        cPass++;
                        break;
                }
                cTotoal++;
                revalidatedCopyOfTests.add(t);
            }
            //check control summs ony for more then one
            int i = 0;
            System.out.println("Control summs '" + name + "'(skipped " + cSkipp + "):");
            if (cTotoal != total) {
                System.out.println("Total tests expected " + total + " got " + cTotoal);
                i++;
            }
            if (cFail != failures) {
                System.out.println("Failed tests expected " + failures + " got " + cFail);
                i++;
            }
            if (cErr != errors) {
                System.out.println("Error tests expected " + errors + " got " + cErr);
                i++;
            }
            if (cErr + cFail + cPass != cTotoal) {
                System.out.println("Total(2) tests expected " + cTotoal + " got " + (cErr + cFail + cPass));
                i++;
            }
            if (i == 0) {
                System.out.println("Pass");
            }
            validated = true;
        }

        private JtregBackwardCompatibileSuite(String name, int failures, int errors, int totals) {
            this.name = name;
            this.failures = failures;
            this.errors = errors;
            this.total = totals;
        }

        private Collection<? extends Test> getTests() {
            if (!validated) {
                revalidateTests();
            }
            return revalidatedCopyOfTests;
        }

        private void setStatusLine(String statusLine) {
            this.statusLine = statusLine;
        }

        private void setOutputs(List<TestOutput> outputs) {
            this.outputs = outputs;
        }

        private void setStatus(TestStatus status) {
            this.status = status;
        }

        private void add(JtregBackwardCompatibileTest test) {
            validated = false;
            settedTests.add(test);
        }

        private Test mergeOutputs(String nwName, JtregBackwardCompatibileTest testcase, TestStatus st) {
            List<TestOutput> newOutputs = Arrays.asList(
                    new TestOutput(SYSTEMOUT, "---- suite ----\n" + outputs.get(0).getValue() + "\n---- test ----\n" + testcase.getOutputs().get(0).getValue()),
                    new TestOutput(SYSTEMERR, "---- suite ----\n" + outputs.get(1).getValue() + "\n---- test ----\n" + testcase.getOutputs().get(1).getValue() + "\n---- fail ----\n" + testcase.failureOutput)
            );
            //merge status line of suite, and testcase (failure is already in stderr)
            String nwStatus;
            if (statusLine.equals(testcase.getStatusLine())) {
                nwStatus = statusLine;
            } else {
                String del = "";
                if (statusLine.trim().length() > 0 && testcase.getStatusLine().trim().length() > 0) {
                    del = " ; ";
                }
                nwStatus = statusLine + del + testcase.getStatusLine();
            }
            return new Test(nwName, st, nwStatus, newOutputs);

        }

    }

}
