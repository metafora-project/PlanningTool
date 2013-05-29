package de.kuei.metafora.server.planningtool.graphData;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.kuei.metafora.server.planningtool.mysql.MysqlConnector;
import de.kuei.metafora.server.planningtool.xml.XMLException;
import de.kuei.metafora.server.planningtool.xml.XMLUtils;

public class GraphEdge implements Cloneable {

	private GraphNode start;
	private GraphNode end;

	private String id;
	private String xml;
	private String innerXml;

	private Document owner;

	private String graphName;

	private DirectedGraph graph;

	public GraphEdge(String id, String xml, String graphName,
			DirectedGraph graph) {
		this.id = id;
		this.xml = xml;
		this.graphName = graphName;
		this.graph = graph;

		parseXml(true);

		MysqlConnector.getInstance().createEdge(graphName, this.id, this.xml,
				this.innerXml, start.getId(), end.getId());
	}

	public GraphEdge(String id, String xml, String graphName, boolean fromDB,
			DirectedGraph graph) {
		this.id = id;
		this.xml = xml;
		this.graphName = graphName;
		this.graph = graph;

		parseXml(true);
	}

	private void parseXml(boolean init) {
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
					System.err.println("Edge update error! Edge " + this.id
							+ " is invalid!");
				}

				Document innerdoc = XMLUtils.parseXMLString(innerXml, true);
				NodeList nl = innerdoc.getElementsByTagName("edge");
				if (nl.getLength() == 0) {
					System.err.println("Edge update error! Edge " + this.id
							+ " is invalid!");
				}

				Element node = (Element) nl.item(0);
				node.setAttribute("id", this.id);

				if (init) {

					NodeList users = doc.getElementsByTagName("user");
					Document userdoc = XMLUtils.createDocument();
					Element owner = userdoc.createElement("Owner");
					userdoc.appendChild(owner);
					for (int i = 0; i < users.getLength(); i++) {
						Element user = (Element) users.item(i);
						owner.appendChild(userdoc.importNode(user, true));
					}
					this.owner = userdoc;

					int pos = this.id.indexOf("_edge_");
					if (pos == -1) {
						System.err.println("GraphEdge.parseXml: " + this.id);
					}
					String graphName = this.graphName;

					NodeList startList = innerdoc.getElementsByTagName("start");
					if (startList.getLength() > 0) {
						Element startNode = (Element) startList.item(0);
						String startid = startNode.getAttribute("value");
						if (startid != null && startid.length() > 0) {
							if (!startid.startsWith(graphName)) {
								int p = startid.indexOf("_node_");
								startid = graphName
										+ startid
												.substring(p, startid.length());
								startNode.setAttribute("value", startid);

								System.err.println("Edge: startId changed: "
										+ startid);
							}
						}
						DirectedGraph graph = this.graph;
						if (graph == null)
							graph = GraphManager.getGraph(graphName);
						this.start = graph.getNodeForId(startid);
					}

					NodeList endList = innerdoc.getElementsByTagName("end");
					if (endList.getLength() > 0) {
						Element endNode = (Element) endList.item(0);
						String endid = endNode.getAttribute("value");
						if (endid != null && endid.length() > 0) {
							if (!endid.startsWith(graphName)) {
								int p = endid.indexOf("_node_");
								endid = graphName
										+ endid.substring(p, endid.length());
								endNode.setAttribute("value", endid);

								System.err.println("Edge: endId changed: "
										+ endid);
							}
						}

						if (graph == null)
							graph = GraphManager.getGraph(graphName);
						this.end = graph.getNodeForId(endid);
					}

				}
				innerXml = XMLUtils
						.documentToString(innerdoc,
								"http://metafora.ku-eichstaett.de/dtd/planningtoolelement.dtd");

				object.setAttribute("type", innerXml);

				xml = XMLUtils
						.documentToString(doc,
								"http://metafora.ku-eichstaett.de/dtd/commonformat.dtd");
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	public GraphNode getStart() {
		return start;
	}

	public GraphNode getEnd() {
		return end;
	}

	public String getType() {
		if (innerXml != null) {
			try {
				Document doc = XMLUtils.parseXMLString(innerXml, true);
				NodeList list = doc.getElementsByTagName("type");

				if (list.getLength() > 0) {
					Element elem = (Element) list.item(0);
					String type = elem.getAttribute("value");
					return type;
				}
			} catch (XMLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return getInnerXml();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof GraphEdge) {
			GraphEdge other = (GraphEdge) o;
			if (other.getStart().getId() == getStart().getId()
					&& other.getEnd().getId() == getEnd().getId()) {
				return true;
			}
		}
		return false;
	}

	public String getId() {
		return id;
	}

	public void update(String xml) {
		this.xml = xml;
		parseXml(false);

		MysqlConnector.getInstance().updateEdge(graphName, id, this.xml,
				innerXml);
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

	public void setAttrib(String parentElement, String attribute, String value) {
		try {
			Document inner = XMLUtils.parseXMLString(innerXml, true);
			NodeList list = inner.getElementsByTagName(parentElement);
			if (list.getLength() > 0) {
				Element parent = (Element) list.item(0);
				parent.setAttribute(attribute, value);

				String iXml = XMLUtils
						.documentToString(inner,
								"http://metafora.ku-eichstaett.de/dtd/planningtoolelement.dtd");
				this.innerXml = iXml;

				Document full = XMLUtils.parseXMLString(xml, true);
				NodeList nl = full.getElementsByTagName("object");
				if (nl.getLength() > 0) {
					Element elem = (Element) nl.item(0);
					elem.setAttribute("type", iXml);

					String oxml = XMLUtils
							.documentToString(full,
									"http://metafora.ku-eichstaett.de/dtd/commonformat.dtd");
					this.xml = oxml;
				}
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	public String getAttrib(String element, String key) {
		try {
			Document inner = XMLUtils.parseXMLString(innerXml, true);
			NodeList list = inner.getElementsByTagName(element);
			if (list.getLength() > 0) {
				Element parent = (Element) list.item(0);

				return parent.getAttribute(key);
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
