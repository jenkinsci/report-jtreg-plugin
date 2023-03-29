package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.main.diff.cmdline.JobsRecognition;

import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

public class Builds {
    // gets all NVRs from the given job and returns them as a list
    public static ArrayList<String> getJobNvrs(File job) {
        ArrayList<String> nvrs = new ArrayList<>();

        File buildDir = new File(job.getAbsolutePath() + "/builds/");
        File[] filesInDir = buildDir.listFiles();
        // no builds => return empty list
        if (filesInDir == null) {
            return nvrs;
        }
        File[] buildsInDir = Arrays
                .stream(filesInDir)
                .filter(File::isDirectory)
                .toArray(File[]::new);

        for (File build : buildsInDir) {
            String nvr = JobsRecognition.getChangelogsNvr(build); // method from jenkins-report-jck
            if (nvr != null && !nvrs.contains(nvr)) {
                nvrs.add(nvr);
            }
        }
        return nvrs;
    }

    // checks if the given build was successful
    private static boolean checkIfCorrect(File build, boolean requireSuccessful) {
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

            if (requireSuccessful) {
                return result.equals("SUCCESS") || result.equals("UNSTABLE");
            } else {
                // skipping ABORTED builds anyway
                return !result.equals("ABORTED");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // checks if the build has the same NVR as given
    private static boolean checkForNvr(File build, String nvrQuery) {
        // nvrQuery has the same syntax as query string
        if (nvrQuery.equals("") || nvrQuery.equals("*")) {
            return true;
        } else if (nvrQuery.charAt(0) == '{') {
            if (nvrQuery.charAt(nvrQuery.length() - 1) != '}') {
                throw new RuntimeException("Expected closing }.");
            }
            String[] nvrs = nvrQuery.substring(1, nvrQuery.length() - 1).split(",");
            String buildNvr = JobsRecognition.getChangelogsNvr(build);
            return Arrays.asList(nvrs).contains(buildNvr);
        } else {
            return nvrQuery.equals(JobsRecognition.getChangelogsNvr(build));
        }
    }

    // gets all the compatible builds with the given parameters and returns them in a list
    public static ArrayList<File> getBuilds(File job, boolean skipFailed, String nvrQuery, int numberOfBuilds) {
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

        HashMap<String, Integer> nvrCount = new HashMap<>();
        int expectedSize = numberOfBuilds;
        if (!nvrQuery.equals("") && nvrQuery.charAt(0) == '{') {
            expectedSize = expectedSize * nvrQuery.split(",").length;
        }

        for (File build : buildsInDir) {
            if (checkIfCorrect(build, skipFailed) && checkForNvr(build, nvrQuery) && listOfBuilds.size() < expectedSize) {
                nvrCount.putIfAbsent(getNvr(build), 0);
                if (nvrCount.get(getNvr(build)) < numberOfBuilds) {
                    listOfBuilds.add(build);
                    nvrCount.replace(getNvr(build), nvrCount.get(getNvr(build)) + 1);
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

    public static String getNvr(File build) {
        return JobsRecognition.getChangelogsNvr(build);
    }
}