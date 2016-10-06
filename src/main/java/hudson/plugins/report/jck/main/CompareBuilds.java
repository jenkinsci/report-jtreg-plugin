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

import hudson.plugins.report.jck.main.cmdline.Arguments;
import hudson.plugins.report.jck.BuildReportExtended;
import hudson.plugins.report.jck.BuildSummaryParser;
import hudson.plugins.report.jck.JckReportPublisher;
import hudson.plugins.report.jck.main.cmdline.Options;
import hudson.plugins.report.jck.main.formatters.Formatter;
import hudson.plugins.report.jck.model.BuildReport;
import hudson.plugins.report.jck.model.Report;
import hudson.plugins.report.jck.model.Suite;
import hudson.plugins.report.jck.model.SuiteTestChanges;
import hudson.plugins.report.jck.model.Test;
import hudson.plugins.report.jck.model.TestOutput;
import hudson.plugins.report.jck.wrappers.RunWrapperFromDir;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class CompareBuilds {

    public static void main(String[] args) throws Exception {
        Options options = new Arguments(args).parse();
        options.setStream(System.out);
        new CompareBuilds(options).work();
    }
    private final Options options;

    private CompareBuilds(Options options) {
        this.options = options;
    }

    private void work() throws IOException, Exception {
        try {
            format().initDoc();
            workImpl();
        } finally {
            format().closeDoc();
        }
    }

    private void workImpl() throws IOException, Exception {
        for (int i = 0; i < options.getDirsToWork().size(); i++) {
            File newOne = options.getDirsToWork().get(i);
            File oldOne = null;
            if (i < options.getDirsToWork().size() - 1) {
                oldOne = options.getDirsToWork().get(i + 1);
            }
            JckReportPublisher jcp = new JckReportPublisher("report-{runtime,devtools,compiler}.xml.gz");
            BuildSummaryParser bs = new BuildSummaryParser(Arrays.asList("jck", "jtreg"), jcp);

            BuildReport br = bs.parseJobReports(newOne);
            if (options.isInfo()) {
                printName(br, null);
            }
            if (options.viewInfoSummary()) {
                printReport(br, null);
            }
            if (options.viewInfoSummarySuites()) {
                printSuites(br.getSuites(), null);
            }
            if (options.viewInfoProblems()) {
                printProblems(br.getSuites());
            }
            if (oldOne != null) {
                BuildReport br1 = bs.parseJobReports(oldOne);
                if (options.isInfo()) {
                    printName(br1, null);
                }
                if (options.viewInfoSummary()) {
                    printReport(br1, null);
                }
                if (options.viewInfoSummarySuites()) {
                    printSuites(br1.getSuites(), null);
                }
                if (options.viewInfoProblems()) {
                    printProblems(br1.getSuites());
                }

                BuildReportExtended bex = null;
                if (options.isDiff()) {
                    format().startTitle3();
                    format().println("----------- diff summary -----------");
                    format().reset();
                    bex = bs.parseBuildReportExtended(new RunWrapperFromDir(newOne), new RunWrapperFromDir(oldOne));
                    printName(bex, br1);
                }
                if (options.viewDiffSummary()) {
                    printReport(bex, br1);
                }
                if (options.viewDiffSummarySuites()) {
                    printSuites(bex.getSuites(), br1.getSuites());
                }
                if (options.viewDiffDetails() || options.viewDiffList()) {
                    format().startTitle3();
                    format().println("----------- comaprsion -----------");
                    format().reset();
                    format().println("    Removed suites: " + bex.getRemovedSuites().size());
                    printStringList("        ", bex.getRemovedSuites());
                    format().println("      Added suites: " + bex.getRemovedSuites().size());
                    printStringList("        ", bex.getAddedSuites());
                }
                if (options.viewDiffDetails()) {
                    printTestChangesSummary(bex.getTestChanges());
                }
                if (options.viewDiffList()) {
                    format().startTitle3();
                    format().println("----------- comaprsion details -----------");
                    format().reset();
                    printTestChangesDetails(bex.getTestChanges());
                }

            }
        }
    }

    private void printStringList(String prefix, List<String> ss) {
        for (String s : ss) {
            format().println(prefix + s);
        }
    }

    private Suite getSuiteByName(Suite s, List<Suite> suites) {
        if (suites == null) {
            return null;
        }
        for (Suite suite : suites) {
            if (suite.compareTo(s) == 0) {
                return suite;
            }
        }
        return null;

    }

    private void printSuites(List<Suite> suites, List<Suite> suitesOld) {
        for (Suite s : suites) {
            Suite oldSuite = getSuiteByName(s, suitesOld);
            Report old = null;
            if (oldSuite != null) {
                old = oldSuite.getReport();
            }
            Report br = s.getReport();
            format().print("    " + s.getName());
            if (old != null) {
                format().print(" x(old) " + oldSuite.getName());
            }
            format().println();
            if (!options.hidePositives()) {
                format().print("    Passed  : " + br.getTestsPassed());
                if (old != null) {
                    format().print(" x(old) " + old.getTestsPassed() + " = " + intDiffToString(br.getTestsPassed(), old.getTestsPassed())
                    );
                }
                format().println();
            }
            if (!options.hideNegatives()) {
                format().print("    Failed  : " + br.getTestsFailed());
                if (old != null) {
                    format().print(" x(old) " + old.getTestsFailed() + " = " + intDiffToString(br.getTestsFailed(), old.getTestsFailed()));
                }
                format().println();
                format().print("    Error   : " + br.getTestsError());
                if (old != null) {
                    format().print(" x(old) " + old.getTestsError() + " = " + intDiffToString(br.getTestsError(), old.getTestsError()));
                }
                format().println();
            }
            if (!options.hideTotals()) {
                format().print("    Total   : " + br.getTestsTotal());
                if (old != null) {
                    format().print(" x(old) " + old.getTestsTotal() + " = " + intDiffToString(br.getTestsTotal(), old.getTestsTotal()));
                }
                format().println();
            }
            if (!options.hideMisses()) {
                format().print("    Ignored : " + br.getTestsNotRun());
                if (old != null) {
                    format().print(" x(old) " + old.getTestsNotRun() + " = " + intDiffToString(br.getTestsNotRun(), old.getTestsNotRun()));
                }
                format().println();
            }
            if (!options.hideNegatives()) {
                format().print("    Problem : " + br.getTestProblems().size());
                if (old != null) {
                    format().print(" x(old) " + old.getTestProblems().size() + " = " + intDiffToString(br.getTestProblems().size(), old.getTestProblems().size()));
                }
                format().println();
            }
        }
    }

    private void printName(BuildReport br, BuildReport old) {
        format().startTitle2();
        format().print(br.getBuildNumber() + ": " + br.getBuildName());
        if (old != null) {
            format().print(" x(old) " + old.getBuildNumber() + ": " + old.getBuildName());
        }
        format().reset();
        format().println();
        String nwNra = getChangelogsNvr(br.getBuildName());
        format().startTitle1();
        format().print(nwNra);
        String nwNraOld = null;
        if (old != null) {
            nwNraOld = getChangelogsNvr(old.getBuildName());
            format().print(" x(old) " + nwNraOld);
        }
        if (nwNra != null || nwNraOld != null) {
            format().println();
        }
        format().reset();
    }

    private void printReport(BuildReport br, BuildReport old) {
        if (!options.hidePositives()) {
            format().print("Passed  : " + br.getPassed());
            if (old != null) {
                format().print(" x(old) " + old.getPassed() + " = " + intDiffToString(br.getPassed(), old.getPassed()));
            }
            format().println();
        }
        if (!options.hideNegatives()) {
            format().print("Failed  : " + br.getFailed());
            if (old != null) {
                format().print(" x(old) " + old.getFailed() + " = " + intDiffToString(br.getFailed(), old.getFailed())
                );
            }
            format().println();
            format().print("Error   : " + br.getError());
            if (old != null) {
                format().print(" x(old) " + old.getError() + " = " + intDiffToString(br.getError(), old.getError()));
            }
            format().println();
        }
        if (!options.hideTotals()) {
            format().print("Total   : " + br.getTotal());
            if (old != null) {
                format().print(" x(old) " + old.getTotal() + " = " + intDiffToString(br.getTotal(), old.getTotal()));
            }
            format().println();
        }
        if (!options.hideMisses()) {
            format().print("Ignored : " + br.getNotRun());
            if (old != null) {
                format().print(" x(old) " + old.getNotRun() + " = " + intDiffToString(br.getNotRun(), old.getNotRun()));
            }
            format().println();
        }
        format().print("Suites  : " + br.getSuites().size());
        if (old != null) {
            format().print(" x(old) " + old.getSuites().size() + " = " + intDiffToString(br.getSuites().size(), old.getSuites().size()));
        }
        format().println();
    }

    private void printProblems(List<Suite> suites) {
        for (Suite s : suites) {
            format().startTitle3();
            format().println("    *** " + s.getName() + " *** ");
            format().reset();
            for (Test t : s.getReport().getTestProblems()) {
                format().println("       Name : " + t.getName());
                if (!options.hideValues()) {
                    format().println("       Line : " + t.getStatusLine());
                    for (TestOutput o : t.getOutputs()) {
                        format().println("         Name  :\n" + o.getName());
                        format().println("         Value :\n" + o.getValue());
                    }
                }

            }
        }
    }

    private void printTestChangesSummary(List<SuiteTestChanges> testChanges) {
        for (SuiteTestChanges st : testChanges) {
            format().startTitle3();
            format().println("       *** " + st.getName() + " *** ");
            format().reset();
            if (!options.hideMisses()) {
                format().println("        removed : " + st.getRemoved().size());
                format().println("        added   : " + st.getAdded().size());
            }
            if (!options.hidePositives()) {
                format().println("        fixes   : " + st.getFixes().size());
            }
            if (!options.hideNegatives()) {
                format().println("        errors  : " + st.getErrors().size());
                format().println("        failures: " + st.getFailures().size());
            }

        }
    }

    private void printTestChangesDetails(List<SuiteTestChanges> testChanges) {
        for (SuiteTestChanges st : testChanges) {
            format().println("       *** " + st.getName() + " *** ");
            if (!options.hideMisses()) {
                format().println("        removed : ");
                printStringList("            ", st.getRemoved());
                format().println("        added   : ");
                printStringList("            ", st.getAdded());
            }
            if (!options.hidePositives()) {
                format().println("        fixes   : ");
                printStringList("            ", st.getFixes());
            }
            if (!options.hideNegatives()) {
                format().println("        errors  : ");
                printStringList("            ", st.getErrors());
                format().println("        failures: ");
                printStringList("            ", st.getFailures());
            }

        }
    }

    private String intDiffToString(int iN, int iO) {
        int i = iN - iO;
        if (i <= 0) {
            return "" + i;
        }
        return "+" + i;

    }

    private String getChangelogsNvr(String buildName) {
        File f = new File(buildName, "changelog.xml");
        try {
            String content = new Scanner(f).useDelimiter("\\Z").next();
            String[] lines = content.split("[<>]");
            boolean read1 = true;
            boolean read2 = false;
            for (String line : lines) {
                line = line.replaceAll("\\s+", "");
                if (line.isEmpty()) {
                    continue;
                }
                if (read1 && read2) {
                    return line;
                }
                if (line.equals("rpms")) {
                    read1 = false;
                }
                if (line.equals("/rpms")) {
                    read1 = true;
                }
                if (line.equals("nvr")) {
                    read2 = true;
                }
                if (line.equals("/nvr")) {
                    read2 = false;
                }
            }
        } catch (Exception ex) {
            return null;
        }
        return null;
    }

    //shortcut
    private Formatter format() {
        return options.getFormatter();
    }
}
