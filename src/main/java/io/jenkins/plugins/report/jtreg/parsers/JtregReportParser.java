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
package io.jenkins.plugins.report.jtreg.parsers;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.plugins.report.jtreg.model.ReportFull;
import io.jenkins.plugins.report.jtreg.model.Suite;
import io.jenkins.plugins.report.jtreg.model.Test;
import io.jenkins.plugins.report.jtreg.model.TestOutput;
import io.jenkins.plugins.report.jtreg.model.TestStatus;
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

import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;

public class JtregReportParser implements ReportParser {

    private static final Map<String, ArchiveFactory> SUPPORTED_ARCHIVE_TYPES_MAP = createSupportedArchiveTypesMap();

    @Override
    @SuppressFBWarnings(value = {"REC_CATCH_EXCEPTION", "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"}, justification = " npe of spotbugs sucks")
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
                (int) testsList.stream().sequential().filter(t -> t.getStatus() == TestStatus.NOT_RUN).count(),
                (int) testsList.stream().sequential().filter(t -> t.getStatus() == TestStatus.FAILED).count(),
                (int) testsList.stream().sequential().filter(t -> t.getStatus() == TestStatus.ERROR).count(),
                testsList.size(), testProblems, testNames);
        return new Suite(suiteName(path), fullReport);
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "We desrve to die")
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
    private static final String SKIPPED = "skipped";
    private static final String ERROR = "error";

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
            if (event == CDATA || event == CHARACTERS) {
                StringBuilder outputString = new StringBuilder();
                do {
                    outputString.append(in.getText());
                    event = in.next();
                } while (event == CDATA || event == CHARACTERS);
                return outputString.toString().trim();
            }
        }
        return "";
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "We desrve to die")
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
        final String totalSkip = findAttributeValue(in, "skipped");

        final int failures = tryParseString(failuresStr);
        final int errors = tryParseString(errorsStr);
        final int total = tryParseString(totalStr);
        final int skipped = tryParseString(totalSkip);

        JtregBackwardCompatibileSuite suite = new JtregBackwardCompatibileSuite(name, failures, errors, total, skipped);

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

        //order imortant! see revalidateTests
        List<TestOutput> outputs = Arrays.asList(
                new TestOutput(SYSTEMOUT, stdOutput),
                new TestOutput(SYSTEMERR, errOutput)
        );

        suite.setStatusLine(statusLine);
        suite.setOutputs(outputs);
        return suite;

    }

    /**
     * Test status of testcase is determined from its child tags( <skipped>, <failure>, <error> ).
     * If it doesn't contain any of these tags, test is considered passed. If it contains both failure and error tags,
     * test status is set to error.
     */

    private JtregBackwardCompatibileTest parseTestcase(XMLStreamReader in) throws Exception {

        final String testName = findAttributeValue(in, "name");
        final String className = findAttributeValue(in, "classname");

        TestStatus status = TestStatus.PASSED;

        String failureOutput = "";
        String stdOutput = "";
        String sysErrOutput = "";
        String message = "";
        StringBuilder errOutput = new StringBuilder();

        while (in.hasNext()) {
            int event = in.next();
            if (event == START_ELEMENT && FAILURE.equals(in.getLocalName())) {
                final String lEmessage = findAttributeValue(in, "message");
                if (lEmessage != null) {
                    message = lEmessage;
                }
                failureOutput = captureCharacters(in, FAILURE);
                if (status != TestStatus.ERROR) {
                    status = TestStatus.FAILED;
                }
                continue;
            }
            if (event == START_ELEMENT && SYSTEMOUT.equals(in.getLocalName())) {
                stdOutput = captureCharacters(in, SYSTEMOUT);
                continue;
            }
            if (event == START_ELEMENT && SYSTEMERR.equals(in.getLocalName())) {
                sysErrOutput = captureCharacters(in, SYSTEMERR);
                continue;
            }
            if (event == START_ELEMENT && SKIPPED.equals(in.getLocalName())) {
                status = TestStatus.NOT_RUN;
                continue;
            }
            if (event == START_ELEMENT && ERROR.equals(in.getLocalName())) {
                status = TestStatus.ERROR;
                errOutput.append(captureCharacters(in, ERROR)).append('\n');
                continue;
            }
            if (event == END_ELEMENT && TESTCASE.equals(in.getLocalName())) {
                break;
            }
        }

        //order imortant! see revalidateTests
        List<TestOutput> outputs = Arrays.asList(
                new TestOutput(SYSTEMOUT, stdOutput),
                new TestOutput(SYSTEMERR, sysErrOutput)
        );

        return new JtregBackwardCompatibileTest(className, status, message, outputs, testName, failureOutput, errOutput.toString());
    }

    @FunctionalInterface
    private static interface ArchiveFactory {

        ArchiveInputStream create(InputStream in) throws IOException;
    }

    @SuppressFBWarnings(value = "EQ_DOESNT_OVERRIDE_EQUALS", justification = "The Test ishandlig classes properly,but yes, this may get broken any time")
    private static class JtregBackwardCompatibileTest extends Test {

        private final String testName;
        private final String failureOutput;
        private final String errorOutput;

        public JtregBackwardCompatibileTest(String className, TestStatus status, String statusLine, List<TestOutput> outputs, String testName, String failureOutput, String errorOutput) {
            super(className, status, statusLine, outputs);
            this.testName = testName;
            this.failureOutput = failureOutput;
            this.errorOutput = errorOutput;
        }

    }

    private static class JtregBackwardCompatibileSuite {

        private final String name;
        private final int failures;
        private final int errors;
        private final int total;
        private final int skipped;
        private List<TestOutput> outputs;
        private final List<JtregBackwardCompatibileTest> settedTests = new ArrayList<>();
        private List<Test> revalidatedCopyOfTests;
        private String statusLine;
        private boolean validated = false;

        /**
         * This method is handling backward compatibility by converting JtregBackwardCompatibleTest to Test
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
                final Test t = mergeOutputs(name + '#' + testcase.testName, testcase, testcase.getStatus());

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
            if (cSkipp != skipped) {
                System.out.println("Skipped tests expected " + skipped + " got " + cSkipp);
                i++;
            }
            if (cErr + cFail + cPass + cSkipp != cTotoal) {
                System.out.println("Total(2) tests expected " + cTotoal + " got " + (cErr + cFail + cPass + cSkipp));
                i++;
            }
            if (i == 0) {
                System.out.println("Pass");
            }
            validated = true;
        }

        private JtregBackwardCompatibileSuite(String name, int failures, int errors, int totals, int skipped) {
            this.name = name;
            this.failures = failures;
            this.errors = errors;
            this.total = totals;
            this.skipped = skipped;
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

        private void add(JtregBackwardCompatibileTest test) {
            validated = false;
            settedTests.add(test);
        }

        private Test mergeOutputs(String nwName, JtregBackwardCompatibileTest testcase, TestStatus st) {
            List<TestOutput> newOutputs = Arrays.asList(
                    new TestOutput(SYSTEMOUT, "---- suite ----\n" + outputs.get(0).getValue() + "\n---- test ----\n" + testcase.getOutputs().get(0).getValue()),
                    new TestOutput(SYSTEMERR, "---- suite ----\n" + outputs.get(1).getValue() + "\n---- test ----\n" + testcase.getOutputs().get(1).getValue() + "\n---- fail ----\n" + testcase.failureOutput + "\n---- error ----\n" + testcase.errorOutput)
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