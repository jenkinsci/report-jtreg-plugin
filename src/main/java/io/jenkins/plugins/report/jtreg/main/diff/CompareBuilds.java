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
package io.jenkins.plugins.report.jtreg.main.diff;

import io.jenkins.plugins.report.jtreg.main.diff.cmdline.Arguments;
import io.jenkins.plugins.report.jtreg.BuildReportExtended;
import io.jenkins.plugins.report.jtreg.BuildSummaryParser;
import io.jenkins.plugins.report.jtreg.JckReportPublisher;
import io.jenkins.plugins.report.jtreg.main.diff.cmdline.JobsRecognition;
import io.jenkins.plugins.report.jtreg.main.diff.cmdline.Options;
import io.jenkins.plugins.report.jtreg.main.diff.formatters.Formatter;
import io.jenkins.plugins.report.jtreg.model.BuildReport;
import io.jenkins.plugins.report.jtreg.model.Report;
import io.jenkins.plugins.report.jtreg.model.Suite;
import io.jenkins.plugins.report.jtreg.model.SuiteTestChanges;
import io.jenkins.plugins.report.jtreg.model.SuiteTestsWithResults;
import io.jenkins.plugins.report.jtreg.model.Test;
import io.jenkins.plugins.report.jtreg.model.TestOutput;
import io.jenkins.plugins.report.jtreg.wrappers.RunWrapperFromDir;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CompareBuilds {

    public static void main(String[] args) throws Exception {
        Options options = new Arguments(args).parse();
        if (options == null){
            return;
        }
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

            if (options.viewAllTests()) {
                printAllTests(bs.parseBuildReportExtended(new RunWrapperFromDir(newOne), null));
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
                    printStringListUnfiltered("        ", bex.getRemovedSuites());
                    format().println("      Added suites: " + bex.getRemovedSuites().size());
                    printStringListUnfiltered("        ", bex.getAddedSuites());
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

    private void printStringListUnfiltered(String prefix, List<String> ss) {
        for (String s : ss) {
            format().println(prefix + s);
        }
    }

    private void printStringListFiltered(String prefix, List<String> ss) {
        for (String s : ss) {
            if (options.getTrackingRegexChanges().matcher(s).matches()) {
                format().println(prefix + s);
            }
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
            format().startTitle4();
            format().print("    " + s.getName());
            format().reset();
            if (old != null) {
                format().startTitle4();
                format().print(" x(old) " + oldSuite.getName());
                format().reset();
            }
            format().println();
            if (!options.hidePositives()) {
                setFontByDiff(br, old, "getTestsPassed", Formatter.SupportedColors.Green, Formatter.SupportedColors.LightGreen);
                format().print("    Passed  : " + br.getTestsPassed());
                if (old != null) {
                    format().print(" x(old) " + old.getTestsPassed() + " = " + intDiffToString(br.getTestsPassed(), old.getTestsPassed())
                    );
                }
                format().println();
                format().reset();
            }
            if (!options.hideNegatives()) {
                setFontByDiffNeg(br, old, "getTestsFailed", Formatter.SupportedColors.Red, Formatter.SupportedColors.LightRed);
                format().print("    Failed  : " + br.getTestsFailed());
                if (old != null) {
                    format().print(" x(old) " + old.getTestsFailed() + " = " + intDiffToString(br.getTestsFailed(), old.getTestsFailed()));
                }
                format().reset();
                format().println();
                setFontByDiffNeg(br, old, "getTestsError", Formatter.SupportedColors.Red, Formatter.SupportedColors.LightRed);
                format().print("    Error   : " + br.getTestsError());
                if (old != null) {
                    format().print(" x(old) " + old.getTestsError() + " = " + intDiffToString(br.getTestsError(), old.getTestsError()));
                }
                format().reset();
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
                setFontByDiffAbs(br, old, "getTestsNotRun", Formatter.SupportedColors.Yellow, Formatter.SupportedColors.Yellow);
                format().print("    Ignored : " + br.getTestsNotRun());
                if (old != null) {
                    format().print(" x(old) " + old.getTestsNotRun() + " = " + intDiffToString(br.getTestsNotRun(), old.getTestsNotRun()));
                }
                format().reset();
                format().println();
            }
            if (!options.hideNegatives()) {
                setFontByDiffNeg(br, old, "getTestProblems", Formatter.SupportedColors.Red, Formatter.SupportedColors.LightRed);
                format().print("    Problem : " + br.getTestProblems().size());
                if (old != null) {
                    format().print(" x(old) " + old.getTestProblems().size() + " = " + intDiffToString(br.getTestProblems().size(), old.getTestProblems().size()));
                }
                format().reset();
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
        String nwNra = JobsRecognition.getChangelogsNvr(new File(br.getBuildName()));
        format().startTitle1();
        format().print(nwNra);
        String nwNraOld = null;
        if (old != null) {
            nwNraOld = JobsRecognition.getChangelogsNvr(new File(old.getBuildName()));
            format().print(" x(old) " + nwNraOld);
        }
        if (nwNra != null || nwNraOld != null) {
            format().println();
        }
        format().reset();
    }

    private void printReport(BuildReport br, BuildReport old) {
        if (!options.hidePositives()) {
            setFontByDiff(br, old, "getPassed", Formatter.SupportedColors.Green, Formatter.SupportedColors.LightGreen);
            format().print("Passed  : " + br.getPassed());
            if (old != null) {
                format().print(" x(old) " + old.getPassed() + " = " + intDiffToString(br.getPassed(), old.getPassed()));
            }
            format().println();
            format().reset();
        }
        if (!options.hideNegatives()) {
            setFontByDiffNeg(br, old, "getFailed", Formatter.SupportedColors.Red, Formatter.SupportedColors.LightRed);
            format().print("Failed  : " + br.getFailed());
            if (old != null) {
                format().print(" x(old) " + old.getFailed() + " = " + intDiffToString(br.getFailed(), old.getFailed())
                );
            }
            format().reset();
            format().println();
            setFontByDiffNeg(br, old, "getError", Formatter.SupportedColors.Red, Formatter.SupportedColors.LightRed);
            format().print("Error   : " + br.getError());
            if (old != null) {
                format().print(" x(old) " + old.getError() + " = " + intDiffToString(br.getError(), old.getError()));
            }
            format().reset();
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
            setFontByDiffAbs(br, old, "getNotRun", Formatter.SupportedColors.Yellow, Formatter.SupportedColors.Yellow);
            format().print("Ignored : " + br.getNotRun());
            if (old != null) {
                format().print(" x(old) " + old.getNotRun() + " = " + intDiffToString(br.getNotRun(), old.getNotRun()));
            }
            format().reset();
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
                if (options.getTrackingRegex().matcher(t.getName()).matches()) {
                    format().startBold();
                    format().startColor(Formatter.SupportedColors.LightRed);
                    format().println("       Name : " + t.getName());
                    format().reset();
                    format().startColor(Formatter.SupportedColors.Red);
                    if (!options.hideValues()) {
                        format().println("       Line : " + t.getStatusLine());
                        for (TestOutput o : t.getOutputs()) {
                            format().startColor(Formatter.SupportedColors.LightRed);
                            format().println("         Name  :" + o.getName());
                            format().reset();
                            format().startColor(Formatter.SupportedColors.Red);
                            format().print("         Value :\n");
                            format().pre();
                            format().println(o.getValue());
                            format().preClose();
                            format().reset();
                        }
                        format().reset();
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
                setFontByKNownResult(-1 * st.getRemoved().size(), Formatter.SupportedColors.Red);
                format().println("  newly removed : " + intDiffToString(st.getRemoved().size()));
                format().reset();
                setFontByKNownResult(-1 * st.getAdded().size(), Formatter.SupportedColors.Green);
                format().println("  newly added   : " + intDiffToString(st.getAdded().size()));
                format().reset();
            }
            if (!options.hidePositives()) {
                setFontByKNownResult(-1 * st.getFixes().size(), Formatter.SupportedColors.LightGreen);
                format().println("    new fixes   : " + intDiffToString(st.getFixes().size()));
                format().reset();
            }
            if (!options.hideNegatives()) {
                setFontByKNownResult(-1 * (st.getErrors().size()), Formatter.SupportedColors.LightRed);
                format().println("    new errors  : " + intDiffToString(st.getErrors().size()));
                format().reset();
                setFontByKNownResult(-1 * (st.getFailures().size()), Formatter.SupportedColors.LightRed);
                format().println("    new failures: " + intDiffToString(st.getFailures().size()));
                format().reset();
            }

        }
    }

    private void printTestChangesDetails(List<SuiteTestChanges> testChanges) {
        for (SuiteTestChanges st : testChanges) {
            format().println("       *** " + st.getName() + " *** ");
            if (!options.hideMisses()) {
                format().println("  newly removed : ");
                setFontByKNownResult(-1 * st.getRemoved().size(), Formatter.SupportedColors.Red);
                printStringListFiltered("            ", st.getRemoved());
                format().reset();
                format().println("  newly added   : ");
                setFontByKNownResult(-1 * st.getAdded().size(), Formatter.SupportedColors.Green);
                printStringListFiltered("            ", st.getAdded());
                format().reset();
            }
            if (!options.hidePositives()) {
                format().println("    new fixes   : ");
                setFontByKNownResult(-1 * st.getFixes().size(), Formatter.SupportedColors.LightGreen);
                printStringListFiltered("            ", st.getFixes());
                format().reset();
            }
            if (!options.hideNegatives()) {
                format().println("    new errors  : ");
                setFontByKNownResult(-1 * (st.getErrors().size()), Formatter.SupportedColors.LightRed);
                printStringListFiltered("            ", st.getErrors());
                format().reset();
                format().println("    new failures: ");
                setFontByKNownResult(-1 * (st.getFailures().size()), Formatter.SupportedColors.LightRed);
                printStringListFiltered("            ", st.getFailures());
                format().reset();
            }

        }
    }

    private String intDiffToString(int iN, int iO) {
        return intDiffToString(intDiff(iN, iO));
    }

    private String intDiffToString(int i) {
        if (i <= 0) {
            return "" + i;
        }
        return "+" + i;

    }

    private int intDiff(int iN, int iO) {
        return iN - iO;

    }

    //shortcut
    private Formatter format() {
        return options.getFormatter();
    }

    private void setFontByDiff(Object br, Object old, String method, Formatter.SupportedColors c0, Formatter.SupportedColors cNonZero) {
        setFontByDiff(br, old, method, 1, c0, cNonZero);
    }

    private void setFontByDiffNeg(Object br, Object old, String method, Formatter.SupportedColors c0, Formatter.SupportedColors cNonZero) {
        setFontByDiff(br, old, method, -1, c0, cNonZero);
    }

    private void setFontByDiffAbs(Object br, Object old, String method, Formatter.SupportedColors c0, Formatter.SupportedColors cNonZero) {
        setFontByDiff(br, old, method, 0, c0, cNonZero);
    }

    private void setFontByDiff(Object br, Object old, String method, int difMultiplier, Formatter.SupportedColors c0, Formatter.SupportedColors cNonZero) {

        int eval = 0;
        if (old != null) {
            eval = intDiff(callMethod(br, method), callMethod(old, method));
            if (difMultiplier == 0 && eval > 0) {
                eval = eval * -1;
            } else {
                eval = difMultiplier * eval;
            }
        }
        setFontByKNownResult(eval, cNonZero, c0);

    }

    //negative makes it bold
    private void setFontByKNownResult(int eval, Formatter.SupportedColors c) {
        setFontByKNownResult(eval, c, c);
    }

    //when result is zero, one color is shown
    //when nonzero, second color is shown
    //when negative, it is bold in addition
    private void setFontByKNownResult(int eval, Formatter.SupportedColors cNonZero, Formatter.SupportedColors c0) {
        if (eval > 0) {
            format().startColor(cNonZero);
        } else if (eval < 0) {
            format().startColor(cNonZero);
            format().startBold();
        } else {
            format().startColor(c0);
        }
    }

    private int callMethod(Object o, String method) {
        try {
            return callMethodImpl(o, method);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private int callMethodImpl(Object o, String method) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method m = o.getClass().getMethod(method);
        Object res = m.invoke(o);
        if (res instanceof Collection) {
            return ((Collection) res).size();
        }
        return (int) res;
    }

    private void printAllTests(BuildReportExtended bre) {
        format().startTitle2();
        format().println("  ***  All tests!  *** ");
        format().reset();
        format().startTitle3();
        String nwNra = JobsRecognition.getChangelogsNvr(new File(bre.getBuildName()));
        format().print(nwNra + " (" + bre.getBuildNumber() + ": " + bre.getBuildName() + ")");
        format().reset();
        List<SuiteTestsWithResults> all = bre.getAllTests().getAllTestsAndSuites();
        for (SuiteTestsWithResults suiteTest : all) {
            format().startTitle3();
            format().println("    *** " + suiteTest.getName() + " *** ");
            format().reset();
            for (SuiteTestsWithResults.StringWithResult test : suiteTest.getTests()) {
                if (options.getTrackingRegex().matcher(test.getTestName()).matches()) {
                    if (test.getStatus() == SuiteTestsWithResults.TestStatusSimplified.FAILED_OR_ERROR) {
                        if (!options.hideNegatives()) {
                            format().startColor(Formatter.SupportedColors.Red);
                            format().println("          " + test.getTestName() + " " + test.getStatus().toString());
                        }
                    } else if (!options.hidePositives()) {
                        format().startColor(Formatter.SupportedColors.Green);
                        format().print("          " + test.getTestName() + " ");
                        format().startColor(Formatter.SupportedColors.Yellow);
                        format().println(test.getStatus().toString());
                    }

                    format().reset();
                }
            }

        }
    }

}
