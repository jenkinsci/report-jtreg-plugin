package io.jenkins.plugins.report.jtreg;

import io.jenkins.plugins.report.jtreg.model.Test;
import io.jenkins.plugins.report.jtreg.model.TestOutput;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.GZIPInputStream;


import io.jenkins.plugins.report.jtreg.model.Suite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jenkins.plugins.report.jtreg.parsers.JtregReportParser;
import io.jenkins.plugins.report.jtreg.writers.WritersManager;

class JtregReportParserTest {

    private static final String RHQE_FILE_NAME = "rhqe.tar.gz";

    private static void copyStream(InputStream is, OutputStream os) throws IOException {
        int b;
        while ((b = is.read()) >= 0) {
            os.write(b);
        }
        is.close();
    }

    private static void checkReport(Suite actualReport) throws IOException {
        List<Test> failures = actualReport.getReport().getTestProblems();
        assertEquals(2, failures.size());
        List<TestOutput> outputs1 = failures.get(0).getOutputs();
        assertEquals(2, outputs1.size());
        assertTrue(outputs1.get(1).getValue().contains("grep"));
        assertTrue(outputs1.get(1).getValue().contains("Pack"));
        List<TestOutput> outputs2 = failures.get(1).getOutputs();
        assertEquals(2, outputs2.size());
        assertTrue(outputs2.get(1).getValue().contains("head"));
        assertTrue(outputs2.get(1).getValue().contains("Attempt"));

        File tmpJMainDir = File.createTempFile("jtregJenkinsJckPlugin","test.json");
        tmpJMainDir.delete();
        tmpJMainDir.mkdir();
        tmpJMainDir.deleteOnExit();
        File tmpDir2 = new File(tmpJMainDir, "jobs/builds/5");
        tmpDir2.mkdirs();
        WritersManager.storeAllSummaries("jtreg", List.of(actualReport), tmpDir2, "testBuild5", null, null);
        String s = String.join("\n", Files.readAllLines(new File(tmpDir2,"jtreg-" + Constants.REPORT_JSON).toPath()));
        assertTrue(s.contains("grep"));
        assertTrue(s.contains("Pack"));
        assertTrue(s.contains("head"));
        assertTrue(s.contains("Attempt"));
    }

    @org.junit.jupiter.api.Test
    void parseRhqeTestGz() throws Exception {
        String tarball = "src/test/resources/" + RHQE_FILE_NAME; //jtreg arser currently depends on file, and that file have to be tarball with named xmls
        final JtregReportParser parser = new JtregReportParser();
        Suite actualReport = parser.parsePath(new File(tarball).toPath());
        checkReport(actualReport);
    }

    @org.junit.jupiter.api.Test
    void parseRhqeTestXz() throws Exception {
        // repack as .tar.xz and test
        Path path = Files.createTempFile("rhqe", ".tar.xz");
        try (InputStream is = this.getClass().getResourceAsStream("/" + RHQE_FILE_NAME);
             GZIPInputStream gis = new GZIPInputStream(is);
             FileOutputStream fos = new FileOutputStream(path.toFile());
             OutputStream xzos = new XZOutputStream(fos, new LZMA2Options())) {
            copyStream(gis, xzos);
            xzos.close();
            final JtregReportParser parser = new JtregReportParser();
            Suite actualReport = parser.parsePath(path);
            assertNotNull(actualReport, "Suite in not null");
            checkReport(actualReport);
        } finally {
            Files.delete(path);
        }
    }

    private static Path extractRhqeXml(String suffix) throws IOException {
        // rhqe.tar.gz contains exactly one entry, rhqe.jtr.xml
        Path dir = Files.createTempDirectory("rhqe-plain");
        Path xml = dir.resolve("rhqe" + suffix);
        try (InputStream is = JtregReportParserTest.class.getResourceAsStream("/" + RHQE_FILE_NAME);
             TarArchiveInputStream tis = new TarArchiveInputStream(new GZIPInputStream(is))) {
            assertNotNull(tis.getNextEntry());
            Files.copy(tis, xml);
        }
        return xml;
    }

    private static void deleteWithParent(Path file) throws IOException {
        Files.deleteIfExists(file);
        Files.deleteIfExists(file.getParent());
    }

    @org.junit.jupiter.api.Test
    void suiteNameOfArchive() throws Exception {
        Suite actualReport = new JtregReportParser().parsePath(new File("src/test/resources/" + RHQE_FILE_NAME).toPath());
        assertEquals("rhqe", actualReport.getName());
    }

    @org.junit.jupiter.api.Test
    void parsePlainJtrXml() throws Exception {
        Path xml = extractRhqeXml(".jtr.xml");
        try {
            Suite actualReport = new JtregReportParser().parsePath(xml);
            assertNotNull(actualReport, "Suite in not null");
            assertEquals("rhqe", actualReport.getName());
            checkReport(actualReport);
        } finally {
            deleteWithParent(xml);
        }
    }

    @org.junit.jupiter.api.Test
    void parsePlainXml() throws Exception {
        Path xml = extractRhqeXml(".XML");
        try {
            Suite actualReport = new JtregReportParser().parsePath(xml);
            assertNotNull(actualReport, "Suite in not null");
            assertEquals("rhqe", actualReport.getName());
            checkReport(actualReport);
        } finally {
            deleteWithParent(xml);
        }
    }

    @org.junit.jupiter.api.Test
    void brokenArchiveIsNotParsedAsXml() throws Exception {
        Path broken = Files.createTempFile("broken", ".tar.xz");
        try {
            Files.write(broken, "<testsuites><testsuite name=\"x\"><testcase classname=\"c\" name=\"t\"/></testsuite></testsuites>"
                    .getBytes(StandardCharsets.UTF_8));
            assertNull(new JtregReportParser().parsePath(broken));
        } finally {
            Files.delete(broken);
        }
    }

    @org.junit.jupiter.api.Test
    void externalEntitiesAreNotResolved() throws Exception {
        Path dir = Files.createTempDirectory("xxe");
        Path secret = dir.resolve("secret.txt");
        Path xml = dir.resolve("xxe.xml");
        try {
            Files.write(secret, "TOP-SECRET".getBytes(StandardCharsets.UTF_8));
            Files.write(xml, ("<?xml version=\"1.0\"?>\n"
                    + "<!DOCTYPE testsuites [<!ENTITY xxe SYSTEM \"" + secret.toUri() + "\">]>\n"
                    + "<testsuites><testsuite name=\"s\" tests=\"1\">"
                    + "<testcase classname=\"c\" name=\"&xxe;\"><failure message=\"m\">&xxe;</failure></testcase>"
                    + "</testsuite></testsuites>").getBytes(StandardCharsets.UTF_8));
            Suite actualReport = new JtregReportParser().parsePath(xml);
            if (actualReport != null) {
                for (Test problem : actualReport.getReport().getTestProblems()) {
                    assertFalse(problem.getName().contains("TOP-SECRET"), problem.getName());
                    for (TestOutput output : problem.getOutputs()) {
                        assertFalse(output.getValue().contains("TOP-SECRET"), output.getValue());
                    }
                }
            }
        } finally {
            Files.deleteIfExists(xml);
            Files.deleteIfExists(secret);
            Files.deleteIfExists(dir);
        }
    }

    private static byte[] rhqeJtrXmlBytes() throws IOException {
        try (InputStream is = JtregReportParserTest.class.getResourceAsStream("/" + RHQE_FILE_NAME);
             TarArchiveInputStream tis = new TarArchiveInputStream(new GZIPInputStream(is))) {
            assertNotNull(tis.getNextEntry());
            return tis.readAllBytes();
        }
    }

    private static void writeTar(OutputStream os, byte[] entry) throws IOException {
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(os)) {
            TarArchiveEntry e = new TarArchiveEntry("rhqe.jtr.xml");
            e.setSize(entry.length);
            tos.putArchiveEntry(e);
            tos.write(entry);
            tos.closeArchiveEntry();
        }
    }

    /**
     * Stores the very same report as <code>dir/fileName</code>, in the format given by its suffix.
     */
    private static Path storeRhqeAs(Path dir, String fileName) throws IOException {
        byte[] xml = rhqeJtrXmlBytes();
        Path target = dir.resolve(fileName);
        String lower = fileName.toLowerCase();
        try (OutputStream os = Files.newOutputStream(target)) {
            if (lower.endsWith(".xml")) {
                os.write(xml);
            } else if (lower.endsWith(".zip")) {
                try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(os)) {
                    zos.putArchiveEntry(new ZipArchiveEntry("rhqe.jtr.xml"));
                    zos.write(xml);
                    zos.closeArchiveEntry();
                }
            } else if (lower.endsWith(".tar.gz")) {
                writeTar(new GzipCompressorOutputStream(os), xml);
            } else if (lower.endsWith(".tar.bz2")) {
                writeTar(new BZip2CompressorOutputStream(os), xml);
            } else if (lower.endsWith(".tar.xz")) {
                writeTar(new XZOutputStream(os, new LZMA2Options()), xml);
            } else if (lower.endsWith(".tar")) {
                writeTar(os, xml);
            } else {
                throw new IllegalArgumentException("Unknown format of " + fileName);
            }
        }
        return target;
    }

    @ParameterizedTest
    @ValueSource(strings = {"rhqe.zip", "rhqe.tar", "rhqe.tar.gz", "rhqe.tar.bz2", "rhqe.tar.xz", "rhqe.xml", "rhqe.jtr.xml",
            "rhqe.ZIP", "rhqe.TAR.XZ", "rhqe.XML", "rhqe.JTR.XML"})
    void sameReportGivesSameSuiteInEveryFormat(String fileName) throws Exception {
        Path dir = Files.createTempDirectory("rhqe-formats");
        Path stored = storeRhqeAs(dir, fileName);
        try {
            Suite actualReport = new JtregReportParser().parsePath(stored);
            assertNotNull(actualReport, "Suite in not null for " + fileName);
            assertEquals("rhqe", actualReport.getName(), "suite name of " + fileName);
            assertEquals(20, actualReport.getReport().getTestsTotal(), "total of " + fileName);
            checkReport(actualReport);
        } finally {
            Files.deleteIfExists(stored);
            Files.deleteIfExists(dir);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "jdk.hotspot.tar.xz, jdk.hotspot",
            "jdk.hotspot.xml, jdk.hotspot",
            "tier1.jdk_core.jtr.xml, tier1.jdk_core",
            "Mixed.Case.Tar.Gz, Mixed.Case"
    })
    void suiteNameFromRealFile(String fileName, String expected) throws Exception {
        Path dir = Files.createTempDirectory("rhqe-names");
        Path stored = storeRhqeAs(dir, fileName);
        try {
            Suite actualReport = new JtregReportParser().parsePath(stored);
            assertNotNull(actualReport, "Suite in not null for " + fileName);
            assertEquals(expected, actualReport.getName());
        } finally {
            Files.deleteIfExists(stored);
            Files.deleteIfExists(dir);
        }
    }

}
