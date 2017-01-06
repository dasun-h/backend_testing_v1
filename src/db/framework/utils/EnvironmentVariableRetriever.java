package db.framework.utils;

import db.framework.exceptions.FrameworkException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * Created by Zone on 7/1/2016.
 */
public class EnvironmentVariableRetriever {

    public static String BROWSER;
    public static String SAUCE_USER;
    public static String SAUCE_KEY;
    public static String OPERATING_SYSTEM;
    public static String SCENARIOS;
    public static String TAGS;
    public static String WEBSITE;

    static {
        try {
            try {
                loadApplicationXmlProperties();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        } catch (FrameworkException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loading xml values to global variables.
     */
    public static void loadApplicationXmlProperties() throws FrameworkException, IOException, SAXException, ParserConfigurationException {
        File fXmlFile = new File("src/db/shared/resources/data/environment_variables.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        doc.getDocumentElement().normalize();
        System.out.println("--------------------------------------------");
        System.out.println(doc.getDocumentElement().getNodeName() + " Selected are:");
        System.out.println("--------------------------------------------");
        NodeList nList = doc.getElementsByTagName("environment_variables");

        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) nNode;
                SAUCE_USER = eElement.getElementsByTagName("sauce_user").item(0).getTextContent();
                SAUCE_KEY = eElement.getElementsByTagName("sauce_key").item(0).getTextContent();
                OPERATING_SYSTEM = eElement.getElementsByTagName("os").item(0).getTextContent();
                SCENARIOS = eElement.getElementsByTagName("scenarios").item(0).getTextContent();
                TAGS = eElement.getElementsByTagName("tags").item(0).getTextContent();
                WEBSITE = eElement.getElementsByTagName("website").item(0).getTextContent();
                BROWSER = eElement.getElementsByTagName("browser").item(0).getTextContent();
            }
        }
    }
}
