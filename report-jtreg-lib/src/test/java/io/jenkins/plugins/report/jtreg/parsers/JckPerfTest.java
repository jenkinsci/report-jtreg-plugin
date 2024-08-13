package io.jenkins.plugins.report.jtreg.parsers;

import io.jenkins.plugins.report.jtreg.model.ReportFull;
import io.jenkins.plugins.report.jtreg.model.Suite;
import io.jenkins.plugins.report.jtreg.model.Test;
import io.jenkins.plugins.report.jtreg.model.TestOutput;
import org.apache.commons.io.input.ReaderInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class JckPerfTest {

    @org.junit.Test
    public void xpathSpeedTest() {
        //Document document = createDocument("big-new-fixed.xz");
        Document document = createDocument("big-old-broken.xz");
        String testResults = getText("//Report/TestResults/TestResult/@status", document);
        System.out.println(testResults);
    }

    private String getText(String xPathExpr, Document document) {
        String content;
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            Object raw =  xPath.evaluate(xPathExpr, document, XPathConstants.NODESET);
            NodeList rraw = (NodeList) raw;
            content = "found statuses: " + (rraw.getLength());
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
        return content.trim();
    }
        private Document createDocument(String xmlFileName) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            BufferedReader reader = new BufferedReader(new JckReportParser.FixingReader(new  org.tukaani.xz.XZInputStream(this.getClass().getResourceAsStream("/" + xmlFileName)), "UTF-8"));
            //BufferedReader reader = new BufferedReader(new InputStreamReader(new  org.tukaani.xz.XZInputStream(this.getClass().getResourceAsStream("/" + xmlFileName)), "UTF-8"));
            reader.readLine();
            return documentBuilder.parse(new ReaderInputStream(reader, StandardCharsets.UTF_8));
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }



}
