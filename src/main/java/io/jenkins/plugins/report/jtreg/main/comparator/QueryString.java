package io.jenkins.plugins.report.jtreg.main.comparator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class QueryString {
    private static ArrayList<String[]> cachedQueryList = null;

    // parses the given query string into a list of string arrays
    private static ArrayList<String[]> parseToList(String queryString) {
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
    static boolean checkJobWithQuery(File job, String queryString) {
        if (!queryString.equals("")) {
            String[] jobArray = job.getName().split("[.-]");
            // caching the queryList to not call parseToList() every time
            if (cachedQueryList == null) {
                cachedQueryList = parseToList(queryString);
            }

            for (int i = 0; i < cachedQueryList.size(); i++) {
                String[] qA = cachedQueryList.get(i); // get a single array from the queryList

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
        }
        return true;
    }
}
