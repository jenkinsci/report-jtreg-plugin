/*
 * The MIT License
 *
 * Copyright 2016 user.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
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
                    testsList.addAll(parseTest(reader));
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

    private List<Test> parseTest(XMLStreamReader in) throws Exception {
        List<Test> r = new ArrayList<>();
        while (in.hasNext()) {
            int event = in.next();
            if (event == START_ELEMENT && TESTSUITE.equals(in.getLocalName())) {

                String name = findAttributeValue(in, "name");
                String failuresStr = findAttributeValue(in, "failures");
                String errorsStr = findAttributeValue(in, "errors");

                int failures = tryParseString(failuresStr);
                int errors = tryParseString(errorsStr);

                String statusLine = "";
                String stdOutput = "";
                String errOutput = "";

                while (in.hasNext()) {
                    event = in.next();
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

                List<TestOutput> outputs = Arrays.asList(
                        new TestOutput(SYSTEMOUT, stdOutput),
                        new TestOutput(SYSTEMERR, errOutput)
                );

                Test t = new Test(name,
                        status,
                        statusLine,
                        outputs);
                r.add(t);

            }
        }
        return r;
    }
    private static final String SYSTEMOUT = "system-out";
    private static final String SYSTEMERR = "system-err";
    private static final String TESTSUITE = "testsuite";
    private static final String PROPERTIES = "properties";

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

    @FunctionalInterface
    private static interface ArchiveFactory {

        ArchiveInputStream create(InputStream in) throws IOException;
    }

}
