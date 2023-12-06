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

    // filter the jobs, keep only those, that are matching all user defined configurations
    private static File[] filterByConfig(File[] builds, Map<String, Options.Configuration> configs) {
        List<File> filteredBuilds = new ArrayList<>();
        for (File build : builds) {
            boolean correct = true;
            for (Map.Entry<String, Options.Configuration> entry : configs.entrySet()) {
                String desiredValue = entry.getValue().getValue();
                String valueInConfig = new ConfigFinder(entry.getValue().findConfigFile(build), entry.getKey(), entry.getValue().getFindQuery()).findInConfig();

                if (valueInConfig == null) {
                    correct = false;
                } else if (desiredValue == null || desiredValue.isEmpty()) {
                    correct = true;
                } else if (desiredValue.charAt(0) == '{') {
                    // match multiple values
                    if (desiredValue.charAt(desiredValue.length() - 1) != '}') {
                        throw new RuntimeException("Expected closing } in the --" + entry.getKey() + "  value.");
                    }

                    String[] values = desiredValue.substring(1, desiredValue.length() - 1).split(",");
                    correct = Arrays.stream(values).anyMatch(valueInConfig::matches);
                } else {
                    correct = valueInConfig.matches(desiredValue);
                }
            }

            if (correct) {
                filteredBuilds.add(build);
            }
        }

        return filteredBuilds.toArray(new File[0]);
    }

    // gets all the compatible builds with the given parameters and returns them in a list
    public static List<File> getBuilds(File job, Options options) {
        List<File> listOfBuilds = new ArrayList<>();

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

        buildsInDir = filterByConfig(buildsInDir, options.getAllConfigurations());

        int buildsChecked = 0;
        for (File build : buildsInDir) {
            if (checkIfCorrect(build, options.isSkipFailed()) && buildsChecked < options.getNumberOfBuilds()) {
                listOfBuilds.add(build);
            }

            // only add to the counter when the build was successful, or when we also take unsuccessful builds
            if (!options.isSkipFailed() || checkIfCorrect(build, true)) {
                buildsChecked++;
            }
        }

        if (listOfBuilds.isEmpty() && options.isUseDefaultBuild()) {
            for (File build : buildsInDir) {
                if (checkIfCorrect(build, options.isSkipFailed())) {
                    listOfBuilds.add(build);
                    options.getFormatter().startColor(Formatter.SupportedColors.Yellow);
                    options.getFormatter().println("Cannot find any builds of job " + getJobName(build) + " that matches your criteria, instead using default build " + getBuildNumber(build) + ".");
                    options.getFormatter().reset();
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
        if (nvrConfig != null) {
            return new ConfigFinder(nvrConfig.findConfigFile(build), "nvr", nvrConfig.getFindQuery()).findInConfig();
        } else {
            return "";
        }
    }
}