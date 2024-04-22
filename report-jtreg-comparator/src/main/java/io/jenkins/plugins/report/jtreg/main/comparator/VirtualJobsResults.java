package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.ConfigFinder;
import io.jenkins.plugins.report.jtreg.formatters.Formatter;
import io.jenkins.plugins.report.jtreg.formatters.JtregPluginServicesCell;
import io.jenkins.plugins.report.jtreg.formatters.JtregPluginServicesLinkWithTooltip;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class VirtualJobsResults {
    private static final List<String> RESULTS = Arrays.asList("SUCCESS", "UNSTABLE", "FAILURE", "ABORTED", "NOT_BUILT", "RUNNING?");

    private static String getBuildResult(File build, Options.Configuration resultConfig) {
        String result = new ConfigFinder(resultConfig.findConfigFile(build), "result", resultConfig.getFindQuery()).findInConfig();

        if (result == null) {
            // if there is no result in the config file, it is possible that the build is still running
            return "RUNNING?";
        }

        if (RESULTS.contains(result)) {
            return result;
        } else {
            throw new RuntimeException("The " + result + " result is not a valid build result.");
        }
    }

    public static void printVirtualTable(ArrayList<File> buildsToCompare, Options options) {
        Formatter formatter = options.getFormatter();
        formatter.startBold();
        formatter.println("Virtual builds' results table:");
        formatter.println();
        formatter.reset();

        JtregPluginServicesCell[][] table = new JtregPluginServicesCell[RESULTS.size() + 1][buildsToCompare.size() + 1];

        // first column definitions
        for (int i = 1; i <= RESULTS.size(); i++) {
            table[i][0] = options.getFormatter().createCell(new JtregPluginServicesLinkWithTooltip(RESULTS.get(i - 1)));
        }

        for (int i = 1; i <= buildsToCompare.size(); i++) {
            File build = buildsToCompare.get(i - 1);
            List<String> otherLines = new ArrayList<>();
            // create the other lines (job properties)
            for (Map.Entry<String, Options.Configuration> entry : options.getAllConfigurations().entrySet()) {
                String line = entry.getKey() + " : " +
                        new ConfigFinder(entry.getValue().findConfigFile(build), entry.getKey(), entry.getValue().getFindQuery()).findInConfig();
                otherLines.add(line);
            }

            table[0][i] = formatter.generateTableHeaderItemAsCell(Builds.getJobName(build), Builds.getBuildNumber(build), otherLines, options.getJenkinsUrl());

            String result = getBuildResult(build, options.getConfiguration("result"));
            String buildName =Builds.getJobName(build);
            String jobId = Builds.getBuildNumber(build);
            String id = "virtual-" + result + "-" + buildName + "-" + jobId;
            table[RESULTS.indexOf(result) + 1][i] = formatter.createCell(new JtregPluginServicesLinkWithTooltip("X", null, id, createTooltip(result, buildName, jobId, i, id, options.getJenkinsUrl()), true));
        }

        formatter.printTable(table, RESULTS.size() + 1, buildsToCompare.size() + 1);
    }

    public static List<JtregPluginServicesLinkWithTooltip> createTooltip(String result, String buildName, String buildId, int column, String id, String url) {
        List<JtregPluginServicesLinkWithTooltip> tooltips = new ArrayList<>();
        tooltips.add(new JtregPluginServicesLinkWithTooltip(result, url + "/job/" + buildName + "/" + buildId + "/java-reports#" + result));
        tooltips.add(new JtregPluginServicesLinkWithTooltip(" * self: " + column, "#" + id));
        tooltips.add(new JtregPluginServicesLinkWithTooltip(" * header: " + column, "#legend-" + column));
        tooltips.add(new JtregPluginServicesLinkWithTooltip(" * column: " + column, "#table-" + column));
        tooltips.add(new JtregPluginServicesLinkWithTooltip(" * build: " + buildId + " in: ", url + "/job/" + buildName + "/" + buildId));
        tooltips.add(new JtregPluginServicesLinkWithTooltip(buildName, url + "/job/" + buildName));
        return tooltips;
    }


}