package io.jenkins.plugins.report.jtreg.main.comparator;

import java.util.Arrays;

public class PrintTable {
    public static void print(String[][] table, int rowSize, int columnSize) {
        // first print the first row definitions
        for (int i = 1; i < table[0].length; i++) {
            System.out.println(i + ") " + table[0][i]);
            table[0][i] = Integer.toString(i); // replace the item with its definition (number)
        }

        // get the length of the longest item in each column
        int[] lengths = new int[table[0].length];
        Arrays.fill(lengths, 0);
        for (int i = 0; i < rowSize; i++) {
            for (int j = 0; j < columnSize; j++) {
                if (table[i][j] != null && table[i][j].length() > lengths[j]) {
                    lengths[j] = table[i][j].length();
                }
            }
        }

        // print the table
        for (int i = 0; i < rowSize; i++) {
            for (int j = 0; j < columnSize; j++) {
                int len = 0;
                if (table[i][j] != null) {
                    System.out.print(table[i][j] + " ");
                    len = table[i][j].length();
                } else {
                    System.out.print(" ");
                }
                // printing spaces to make the table look better
                for (int k = len; k < lengths[j]; k++) {
                    System.out.print(" ");
                }
                System.out.print("| ");
            }
            System.out.print("\n");
        }
    }
}
