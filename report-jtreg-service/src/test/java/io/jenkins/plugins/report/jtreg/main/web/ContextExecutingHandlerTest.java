package io.jenkins.plugins.report.jtreg.main.web;

import org.junit.Assert;

import java.io.IOException;
import java.util.Arrays;


public class ContextExecutingHandlerTest {
    @org.junit.Test
    public void checkForBedCharsKeywordTest() throws IOException {
        Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("exec"), null));
        Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("eval"), null));
        Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("'exec'"), null));
        Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("'eval'"), null));
        Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("\"exec\""), null));
        Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("\"eval\""), null));
    }

    @org.junit.Test
    public void checkForBedCharsCharsTest() throws IOException {
        Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("|"), null));
        Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("&"), null));
        Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList(";"), null));

        Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("''|"), null));
        Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("''&"), null));
        Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("'';"), null));

        Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("\"\"|"), null));
        Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("\"\"&"), null));
        Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("\"\";"), null));

        Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("\"|\""), null));
        Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("\"&\""), null));
        Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("\";\""), null));
        Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("'|'"), null));
        Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("'&'"), null));
        Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("';'"), null));

        Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("\"'|\""), null));
        Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("\"'&\""), null));
        Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("\"';\""), null));
        Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("'\"|'"), null));
        Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("'\"&'"), null));
        Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("'\";'"), null));
    }
}
