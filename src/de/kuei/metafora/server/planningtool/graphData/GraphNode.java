package de.kuei.metafora.server.planningtool.graphData;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.kuei.metafora.server.planningtool.StartupServlet;
import de.kuei.metafora.server.planningtool.mysql.MysqlConnector;
import de.kuei.metafora.server.planningtool.xml.XMLException;
import de.kuei.metafora.server.planningtool.xml.XMLUtils;

public class GraphNode {

	private String id;
	private String xml;
	private String innerXml;

	private Document owner;

	private String graphName;

	public GraphNode(String id, String xml, String graphName) {
		this.graphName = graphName;

		this.id = id;
		this.xml = xml;
		parseXml(xml, true);

		MysqlConnector.getInstance().createNode(this.graphName, this.id,
				this.xml, this.innerXml);
	}

	public GraphNode(String id, String xml, String graphName, boolean fromDB) {
		this.graphName = graphName;

		this.id = id;
		this.xml = xml;
		parseXml(xml, true);
	}

	public String updateNode(String xml) {
		parseXml(xml, false);

		MysqlConnector.getInstance().updateNode(this.graphName, getId(),
				this.xml, this.innerXml);

		return this.xml;
	}

	private void parseXml(String xml, boolean init) {
		try {
			Document doc = XMLUtils.parseXMLString(xml, true);
			NodeList list = doc.getElementsByTagName("object");
			if (list.getLength() > 0) {
				Element object = (Element) list.item(0);
				String id = object.getAttribute("id");
				if (!id.equals(this.id)) {
					object.setAttribute("id", this.id);
				}
				innerXml = object.getAttribute("type");
				if (innerXml == null) {
					System.err.println("Node update error! Node " + this.id
							+ " is invalid!");
				} else {
					Document innerdoc = XMLUtils.parseXMLString(innerXml, true);
					NodeList nl = innerdoc.getElementsByTagName("node");
					Element node = (Element) nl.item(0);
					node.setAttribute("id", this.id);
					innerXml = XMLUtils
							.documentToString(innerdoc,
									StartupServlet.planningtoolformat);

					if (init) {
						object.setAttribute("type", innerXml);
						this.xml = XMLUtils
								.documentToString(doc,
										StartupServlet.commonformat);
					} else {
						Document outer = XMLUtils
								.parseXMLString(this.xml, true);
						NodeList nlist = outer.getElementsByTagName("object");
						if (nlist.getLength() > 0) {
							Element obj = (Element) nlist.item(0);
							obj.setAttribute("type", innerXml);
							this.xml = XMLUtils
									.documentToString(outer,
											StartupServlet.commonformat);
						}
					}
				}
			}

			if (init) {
				NodeList users = doc.getElementsByTagName("user");
				Document userdoc = XMLUtils.createDocument();
				Element owner = userdoc.createElement("Owner");
				userdoc.appendChild(owner);
				for (int i = 0; i < users.getLength(); i++) {
					Element user = (Element) users.item(i);
					userdoc.adoptNode(user);
					owner.appendChild(user);
				}
				this.owner = userdoc;
			}

		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	public String getCategory() {
		return getAttrib("categorie", "value");
	}

	public String getTool() {
		return getAttrib("tool", "value");
	}

	public void setTool(String tool) {
		setAttrib("tool", "value", tool);
	}

	public int getCenterx() {
		String xpos = getAttrib("position", "x");
		if (xpos != null) {
			try {
				return Integer.parseInt(xpos);
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
			}
		}
		return 0;
	}

	public void setCenterx(int centerx) {
		setAttrib("position", "x", centerx + "");
	}

	public int getCentery() {
		String xpos = getAttrib("position", "y");
		if (xpos != null) {
			try {
				return Integer.parseInt(xpos);
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
			}
		}
		return 0;
	}

	public void setCentery(int centery) {
		setAttrib("position", "y", centery + "");
	}

	public String getId() {
		return id;
	}

	public String getUrl() {
		return getAttrib("pictureurl", "value");
	}

	@Override
	public String toString() {
		return innerXml;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof GraphNode) {
			if (((GraphNode) o).getId() == getId()) {
				return true;
			}
		}
		return false;
	}

	public String getAttrib(String parentelement, String attribute) {
		try {
			Document inner = XMLUtils.parseXMLString(innerXml, true);
			NodeList list = inner.getElementsByTagName(parentelement);

			if (list.getLength() > 0) {
				Element elem = (Element) list.item(0);
				return elem.getAttribute(attribute);
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setAttrib(String parentElement, String attribute, String value) {
		try {
			Document inner = XMLUtils.parseXMLString(innerXml, true);
			NodeList list = inner.getElementsByTagName(parentElement);
			if (list.getLength() > 0) {
				Element parent = (Element) list.item(0);
				parent.setAttribute(attribute, value);

				String iXml = XMLUtils
						.documentToString(inner,
								StartupServlet.planningtoolformat);
				this.innerXml = iXml;

				Document full = XMLUtils.parseXMLString(xml, true);
				NodeList nl = full.getElementsByTagName("object");
				if (nl.getLength() > 0) {
					Element elem = (Element) nl.item(0);
					elem.setAttribute("type", iXml);

					String oxml = XMLUtils
							.documentToString(full,
									StartupServlet.commonformat);
					this.xml = oxml;
				}
			}

			MysqlConnector.getInstance().updateNode(graphName, id, xml,
					innerXml);
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	public String toXml() {
		return xml;
	}

	public String getInnerXml() {
		return innerXml;
	}

	public void appendXmlForSave(Document doc) {
		try {
			Document document = XMLUtils.parseXMLString(innerXml, true);
			NodeList list = document.getElementsByTagName("object");

			if (list.getLength() > 0) {
				Element elem = (Element) list.item(0);
				NodeList owner = this.owner.getElementsByTagName("user");
				for (int i = 0; i < owner.getLength(); i++) {
					Node own = document.adoptNode(owner.item(i));
					elem.appendChild(own);
				}
				NodeList nl = doc.getElementsByTagName("graph");
				if (nl.getLength() > 0) {
					doc.adoptNode(elem);
					((Element) nl.item(0)).appendChild(elem);
				}
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}
}
