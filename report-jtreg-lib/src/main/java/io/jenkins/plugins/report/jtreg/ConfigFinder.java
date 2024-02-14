package io.jenkins.plugins.report.jtreg;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
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
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigFinder {
    private static final Map<File, Map<String, String>> configCache = new HashMap<>();

    private final File configFile;
    private final String whatToFind;
    private final String findQuery;

    public ConfigFinder(File configFile, String whatToFind, String findQuery) {
        this.configFile = configFile;
        this.whatToFind = whatToFind;
        this.findQuery = findQuery;
    }

    public String findInConfig() {
        return findInConfigStatic(configFile, whatToFind, findQuery);
    }

    public static String findInConfigStatic(File configFile, String whatToFind, String findQuery) {
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
        } else if (configFile.getName().endsWith("json")) {
            value = findInJson(configFile, findQuery);
        } else {
            value = findInProperties(configFile, findQuery);
        }

        // puts the value to the cache if not null
        if (value != null) {
            if (cachedMap == null) {
                cachedMap = new HashMap<>();
            }
            cachedMap.put(whatToFind, value);
            configCache.put(configFile, cachedMap);
        }

        return value;
    }

    private static String findInXml(File configFile, String xpath) {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document doc = builder.parse(configFile);

            XPath xPath = XPathFactory.newInstance().newXPath();
            Node node = (Node) xPath.compile(xpath).evaluate(doc, XPathConstants.NODE);

            if (node != null) {
                return node.getFirstChild().getNodeValue();
            } else {
                // warn the user that the value was not found and then return null
                System.err.println("Warning, the value defined by " + xpath + " in file " + configFile.getAbsolutePath() +
                        " does not exist, returning null.");
                return null;
            }
        } catch (ParserConfigurationException | XPathExpressionException | IOException | SAXException e) {
            // warn the user that an exception was thrown, print its stack trace and return null
            System.err.println("Warning, an exception was thrown when looking for a value defined by " + xpath + " in file " +
                    configFile.getAbsolutePath() + ", returning null.");
            e.printStackTrace();
            return null;
        }
    }

    private static String findInProperties(File configFile, String key) {
        try (FileReader configReader = new FileReader(configFile, StandardCharsets.UTF_8)) {
            Properties properties = new Properties();
            properties.load(configReader);

            return properties.getProperty(key);
        } catch (IOException e) {
            System.err.println("Warning, an exception was thrown when looking for a value defined by " + key + " in file " +
                    configFile.getAbsolutePath() + ", returning null.");
            e.printStackTrace();
            return null;
        }
    }

    private static String findInJson(File configFile, String jsonQuery) {
        String[] strings = jsonQuery.split("\\."); // split the parsing string on dots
        try {
            InputStream stream = new FileInputStream(configFile);
            String jsonString = IOUtils.toString(stream, StandardCharsets.UTF_8);
            JsonObject current = new Gson().fromJson(jsonString, JsonObject.class);
            JsonElement je = null;

            // go through all the parts (separated from the json query by dots)
            for (String part : strings) {
                if (je != null) {
                    current = je.getAsJsonObject();
                }

                if (part.equals("$") && Arrays.asList(strings).indexOf(part) == 0) {
                    // if the first is $, ignore it
                    continue;
                } else if (part.matches(".*\\[[0-9]*]")) {
                    // if there is an array in the json query, get the right element
                    String[] split = part.split("\\[");
                    int num = Integer.parseInt(split[1].split("]")[0]);

                    // check if it is not null
                    if (current.get(split[0]) != null) {
                        try {
                            je = current.get(split[0]).getAsJsonArray().get(num);
                        } catch (IllegalStateException e) {
                            // the element is not an array
                            System.err.println("Warning, an exception was thrown when looking for a value defined by " + jsonQuery + " in file " +
                                    configFile.getAbsolutePath() + ", returning null.");
                            e.printStackTrace();
                            return null;
                        }

                    } else {
                        System.err.println("Warning, the value defined by " + jsonQuery + " in file " + configFile.getAbsolutePath() +
                                " does not exist, returning null.");
                        return null;
                    }
                } else {
                    // get the corresponding part of the json
                    je = current.get(part);
                }
            }

            if (je == null) {
                System.err.println("Warning, the value defined by " + jsonQuery + " in file " + configFile.getAbsolutePath() +
                        " does not exist, returning null.");
                return null;
            } else {
                try {
                    // try to get the value as string
                    return je.getAsString();
                } catch (Exception e) {
                    System.err.println("Warning, an exception was thrown when looking for a value defined by " + jsonQuery + " in file " +
                            configFile.getAbsolutePath() + ", returning null.");
                    e.printStackTrace();
                    return null;
                }
            }
        } catch (IOException e) {
            System.err.println("Warning, an exception was thrown when looking for a value defined by " + jsonQuery + " in file " +
                    configFile.getAbsolutePath() + ", returning null.");
            e.printStackTrace();
            return null;
        }
    }
}
