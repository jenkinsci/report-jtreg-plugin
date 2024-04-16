package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.BuildReportExtended;
import io.jenkins.plugins.report.jtreg.BuildSummaryParser;
import io.jenkins.plugins.report.jtreg.ConfigFinder;
import io.jenkins.plugins.report.jtreg.formatters.JtregPluginServicesCell;
import io.jenkins.plugins.report.jtreg.formatters.JtregPluginServicesLinkWithTooltip;
import io.jenkins.plugins.report.jtreg.model.*;
import io.jenkins.plugins.report.jtreg.wrappers.RunWrapperFromDir;

import java.io.File;
import java.util.*;

public class StackTraceCompare {
    private static final BuildSummaryParser bs = new BuildSummaryParser(Arrays.asList("jck", "jtreg"), null);

    public static void compareTraces(Map<String, ArrayList<String>> failedMap, Options options) {
        // hide passed builds (no failed tests), if desired
        if (options.isHidePasses()) {
            failedMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }

        // get all non-duplicate failed tests from the map
        Set<String> nonRepeatingTests = new HashSet<>();
        for (ArrayList<String> values : failedMap.values()) {
            nonRepeatingTests.addAll(values);
        }
        // convert it into list and sort it
        List<String> failedTests = new ArrayList<>(nonRepeatingTests);
        Collections.sort(failedTests);

        // get the builds from the map and sort them
        List<String> jobBuilds = new ArrayList<>(failedMap.keySet());
        Collections.sort(jobBuilds);

        // create the table itself (2D array), where [rows][columns]
        JtregPluginServicesCell[][] table = new JtregPluginServicesCell[failedTests.size() + 1][jobBuilds.size() + 1];

        // put values into the table header (first row)
        for (int i = 1; i < jobBuilds.size() + 1; i++) {
            // convert the path into a file and then format it into the header
            File build = new File(jobBuilds.get(i - 1));
            List<String> otherLines = new ArrayList<>();
            for (Map.Entry<String, Options.Configuration> configEntry : options.getAllConfigurations().entrySet()) {
                String line = configEntry.getKey() + " : " +
                        new ConfigFinder(configEntry.getValue().findConfigFile(build), configEntry.getKey(), configEntry.getValue().getFindQuery()).findInConfig();
                otherLines.add(line);
            }

            table[0][i] = options.getFormatter().generateTableHeaderItemAsCell(Builds.getJobName(build),Builds.getBuildNumber(build), otherLines, options.getJenkinsUrl());
        }

        // add the similarity percentages into the table
        int i = 1;
        for (String test : failedTests) {
            table[i][0] = options.getFormatter().createCell(new JtregPluginServicesLinkWithTooltip(test)); // put the test name into
            // first column

            // create a list of values where to put the percentages
            List<String> putList = new ArrayList<>();
            for (Map.Entry<String, ArrayList<String>> entry : failedMap.entrySet()) {
                if (entry.getValue().contains(test)) {
                    putList.add(entry.getKey());
                }
            }

            Collections.sort(putList); // just to be sure

            // get the referential stack trace
            String reference = null;
            if (options.getReferentialJobName() == null || options.getReferentialBuildNumber() == -1) {
                // if not set, get the first one in table as referential
                reference = getTestTrace(new File(putList.get(0)), test, options.getSubstringSide(), options.getSubstringLength());
            } else {
                String value = putList.stream()
                        .filter(build -> build.matches(".*jobs/" + options.getReferentialJobName() + "/builds/" + options.getReferentialBuildNumber()))
                        .findFirst()
                        .orElse(null);

                if (value != null) {
                    reference = getTestTrace(new File(value), test, options.getSubstringSide(), options.getSubstringLength());
                }

            }

            // calculate and put the percentages (or - if no referential was found)
            for (String value : putList) {
                String stringToPut = "N/A";

                if (reference != null) {
                    String second = getTestTrace(new File(value), test, options.getSubstringSide(), options.getSubstringLength());
                    stringToPut = String.valueOf(getTraceSimilarity(reference, second));
                }

                List<JtregPluginServicesLinkWithTooltip> maybeSeveralComaprisons = new ArrayList<>();
                maybeSeveralComaprisons.add(new JtregPluginServicesLinkWithTooltip(stringToPut, null, getLinksTooltip(), true));
                //this is just demo
                //FIXME repace by real logic
                if ((jobBuilds.indexOf(value) + i + 1 )%2 == 0){
                    maybeSeveralComaprisons.add(new JtregPluginServicesLinkWithTooltip("X", "test", getLinksTooltip(), true));
                }
                table[i][jobBuilds.indexOf(value) + 1] = options.getFormatter().createCell(maybeSeveralComaprisons);
            }

            // TODO delete, just for debug logging
            System.err.println("Test " + (i - 1) + "/" + failedTests.size() + " - " + (int)((i - 1)/(double)failedTests.size() * 100) + "%");

            i++;
        }

        // print the table into stdout
        options.getFormatter().printTable(table, failedTests.size() + 1, jobBuilds.size() + 1);
    }

    private static List<JtregPluginServicesLinkWithTooltip> getLinksTooltip() {
        List<JtregPluginServicesLinkWithTooltip> list = new ArrayList<>();
        list.add(new JtregPluginServicesLinkWithTooltip("this is tool tip of comparison of $TEST trace of  $job1:$X x $job2:$Y"));
        list.add(new JtregPluginServicesLinkWithTooltip("use this as base", "some link", null));
        list.add(new JtregPluginServicesLinkWithTooltip("show diff agaisnt base", "some otjer link", null));
        list.add(new JtregPluginServicesLinkWithTooltip("show diff ind ifferent setup", "other link", null));
        return list;
    }

    public static void printTraceDiff(Options options) {
        Options.DiffInfo diffInfo = options.getDiffInfo();
        File buildOne = diffInfo.getBuildOne(options.getJobsPath());
        String testRegex = options.getExactTestsRegex();
        List<String> matchedFailedTests = new ArrayList<>();

        try {
            BuildReportExtended bex = bs.parseBuildReportExtended(new RunWrapperFromDir(buildOne), null);
            SuitesWithResults swr = bex.getAllTests();

            for (SuiteTestsWithResults stwr : swr.getAllTestsAndSuites()) {
                for (SuiteTestsWithResults.StringWithResult t : stwr.getTests()) {
                    if (t.getStatus().isFailed() && t.getTestName().matches(testRegex)) {
                        matchedFailedTests.add(t.getTestName());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("An exception was caught when trying to get the failed tests of " + diffInfo.getBuildOneName());
            e.printStackTrace();
        }

        for (String test : matchedFailedTests) {
            String traceOne = getTestTrace(buildOne, test, options.getSubstringSide(), options.getSubstringLength());
            String traceTwo = getTestTrace(diffInfo.getBuildTwo(options.getJobsPath()), test, options.getSubstringSide(), options.getSubstringLength());

            options.getFormatter().printDiff(traceOne, diffInfo.getBuildOneName() + " : test " + test, traceTwo,
                    diffInfo.getBuildTwoName() + " : test " + test, options.getDiffInfo().getTypeOfDiff());
        }
    }

    private static String getTestTrace(File build, String testName, Options.Side cutSide, int cutLength) {
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

                        if ((cutSide == Options.Side.HeadEach || cutSide == Options.Side.TailEach) && out.getValue().length() > cutLength) {
                            if (cutSide == Options.Side.HeadEach) {
                                wholeTrace.append(out.getValue(), 0, cutLength);
                            } else {
                                wholeTrace.append(out.getValue(), out.getValue().length() - cutLength, out.getValue().length());
                            }
                        } else {
                            wholeTrace.append(out.getValue());
                        }

                        wholeTrace.append("\n\n");
                    }

                    if ((cutSide == Options.Side.Head || cutSide == Options.Side.Tail) && wholeTrace.length() > cutLength) {
                        if (cutSide == Options.Side.Head) {
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

    private static int getTraceSimilarity(String one, String two) {
        // TODO:
        // - try the second, memory efficient algorithm (maybe give the user a choice?)
        // - switches for removing whitespace, case sensitivity, ...

        if (one == null || two == null) {
            return 0;
        }

        // ALGORITHM FOR CALCULATING LEVENSHTEIN DISTANCE,
        // taken and edited from: https://github.com/judovana/similars
        int[][] matrix = new int[one.length() + 1][two.length() + 1];

        for (int i = 0; i <= one.length(); i++) {
            for (int j = 0; j <= two.length(); j++) {
                if (i == 0) {
                    matrix[i][j] = j;
                } else if (j == 0) {
                    matrix[i][j] = i;
                } else {
                    int substitution = matrix[i - 1][j - 1] + ((one.charAt(i - 1) == two.charAt(j - 1)) ? 0 : 1);
                    int insertion = matrix[i][j - 1] + 1;
                    int deletion = matrix[i - 1][j] + 1;

                    matrix[i][j] = Math.min(substitution, Math.min(insertion, deletion));
                }
            }
        }
        int changes = matrix[one.length()][two.length()]; // result is in the bottom-right corner

        // calculate the similarity of the traces in percentages
        return 100 - ((changes * 100) / Math.max(one.length(), two.length()));
    }
}
