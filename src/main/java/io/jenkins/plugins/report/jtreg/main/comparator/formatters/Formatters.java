package io.jenkins.plugins.report.jtreg.main.comparator.formatters;

public interface Formatters {
    public void print(String s);
    public void println(String s);
    public void printBold(String s);
    public void printItalics(String s);
    public void printRed(String s);
    public void printYellow(String s);
    public void printBlue(String s);
    public void printGreen(String s);
    void printTable(String[][] table, int rowSize, int columnSize);
}
