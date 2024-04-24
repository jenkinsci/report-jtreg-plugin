package io.jenkins.plugins.report.jtreg.main.comparator.jobs;

import io.jenkins.plugins.report.jtreg.Constants;
import io.jenkins.plugins.report.jtreg.arguments.Argument;
import io.jenkins.plugins.report.jtreg.main.comparator.arguments.ComparatorArgDeclaration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class JobsByQuery implements JobsProvider {
    public static final Argument queryArg = new Argument("--query", "Filtering of the jobs by a query string (the syntax is described below).", " <querystring>");
    public static final Argument exactLengthArg = new Argument("--exact-length", "Meant to be used in combination with --query. It filters only the jobs that have this specified length (number of elements in its name).", " <number>");
    public static final String queryStringUsage =
            "    The tool splits every job name by . or - characters and compares each\n" +
            "    split part with the query string. The query string consists of N parts\n" +
            "    separated by spaces (or other whitespace) and each of these parts\n" +
            "    corresponds with 1st..Nth part of the split job name.\n" +
            "    Example with explanation:\n" +
            "     \"jtreg~full jp17 * {f37,el8} !aarch64 !{fastdebug,slowdebug} * * *\"\n" +
            "        jtreg~full - specifies that the job's first part should be exactly\n" +
            "                     jtreg~full.\n" +
            "        jp17 - specifies that the job's second part should be exactly jp17.\n" +
            "        * - asterisk is a powerful wildcard that matches everything, so in\n" +
            "            this example, the job's parts on the 3rd, 7th, 8th and 9th don't\n" +
            "            matter = the tool takes everything on these positions.\n" +
            "            To stop the tool from draining system resources by looking at\n" +
            "            all jobs, if more than half of the query elements are asterisks,\n" +
            "            or the query has less than 4 parts, you must combine it with the\n" +
            "            \"" + ComparatorArgDeclaration.forceArg.getName() + "\" switch.\n" +
            "        {f37,el8} - this is a set of possible matches, so the jobs's part on\n" +
            "                    4th position can be either f37 or el8. There can me as\n" +
            "                    many elements as you want, but they must be split by\n" +
            "                    commas with no spaces between them.\n" +
            "        !aarch64 - matches everything, BUT aarch64.\n" +
            "        !{fastdebug,slowdebug} - matches everything, but the elements in\n" +
            "                                 the set.\n";

    private String queryString;
    private int exactLength;
    private boolean forceVague;
    private ArrayList<File> jobsInDir;
    private ArrayList<File> matchedJobs;

    public JobsByQuery() {
        this.queryString = "";
        this.exactLength = -1; // the default value that does not limit the length
        this.forceVague = false;
        this.jobsInDir = new ArrayList<>();
        this.matchedJobs = new ArrayList<>();
    }

    public static ArrayList<String> getSupportedArgsStatic() {
        ArrayList<String> supportedArgs = new ArrayList<>();
        supportedArgs.add(queryArg.getName());
        supportedArgs.add(exactLengthArg.getName());
        return supportedArgs;
    }

    public ArrayList<String> getSupportedArgs() {
        return getSupportedArgsStatic();
    }

    public void parseArguments(String argument, String value) {
        if (argument.equals(queryArg.getName())) {
            this.queryString = value;
        } else if (argument.equals(exactLengthArg.getName())) {
            this.exactLength = Integer.parseInt(value);
        } else if (argument.equals(ComparatorArgDeclaration.forceArg.getName())) {
            this.forceVague = true;
        } else {
            throw new RuntimeException("JobsByQuery got an unexpected argument.");
        }
    }

    public void addJobs(ArrayList<File> jobsInDir) {
        this.jobsInDir = jobsInDir;
    }

    public void filterJobs() {
        // check if the query is not too vague (more than half of query are * or the query is shorter than 4 elements)
        int numOfAsterisks = this.queryString.length() - this.queryString.replace("*", "").length();
        int lengthOfQuery = this.queryString.split("\\s+").length;
        if ((lengthOfQuery < Constants.VAGUE_QUERY_LENGTH_THRESHOLD ||
                (numOfAsterisks != 0 && (double)numOfAsterisks / (double)lengthOfQuery > Constants.VAGUE_QUERY_THRESHOLD)) &&
                !this.forceVague) {
            throw new RuntimeException("The query string is too vague (too many * or short query), run with --force to continue anyway.");
        }

        ArrayList<String[]> queryList = parseToList(queryString);

        this.matchedJobs = new ArrayList<>();
        for (File job : this.jobsInDir) {
            if (checkJobWithQuery(queryList, job)) {
                this.matchedJobs.add(job);
            }
        }
    }

    // parses the given query string into a list of string arrays
    private ArrayList<String[]> parseToList(String queryString) {
        String[] queryArray = queryString.split("\\s+");
        ArrayList<String[]> queryList = new ArrayList<>();

        for (String s : queryArray) {
            boolean reversed = false;
            if (s.charAt(0) == '!') {
                reversed = true;
                s = s.substring(1);
            }
            // checks for array in the query
            if (s.charAt(0) == '{') {
                if (s.charAt(s.length() - 1) != '}') {
                    throw new RuntimeException("Expected closing }.");
                }
                String[] array = s.substring(1, s.length() - 1).split(",");
                if (reversed) {
                    for (int i = 0; i < array.length; i++) {
                        array[i] = "!" + array[i];
                    }
                    queryList.add(array);
                } else {
                    queryList.add(array);
                }
            } else {
                String[] arrayOfOne = new String[1];
                if (reversed) {
                    arrayOfOne[0] = "!" + s;
                } else {
                    arrayOfOne[0] = s;
                }
                queryList.add(arrayOfOne);
            }
        }
        return queryList;
    }

    // checks if the given job matches the query string
    private boolean checkJobWithQuery(ArrayList<String[]> queryList, File job) {
        String[] jobArray = job.getName().split("[.-]");

        if (this.exactLength != -1 && this.exactLength != jobArray.length) {
            return false;
        }

        for (int i = 0; i < queryList.size(); i++) {
            if (jobArray.length <= i) {
                break;
            }

            String[] qA = queryList.get(i); // get a single array from the queryList

            boolean reversed = qA[0].charAt(0) == '!'; // check for reversed query

            if (qA.length == 1) {
                if ( (!reversed && !qA[0].equals("*") && !qA[0].equals(jobArray[i])) ||
                        (reversed && !qA[0].equals("*") && qA[0].equals("!" + jobArray[i])) ) {
                    return false;
                }
            } else {
                if ( (!reversed && !Arrays.asList(qA).contains(jobArray[i])) ||
                        (reversed && Arrays.asList(qA).contains("!" + jobArray[i])) ) {
                    return false;
                }
            }
        }
        return true;
    }

    public ArrayList<File> getJobs() {
        return matchedJobs;
    }
}
