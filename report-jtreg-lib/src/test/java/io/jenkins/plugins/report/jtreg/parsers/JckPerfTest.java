package io.jenkins.plugins.report.jtreg.parsers;

import org.apache.commons.io.input.ReaderInputStream;
import org.junit.Assert;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;


public class JckPerfTest {

    @org.junit.Test
    public void xpathSpeedTest() {
        //Document document = createDocument("big-new-fixed.xz");
        Document document = createDocument("big-old-broken.xz");
        int statuses = getTSatuses("//Report/TestResults/TestResult/@status", document);
        Assert.assertTrue(statuses > 64700);
    }

    private int getTSatuses(String xPathExpr, Document document) {
        String content;
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            Object raw =  xPath.evaluate(xPathExpr, document, XPathConstants.NODESET);
            NodeList rraw = (NodeList) raw;
            return rraw.getLength();
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }
        private Document createDocument(String xmlFileName) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            //BufferedReader reader = new BufferedReader(new JckReportParser.FixingReader(new  org.tukaani.xz.XZInputStream(this.getClass().getResourceAsStream("/" + xmlFileName)), "UTF-8"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(new  org.tukaani.xz.XZInputStream(this.getClass().getResourceAsStream("/" + xmlFileName)), "UTF-8"));
            return documentBuilder.parse(new ReaderInputStream(reader, StandardCharsets.UTF_8));
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }



}
