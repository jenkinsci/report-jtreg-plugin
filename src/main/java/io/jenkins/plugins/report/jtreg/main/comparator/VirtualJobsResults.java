package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.main.diff.formatters.Formatter;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VirtualJobsResults {
    private static final List<String> results = Arrays.asList("SUCCESS", "UNSTABLE", "FAILURE", "ABORTED", "RUNNING?");

    private static String getBuildResult(File build) {
        Document doc = null;
        try {
            File buildXml = new File(build.getAbsolutePath() + "/build.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(buildXml);
            doc.getDocumentElement().normalize();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (doc == null) {
            return "RUNNING?";
        }

        try {
            return doc.getElementsByTagName("result").item(0)
                    .getChildNodes().item(0)
                    .getNodeValue();
        } catch (Exception e) {
            return "RUNNING?";
        }
    }

    public static void printVirtualTable(ArrayList<File> buildsToCompare, Formatter formatter) {
        String[][] table = new String[buildsToCompare.size() + 1][results.size() + 1];

        // first row - definitions
        for (int i = 1; i <= results.size(); i++) {
            table[0][i] = results.get(i - 1);
        }

        for (int i = 1; i  <= buildsToCompare.size(); i++) {
            File build = buildsToCompare.get(i - 1);
            table[i][0] = Builds.getJobName(build)
                    + " - build:" + Builds.getBuildNumber(build)
                    + " - nvr:" + Builds.getNvr(build);

            String result = getBuildResult(build);
            table[i][results.indexOf(result) + 1] = "X";
        }

        formatter.printTable(table, buildsToCompare.size() + 1, results.size() + 1);
    }
}