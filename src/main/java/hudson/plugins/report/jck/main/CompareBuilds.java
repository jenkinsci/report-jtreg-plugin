/*
 * The MIT License
 *
 * Copyright 2016 jvanek.
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
package hudson.plugins.report.jck.main;

import hudson.plugins.report.jck.BuildReportExtended;
import hudson.plugins.report.jck.BuildSummaryParser;
import hudson.plugins.report.jck.JckReportPublisher;
import hudson.plugins.report.jck.model.BuildReport;
import hudson.plugins.report.jck.model.Suite;
import hudson.plugins.report.jck.model.SuiteTestChanges;
import hudson.plugins.report.jck.model.Test;
import hudson.plugins.report.jck.model.TestOutput;
import hudson.plugins.report.jck.wrappers.RunWrapperFromDir;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CompareBuilds {

    public static void main(String[] args) throws Exception {
//        args = new String[]{
//            //            "/home/tester/jenkins/jenkins_home/jobs/tck-ojdk7-rhel6-x64/builds/27",
//            //            "/home/tester/jenkins/jenkins_home/jobs/tck-ojdk7-rhel6-x64/builds/28"};
//            "/home/tester/jenkins/jenkins_home/jobs/jtreg-ojdk8-rhel6-i586/builds/27",
//            "/home/tester/jenkins/jenkins_home/jobs/jtreg-ojdk8-rhel6-i586/builds/28"};
        new CompareBuilds().work(args[0], args[1]);
    }

    private void work(String oldOne, String newOne) throws IOException, Exception {
        JckReportPublisher jcp = new JckReportPublisher("report-{runtime,devtools,compiler}.xml.gz");
        BuildSummaryParser bs = new BuildSummaryParser(Arrays.asList("jck", "jtreg"), jcp);

        List<BuildReport> brs = bs.parseJobReports(new File(oldOne), new File(newOne));
        for (BuildReport br : brs) {
            printReport(br);
            printSuites(br.getSuites());
            printProblems(br.getSuites());

        }
        System.out.println("----------- highlight -----------");
        BuildReportExtended bex = bs.parseBuildReportExtended(new RunWrapperFromDir(new File(newOne)), new RunWrapperFromDir(new File(oldOne)));
        printReport(bex);
        printSuites(bex.getSuites());
        System.out.println("----------- comaprsion -----------");
        System.out.println("    Removed suites: " + bex.getRemovedSuites().size());
        printStringList("        ", bex.getRemovedSuites());
        System.out.println("      Added suites: " + bex.getRemovedSuites().size());
        printStringList("        ", bex.getAddedSuites());
        printTestChangesSummary(bex.getTestChanges());
        System.out.println("----------- comaprsion details -----------");
        printTestChangesDetails(bex.getTestChanges());

    }

    private void printStringList(String prefix, List<String> ss) {
        for (String s : ss) {
            System.out.println(prefix + s);
        }
    }

    private void printSuites(List<Suite> suites) {
        for (Suite s : suites) {
            System.out.println("    " + s.getName());
            System.out.println("    Passed  : " + s.getReport().getTestsPassed());
            System.out.println("    Failed  : " + s.getReport().getTestsFailed());
            System.out.println("    Error   : " + s.getReport().getTestsError());
            System.out.println("    Total   : " + s.getReport().getTestsTotal());
            System.out.println("    Ignored : " + s.getReport().getTestsNotRun());
            System.out.println("    Problem : " + s.getReport().getTestProblems().size());
        }
    }

    private void printReport(BuildReport br) {
        System.out.println(br.getBuildNumber() + ": " + br.getBuildName());
        System.out.println("Passed  : " + br.getPassed());
        System.out.println("Failed  : " + br.getFailed());
        System.out.println("Error   : " + br.getError());
        System.out.println("Total   : " + br.getTotal());
        System.out.println("Ignored : " + br.getNotRun());
        System.out.println("Suites  : " + br.getSuites().size());
    }

    private void printProblems(List<Suite> suites) {
        for (Suite s : suites) {
            System.out.println("    *** " + s.getName() + " *** ");
            for (Test t : s.getReport().getTestProblems()) {
                System.out.println("       Name : " + t.getName());
                System.out.println("       Line : " + t.getStatusLine());
                for (TestOutput o : t.getOutputs()) {
                    System.out.println("         Name  :\n" + o.getName());
                    System.out.println("         Value :\n" + o.getValue());
                }

            }
        }
    }

    private void printTestChangesSummary(List<SuiteTestChanges> testChanges) {
        for (SuiteTestChanges st : testChanges) {
            System.out.println("       *** " + st.getName() + " *** ");
            System.out.println("        removed : " + st.getRemoved().size());
            System.out.println("        added   : " + st.getAdded().size());
            System.out.println("        fixes   : " + st.getFixes().size());
            System.out.println("        errors  : " + st.getErrors().size());
            System.out.println("        failures: " + st.getFailures().size());

        }
    }

    private void printTestChangesDetails(List<SuiteTestChanges> testChanges) {
        for (SuiteTestChanges st : testChanges) {
            System.out.println("       *** " + st.getName() + " *** ");
            System.out.println("        removed : ");
            printStringList("            ", st.getRemoved());
            System.out.println("        added   : ");
            printStringList("            ", st.getAdded());
            System.out.println("        fixes   : ");
            printStringList("            ", st.getFixes());
            System.out.println("        errors  : ");
            printStringList("            ", st.getErrors());
            System.out.println("        failures: ");
            printStringList("            ", st.getFailures());

        }
    }
}
