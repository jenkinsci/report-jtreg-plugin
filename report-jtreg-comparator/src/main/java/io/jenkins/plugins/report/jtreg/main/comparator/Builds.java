package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.ConfigFinder;

import io.jenkins.plugins.report.jtreg.formatters.Formatter;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

public class Builds {
    // checks if the given build was successful
    private static boolean checkIfCorrect(File build, boolean requireSuccessful) {
        if (requireSuccessful) {
            try {
                File buildXml = new File(build.getAbsolutePath() + "/build.xml");
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(buildXml);
                doc.getDocumentElement().normalize();

                String result = doc
                        .getElementsByTagName("result").item(0)
                        .getChildNodes().item(0)
                        .getNodeValue();

                return result.equals("SUCCESS") || result.equals("UNSTABLE");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        } else {
            return true;
        }
    }

    // checks if the build has the same NVR as given
    static boolean checkForNvr(File build, String nvrQuery, Options.Configuration nvrConfig) {
        // nvrQuery has the same syntax as query string
        if (nvrQuery.equals("") || nvrQuery.equals("*")) {
            return true;
        }
        String buildNvr = new ConfigFinder(nvrConfig.findConfigFile(build), "nvr", nvrConfig.getFindQuery()).findInConfig();

        if (buildNvr == null) {
            return false;
        } else if (nvrQuery.charAt(0) == '{') {
            if (nvrQuery.charAt(nvrQuery.length() - 1) != '}') {
                throw new RuntimeException("Expected closing }.");
            }
            String[] nvrs = nvrQuery.substring(1, nvrQuery.length() - 1).split(",");
            return Arrays.stream(nvrs).anyMatch(s -> buildNvr.matches(s));
        } else {
            return buildNvr.matches(nvrQuery);
        }
    }

    // gets all the compatible builds with the given parameters and returns them in a list
    public static ArrayList<File> getBuilds(
            File job, boolean skipFailed, String nvrQuery, int numberOfBuilds, boolean useDefaultBuild, Formatter formatter, Options.Configuration nvrConfig) {

        ArrayList<File> listOfBuilds = new ArrayList<>();

        File buildDir = new File(job.getAbsolutePath() + "/builds/");
        File[] filesInDir = buildDir.listFiles();
        // no builds => return empty list
        if (filesInDir == null) {
            return listOfBuilds;
        }
        File[] buildsInDir = Arrays
                .stream(filesInDir)
                .filter(File::isDirectory)
                .toArray(File[]::new);

        // sorting and then reversing to be sure it goes from the newest build to the oldest
        // sorting by parsing the build name to an integer first (to sort correctly)
        Arrays.sort(buildsInDir, Comparator.comparingInt(a -> Integer.parseInt(a.getName())));
        Collections.reverse(Arrays.asList(buildsInDir));

        int buildsChecked = 0;
        for (File build : buildsInDir) {
            if (checkIfCorrect(build, skipFailed) && checkForNvr(build, nvrQuery, nvrConfig) && buildsChecked < numberOfBuilds) {
                listOfBuilds.add(build);
            }

            // only add to the counter when the build was successful, or when we also take unsuccessful builds
            if (!skipFailed || checkIfCorrect(build, true)) {
                buildsChecked++;
            }
        }

        if (!nvrQuery.equals("") && listOfBuilds.size() == 0 && useDefaultBuild) {
            for (File build : buildsInDir) {
                if (checkIfCorrect(build, skipFailed)) {
                    listOfBuilds.add(build);
                    formatter.startColor(Formatter.SupportedColors.Yellow);
                    formatter.println("Cannot find job " + getJobName(build) + " which matches " + nvrQuery +
                            ", instead using build " + getBuildNumber(build) + " with nvr " + getNvr(build, nvrConfig) + ".");
                    formatter.reset();
                    break;
                }
            }
        }

        return listOfBuilds;
    }

    public static String getJobName(File build) {
        String path = build.getAbsolutePath();
        String[] split = path.split("/");
        if (split[split.length - 2].equals("builds")) { // second last should be the "builds" directory
            return split[split.length - 3]; // third last is the job name
        } else {
            throw new RuntimeException("The getJobName() function got invalid build path.");
        }
    }

    public static String getBuildNumber(File build) {
        String path = build.getAbsolutePath();
        String[] split = path.split("/");
        if (split[split.length - 2].equals("builds")) { // second last should be the "builds" directory
            return split[split.length - 1]; // the last is the build number
        } else {
            throw new RuntimeException("The getBuildNumber() function got invalid build path.");
        }
    }

    public static String getNvr(File build, Options.Configuration nvrConfig) {
        return new ConfigFinder(nvrConfig.findConfigFile(build), "nvr", nvrConfig.getFindQuery()).findInConfig();
    }
}