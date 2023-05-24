package io.jenkins.plugins.report.jtreg.main.comparator.formatters;

public interface TablePrinter {
    void printTable(String[][] table, int rowSize, int columnSize);
}
