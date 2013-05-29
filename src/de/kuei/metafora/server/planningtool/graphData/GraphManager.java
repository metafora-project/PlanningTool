package de.kuei.metafora.server.planningtool.graphData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.kuei.metafora.server.planningtool.mysql.MysqlConnector;
import de.kuei.metafora.server.planningtool.xml.XMLException;
import de.kuei.metafora.server.planningtool.xml.XMLUtils;

public class GraphManager {
	/**
	 * Map for the different PlanningTool graphs.
	 */
	private static HashMap<String, DirectedGraph> documents = new HashMap<String, DirectedGraph>();

	/**
	 * This method returns the DirectedGraph with the given name. If the graph
	 * doesn't exist a new map with the given name will be created.
	 * 
	 * @param name
	 *            name of the graph
	 * @return DirectedGraph with the given name
	 */
	public static synchronized DirectedGraph getGraph(String name) {

		DirectedGraph graph = null;
		if (documents.containsKey(name)) {
			graph = documents.get(name);
			if (graph == null) {
				graph = MysqlConnector.getInstance().loadNetFromDatabase(name);
				if (graph != null) {
					documents.put(name, graph);
				}
			}
		}

		if (graph == null) {
			graph = new DirectedGraph(name, new Vector<String>(), "Metafora");
			documents.put(name, graph);
		}

		return graph;
	}

	public static synchronized boolean createGraph(String name,
			Vector<String> users, String group) {
		if (!documents.containsKey(name)) {
			DirectedGraph g = new DirectedGraph(name, users, group);
			documents.put(name, g);
			return true;
		} else {
			return false;
		}
	}

	public static synchronized boolean deleteGraph(String name) {
		if (documents.containsKey(name)) {
			try {
				MysqlConnector.getInstance().deleteGraph(name);
				documents.remove(name);
				return true;
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
		return false;
	}

	public static synchronized void graphFromDB(String name) {
		documents.put(name, null);
	}

	public synchronized static boolean saveGraph(String map, String name)
			throws CloneNotSupportedException {
		if (documents.containsKey(map) && !documents.containsKey(name)) {
			DirectedGraph graph = getGraph(map);
			graph = (DirectedGraph) graph.cloneGraph(name);
			return true;
		}
		return false;
	}

	public synchronized static void addGraphToDocuments(String name,
			DirectedGraph graph) {
		documents.put(name, graph);
	}

	public synchronized static String saveGraphAsXML(String mapname) {
		Document document;
		DirectedGraph graph = getGraph(mapname);

		try {
			document = XMLUtils.createDocument();
		} catch (XMLException e) {
			System.err.println("Error: document = XMLUtils.createDocument()");
			e.printStackTrace();
			return null;
		}

		Element graphNode = document.createElement("graph");
		graphNode.setAttribute("name", graph.getName());
		document.appendChild(graphNode);

		Vector<GraphNode> nodes = graph.getNodesVect();
		for (int i = 0; i < nodes.size(); i++) {
			GraphNode node = nodes.get(i);
			node.appendXmlForSave(document);
		}

		Vector<GraphEdge> edges = graph.getEdgesVect();
		for (int i = 0; i < edges.size(); i++) {
			GraphEdge edge = edges.get(i);
			edge.appendXmlForSave(document);
		}

		try {
			String xml = XMLUtils
					.documentToString(document,
							"http://metafora.ku-eichstaett.de/dtd/planningtoolgraph.dtd");
			return xml;
		} catch (XMLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String[] getMapnames(Vector<String> users, String group) {
		Vector<String> maps = MysqlConnector.getInstance().getMapNames(users,
				group);
		String[] array = (String[]) maps.toArray(new String[0]);
		Arrays.sort(array);
		return array;
	}

}
