package de.kuei.metafora.server.planningtool.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TemplateParser extends DefaultHandler {

	private boolean ready = false;
	private HashMap<String, Vector<String>> template;
	private Vector<String> categoryOrder;

	private String category = null;
	private Vector<String> categoryVector = null;

	public TemplateParser() {
		template = new HashMap<String, Vector<String>>();
		categoryOrder = new Vector<String>();
		template.put("categoryorder", categoryOrder);
	}

	public HashMap<String, Vector<String>> getTemplate() {
		if (!ready)
			return null;
		return template;
	}

	public void parseTemplate(String template) {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			ByteArrayInputStream is;
			try {
				is = new ByteArrayInputStream(template.getBytes("UTF-8"));
				try {
					parser.parse(is, this);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void endDocument() throws SAXException {
		ready = true;
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (qName.equals("category")) {
			template.put(category, categoryVector);
			category = null;
			categoryVector = null;
		}
	}

	@Override
	public void startDocument() throws SAXException {

	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if (qName.equals("category")) {
			category = atts.getValue("id");
			categoryOrder.add(category);
			categoryVector = new Vector<String>();
		} else if (qName.equals("card")) {
			if (category != null && categoryVector != null) {
				categoryVector.add(atts.getValue("id"));
			}
		}
	}
}
