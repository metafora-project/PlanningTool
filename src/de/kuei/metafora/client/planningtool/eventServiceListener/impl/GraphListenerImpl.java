package de.kuei.metafora.client.planningtool.eventServiceListener.impl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.PlanningTool;
import de.kuei.metafora.client.planningtool.PlanningToolWidget;
import de.kuei.metafora.client.planningtool.eventServiceListener.GraphListener;
import de.kuei.metafora.client.planningtool.xml.GUIEventCreator;
import de.kuei.metafora.shared.event.graph.CenterNodeEvent;
import de.kuei.metafora.shared.event.graph.EdgeEvent;
import de.kuei.metafora.shared.event.graph.FrameworkEvent;
import de.kuei.metafora.shared.event.graph.NodeEvent;
import de.novanic.eventservice.client.event.Event;
import de.novanic.eventservice.client.event.filter.EventFilter;

public class GraphListenerImpl implements GraphListener {

	// i18n
	final static Languages language = GWT.create(Languages.class);
	private static GraphListenerImpl instance = null;

	public static GraphListenerImpl getInstance() {
		if (instance == null) {
			instance = new GraphListenerImpl();
		}
		return instance;
	}

	private GUIEventCreator eventCreator;
	private EventFilter filter;

	private GraphListenerImpl() {
		eventCreator = GUIEventCreator.getInstance();
	}

	public void setFilter(EventFilter filter) {
		this.filter = filter;
	}

	@Override
	public void apply(Event anEvent) {
		if (filter != null) {
			if (!filter.match(anEvent)) {
				return;
			}
		}

		if (anEvent instanceof NodeEvent) {
			nodeEvent((NodeEvent) anEvent);
		} else if (anEvent instanceof EdgeEvent) {
			edgeEvent((EdgeEvent) anEvent);
		} else if (anEvent instanceof FrameworkEvent) {
			frameworkEvent((FrameworkEvent) anEvent);
		} else if (anEvent instanceof CenterNodeEvent) {
			centerNodeEvent((CenterNodeEvent) anEvent);
		}
	}

	@Override
	public void nodeEvent(NodeEvent event) {
		parseNode(event.getNodeXML());
	}

	/**
	 * This method parses node xml messages. It is called from nodeEvent and
	 * from the PlanningToolWidget on graph init.
	 * 
	 * @param xml
	 *            node xml
	 */
	public void parseNode(String xml) {
		Document doc = XMLParser.parse(xml);

		String text = ((Element) doc.getElementsByTagName("object").item(0))
				.getAttributeNode("type").getNodeValue();

		Document doObject = XMLParser.parse(text);
		String[] values = new String[8];

		values = parse(doObject);

		if (values[2].contains(".")) {
			values[2] = values[2].substring(0, values[2].indexOf('.'));
		}

		if (values[3].contains(".")) {
			values[3] = values[3].substring(0, values[3].indexOf('.'));
		}

		if (values[2].contains(",")) {
			values[2] = values[2].substring(0, values[2].indexOf(','));
		}

		if (values[3].contains(",")) {
			values[3] = values[3].substring(0, values[3].indexOf(','));
		}

		if (values != null) {
			String nodeId = values[0];
			String color = values[1];

			int[] position = new int[] { Integer.parseInt(values[2]),
					Integer.parseInt(values[3]) };
			String nodeText = values[4];
			String pictureUrl = values[5];
			String toolUrl = values[6];
			String categorie = values[7];
			String name = values[8];
			String scalefactor = values[9];

			if (((Element) doc.getElementsByTagName("actiontype").item(0))
					.getAttribute("classification").equals("create")) {

				String[] creators = getUsers(doc);
				eventCreator.createNode(nodeId, color, position, nodeText,
						pictureUrl, toolUrl, categorie, name, creators,
						scalefactor);

			} else if (((Element) doc.getElementsByTagName("actiontype")
					.item(0)).getAttribute("classification").equals("modify")) {

				eventCreator.updateNode(nodeId, color, position, nodeText,
						pictureUrl, toolUrl, categorie, name, scalefactor);

			} else if (((Element) doc.getElementsByTagName("actiontype")
					.item(0)).getAttribute("classification").equals("move")) {

				Window.alert("move node " + nodeId);

				eventCreator.updateNode(nodeId, color, position, nodeText,
						pictureUrl, toolUrl, categorie, name, scalefactor);

			} else if (((Element) doc.getElementsByTagName("actiontype")
					.item(0)).getAttribute("classification").equals("delete")) {

				eventCreator.deleteNode(nodeId);

			}
		}
	}

	@Override
	public void edgeEvent(EdgeEvent event) {
		parseEdge(event.getEdgeXML());
	}

	public void parseEdge(String xml) {
		Document doc = XMLParser.parse(xml);

		String text = ((Element) doc.getElementsByTagName("object").item(0))
				.getAttributeNode("type").getNodeValue();

		Document doObject = XMLParser.parse(text);
		String[] values = parse(doObject);

		if (values != null) {
			String edgeId = values[0];
			String startNodeId = values[1];
			String endNodeId = values[2];
			String type = values[3];
			String label = values[4];

			if (((Element) doc.getElementsByTagName("actiontype").item(0))
					.getAttribute("classification").equals("create")) {

				String[] creators = getUsers(doc);
				eventCreator.createEdge(edgeId, startNodeId, endNodeId, type,
						label, creators);

			} else if (((Element) doc.getElementsByTagName("actiontype")
					.item(0)).getAttribute("classification").equals("modify")) {

				eventCreator.updateEdge(edgeId, startNodeId, endNodeId, type);

			} else if (((Element) doc.getElementsByTagName("actiontype")
					.item(0)).getAttribute("classification").equals("delete")) {

				eventCreator.deleteEdge(edgeId);

			}
		}

	}

	@Override
	public void frameworkEvent(FrameworkEvent event) {
		Document doc = XMLParser.parse(event.getFrameworkXML());

		NodeList objectList = doc.getElementsByTagName("object");

		if (objectList.getLength() > 0) {
			Element object = (Element) objectList.item(0);

			String graph = object.getAttribute("id");
			String innerXml = object.getAttribute("type");
			Document innerDoc = XMLParser.parse(innerXml);

			NodeList areasizeList = innerDoc.getElementsByTagName("areasize");
			if (areasizeList.getLength() > 0) {
				Element areasize = (Element) areasizeList.item(0);
				String xval = areasize.getAttributes().getNamedItem("x")
						.getNodeValue();
				String yval = areasize.getAttributes().getNamedItem("y")
						.getNodeValue();
				if (graph.equals(PlanningToolWidget.getInstance()
						.getGraphName()))
					eventCreator.areaSizeChanged(xval, yval);
			} else {
				NodeList newmapList = innerDoc.getElementsByTagName("NEW_MAP");
				if (newmapList.equals("NEW_MAP")) {
					if (newmapList.getLength() > 0) {
						Element newmap = (Element) newmapList.item(0);
						String name = newmap.getAttributes()
								.getNamedItem("NAME").getNodeValue();
						eventCreator.newMapAdded(name);
					}
				} else if (newmapList.equals("DELETE_MAP")) {
					if (newmapList.getLength() > 0) {
						Element newmap = (Element) newmapList.item(0);
						String name = newmap.getAttributes()
								.getNamedItem("name").getNodeValue();
						eventCreator.deleteMap(name);
					}
				}
			}
		}
	}

	public void parsePaletteNode(String xml, String graphName) {
		Document doc = XMLParser.parse(xml);

		Document doObject = XMLParser.parse(((Element) doc
				.getElementsByTagName("object").item(0)).getAttributeNode(
				"type").getNodeValue());

		String[] values = parse(doObject);

		String nodeId = values[0];
		String color = values[1];
		int[] position = { Integer.parseInt(values[2]),
				Integer.parseInt(values[3]) };

		String nodeText = values[4];
		String pictureUrl = values[5];
		String toolUrl = values[6];
		String categorie = values[7];
		String name = values[8];
		String scalefactor = values[9];

		if (((Element) doc.getElementsByTagName("actiontype").item(0))
				.getAttribute("classification").equals("create")) {

			String[] creators = getUsers(doc);
			eventCreator.createPaletteNode(nodeId, color, position, nodeText,
					pictureUrl, toolUrl, categorie, name, creators, graphName,
					scalefactor);

		}

	}

	private String[] parse(Document doc) {
		String[] values = null;

		if (doc.getElementsByTagName("node").getLength() != 0) {
			values = new String[10];

			if (doc.getElementsByTagName("node").getLength() != 0) {
				values[0] = ((Element) (doc.getElementsByTagName("node")
						.item(0))).getAttribute("id");
				;
			} else {
				Window.alert(language.InvalidNodeReceived()+"\n" + doc);
				return null;
			}

			if (doc.getElementsByTagName("bordercolor").getLength() != 0) {
				values[1] = ((Element) (doc.getElementsByTagName("bordercolor")
						.item(0))).getAttribute("value");
			} else {
				values[1] = "#AAAAAA";
			}

			if (doc.getElementsByTagName("position").getLength() != 0) {
				values[2] = ((Element) (doc.getElementsByTagName("position")
						.item(0))).getAttribute("x");
				values[3] = ((Element) (doc.getElementsByTagName("position")
						.item(0))).getAttribute("y");
			} else {
				values[2] = "0";
				values[3] = "0";
			}

			if (doc.getElementsByTagName("text").getLength() != 0) {
				values[4] = ((Element) (doc.getElementsByTagName("text")
						.item(0))).getAttribute("value");
			} else {
				values[4] = "";
			}

			if (doc.getElementsByTagName("pictureurl").getLength() != 0) {
				values[5] = ((Element) (doc.getElementsByTagName("pictureurl")
						.item(0))).getAttribute("value");
			} else {
				values[5] = "http://www.metafora-project.org/images/articles/logo_UE.gif";
			}

			if (doc.getElementsByTagName("tool").getLength() != 0) {
				values[6] = ((Element) (doc.getElementsByTagName("tool")
						.item(0))).getAttribute("value");
			} else {
				// values[6] = "http://www.metafora-project.org";
				values[6] = null;
			}

			if (doc.getElementsByTagName("categorie").getLength() != 0) {
				values[7] = ((Element) (doc.getElementsByTagName("categorie")
						.item(0))).getAttribute("value");
			} else {
				values[7] = "default";
			}

			if (doc.getElementsByTagName("name").getLength() != 0) {
				values[8] = ((Element) (doc.getElementsByTagName("name")
						.item(0))).getAttribute("value");
			} else {
				values[8] = "unknown";
			}

			if (doc.getElementsByTagName("scalefactor").getLength() != 0) {
				Element elem = (Element) doc
						.getElementsByTagName("scalefactor").item(0);
				if (elem.hasAttribute("value")) {
					values[9] = elem.getAttribute("value");
				} else {
					values[9] = "100";
				}
			} else {
				values[9] = "100";
			}
		} else if (doc.getElementsByTagName("edge").getLength() != 0) {
			values = new String[5];

			if (doc.getElementsByTagName("edge").getLength() != 0) {
				values[0] = ((Element) (doc.getElementsByTagName("edge")
						.item(0))).getAttribute("id");
			} else {
				Window.alert(language.InvalidEdgeReceived()+" id\n" + doc);
				return null;
			}

			if (doc.getElementsByTagName("start").getLength() != 0) {
				values[1] = ((Element) (doc.getElementsByTagName("start")
						.item(0))).getAttribute("value");
			} else {
				Window.alert(language.InvalidEdgeReceived()+" start\n" + doc);
				return null;
			}

			if (doc.getElementsByTagName("end").getLength() != 0) {
				values[2] = ((Element) (doc.getElementsByTagName("end").item(0)))
						.getAttribute("value");
			} else {
				Window.alert(language.InvalidEdgeReceived()+" end\n" + doc);
				return null;
			}

			if (doc.getElementsByTagName("type").getLength() != 0) {
				values[3] = ((Element) (doc.getElementsByTagName("type")
						.item(0))).getAttribute("value");
			} else {
				values[3] = "SOLID_BLUE";
			}

			if (doc.getElementsByTagName("label").getLength() > 0) {
				values[4] = ((Element) (doc.getElementsByTagName("label")
						.item(0))).getAttribute("value");
			} else {
				values[4] = "";
			}
		}

		return values;
	}

	private String[] getUsers(Document doc) {
		String[] users = new String[doc.getElementsByTagName("user")
				.getLength()];

		for (int i = 0; i < doc.getElementsByTagName("user").getLength(); i++) {
			users[i] = ((Element) doc.getElementsByTagName("user").item(i))
					.getAttribute("id");
		}

		return users;
	}

	@Override
	public void centerNodeEvent(CenterNodeEvent event) {
		String nodeId = event.getNodeId();
		if (nodeId != null && event.getMap() != null
				&& event.getConnectionId() != null) {
			if (PlanningTool.getConnectionId().equals(event.getConnectionId())
					&& PlanningToolWidget.getInstance().getGraphName()
							.equals(event.getMap())) {
				if (GUIEventCreator.getInstance().getNode(nodeId) == null) {
					//TODO: localize
					Window.alert(language.TheNode()+" " + nodeId
							+ language.WasDeletedAndNoLongerAvailable());
				}
				PlanningTool.setCenterNode(nodeId);
				PlanningToolWidget.getInstance().centerNode();
			}
		}
	}

}
