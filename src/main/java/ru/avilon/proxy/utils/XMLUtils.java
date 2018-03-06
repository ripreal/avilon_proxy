package ru.avilon.proxy.utils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLUtils {

	static DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
	
	public static Element initNewDocumentAndCreateDataElement(Long packageNumber) throws ParserConfigurationException {
		DocumentBuilder icBuilder = icFactory.newDocumentBuilder();
		Document doc = icBuilder.newDocument();
		Element V8Exch = doc.createElement("V8Exch");

		V8Exch.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:V8Exch", "http://www.1c.ru/V8/1CV8DtUD/");
		V8Exch.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:core", "http://v8.1c.ru/data");
		V8Exch.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:v8",
				"http://v8.1c.ru/8.1/data/enterprise/current-config");
		V8Exch.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xs", "http://www.w3.org/2001/XMLSchema");
		V8Exch.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		
		doc.appendChild(V8Exch);
		
//		Element V8Exch_Package = doc.createElement("V8Exch:Package");
//		V8Exch_Package.setTextContent(packageNumber != null ? String.valueOf(packageNumber) : "");
//		V8Exch.appendChild(V8Exch_Package);
		
		Element V8Exch_Data = doc.createElement("V8Exch:Data");
		V8Exch.appendChild(V8Exch_Data); 
		
		return V8Exch_Data;
	}
	
	static  TransformerFactory tf = TransformerFactory.newInstance();
	public static String serializeXml(Node doc) throws TransformerException {
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString();
            return output;
        } catch (TransformerException e) {
            throw e;
        }
	}
	
	public static void setObjectProperty(Document doc, Element v8_CatalogObject_Пользователи, String value, String attrName) {
		Element ref = doc.createElement(attrName);
		ref.setTextContent(value);
		v8_CatalogObject_Пользователи.appendChild(ref);
	}
	
	public static Document getXml(String xmlString) throws ParserConfigurationException, SAXException, IOException {
		  
	    DocumentBuilder builder = icFactory.newDocumentBuilder();  
	    Document document = builder.parse( new InputSource( new StringReader( xmlString ) ) );
	    return document;
	}
	
	public static void appendNodeFromString(String src, Element target) throws ParserConfigurationException, SAXException, IOException {
		Document nodeDoc = getXml(src);
		Node importedNode = target.getOwnerDocument().importNode(nodeDoc.getDocumentElement(), true);
		target.appendChild(importedNode);
	}
	
	//final static Pattern pattern = Pattern.compile("<([xmlns:|xsi:][^>]+)>([^<]*)<\\/[^>]+>");
	final static Pattern pattern = Pattern.compile("<([xmlns:v8][^>]+)>([^<]*)<\\/[^>]+>");

	public static String moveAttributesFromeNodes(String xml) {
		
		System.err.println(xml);
		
		Matcher matcher = pattern.matcher(xml);
		// Check all occurrences
		while (matcher.find()) {

			String attr = matcher.group(1) + "=\"" + matcher.group(2) + "\"";
			StringBuilder sb = new StringBuilder(xml);

			sb.delete(matcher.start(), matcher.end());
			
			if(matcher.group(1).equals("xmlns:v8")) {
				xml = sb.toString();
				matcher = pattern.matcher(xml);
				continue;
			}
			
			sb.insert(matcher.start() - 1, " " + attr);
			xml = sb.toString();
			matcher = pattern.matcher(xml);
		}
		
		return xml;
	}
		
	
	public static void moveNamespaceNodesToAttributes(Node node) {
	    // do something with the current node instead of System.out
		if(node.getNodeName().startsWith("xmlns:") ||node.getNodeName().startsWith("xsi:")) {
			Element parentElement = (Element)node.getParentNode();
			parentElement.setAttribute(node.getNodeName(), node.getTextContent());
			parentElement.removeChild(node);
			moveNamespaceNodesToAttributes(parentElement);
			return;
		}

	    NodeList nodeList = node.getChildNodes();
	    for (int i = 0; i < nodeList.getLength(); i++) {
	        Node currentNode = nodeList.item(i);
	        if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
	            //calls this method for all the children which is Element
	            moveNamespaceNodesToAttributes(currentNode);
	        }
	    }
	}

	
	
	
	
}
