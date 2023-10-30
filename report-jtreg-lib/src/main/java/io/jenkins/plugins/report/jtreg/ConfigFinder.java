package io.jenkins.plugins.report.jtreg;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigFinder {
    private static final Map<File, Map<String, String>> configCache = new HashMap<>();

    public static String findInConfig(File configFile, String whatToFind, String findQuery) {
        // checks if the file/items in the file are already cached
        Map<String, String> cachedMap = configCache.get(configFile);
        if (cachedMap != null) {
            String cachedValue = cachedMap.get(whatToFind);
            if (cachedValue != null) {
                return cachedValue;
            }
        }

        // checks for file extension
        String value;
        if (configFile.getName().endsWith("xml")) {
            value = findInXml(configFile, findQuery);
        } else {
            throw new RuntimeException("Unsupported config file type.");
        }

        // puts the value to the cache
        Map<String, String> newMap = new HashMap<>();
        newMap.put(whatToFind, value);
        configCache.put(configFile, newMap);

        return value;
    }

    private static String findInXml(File configFile, String xpath) {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document doc = builder.parse(configFile);

            XPath xPath = XPathFactory.newInstance().newXPath();
            Node node = (Node) xPath.compile(xpath).evaluate(doc, XPathConstants.NODE);

            return node.getFirstChild().getNodeValue();
        } catch (ParserConfigurationException | XPathExpressionException | IOException | SAXException e) {
            return null;
        }
    }
}
