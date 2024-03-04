package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.ConfigFinder;

import java.io.File;
import java.util.*;

public class Builds {
    // filter the jobs, keep only those, that are matching all user defined configurations
    private static List<File> filterByConfig(File[] builds, Map<String, Options.Configuration> configs) {
        List<File> filteredBuilds = new ArrayList<>();
        for (File build : builds) {
            boolean correct = true;
            for (Map.Entry<String, Options.Configuration> entry : configs.entrySet()) {
                // only filter by configurations in the build directories
                if (entry.getValue().getLocation() != Options.Locations.Build) {
                    continue;
                }

                String desiredValue = entry.getValue().getValue();
                String valueInConfig = new ConfigFinder(entry.getValue().findConfigFile(build), entry.getKey(), entry.getValue().getFindQuery()).findInConfig();

                if (valueInConfig == null) {
                    // if the config it is looking for, is the build result, and it is set to false (--skip-failed false),
                    // the job could still be running, the build.xml does not exist and the result is still valid
                    if (!(entry.getKey().equals("result") && entry.getValue().getValue().equals(".*"))) {
                        correct = false;
                    }
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

                if (!correct) {
                    break;
                }
            }

            if (correct) {
                filteredBuilds.add(build);
            }
        }

        return filteredBuilds;
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

        // check only N builds (based on --history switch, default is 1)
        File[] choppedArray = Arrays.copyOfRange(buildsInDir, 0, options.getNumberOfBuilds());

        listOfBuilds = filterByConfig(choppedArray, options.getAllConfigurations());

        if (listOfBuilds.isEmpty() && options.isUseDefaultBuild()) {
            listOfBuilds.add(buildsInDir[0]);
            System.err.println("Cannot find any builds of job " + getJobName(buildsInDir[0]) + " that matches your criteria, instead using default build " + getBuildNumber(buildsInDir[0]) + ".");
        }

        return listOfBuilds;
    }

    public static String getJobName(File build) {
        if (build == null) {
            return "";
        }
        String path = build.getAbsolutePath();
        String[] split = path.split("/");
        if (split[split.length - 2].equals("builds")) { // second last should be the "builds" directory
            return split[split.length - 3]; // third last is the job name
        } else {
            throw new RuntimeException("The getJobName() function got invalid build path.");
        }
    }

    public static String getBuildNumber(File build) {
        if (build == null) {
            return "";
        }
        String path = build.getAbsolutePath();
        String[] split = path.split("/");
        if (split[split.length - 2].equals("builds")) { // second last should be the "builds" directory
            return split[split.length - 1]; // the last is the build number
        } else {
            throw new RuntimeException("The getBuildNumber() function got invalid build path.");
        }
    }

    public static String getNvr(File build, Options.Configuration nvrConfig) {
        if (build != null && nvrConfig != null) {
            return new ConfigFinder(nvrConfig.findConfigFile(build), "nvr", nvrConfig.getFindQuery()).findInConfig();
        } else {
            return "";
        }
    }
}