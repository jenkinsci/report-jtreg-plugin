package io.jenkins.plugins.report.jtreg;

import io.jenkins.plugins.report.jtreg.model.Test;
import io.jenkins.plugins.report.jtreg.model.TestOutput;
import org.junit.Assert;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;


import io.jenkins.plugins.report.jtreg.model.Suite;
import io.jenkins.plugins.report.jtreg.parsers.JtregReportParser;

public class JtregReportParserTest {

    private final static String rhqeFileName = "rhqe.tar.gz";

    private void copyStream(InputStream is, OutputStream os) throws IOException {
        int b;
        while ((b = is.read()) >= 0) {
            os.write(b);
        }
        is.close();
    }

    private void checkReport(Suite actualReport) throws IOException {
        List<Test> failures = actualReport.getReport().getTestProblems();
        Assert.assertEquals(2, failures.size());
        List<TestOutput> outputs1 = failures.get(0).getOutputs();
        Assert.assertEquals(2, outputs1.size());
        Assert.assertTrue(outputs1.get(1).getValue().contains("grep"));
        Assert.assertTrue(outputs1.get(1).getValue().contains("Pack"));
        List<TestOutput> outputs2 = failures.get(1).getOutputs();
        Assert.assertEquals(2, outputs2.size());
        Assert.assertTrue(outputs2.get(1).getValue().contains("head"));
        Assert.assertTrue(outputs2.get(1).getValue().contains("Attempt"));

        File tmpJMainDir = File.createTempFile("jtregJenkinsJckPlugin","test.json");
        tmpJMainDir.delete();
        tmpJMainDir.mkdir();
        tmpJMainDir.deleteOnExit();
        File tmpDir2 = new File(tmpJMainDir, "jobs/builds/5");
        tmpDir2.mkdirs();
        File tmpJson = new File(tmpDir2,"test.json");
        JtregReportPublisher jp = new JtregReportPublisher("notImportantNow");
        jp.storeFailuresSummary(Arrays.asList(actualReport), tmpJson);
        String s = Files.readAllLines(tmpJson.toPath()).stream().collect(Collectors.joining("\n"));
        Assert.assertTrue(s.contains("grep"));
        Assert.assertTrue(s.contains("Pack"));
        Assert.assertTrue(s.contains("head"));
        Assert.assertTrue(s.contains("Attempt"));
    }

    @org.junit.Test
    public void parseRhqeTestGz() throws IOException {
        String tarball = "src/test/resources/" + rhqeFileName; //jtreg arser currently depends on file, and that file have to be tarball with named xmls
        final JtregReportParser parser = new JtregReportParser();
        Suite actualReport = parser.parsePath(new File(tarball).toPath());
        checkReport(actualReport);
    }

    @org.junit.Test
    public void parseRhqeTestXz() throws IOException {
        // repack as .tar.xz and test
        Path path = Files.createTempFile("rhqe", ".tar.xz");
        try (InputStream is = this.getClass().getResourceAsStream("/" + rhqeFileName);
            GZIPInputStream gis = new GZIPInputStream(is);
            FileOutputStream fos = new FileOutputStream(path.toFile());
            OutputStream xzos = new XZOutputStream(fos, new LZMA2Options())) {
            copyStream(gis, xzos);
            xzos.close();
            final JtregReportParser parser = new JtregReportParser();
            Suite actualReport = parser.parsePath(path);
            Assert.assertNotNull("Suite in not null", actualReport);
            checkReport(actualReport);
        } finally {
            Files.delete(path);
        }
    }

}
