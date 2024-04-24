package io.jenkins.plugins.report.jtreg.utils;

import io.jenkins.plugins.report.jtreg.BuildReportExtended;
import io.jenkins.plugins.report.jtreg.BuildSummaryParser;
import io.jenkins.plugins.report.jtreg.CommonOptions;
import io.jenkins.plugins.report.jtreg.model.Suite;
import io.jenkins.plugins.report.jtreg.model.Test;
import io.jenkins.plugins.report.jtreg.model.TestOutput;
import io.jenkins.plugins.report.jtreg.wrappers.RunWrapperFromDir;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class StackTraceTools {
    private static BuildSummaryParser bs = new BuildSummaryParser(Arrays.asList("jck", "jtreg"), null);

    public static String getTestTrace(File build, String testName, CommonOptions.Side cutSide, int cutLength) {
        try {
            BuildReportExtended bex = bs.parseBuildReportExtended(new RunWrapperFromDir(build), null);

            // go through the suites
            for (Suite s : bex.getSuites()) {
                // filter for the test with the name we are looking for
                Test t = s.getReport().getTestProblems().stream().filter(test -> test.getName().equals(testName)).findFirst().orElse(null);
                if (t != null) {
                    // return the output (stack trace)
                    StringBuilder wholeTrace = new StringBuilder();
                    wholeTrace.append(t.getStatusLine());
                    wholeTrace.append("\n\n");

                    // get outputs, sort them by their name (to be deterministic) and append them to the string
                    List<TestOutput> outs = t.getOutputs();
                    outs.sort(Comparator.comparing(TestOutput::getName));

                    for (TestOutput out : outs) {
                        wholeTrace.append(out.getName());
                        wholeTrace.append(" : \n");

                        if ((cutSide == CommonOptions.Side.HeadEach || cutSide == CommonOptions.Side.TailEach) && out.getValue().length() > cutLength) {
                            if (cutSide == CommonOptions.Side.HeadEach) {
                                wholeTrace.append(out.getValue(), 0, cutLength);
                            } else {
                                wholeTrace.append(out.getValue(), out.getValue().length() - cutLength, out.getValue().length());
                            }
                        } else {
                            wholeTrace.append(out.getValue());
                        }

                        wholeTrace.append("\n\n");
                    }

                    if ((cutSide == CommonOptions.Side.Head || cutSide == CommonOptions.Side.Tail) && wholeTrace.length() > cutLength) {
                        if (cutSide == CommonOptions.Side.Head) {
                            return wholeTrace.substring(0, cutLength);
                        } else {
                            return wholeTrace.substring(wholeTrace.length() - cutLength, wholeTrace.length());
                        }
                    }

                    return wholeTrace.toString();
                }

            }
        } catch (Exception e) {
            System.err.println("An exception was caught when trying to get the stack trace of " + testName + " test:");
            e.printStackTrace();
        }

        return null;
    }
}
