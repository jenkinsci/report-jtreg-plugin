/*
 * The MIT License
 *
 * Copyright 2015-2023 report-jtreg plugin contributors
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
package io.jenkins.plugins.report.jtreg;

import io.jenkins.plugins.report.jtreg.items.ComparatorLinksGroup;
import io.jenkins.plugins.report.jtreg.items.ConfigItem;
import io.jenkins.plugins.report.jtreg.items.LinkToComparator;
import io.jenkins.plugins.report.jtreg.items.TestLink;
import io.jenkins.plugins.report.jtreg.model.*;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuildReportExtendedPlugin extends BuildReportExtended {

    private final UrlsProvider urlsProvider;

    public BuildReportExtendedPlugin(int buildNumber, String buildName, int passed, int failed, int error, List<Suite> suites,
                               List<String> addedSuites, List<String> removedSuites, List<SuiteTestChanges> testChanges, int total,
                                int notRun, SuitesWithResults allTests, String job, UrlsProvider urlProvider) {
        super(buildNumber, buildName, passed, failed, error, suites, addedSuites, removedSuites, testChanges, total, notRun, allTests, job);
        this.urlsProvider = urlProvider;
        allTests.setUrlProviser(urlsProvider);
    }

    public List<ComparatorLinksGroup> getMatchedComparatorLinksGroups() {
        List<ComparatorLinksGroup> matchedComparatorLinksGroup = new ArrayList<>();
        for (ComparatorLinksGroup link : JenkinsReportJckGlobalConfig.getGlobalComparatorLinksGroups()) {
            if (job.matches(link.getJobMatchRegex())) {
                matchedComparatorLinksGroup.add(link);
            }
        }

        return matchedComparatorLinksGroup;
    }

    public String createComparatorLinkUrl(LinkToComparator ltc, boolean configArgs) {
        StringBuilder url = new StringBuilder();

        for (String arg : ltc.getComparatorArguments().split("(\\n|\\r\\n)")) {
            url.append(parseQueryToText(ltc.getSpliterator(), arg, null));
            url.append(" ");
        }

        if (configArgs) {
            for (ConfigItem ci : JenkinsReportJckGlobalConfig.getGlobalConfigItems()) {
                url.append(ci.generateConfigArgument());
                url.append(" ");
            }
        }

        return createBaseUrl(ltc.getBasePage(), ltc.getBasePageCustom()) + URLEncoder.encode(url.toString(), StandardCharsets.UTF_8);
    }

    private String parseQueryToText(String spliterator, String query, String testName) {
        String[] splitJob = job.split(spliterator);

        String converted = query;

        // finds %{X} in the query from Jenkins config and replaces it with corresponding part of job name or value from config
        Pattern p = Pattern.compile("%\\{[a-zA-Z0-9+-]+}");
        Matcher m = p.matcher(converted);

        while (m.find()) {
            String insideBrackets = converted.substring(m.start() + 1 + 1, m.end() - 1); // get just the inside of the brackets

            String replacement = "";
            if (insideBrackets.equals("S") || insideBrackets.equals("SPLIT")) {
                replacement = spliterator;
            } else if (insideBrackets.equals("JOBNAME")) {
                replacement = job;
            } else if (insideBrackets.matches("N?[+-]?[0-9]+|N")) {
                int number;
                if (insideBrackets.charAt(0) == 'N' && insideBrackets.length() > 1) {
                    number = splitJob.length + Integer.parseInt(insideBrackets.substring(1));
                } else if (insideBrackets.charAt(0) == 'N') {
                    number = splitJob.length;
                } else {
                    number = Integer.parseInt(insideBrackets) - 1;
                }

                if (number < 0 || number >= splitJob.length) {
                    System.err.println("WARNING: The number given in the --regex argument of \"Comparator links\" section is out of range of the job name!");
                    return "The number given is out of range of the job name!";
                }

                replacement = splitJob[number];
            } else if (insideBrackets.equals("TESTNAME")) {
                if (testName == null) {
                    System.err.println("WARNING: Cannot get test name here!");
                    return "Cannot get test name here!";
                } else {
                    replacement = testName;
                }
            } else {
                List<ConfigItem> configItems = JenkinsReportJckGlobalConfig.getGlobalConfigItems();
                boolean found = false;
                for (ConfigItem item : configItems) {
                    if (item.getWhatToFind().equals(insideBrackets)) {
                        found = true;

                        // get path of Jenkins home
                        String jenkinsHome = System.getProperty("jenkins_home");
                        if (jenkinsHome == null) {
                            jenkinsHome = System.getenv("JENKINS_HOME");
                        }

                        File configFile = getFile(item, jenkinsHome);

                        replacement = new ConfigFinder(configFile, item.getWhatToFind(), item.getFindQuery()).findInConfig();
                        break;
                    }
                }

                if (!found) {
                    System.err.println("WARNING: Cannot find a config item corresponding with \""+ insideBrackets + "\", please set it first!");
                    return "Cannot find \"" + insideBrackets + "\" config item!";
                }
            }

            converted = m.replaceFirst(replacement);
            m = p.matcher(converted);
        }

        return converted;
    }

    private File getFile(ConfigItem item, String jenkinsHome) {
        String path = jenkinsHome + "/jobs/" + job + "/";

        // check if it is looking into job or build directory and add the correct path
        if (item.getConfigLocation().equals("build")) {
            path = path + "builds/" + getBuildNumber();
        } else if (!item.getConfigLocation().equals("job")){
            throw new RuntimeException("Invalid location of config file, only job or build directories are allowed.");
        }

        File configFile = new File(path, item.getConfigFileName());

        ConfigFinder.checkIfConfigIsInParent(new File(path), configFile);

        if (!configFile.exists()) {
            throw new RuntimeException("The file " + path + item.getConfigFileName() + " was not found.");
        }
        return configFile;
    }

    public List<TestLink> getAllTestLinks() {
        return JenkinsReportJckGlobalConfig.getGlobalTestLinks();
    }

    public String createTestLinkUrl(TestLink testLink, String testName) {
        StringBuilder url = new StringBuilder();

        for (String arg : testLink.getArguments().split("(\\n|\\r\\n)")) {
            url.append(parseQueryToText(testLink.getSpliterator(), arg, testName));
            url.append(" ");
        }

        return createBaseUrl(testLink.getBasePage(), testLink.getBasePageCustom()) + URLEncoder.encode(url.toString(), StandardCharsets.UTF_8);
    }

    public boolean isDiffTool() {
        return JenkinsReportJckGlobalConfig.isGlobalDiffUrl();
    }

    private String createBaseUrl(String presetTool, String customUrl) {
        if (!customUrl.isEmpty()) {
            return JenkinsReportJckGlobalConfig.getGlobalDiffUrl() + "/" + customUrl;
        } else {
            String url = "";

            switch (presetTool) {
                case "comparator":
                    url = urlsProvider.getCompServer();
                    break;

                case "diff":
                    url = urlsProvider.getDiffServer();
                    break;

                case "list":
                    url = urlsProvider.getListServer();
                    break;

                default:
                    throw new RuntimeException("Unknown tool selected in the Jenkins config.");
            }

            return url + "?generated-part=&custom-part=";
        }
    }

}