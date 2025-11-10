package io.jenkins.plugins.report.jtreg.main.web;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ContextExecutingHandlerTest {

    @Test
    void checkForBedCharsKeywordTest() throws Exception {
        for (String keyword : new String[]{"exec", "eval"}) {
            assertTrue(ContextExecutingHandler.checkForBedChars(List.of(keyword), null));
            assertTrue(ContextExecutingHandler.checkForBedChars(List.of("'" + keyword + "'"), null));
            assertTrue(ContextExecutingHandler.checkForBedChars(List.of("\"" + keyword + "\""), null));
        }
    }

    @Test
    void checkForBedCharsCharsTest() throws Exception {
        for (char ch : new char[]{'|', '&', ';', '>', '<'}) {
            assertTrue(ContextExecutingHandler.checkForBedChars(List.of("" + ch), null));
            assertTrue(ContextExecutingHandler.checkForBedChars(List.of("''" + ch), null));
            assertTrue(ContextExecutingHandler.checkForBedChars(List.of("\"\"" + ch), null));
            assertFalse(ContextExecutingHandler.checkForBedChars(List.of("\"" + ch + "\""), null));
            assertFalse(ContextExecutingHandler.checkForBedChars(List.of("'" + ch + "'"), null));
            assertFalse(ContextExecutingHandler.checkForBedChars(List.of("\"'" + ch + "\""), null));
            assertFalse(ContextExecutingHandler.checkForBedChars(List.of("'\"" + ch + "'"), null));
        }
    }

    @Test
    void checkForBedNastyEscapedCharsTest() throws Exception {
        for (char ch : new char[]{'|', '&', ';', '>', '<'}) {
            assertTrue(ContextExecutingHandler.checkForBedChars(List.of("\"\\\"\"" + ch), null));
            assertTrue(ContextExecutingHandler.checkForBedChars(List.of("\"\\\"\"" + ch + "echo ahoj"), null));
        }
    }

    @Test
    void checkForBedEscapedCharsTest() throws Exception {
        for (char ch : new char[]{'|', '&', ';', '>', '<'}) {
            //    \" escaped "
            assertTrue(ContextExecutingHandler.checkForBedChars(List.of("\\\"" + ch), null));
            //    \' escaped '
            assertTrue(ContextExecutingHandler.checkForBedChars(List.of("\\'" + ch), null));
            //    \\" escaped \, thus valid "
            assertFalse(ContextExecutingHandler.checkForBedChars(List.of("\\\\\"" + ch), null));
            //    \\' \, thus valid '
            assertFalse(ContextExecutingHandler.checkForBedChars(List.of("\\\\'" + ch), null));

            //    \\\" escaped \ + escaped "
            assertTrue(ContextExecutingHandler.checkForBedChars(List.of("\\\\\\\"" + ch), null));
            //    \\\' escaped \ + escaped  '
            assertTrue(ContextExecutingHandler.checkForBedChars(List.of("\\\\\\'" + ch), null));
            //    \\\\" 2x escaped \, thus valid "
            assertFalse(ContextExecutingHandler.checkForBedChars(List.of("\\\\\\\\\"" + ch), null));
            //    \\\\' 2x escaped \, thus valid '
            assertFalse(ContextExecutingHandler.checkForBedChars(List.of("\\\\\\\\'" + ch), null));
        }
    }

    @Test
    void checkForGoodCharsCharsTest() throws Exception {
        for (char ch : new char[]{'*', '?', '.', 'a', 'b'}) {
            assertFalse(ContextExecutingHandler.checkForBedChars(List.of("" + ch), null));
            assertFalse(ContextExecutingHandler.checkForBedChars(List.of("''" + ch), null));
            assertFalse(ContextExecutingHandler.checkForBedChars(List.of("\"\"" + ch), null));
            assertFalse(ContextExecutingHandler.checkForBedChars(List.of("\"" + ch + "\""), null));
            assertFalse(ContextExecutingHandler.checkForBedChars(List.of("'" + ch + "'"), null));
            assertFalse(ContextExecutingHandler.checkForBedChars(List.of("\"'" + ch + "\""), null));
            assertFalse(ContextExecutingHandler.checkForBedChars(List.of("'\"" + ch + "'"), null));
        }
    }

    @Test
    void checkForBadCharsRealLifeTest() throws Exception {
        assertTrue(ContextExecutingHandler.checkForBedChars(
                Arrays.asList("--compare", "--only-volatile", "true", "--formatting", "html", "--history", "1", "--force", "--regex",
                        "\\\".*;echo ahoj;\\\""), null));
        assertTrue(ContextExecutingHandler.checkForBedChars(
                Arrays.asList("--compare", "--only-volatile", "true", "--formatting", "html", "--history", "1", "--regex", "\"\\\"\"",
                        "--virtual", "--force;echo ahoj"), null));
        assertTrue(ContextExecutingHandler.checkForBedChars(
                Arrays.asList("--compare", "--only-volatile", "true", "--formatting", "html", "--history", "1", "--regex", "\"\\\"\"",
                        "--virtual", "--force;echo", "ahoj"), null));
        assertFalse(ContextExecutingHandler.checkForBedChars(
                Arrays.asList("--compare", "--only-volatile", "true", "--formatting", "html", "--history", "1", "--force", "--regex",
                        "\".*;echo ahoj;\""), null));
    }

    @Test
    void checkForBadCharsRealLifeNastyPinnedTest() throws Exception {
        assertTrue(ContextExecutingHandler.checkForBedChars(List.of("\"\\\"\";echo"), null));
    }

    @Test
    void checkForBadCharsRealLifeNastyTest() throws Exception {
        assertTrue(ContextExecutingHandler.checkForBedChars(List.of("\"\\\"\";echo"), null));
        assertTrue(ContextExecutingHandler.checkForBedChars(
                Arrays.asList("--compare", "--only-volatile", "true", "--formatting", "html", "--history", "1", "--regex",
                        "\"\\\"\";echo", "ahoj"), null));
        assertTrue(ContextExecutingHandler.checkForBedChars(
                Arrays.asList("--compare", "--only-volatile", "true", "--formatting", "html", "--history", "1", "--regex",
                        "\"\\\"\";echo ahoj"), null));
    }
}
