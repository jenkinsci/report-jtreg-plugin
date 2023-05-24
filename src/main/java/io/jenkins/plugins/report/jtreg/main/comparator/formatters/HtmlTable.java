package io.jenkins.plugins.report.jtreg.main.comparator.formatters;


import java.io.PrintStream;

public class HtmlTable implements TablePrinter {
    private final PrintStream ps;
    private static final String RED = "Red";
    private static final String YELLOW = "Yellow";
    private static final String BLUE = "Blue";
    private static final String GREEN = "Green";

    public HtmlTable(PrintStream printStream) {
        this.ps = printStream;
    }

    public void printTable(String[][] table, int rowSize, int columnSize) {
        ps.println("<style>table, td { border: 1px solid black; border-collapse: collapse; padding: 0.5em; }</style>");

        // first print the first row definitions
        ps.println("<ul>");
        for (int i = 1; i < table[0].length; i++) {
            ps.println("<li><b>" + i + ":</b> " + table[0][i] + "</li>");
            table[0][i] = "<b>" + i + "</b>"; // replace the item with its definition (number)
        }
        ps.println("</ul>");

        // print the table
        ps.println("<table>");
        for (int i = 0; i < rowSize; i++) {
            ps.println("<tr>");
            for (int j = 0; j < columnSize; j++) {
                if (table[i][j] != null) {
                    if (table[i][j].equals("X")) {
                        ps.println("<td style=\"color:" + RED + "\">" + table[i][j] + "</td>");
                    } else {
                        ps.println("<td>" + table[i][j] + "</td>");
                    }
                } else {
                    ps.println("<td></td>");
                }
            }
            ps.println("</tr>");
        }
        ps.println("</table>");
    }
}
