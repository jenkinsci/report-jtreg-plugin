/*
 * The MIT License
 *
 * Copyright 2015-2023 report-jtreg plugin contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, mergeOutputs, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.jenkins.plugins.report.jtreg.parsers;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Builds are compared by suite names, so the naming must stay stable and must not depend on the storage format.
 */
class JtregReportParserSuiteNameTest {

    private static String name(String file) {
        return JtregReportParser.suiteName(Paths.get(file));
    }

    @ParameterizedTest
    @ValueSource(strings = {"jdk.zip", "jdk.tar", "jdk.tar.gz", "jdk.tar.bz2", "jdk.tar.xz", "jdk.xml", "jdk.jtr.xml"})
    void everySupportedFormatGivesSameName(String file) {
        assertEquals("jdk", name(file));
    }

    @ParameterizedTest
    @ValueSource(strings = {"jdk.ZIP", "jdk.TAR", "jdk.Tar.Gz", "jdk.TAR.BZ2", "jdk.TAR.XZ", "jdk.XML", "jdk.JTR.XML", "jdk.Jtr.Xml"})
    void suffixIsCaseInsensitive(String file) {
        assertEquals("jdk", name(file));
    }

    @ParameterizedTest
    @CsvSource({
            "JDK.tar.xz, JDK",
            "HotSpot.xml, HotSpot",
            "LangTools.jtr.xml, LangTools"
    })
    void caseOfTheNameItselfIsKept(String file, String expected) {
        assertEquals(expected, name(file));
    }

    @ParameterizedTest
    @CsvSource({
            "jdk.hotspot.tar.xz, jdk.hotspot",
            "tier1.jdk_core.xml, tier1.jdk_core",
            "tier1.jdk_core.jtr.xml, tier1.jdk_core",
            "jdk-25.0.1+8.zip, jdk-25.0.1+8",
            "my-report.tar.gz, my-report"
    })
    void onlyTheSuffixIsStripped(String file, String expected) {
        assertEquals(expected, name(file));
    }

    @ParameterizedTest
    @CsvSource({
            // ".jtr.xml" is stripped as a whole, but only as a real suffix
            "jtr.xml, jtr",
            "myjtr.xml, myjtr",
            "jdk.jtr.xml.xml, jdk.jtr.xml",
            "jdk.xml.tar.xz, jdk.xml",
            "jdk.tar.xz.xml, jdk.tar.xz",
            // only the outermost suffix is removed
            "jdk.tar.tar, jdk.tar",
            "jdk.zip.zip, jdk.zip"
    })
    void suffixLookalikes(String file, String expected) {
        assertEquals(expected, name(file));
    }

    @ParameterizedTest
    @CsvSource({
            "/var/lib/jenkins/jobs/x/builds/5/jdk.tar.xz, jdk",
            "relative/dir/hotspot.xml, hotspot",
            "dir.with.dots.tar.xz/langtools.jtr.xml, langtools",
            "dir.xml/jdk.tar.gz, jdk"
    })
    void directoriesDoNotMatter(String file, String expected) {
        assertEquals(expected, name(file));
    }

    @ParameterizedTest
    @ValueSource(strings = {"jdk.txt", "jdk.xz", "jdk.gz", "jdk.bz2", "jdk.tgz", "jdk", "jdk.xmlx", "jdk.zipx"})
    void unsupportedFormatKeepsFullName(String file) {
        assertEquals(file, name(file));
    }

    @ParameterizedTest
    @ValueSource(strings = {".xml", ".jtr.xml", ".zip", ".tar", ".tar.gz", ".tar.bz2", ".tar.xz", ".XML"})
    void nameIsNeverEmpty(String file) {
        assertEquals(file, name(file));
    }

    @Test
    void rootPathDoesNotFail() {
        Path root = Paths.get("/");
        assertEquals(root.toString(), JtregReportParser.suiteName(root));
    }
}
