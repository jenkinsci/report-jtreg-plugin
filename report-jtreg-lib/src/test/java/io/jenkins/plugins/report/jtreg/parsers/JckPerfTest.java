package io.jenkins.plugins.report.jtreg.parsers;

import org.apache.commons.io.input.ReaderInputStream;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import io.jenkins.plugins.report.jtreg.model.Suite;

import static org.junit.jupiter.api.Assertions.assertTrue;


class JckPerfTest {

    private static final String BIG_OLD_BROKEN_XML_XZ = "big-old-broken.xml.xz";
    private static final String BIG_NEW_FIXED_XML_XZ="big-new-fixed.xml.xz";

    @Test
    void xpathSpeedTestOldBroken() {
        xpathSpeedTest(BIG_OLD_BROKEN_XML_XZ);
    }

    @Test
    void xpathSpeedTestNewFixed() {
        xpathSpeedTest(BIG_NEW_FIXED_XML_XZ);
    }

    @Test
    void ourSuiteSpeedTestOldBroken() {
        ourSuiteSpeedTest(BIG_OLD_BROKEN_XML_XZ);
    }

    @Test
    void ourSuiteSpeedTestNewFixed() {
        ourSuiteSpeedTest(BIG_NEW_FIXED_XML_XZ);
    }

    private void xpathSpeedTest(String file) {
        long duration1 = measureXpath(file, false);
        long duration2 = measureXpath(file, true);
        assertTrue(
                duration2 < duration1 * 2,
                "Is performance of fixstream - " + (duration2 / 1000) + ", not twice as worse as normal - " + (duration1 / 1000));
        System.err.println("Congratulation, our impl is just " + ((float)duration2/(float)duration1) + "x worse thanx to the disc caching");
    }

    private long measureXpath(String file, boolean fixStream) {
        long start = System.currentTimeMillis();
        Document document = createDocument(file, fixStream);
        int statuses = getStatuses("//Report/TestResults/TestResult/@status", document);
        long duration = System.currentTimeMillis() - start;
        assertTrue(statuses > 64700);
        return duration;
    }

    private static long measureOurSuite(String file, boolean fixing) {
        long start1 = System.currentTimeMillis();
        JckReportParser jck1 = new JckReportParser(fixing);
        Suite report1 = jck1.parsePath(new File(System.getProperty("user.dir") + "/src/test/resources/"+file).toPath());
        int tests1 = report1.getReport().getTestsTotal();
        long duration1 = System.currentTimeMillis() - start1;
        assertTrue(tests1 > 64700);
        return duration1;
    }

    private static void ourSuiteSpeedTest(String file) {
        long duration1 = measureOurSuite(file, false);
        long duration2 = measureOurSuite(file, true);
        assertTrue(
                duration2 < duration1 * 2,
                "Is performance of fixstream - " + (duration2 / 1000) + ", not twice as worse as normal - " + (duration1 / 1000));
        System.err.println("Congratulation, our impl is just " + ((float)duration2/(float)duration1) + "x worse thanx to the disc caching");
    }

    private static int getStatuses(String xPathExpr, Document document) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            Object raw =  xPath.evaluate(xPathExpr, document, XPathConstants.NODESET);
            NodeList rraw = (NodeList) raw;
            return rraw.getLength();
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    private Document createDocument(String xmlFileName, boolean fixStream) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            BufferedReader reader;
            if (fixStream) {
                reader = new BufferedReader(new JckReportParser.FixingReader(
                        new org.tukaani.xz.XZInputStream(this.getClass().getResourceAsStream("/" + xmlFileName)), "UTF-8"));
            } else {
                reader = new BufferedReader(
                        new InputStreamReader(new org.tukaani.xz.XZInputStream(this.getClass().getResourceAsStream("/" + xmlFileName)),
                                StandardCharsets.UTF_8));
            }
            return documentBuilder.parse(new ReaderInputStream(reader, StandardCharsets.UTF_8));
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }



}
