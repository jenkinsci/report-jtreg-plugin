package io.jenkins.plugins.report.jtreg.main.web;

import org.junit.Assert;

import java.io.IOException;
import java.util.Arrays;


public class ContextExecutingHandlerTest {
    @org.junit.Test
    public void checkForBedCharsKeywordTest() throws IOException {
        for (String keyword : new String[]{"exec", "eval"}) {
            Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList(keyword), null));
            Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("'" + keyword + "'"), null));
            Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("\"" + keyword + "\""), null));
        }
    }

    @org.junit.Test
    public void checkForBedCharsCharsTest() throws IOException {
        for (char ch : new char[]{'|', '&', ';', '>', '<'}) {
            Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("" + ch), null));
            Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("''" + ch), null));
            Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("\"\"" + ch), null));
            Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("\"" + ch + "\""), null));
            Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("'" + ch + "'"), null));
            Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("\"'" + ch + "\""), null));
            Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("'\"" + ch + "'"), null));
        }
    }

    @org.junit.Test
    public void checkForGoodCharsCharsTest() throws IOException {
        for (char ch : new char[]{'*', '?', '.', 'a', 'b'}) {
            Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("" + ch), null));
            Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("''" + ch), null));
            Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("\"\"" + ch), null));
            Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("\"" + ch + "\""), null));
            Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("'" + ch + "'"), null));
            Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("\"'" + ch + "\""), null));
            Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("'\"" + ch + "'"), null));
        }
    }
}
