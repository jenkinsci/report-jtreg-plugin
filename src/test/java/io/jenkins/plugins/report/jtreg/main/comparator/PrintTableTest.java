package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.main.comparator.formatters.Formatters;
import io.jenkins.plugins.report.jtreg.main.comparator.formatters.HtmlFormatter;
import io.jenkins.plugins.report.jtreg.main.comparator.formatters.PlainFormatter;
import io.jenkins.plugins.report.jtreg.main.comparator.formatters.ColorFormatter;
import org.junit.jupiter.api.*;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class PrintTableTest {
    private static String[][] table;

    @BeforeEach
    public void updateTable() {
        table = new String[][] {
                {null, "first item", "second item", "third item"},
                {"second row", "X", "this is a very long text in a center cell of a table", "X"},
                {"third row", null, "", "X"},
                {"fourth row", "X", "X", "X", "this is out of range and it won't be shown"}
        };
    }

    @Test
    public void testPlainTablePrinting() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outStream);
        Formatters formatter = new PlainFormatter(printStream);

        String[][] newTable = table;

        formatter.printTable(newTable, 4, 4);

        Assertions.assertEquals("1) first item\n" +
                "2) second item\n" +
                "3) third item\n" +
                "           | 1 | 2                                                    | 3 | \n" +
                "second row | X | this is a very long text in a center cell of a table | X | \n" +
                "third row  |   |                                                      | X | \n" +
                "fourth row | X | X                                                    | X | \n", outStream.toString());
    }

    @Test
    public void testColorTablePrinting() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outStream);
        Formatters formatter = new ColorFormatter(printStream);

        formatter.printTable(table, 4, 4);

        Assertions.assertEquals("\u001B[1m1) \u001B[0mfirst item\n" +
                "\u001B[1m2) \u001B[0msecond item\n" +
                "\u001B[1m3) \u001B[0mthird item\n" +
                "           | \u001B[1m1\u001B[0m | \u001B[1m2\u001B[0m                                                    | \u001B[1m3\u001B[0m | \n" +
                "second row | \u001B[31mX\u001B[0m | this is a very long text in a center cell of a table | \u001B[31mX\u001B[0m | \n" +
                "third row  |   |                                                      | \u001B[31mX\u001B[0m | \n" +
                "fourth row | \u001B[31mX\u001B[0m | \u001B[31mX\u001B[0m                                                    | \u001B[31mX\u001B[0m | \n", outStream.toString());
    }

    @Test
    public void testHtmlTablePrinting() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outStream);
        Formatters formatter = new HtmlFormatter(printStream);

        formatter.printTable(table, 4, 4);

        Assertions.assertEquals("<style>table, td { border: 1px solid black; border-collapse: collapse; padding: 0.5em; }</style>\n" +
                "<ul>\n" +
                "<li><b>1:</b> first item</li>\n" +
                "<li><b>2:</b> second item</li>\n" +
                "<li><b>3:</b> third item</li>\n" +
                "</ul>\n" +
                "<table>\n" +
                "<tr>\n" +
                "<td></td>\n" +
                "<td><b>1</b></td>\n" +
                "<td><b>2</b></td>\n" +
                "<td><b>3</b></td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td>second row</td>\n" +
                "<td style=\"color:Red\">X</td>\n" +
                "<td>this is a very long text in a center cell of a table</td>\n" +
                "<td style=\"color:Red\">X</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td>third row</td>\n" +
                "<td></td>\n" +
                "<td></td>\n" +
                "<td style=\"color:Red\">X</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td>fourth row</td>\n" +
                "<td style=\"color:Red\">X</td>\n" +
                "<td style=\"color:Red\">X</td>\n" +
                "<td style=\"color:Red\">X</td>\n" +
                "</tr>\n" +
                "</table>\n", outStream.toString());
    }
}
