package hudson.plugins.report.jck.comparer;

import java.io.File;
import java.util.ArrayList;

public class QueryString {
    // parses the given query string into a list of string arrays
    private static ArrayList<String[]> parseToList(String queryString) {
        String[] queryArray = queryString.split(" ");
        ArrayList<String[]> queryList = new ArrayList<>();

        for (String s : queryArray) {
            // checks for array in the query
            if (s.charAt(0) == '{') {
                if (s.charAt(s.length() - 1) != '}') {
                    throw new RuntimeException("Expected closing }.");
                }
                String[] array = s.substring(1, s.length() - 1).split(",");
                queryList.add(array);
            } else {
                String[] arrayOfOne = new String[1];
                arrayOfOne[0] = s;
                queryList.add(arrayOfOne);
            }
        }
        return queryList;
    }

    // checks if the given job matches the query string
    static boolean checkJobWithQuery(File job, String queryString) {
        if (!queryString.equals("")) {
            String[] jobArray = job.getName().split("[.-]");
            ArrayList<String[]> queryList = parseToList(queryString);

            for (int i = 0; i < queryList.size(); i++) {
                String[] qA = queryList.get(i); // get a single array from the queryList
                if (qA.length == 1) {
                    if (!qA[0].equals("*") && !qA[0].equals(jobArray[i])) {
                        return false;
                    }
                } else {
                    boolean doesContain = false;
                    for (String s : qA) {
                        if (s.equals(jobArray[i])) {
                            doesContain = true;
                            break;
                        }
                    }
                    if (!doesContain) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
