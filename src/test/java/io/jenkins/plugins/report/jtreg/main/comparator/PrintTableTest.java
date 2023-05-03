package io.jenkins.plugins.report.jtreg.main.comparator;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class PrintTableTest {
    private static final PrintStream originalStream = System.out;
    private ByteArrayOutputStream outStream;

    @BeforeEach
    public void setOutputStream() {
        outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));
    }

    @Test
    public void testTablePrinting() {
        String[][] table = {
                {null, "first item", "second item", "third item"},
                {"second row", "X", "this is a very long text in a center cell of a table", "X"},
                {"third row", null, "", "X"},
                {"fourth row", "X", "X", "X", "this is out of range and it won't be shown"}
        };

        PrintTable.print(table, 4, 4);

        Assertions.assertEquals("1) first item\n" +
                "2) second item\n" +
                "3) third item\n" +
                "           | 1 | 2                                                    | 3 | \n" +
                "second row | X | this is a very long text in a center cell of a table | X | \n" +
                "third row  |   |                                                      | X | \n" +
                "fourth row | X | X                                                    | X | \n", outStream.toString());
    }

    @AfterAll
    public static void resetOutputStream() {
        System.setOut(originalStream);
    }
}
