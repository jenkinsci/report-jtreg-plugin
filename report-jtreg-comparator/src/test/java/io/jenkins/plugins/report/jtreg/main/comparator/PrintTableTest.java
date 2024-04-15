package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.formatters.ColorFormatter;
import io.jenkins.plugins.report.jtreg.formatters.Formatter;
import io.jenkins.plugins.report.jtreg.formatters.HtmlFormatter;
import io.jenkins.plugins.report.jtreg.formatters.JtregPluginServicesCell;
import io.jenkins.plugins.report.jtreg.formatters.JtregPluginServicesLinkWithTooltip;
import io.jenkins.plugins.report.jtreg.formatters.PlainFormatter;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class PrintTableTest {

    public JtregPluginServicesCell[][] updateTable(Formatter pf) {
        return new JtregPluginServicesCell[][]{
                {create(pf, null),         create(pf, "first item"), create(pf, "second item"), create(pf, "third item")},
                {create(pf, "second row"), create(pf, "X"),          create(pf, "this is a very long text in a center cell of a table"), create(pf, "X")},
                {create(pf, "third row"),  create(pf, null),         create(pf, ""), create(pf, "X")},
                {create(pf, "fourth row"), create(pf, "X"),          create(pf, "X"), create(pf, "X"), create(pf, "this is out of range and it won't be shown")}
        };
    }

    private static JtregPluginServicesCell create(Formatter pf, String content) {
        return pf.createCell(new JtregPluginServicesLinkWithTooltip(content));
    }

    private String crlfToLf(String s) {
        // replaces windows CRLF newline to unix LF newline
        // needed for the tests to pass both on linux and windows
        return s.replace("\r\n", "\n");
    }

    @Test
    public void testPlainTablePrinting() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outStream);
        Formatter f = new PlainFormatter(printStream);

        JtregPluginServicesCell[][] table = updateTable(f);

        f.printTable(table, 4, 4);

        Assertions.assertEquals("1) first item\n" +
                        "2) second item\n" +
                        "3) third item\n" +
                        "           | 1 | 2                                                    | 3 | \n" +
                        "second row | X | this is a very long text in a center cell of a table | X | \n" +
                        "third row  |   |                                                      | X | \n" +
                        "fourth row | X | X                                                    | X | \n",
                crlfToLf(outStream.toString()));
    }

    @Test
    public void testColorTablePrinting() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outStream);
        Formatter f = new ColorFormatter(printStream);
        JtregPluginServicesCell[][] table = updateTable(f);
        f.printTable(table, 4, 4);

        Assertions.assertEquals("\u001B[1m1) \u001B[0mfirst item\n" +
                        "\u001B[1m2) \u001B[0msecond item\n" +
                        "\u001B[1m3) \u001B[0mthird item\n" +
                        "           | \u001B[1m1\u001B[0m | \u001B[1m2\u001B[0m                                                    | \u001B[1m3\u001B[0m | \n" +
                        "second row | \u001B[31mX\u001B[0m | this is a very long text in a center cell of a table | \u001B[31mX\u001B[0m | \n" +
                        "third row  |   |                                                      | \u001B[31mX\u001B[0m | \n" +
                        "fourth row | \u001B[31mX\u001B[0m | \u001B[31mX\u001B[0m                                                    | \u001B[31mX\u001B[0m | \n",
                crlfToLf(outStream.toString()));
    }

    @Test
    public void testHtmlTablePrinting() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outStream);
        Formatter f = new HtmlFormatter(printStream);
        JtregPluginServicesCell[][] table = updateTable(f);
        f.printTable(table, 4, 4);

        Assertions.assertEquals("<style>\n" +
                ".contents {\n" +
                "    font-family: monospace, monospace;\n" +
                "}\n" +
                "\n" +
                ":target {border-color: #3399FF;border-style: dashed;}\n" +
                ".NameBuildLine{color:Green}\n" +
                ".NameBuildSummary{color:DodgerBlue}\n" +
                ".blk {\n" +
                "    color: Black;\n" +
                "}\n" +
                "\n" +
                "details {\n" +
                "    padding-left: 35px;\n" +
                "}\n" +
                "\n" +
                "table, td {\n" +
                "    border: 1px solid black;\n" +
                "    border-collapse: collapse;\n" +
                "    color: Red;\n" +
                "    padding: 0.5em;\n" +
                "}\n" +
                "</style>\n" +
                "<div class='contents'>\n" +
                "<ul>\n" +
                "<li><b id='legend-1'><a href='#table-1'>1:</a></b> first item</li>\n" +
                "<li><b id='legend-2'><a href='#table-2'>2:</a></b> second item</li>\n" +
                "<li><b id='legend-3'><a href='#table-3'>3:</a></b> third item</li>\n" +
                "</ul>\n" +
                "<button onclick='expandOrCollapse()' style='margin-bottom:25px'>expand / collapse all</button>\n" +
                "<table>\n" +
                "<tr>\n" +
                "<td></td>\n" +
                "<td class='blk'><b id='table-1'><a href='#legend-1'>1</a></b></td>\n" +
                "<td class='blk'><b id='table-2'><a href='#legend-2'>2</a></b></td>\n" +
                "<td class='blk'><b id='table-3'><a href='#legend-3'>3</a></b></td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td class='blk'>second row</td>\n" +
                "<td>X</td>\n" +
                "<td class='blk'>this is a very long text in a center cell of a table</td>\n" +
                "<td>X</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td class='blk'>third row</td>\n" +
                "<td></td>\n" +
                "<td class='blk'></td>\n" +
                "<td>X</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td class='blk'>fourth row</td>\n" +
                "<td>X</td>\n" +
                "<td>X</td>\n" +
                "<td>X</td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</div>\n" +
                "<script>\n" +
                "let opened = false;\n" +
                "\n" +
                "function expandOrCollapse() {\n" +
                "    let detailsElements = document.querySelectorAll('details');\n" +
                "\n" +
                "    detailsElements.forEach((detailsElement) => {\n" +
                "        if (opened) {\n" +
                "            detailsElement.removeAttribute('open');\n" +
                "        } else {\n" +
                "            detailsElement.setAttribute('open', '');\n" +
                "        }\n" +
                "    });\n" +
                "\n" +
                "    opened = !opened;\n" +
                "}\n" +
                "</script>\n",
                crlfToLf(outStream.toString()));
    }
}
