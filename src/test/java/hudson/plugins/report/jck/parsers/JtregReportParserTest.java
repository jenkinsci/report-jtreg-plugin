package hudson.plugins.report.jck.parsers;

import org.junit.Assert;

import java.io.File;
import java.util.List;


import hudson.plugins.report.jck.model.Suite;
import hudson.plugins.report.jck.model.Test;
import hudson.plugins.report.jck.model.TestOutput;

public class JtregReportParserTest {

    private final static String rhqeFileName = "rhqe.tar.gz";

    @org.junit.Test
    public void parseRhqeTest() {
        String tarball = "src/test/resources/" + rhqeFileName; //jtreg arser currently depends on file, and that file have to be tarball with named xmls
        final JtregReportParser parser = new JtregReportParser();
        Suite actualReport = parser.parsePath(new File(tarball).toPath());
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
    }

}
