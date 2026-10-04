package com.ericsson.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.ericsson.util.ParsingException;

public class XMLParser {

	// private static Logger logger = Logger.getLogger(XMLParser.class);

	/**
	 * XMLParser constructor
	 */
	private String Balance = "";

	public XMLParser() {
	}

	public String GetBalance() {
		return this.Balance;
	}

	/**
	 * Retrieve response string
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 * @throws NumberFormatException
	 * @throws StringIndexOutOfBoundsException
	 */
	public String readResponse(InputStream in)
			throws IOException, NumberFormatException, StringIndexOutOfBoundsException {
		StringBuffer sb = new StringBuffer();
		// Reader reader = new InputStreamReader(in, "UTF-8");
		// Reader reader = new InputStreamReader(in);
		int c;
		while ((c = in.read()) != -1)
			sb.append((char) c);

		String document = sb.toString();
		return document;
	}

	/**
	 * Retrieve tag value through parse response string
	 * 
	 * @param requestStr
	 * @param rootName
	 * @param tagName
	 * @return
	 * @throws ParsingException
	 */
	public Object parse(String requestStr, String rootName, String tagName, int lvl, Logger logger)
			throws ParsingException {
		// logger.debug("XMLParser.parse()");
		logger.log(Level.INFO, "XMLParser.parse()");
		String xml = requestStr;

		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(xml.getBytes());
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(new DefaultHandler() {
				public void fatalError(SAXParseException saxparseexception) throws SAXParseException {
					throw saxparseexception;
				}

				public void error(SAXParseException saxparseexception) throws SAXParseException {
					throw saxparseexception;
				}

				public void warning(SAXParseException saxparseexception) throws SAXParseException {
					throw saxparseexception;
				}
			});
			Document document = builder.parse(bis);
			String data = null;
			Element header = (Element) document.getElementsByTagName(rootName).item(0);
			data = header.getElementsByTagName(tagName).item(lvl).getFirstChild().getNodeValue();
			return data;
		} catch (SAXException saxexception) {
			// logger.error(saxexception.getMessage());
			logger.log(Level.SEVERE, "SAXException thrown. \n" + saxexception.getMessage());
		} catch (IOException ioexception) {
			throw new ParsingException(ioexception.getMessage());
		} catch (FactoryConfigurationError e) {
			// logger.error("Could not locate a JAXP DocumentBuilderFactory class");
			logger.log(Level.SEVERE, "Could not locate a JAXP DocumentBuilderFactory class \n" + e.getMessage());
		} catch (ParserConfigurationException parserconfigurationexception) {
			throw new ParsingException(parserconfigurationexception.getMessage());
		} catch (NullPointerException npe) {
			return "Tag Not Found";
		}
		return null;
	}

	public String parseCode_previous(String xmldata, String parseData, Logger logger) throws ParsingException {
		String responseCode = null;
		String xml = xmldata;

		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(xml.getBytes());
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(new DefaultHandler() {
				public void fatalError(SAXParseException saxparseexception) throws SAXParseException {
					throw saxparseexception;
				}

				public void error(SAXParseException saxparseexception) throws SAXParseException {
					throw saxparseexception;
				}

				public void warning(SAXParseException saxparseexception) throws SAXParseException {
					throw saxparseexception;
				}
			});
			Document document = builder.parse(bis);
			// Element header = (Element)document.getElementsByTagName("methodResponse");
			NodeList sections = document.getElementsByTagName("member");
			// data =
			// header.getElementsByTagName(tagName).item(lvl).getFirstChild().getNodeValue();
			int numSections = sections.getLength();
			for (int i = 0; i < numSections; i++) {
				Element section = (Element) sections.item(i);
				Node title = section.getFirstChild();
				if (title.getTextContent().equalsIgnoreCase(parseData)) {
					Node value = title.getNextSibling();
					// System.out.println(value.getTextContent());
					responseCode = value.getTextContent();
				}
			}
			// System.out.println(data);
		} catch (SAXException saxexception) {
			// logger.error(saxexception.getMessage());
			logger.log(Level.SEVERE, "SAXException thrown. \n" + saxexception.getMessage());
		} catch (IOException ioexception) {
			throw new ParsingException(ioexception.getMessage());
		} catch (FactoryConfigurationError e) {
			// logger.error("Could not locate a JAXP DocumentBuilderFactory class");
			logger.log(Level.SEVERE, "Could not locate a JAXP DocumentBuilderFactory class \n" + e.getMessage());
		} catch (ParserConfigurationException parserconfigurationexception) {
			throw new ParsingException(parserconfigurationexception.getMessage());
		} catch (NullPointerException npe) {
			return "Tag Not Found";
		}

		return responseCode;
	}

	public String parseCode(String xmldata, String parseData, Logger logger) throws ParsingException {
		String value = "", balance = "";

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xmldata));

			Document doc = db.parse(is);
			NodeList nodes = doc.getElementsByTagName("member");
			int count = 0;
			for (int i = 0; i < nodes.getLength(); i++) {
				if (count == 2)
					break;
				Element element = (Element) nodes.item(i);
				String Name = getTextValue(element, "name");
				if (Name.equals(parseData)) {
					value = getvalue(element, "value");
					count++;
				} else if (Name.equals("accountValue1")) {
					balance = getvalue(element, "value");
					this.Balance = balance;
					count++;
				}
			}
			return value;
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Could not parse xml data\n" + ex.getMessage());
			return value;
		}
	}

	public String[] parseFaultCode(String xmldata, String parseData, Logger logger) throws ParsingException {
		String[] responseCode = new String[2];

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xmldata));

			Document doc = db.parse(is);
			NodeList nodes = doc.getElementsByTagName("member");
			int count = 0;
			for (int i = 0; i < nodes.getLength(); i++) {
				if (count == 2)
					break;
				Element element = (Element) nodes.item(i);
				String Name = getTextValue(element, "name");
				if (Name.equals(parseData)) {
					responseCode[count] = getvalue(element, "value");
					count++;
				} else if (Name.equals("faultString")) {
					responseCode[count] = getvalue(element, "value");
					count++;
				}
			}
			return responseCode;
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Could not parse xml data\n" + ex.getMessage());
			return responseCode;
		}
	}

	private String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}
		return textVal;
	}

	private String getvalue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		Element element = (Element) nl.item(0);
		Node temp = element.getFirstChild();
		textVal = getTextValue(element, temp.getNodeName());
		return textVal;
	}

	public String[] parseFaultCode_previous(String xmldata, String parseData, Logger logger) throws ParsingException {
		String[] responseCode = new String[2];
		String xml = xmldata;

		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(xml.getBytes());
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(new DefaultHandler() {
				public void fatalError(SAXParseException saxparseexception) throws SAXParseException {
					throw saxparseexception;
				}

				public void error(SAXParseException saxparseexception) throws SAXParseException {
					throw saxparseexception;
				}

				public void warning(SAXParseException saxparseexception) throws SAXParseException {
					throw saxparseexception;
				}
			});
			Document document = builder.parse(bis);
			// Element header = (Element)document.getElementsByTagName("methodResponse");
			NodeList sections = document.getElementsByTagName("member");
			// data =
			// header.getElementsByTagName(tagName).item(lvl).getFirstChild().getNodeValue();
			int numSections = sections.getLength();
			for (int i = 0; i < numSections; i++) {
				Element section = (Element) sections.item(i);
				Node title = section.getFirstChild();
				if (title.getTextContent().equalsIgnoreCase(parseData)) {
					Node value = title.getNextSibling();
					// System.out.println(value.getTextContent());
					responseCode[0] = value.getTextContent();
				}
				if (title.getTextContent().equalsIgnoreCase("faultString")) {
					Node value = title.getNextSibling();
					// System.out.println(value.getTextContent());
					responseCode[1] = value.getTextContent();
				}
			}
			// System.out.println(data);
		} catch (SAXException saxexception) {
			// logger.error(saxexception.getMessage());
			logger.log(Level.SEVERE, "SAXException thrown. \n" + saxexception.getMessage());
		} catch (IOException ioexception) {
			throw new ParsingException(ioexception.getMessage());
		} catch (FactoryConfigurationError e) {
			// logger.error("Could not locate a JAXP DocumentBuilderFactory class");
			logger.log(Level.SEVERE, "Could not locate a JAXP DocumentBuilderFactory class \n" + e.getMessage());
		} catch (ParserConfigurationException parserconfigurationexception) {
			throw new ParsingException(parserconfigurationexception.getMessage());
		} catch (NullPointerException npe) {
			responseCode[0] = "Tag Not Found";

		}

		return responseCode;
	}

	public int getNumOfTags(String requestStr, String rootName, String tagName) throws ParsingException {
		int count = 0;
		String xml = requestStr;

		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(xml.getBytes());
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(new DefaultHandler() {
				public void fatalError(SAXParseException saxparseexception) throws SAXParseException {
					throw saxparseexception;
				}

				public void error(SAXParseException saxparseexception) throws SAXParseException {
					throw saxparseexception;
				}

				public void warning(SAXParseException saxparseexception) throws SAXParseException {
					throw saxparseexception;
				}
			});
			Document document = builder.parse(bis);
			Element header = (Element) document.getElementsByTagName(rootName).item(0);
			// count =
			// header.getElementsByTagName(tagName).item(0).getChildNodes().getLength();
			count = header.getElementsByTagName(tagName).getLength();
		} catch (Exception ex) {
			throw new ParsingException(ex.getMessage());
		}
		return count;
	}

	@SuppressWarnings("unchecked")
	public Object parseAll(String requestStr, String rootName) throws ParsingException {
		String xml = requestStr;

		@SuppressWarnings("rawtypes")
		Hashtable all_data = new Hashtable();
		String name = "";
		String value = "";
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(xml.getBytes());
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(new DefaultHandler() {
				public void fatalError(SAXParseException saxparseexception) throws SAXParseException {
					throw saxparseexception;
				}

				public void error(SAXParseException saxparseexception) throws SAXParseException {
					throw saxparseexception;
				}

				public void warning(SAXParseException saxparseexception) throws SAXParseException {
					throw saxparseexception;
				}
			});
			Document document = builder.parse(bis);
			int paramCount = document.getElementsByTagName(rootName).getLength();
			for (int i = 0; i < paramCount; i++) {
				Element header = (Element) document.getElementsByTagName(rootName).item(i);
				name = header.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();

				String tagName = header.getElementsByTagName("value").item(0).getChildNodes().item(0).getNodeName();
				if (tagName.equals("array")) {
					// got more elements
					value = null;
				} else {
					value = header.getElementsByTagName("value").item(0).getChildNodes().item(0).getFirstChild()
							.getNodeValue();
				}
				all_data.put(name, value);
			}
			return all_data;
		} catch (SAXException saxexception) {
			System.out.println(saxexception.getMessage());
		} catch (IOException ioexception) {
			throw new ParsingException(ioexception.getMessage());
		} catch (FactoryConfigurationError e) {
			System.out.println("Could not locate a JAXP DocumentBuilderFactory class");
		} catch (ParserConfigurationException parserconfigurationexception) {
			throw new ParsingException(parserconfigurationexception.getMessage());
		} catch (NullPointerException npe) {
			return "Tag Not Found";
		}
		return null;
	}

}