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
    public void checkForBedNastyEscapedCharsTest() throws IOException {
        for (char ch : new char[]{'|', '&', ';', '>', '<'}) {
            Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("\"\\\"\"" + ch), null));
            Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("\"\\\"\"" + ch + "echo ahoj"), null));
        }
    }

    @org.junit.Test
    public void checkForBedEscapedCharsTest() throws IOException {
        for (char ch : new char[]{'|', '&', ';', '>', '<'}) {
            //    \" escaped "
            Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("\\\"" + ch), null));
            //    \' escaped '
            Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("\\'" + ch), null));
            //    \\" escaped \, thus valid "
            Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("\\\\\"" + ch), null));
            //    \\' \, thus valid '
            Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("\\\\'" + ch), null));

            //    \\\" escaped \ + escaped "
            Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("\\\\\\\"" + ch), null));
            //    \\\' escaped \ + escaped  '
            Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("\\\\\\'" + ch), null));
            //    \\\\" 2x escaped \, thus valid "
            Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("\\\\\\\\\"" + ch), null));
            //    \\\\' 2x escaped \, thus valid '
            Assert.assertFalse(ContextExecutingHandler.checkForBedChars(Arrays.asList("\\\\\\\\'" + ch), null));
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

    @org.junit.Test
    public void checkForBadCharsRealLifeTest() throws IOException {
        Assert.assertTrue(ContextExecutingHandler.checkForBedChars(
                Arrays.asList("--compare", "--only-volatile", "true", "--formatting", "html", "--history", "1", "--force", "--regex",
                        "\\\".*;echo ahoj;\\\""), null));
        Assert.assertTrue(ContextExecutingHandler.checkForBedChars(
                Arrays.asList("--compare", "--only-volatile", "true", "--formatting", "html", "--history", "1", "--regex", "\"\\\"\"",
                        "--virtual", "--force;echo ahoj"), null));
        Assert.assertTrue(ContextExecutingHandler.checkForBedChars(
                Arrays.asList("--compare", "--only-volatile", "true", "--formatting", "html", "--history", "1", "--regex", "\"\\\"\"",
                        "--virtual", "--force;echo", "ahoj"), null));
        Assert.assertFalse(ContextExecutingHandler.checkForBedChars(
                Arrays.asList("--compare", "--only-volatile", "true", "--formatting", "html", "--history", "1", "--force", "--regex",
                        "\".*;echo ahoj;\""), null));
    }

    @org.junit.Test
    public void checkForBadCharsRealLifeNastyPinnedTest() throws IOException {
        Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("\"\\\"\";echo"), null));
    }

    @org.junit.Test
    public void checkForBadCharsRealLifeNastyTest() throws IOException {
        Assert.assertTrue(ContextExecutingHandler.checkForBedChars(Arrays.asList("\"\\\"\";echo"), null));
        Assert.assertTrue(ContextExecutingHandler.checkForBedChars(
                Arrays.asList("--compare", "--only-volatile", "true", "--formatting", "html", "--history", "1", "--regex",
                        "\"\\\"\";echo", "ahoj"), null));
        Assert.assertTrue(ContextExecutingHandler.checkForBedChars(
                Arrays.asList("--compare", "--only-volatile", "true", "--formatting", "html", "--history", "1", "--regex",
                        "\"\\\"\";echo ahoj"), null));
    }
}
