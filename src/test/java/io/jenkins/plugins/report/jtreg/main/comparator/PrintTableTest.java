package io.jenkins.plugins.report.jtreg.main.comparator;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class PrintTableTest {
    @Test
    public void testTablePrinting() {
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        final PrintStream originalStream = System.out;
        System.setOut(new PrintStream(outStream));

        String[][] table = {
                {null, "first item", "second item", "third item"},
                {"second row", "X", "this is a very long text in a center cell of a table", "X"},
                {"third row", null, "", "X"},
                {"fourth row", "X", "X", "X", "this is out of range and it won't be shown"}
        };

        PrintTable.print(table, 4, 4);

        Assert.assertEquals("1) first item\n" +
                "2) second item\n" +
                "3) third item\n" +
                "           | 1 | 2                                                    | 3 | \n" +
                "second row | X | this is a very long text in a center cell of a table | X | \n" +
                "third row  |   |                                                      | X | \n" +
                "fourth row | X | X                                                    | X | \n", outStream.toString());
        System.setOut(originalStream);
    }
}
