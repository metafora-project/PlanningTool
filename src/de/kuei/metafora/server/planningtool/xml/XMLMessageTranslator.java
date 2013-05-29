package de.kuei.metafora.server.planningtool.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.kuei.metafora.server.planningtool.graphData.DirectedGraph;
import de.kuei.metafora.server.planningtool.graphData.GraphManager;

public class XMLMessageTranslator {

	//TODO: replace with MetaforaXMLTools
	public static String[] actionReceived(String xml, String graph) {
		
		DirectedGraph dgraph = GraphManager.getGraph(graph);
		
		try {
			Document doc = XMLUtils.parseXMLString(xml, true);
			NodeList list = doc.getElementsByTagName("object");
			if (list.getLength() > 0) {
				Element object = (Element) list.item(0);
				String id = object.getAttribute("id");
				if (id != null) {
					list = doc.getElementsByTagName("actiontype");
					if (list.getLength() > 0) {
						Element actiontype = (Element) list.item(0);
						String type = actiontype.getAttribute("type");
						if (type != null) {
							if (type.equalsIgnoreCase("CREATE_NODE")) {
								return dgraph.nodeAdded(xml);
							} else if (type.equalsIgnoreCase("DELETE_NODE")) {
								dgraph.nodeRemoved(id);
							} else if (type.equalsIgnoreCase("MODIFY_NODE")) {
								dgraph.updateNode(id, xml);
							} else if (type.equalsIgnoreCase("CREATE_EDGE")) {
								return dgraph.edgeAdded(xml);
							} else if (type.equalsIgnoreCase("DELETE_EDGE")) {
								dgraph.edgeRemoved(id);
							} else if (type.equalsIgnoreCase("MODIFY_EDGE")) {
								// TODO implement soon
							} else if (type.equalsIgnoreCase("UPDATE_FRAMEWORK")) {
								String innerXml = object.getAttribute("type");
								Document innerDoc = XMLUtils.parseXMLString(
										innerXml, true);

								NodeList areasizeList = innerDoc
										.getElementsByTagName("areasize");
								if (areasizeList.getLength() > 0) {
									Element areasize = (Element) areasizeList
											.item(0);
									String xval = areasize.getAttributes()
											.getNamedItem("x").getTextContent();
									String yval = areasize.getAttributes()
											.getNamedItem("y").getTextContent();

									System.err.println(xval+", "+yval);
									xval = xval.replaceAll("[^0-9]", "");
									yval = yval.replaceAll("[^0-9]", "");
									System.err.println(xval+", "+yval);
									int x = Integer.parseInt(xval);
									int y = Integer.parseInt(yval);
									System.err.println(x+", "+y);
									dgraph.areaSizeChanged(x, y);
								}
							}
						} else {
							System.err.println("ERROR: Message without id!\n"
									+ xml);
						}
					}
				} else {
					System.err.println("ERROR: Message without id!\n" + xml);
				}
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
