package io.jenkins.plugins.report.jtreg;

import io.jenkins.plugins.report.jtreg.model.Test;
import io.jenkins.plugins.report.jtreg.model.TestOutput;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.GZIPInputStream;


import io.jenkins.plugins.report.jtreg.model.Suite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jenkins.plugins.report.jtreg.parsers.JtregReportParser;

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
        File tmpJson = new File(tmpDir2,"test.json");
        JtregReportPublisher jp = new JtregReportPublisher("notImportantNow");
        jp.storeFailuresSummary(List.of(actualReport), tmpJson);
        String s = String.join("\n", Files.readAllLines(tmpJson.toPath()));
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

}
