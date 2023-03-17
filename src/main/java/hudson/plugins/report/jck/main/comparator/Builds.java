package hudson.plugins.report.jck.main.comparator;

import hudson.plugins.report.jck.main.diff.cmdline.JobsRecognition;

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


    // checks if the given build was successful (only if required)
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

                if (result.equals("SUCCESS") || result.equals("UNSTABLE")) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        } else {
            return true;
        }
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
    public static ArrayList<File> getBuilds(File job, boolean skipFailed, String nvrQuery) {
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

        // when the nvr query has array ({...}), expecting more results
        boolean moreResults = !nvrQuery.equals("") && nvrQuery.charAt(0) == '{';
        ArrayList<String> alreadyFoundNvrs = new ArrayList<>(); // to store already found nvrs (to get no duplicates)
        int expectedSize = nvrQuery.split(",").length; // number of all nvrs in the nvr array

        for (File build : buildsInDir) {
            if (checkIfCorrect(build, skipFailed) && checkForNvr(build, nvrQuery)) {
                if (!moreResults) {
                    listOfBuilds.add(build);
                    break;
                } else {
                    String nvr = JobsRecognition.getChangelogsNvr(build);
                    if (!alreadyFoundNvrs.contains(nvr)) {
                        listOfBuilds.add(build);
                        alreadyFoundNvrs.add(nvr);
                    }
                    if (alreadyFoundNvrs.size() == expectedSize) {
                        break;
                    }
                }
            }
        }
        return listOfBuilds;
    }
}