package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.ConfigFinder;
import io.jenkins.plugins.report.jtreg.formatters.Formatter;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class VirtualJobsResults {
    private static final List<String> RESULTS = Arrays.asList("SUCCESS", "UNSTABLE", "FAILURE", "ABORTED", "RUNNING?");

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

        String[][] table = new String[RESULTS.size() + 1][buildsToCompare.size() + 1];

        // first column definitions
        for (int i = 1; i <= RESULTS.size(); i++) {
            table[i][0] = RESULTS.get(i - 1);
        }

        for (int i = 1; i <= buildsToCompare.size(); i++) {
            File build = buildsToCompare.get(i - 1);

            // create main line of the table header
            String mainLine = Builds.getJobName(build) + " - build:" + Builds.getBuildNumber(build);
            List<String> otherLines = new ArrayList<>();

            // create the other lines (job properties)
            for (Map.Entry<String, Options.Configuration> entry : options.getAllConfigurations().entrySet()) {
                String line = entry.getKey() + " : " +
                        new ConfigFinder(entry.getValue().findConfigFile(build), entry.getKey(), entry.getValue().getFindQuery()).findInConfig();
                otherLines.add(line);
            }

            table[0][i] = formatter.generateTableHeaderItem(mainLine, otherLines);

            String result = getBuildResult(build, options.getConfiguration("result"));
            table[RESULTS.indexOf(result) + 1][i] = "X";
        }

        formatter.printTable(table, RESULTS.size() + 1, buildsToCompare.size() + 1);
    }
}