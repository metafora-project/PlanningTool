package de.kuei.metafora.server.planningtool.graphData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import de.kuei.metafora.server.planningtool.mysql.MysqlConnector;
import de.kuei.metafora.shared.event.user.ShareWithGroupEvent;
import de.kuei.metafora.shared.event.user.UnshareWithGroupEvent;
import de.kuei.metafora.shared.eventservice.EventServiceDomains;
import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.domain.DomainFactory;
import de.novanic.eventservice.service.registry.EventRegistryFactory;

public class DirectedGraph implements Cloneable {
	private Domain graphShareDomain = DomainFactory
			.getDomain(EventServiceDomains.GRAPHSHAREDOMAIN);

	/**
	 * The file name of this graph.
	 */
	private String graphName = null;

	/**
	 * Vector for all nodes in this graph.
	 */
	private Vector<GraphNode> nodes;

	/**
	 * Vector for all edges in this graph.
	 */
	private Vector<GraphEdge> edges;

	/**
	 * Graph node id to graph node object mapping.
	 */
	private Map<String, GraphNode> idToNode;

	/**
	 * Graph edge id to graph edge object mapping.
	 */
	private Map<String, GraphEdge> idToEdge;

	/**
	 * Width of the drawing area.
	 */
	private int width;

	/**
	 * Height of the drawing area.
	 */
	private int height;

	/**
	 * Node counter for unique node ids.
	 */
	private int nodeCounter;

	/**
	 * Node counter for unique node ids.
	 */
	private int edgeCounter;

	private Vector<String> users;
	private Vector<String> groups;

	/**
	 * This constructor create a new DriectedGraph object with a drawing size of
	 * 1000 x 1000 dots.
	 */
	protected DirectedGraph(String name, Vector<String> users, String group) {

		graphName = name;

		nodes = new Vector<GraphNode>();
		edges = new Vector<GraphEdge>();

		this.users = new Vector<String>();
		groups = new Vector<String>();

		this.users.addAll(users);
		this.groups.add(group);

		idToNode = Collections
				.synchronizedMap(new HashMap<String, GraphNode>());
		idToEdge = Collections
				.synchronizedMap(new HashMap<String, GraphEdge>());

		nodeCounter = 0;
		edgeCounter = 0;

		width = height = 1000;

		MysqlConnector.getInstance().createGraph(graphName, width, height,
				nodeCounter, edgeCounter, users, group);
	}

	public DirectedGraph(String name, int width, int height, int nodeCounter,
			int edgeCounter, Vector<String> users, String group) {

		graphName = name;

		this.users = new Vector<String>();
		groups = new Vector<String>();

		this.users.addAll(users);
		this.groups.add(group);

		nodes = new Vector<GraphNode>();
		edges = new Vector<GraphEdge>();

		idToNode = Collections
				.synchronizedMap(new HashMap<String, GraphNode>());
		idToEdge = Collections
				.synchronizedMap(new HashMap<String, GraphEdge>());

		this.nodeCounter = nodeCounter;
		this.edgeCounter = edgeCounter;

		this.width = width;
		this.height = height;

		MysqlConnector.getInstance().createGraph(graphName, width, height,
				nodeCounter, edgeCounter, users, group);
	}

	public int getUserCount() {
		return users.size();
	}

	public void addUser(Vector<String> users) {
		this.users.addAll(users);
		for (String user : users) {
			MysqlConnector.getInstance().addUser(this.graphName, user);
		}
	}

	public void addGroupFromDatabase(String group) {
		if (!groups.contains(group)) {
			groups.add(group);
		}
	}

	public void addGroup(String group) {
		if (!groups.contains(group)) {
			groups.add(group);
			MysqlConnector.getInstance().addGroup(this.graphName, group);

			EventRegistryFactory
					.getInstance()
					.getEventRegistry()
					.addEvent(graphShareDomain,
							new ShareWithGroupEvent(this.graphName, group));
		}
	}

	public void removeGroup(String group) {
		if (groups.contains(group)) {
			groups.remove(group);
			MysqlConnector.getInstance().removeGroup(this.graphName, group);

			EventRegistryFactory
					.getInstance()
					.getEventRegistry()
					.addEvent(graphShareDomain,
							new UnshareWithGroupEvent(this.graphName, group));
		}
	}

	public Vector<String> getUsers() {
		return users;
	}

	public Vector<String> getGroups() {
		return groups;
	}

	public Vector<GraphNode> getNodesVect() {
		return nodes;
	}

	public Vector<GraphEdge> getEdgesVect() {
		return edges;
	}

	public String getName() {
		return graphName;
	}

	/**
	 * This method adds a new node to this DirectedGraph.
	 * 
	 * @param nodexml
	 *            XML String for the new node.
	 * 
	 * @return The id of the new node.
	 */
	public String[] nodeAdded(String nodexml) {
		String id = graphName + "_" + "node_" + nodeCounter++;

		while (idToNode.containsKey(id)) {
			System.err.println("GRAPH ERROR: NodeId " + id
					+ " already exists! (DirectedGraph.nodeAdded)");
			id = graphName + "_" + "node_" + nodeCounter++;
		}

		GraphNode node = new GraphNode(id, nodexml, graphName);
		// add node to graph data structure
		nodes.add(node);

		synchronized (idToNode) {
			idToNode.put(id, node);
		}

		MysqlConnector.getInstance().updateGraph(graphName, width, height,
				nodeCounter, edgeCounter);

		return new String[] { id, node.toXml() };
	}

	public void nodeFromDB(String id, String xml) {
		if (idToNode.containsKey(id)) {
			return;
		}

		// create node
		GraphNode node = new GraphNode(id, xml, graphName, true);
		// add node to graph data structure
		nodes.add(node);

		synchronized (idToNode) {
			idToNode.put(id, node);
		}
	}

	public String updateNode(String id, String nodexml) {
		if (idToNode.containsKey(id)) {
			synchronized (idToNode) {
				return idToNode.get(id).updateNode(nodexml);
			}
		} else {
			System.err.println("Invalid id: " + id + "\n" + nodexml);
			return null;
		}
	}

	/**
	 * This method removes the node with the given id from this DirectedGraph.
	 * 
	 * @param id
	 *            The id of the node to remove.
	 */
	public void nodeRemoved(String id) {
		GraphNode node = idToNode.get(id);
		nodes.remove(node);
		synchronized (idToNode) {
			idToNode.remove(id);
		}
		MysqlConnector.getInstance().deleteNode(graphName, node.getId());
	}

	/**
	 * This method adds a new edge to this graph.
	 * 
	 * @param startid
	 *            The id of the start node of the edge.
	 * 
	 * @param endid
	 *            The id of the end node of the edge.
	 * 
	 * @param edgexml
	 *            XML description of the edge (edge inscription, type, color,
	 *            ...)
	 * 
	 * @return The id of the new generated edge.
	 */
	public String[] edgeAdded(String edgexml) {
		String id = graphName + "_" + "edge_" + edgeCounter++;

		while (idToEdge.containsKey(id)) {
			System.err.println("GRAPH ERROR: EdgeId " + id
					+ " already exists! (DirectedGraph.edgeAdded)");
			id = graphName + "_" + "node_" + nodeCounter++;
		}

		GraphEdge edge = new GraphEdge(id, edgexml, graphName, this);
		edges.add(edge);

		synchronized (idToEdge) {
			idToEdge.put(id, edge);
		}

		MysqlConnector.getInstance().updateGraph(graphName, width, height,
				nodeCounter, edgeCounter);

		return new String[] { id, edge.toXml() };
	}

	public void edgeFromDB(String id, String edgexml) {

		if (idToEdge.containsKey(id)) {
			return;
		}

		GraphEdge edge = new GraphEdge(id, edgexml, graphName, true, this);
		edges.add(edge);

		synchronized (idToEdge) {
			idToEdge.put(id, edge);
		}
	}

	/**
	 * This method removes all edges from the node with the startid to the node
	 * with the endid.
	 * 
	 * @param startid
	 *            The id of the node where the edge starts.
	 * 
	 * @param endid
	 *            The id of the node where the edge ends.
	 */
	public void edgeRemoved(String startid, String endid) {
		for (int i = 0; i < edges.size(); i++) {
			if (edges.get(i).getStart().getId() == startid
					&& edges.get(i).getEnd().getId() == endid) {
				GraphEdge edge = edges.get(i);
				edges.remove(i);
				i--;
				MysqlConnector.getInstance()
						.deleteEdge(graphName, edge.getId());
			}
		}
	}

	/**
	 * This method removes the edge with the given id.
	 * 
	 * @param edgeid
	 *            The id of the edge.
	 */
	public void edgeRemoved(String edgeid) {
		synchronized (idToEdge) {
			try {
				GraphEdge edge = idToEdge.get(edgeid);
				if (edgeid != null)
					idToEdge.remove(edgeid);
				if (edge != null)
					edges.remove(edge);
				MysqlConnector.getInstance()
						.deleteEdge(graphName, edge.getId());
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method updates the graphical position of the node with the given id.
	 * 
	 * @param id
	 *            The id of the moved node.
	 * 
	 * @param xpos
	 *            The new x position of the node.
	 * 
	 * @param ypos
	 *            The new y position of the node.
	 */
	public void nodeMoved(String id, int xpos, int ypos) {
		synchronized (idToNode) {
			if (idToNode.get(id) != null) {
				idToNode.get(id).setCenterx(xpos);
				idToNode.get(id).setCentery(ypos);
			}
		}
	}

	/**
	 * This method updates the minimal size of the drawing area of this graph.
	 * 
	 * @param width
	 *            New minimal width of the drawing area.
	 * 
	 * @param height
	 *            New minimal height of the drawing area.
	 */
	public void areaSizeChanged(int width, int height) {
		this.width = width;
		this.height = height;
		MysqlConnector.getInstance().updateGraph(graphName, width, height,
				nodeCounter, edgeCounter);
	}

	/**
	 * This method returns all nodes in this graph as node xml descriptions.
	 * 
	 * @return String-Array of node xml descriptions of all nodes into this
	 *         graph.
	 */
	public String[] getNodes() {
		String[] nodeslist = new String[nodes.size()];
		for (int i = 0; i < nodeslist.length; i++) {
			nodeslist[i] = nodes.get(i).toXml();
		}
		return nodeslist;
	}

	/**
	 * This method returns all edges in this graph as edge xml descriptions.
	 * 
	 * @return String-Array of edge xml descriptions of all edges into this
	 *         graph.
	 */
	public String[] getEdges() {
		String[] edgeslist = new String[edges.size()];
		for (int i = 0; i < edgeslist.length; i++) {
			edgeslist[i] = edges.get(i).toXml();
		}
		return edgeslist;
	}

	/**
	 * This method returns the size of the drawing area of this graph as two
	 * dimensional integer-array.
	 * 
	 * @return size as integer array with [width, height]
	 */
	public int[] getSize() {
		return new int[] { width, height };
	}

	/**
	 * This methode adds or updates a attribute of a graph node.
	 * 
	 * @param id
	 *            The id of the graph node.
	 * 
	 * @param parentElement
	 *            The name of the xml elemente which contains the element.
	 * 
	 * @param attribute
	 *            The attribute name of the attribute.
	 * 
	 * @param value
	 *            The new value of the attribute.
	 */
	public void setNodeAttribute(String id, String parentElement,
			String attribute, String value) {
		synchronized (idToNode) {
			idToNode.get(id).setAttrib(parentElement, attribute, value);
		}
	}

	/**
	 * This methode adds or updates a attribute of a graph edge.
	 * 
	 * @param id
	 *            The id of the graph edge.
	 * 
	 * @param parentElement
	 *            The name of the xml elemente which contains the element.
	 * 
	 * @param attribute
	 *            The attribute name of the attribute.
	 * 
	 * @param value
	 *            The new value of the attribute.
	 */
	public void setEdgeAttribute(String id, String parentElement,
			String attribute, String value) {
		synchronized (idToEdge) {
			idToEdge.get(id).setAttrib(parentElement, attribute, value);
		}
	}

	/**
	 * This methode returns the value of the attribute key of the xml element
	 * element from the node with the given id.
	 * 
	 * @param id
	 *            The id of the graph node.
	 * 
	 * @param element
	 *            The name of the xml element.
	 * 
	 * @param key
	 *            The attribute name.
	 * 
	 * @return The value of the attribute.
	 */
	public String getNodeAttribute(String id, String element, String key) {
		synchronized (idToNode) {
			return idToNode.get(id).getAttrib(element, key);
		}
	}

	/**
	 * This method returns the value of the attribute key of the xml element
	 * element from the edge with the given id.
	 * 
	 * @param id
	 *            The id of the graph edge.
	 * 
	 * @param element
	 *            The name of the xml element.
	 * 
	 * @param key
	 *            The attribute name.
	 * 
	 * @return The value of the attribute.
	 */
	public String getEdgeAttribute(String id, String element, String key) {
		synchronized (idToEdge) {
			return idToEdge.get(id).getAttrib(element, key);
		}

	}

	public GraphNode getNodeForId(String id) {
		synchronized (idToNode) {
			GraphNode node = idToNode.get(id);
			return node;
		}

	}

	protected DirectedGraph cloneGraph(String graphName) {
		try {
			DirectedGraph clone = (DirectedGraph) this.clone();
			GraphManager.addGraphToDocuments(graphName, clone);

			if (groups.size() > 0) {
				MysqlConnector.getInstance().createGraph(graphName,
						clone.getSize()[0], clone.getSize()[1],
						clone.nodeCounter, clone.edgeCounter, users,
						groups.get(0));

				for (int i = 1; i < groups.size(); i++) {
					MysqlConnector.getInstance().addGroup(graphName,
							groups.get(i));
				}
			} else {
				MysqlConnector.getInstance()
						.createGraph(graphName, clone.getSize()[0],
								clone.getSize()[1], clone.nodeCounter,
								clone.edgeCounter, users, "Metafora");
			}
			clone.cloneData(graphName);
			clone.graphName = graphName;

			return clone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void cloneData(String newName) {
		nodes.clear();
		edges.clear();

		synchronized (idToNode) {
			Map<String, GraphNode> nodeMap = Collections
					.synchronizedMap(new HashMap<String, GraphNode>());
			Iterator<String> nodes = idToNode.keySet().iterator();
			while (nodes.hasNext()) {
				String nodeId = nodes.next();
				GraphNode node = idToNode.get(nodeId);
				String newNodeId = nodeId.replaceFirst(graphName, newName);
				GraphNode newNode = new GraphNode(newNodeId, node.toXml(),
						newName);
				nodeMap.put(newNodeId, newNode);
				this.nodes.add(newNode);
			}
			this.idToNode = nodeMap;
		}

		synchronized (idToEdge) {
			Map<String, GraphEdge> edgeMap = Collections
					.synchronizedMap(new HashMap<String, GraphEdge>());
			Iterator<String> edges = idToEdge.keySet().iterator();
			while (edges.hasNext()) {
				String edgeId = edges.next();
				GraphEdge edge = idToEdge.get(edgeId);
				String newEdgeId = edgeId.replaceFirst(graphName, newName);
				String xml = edge.toXml();
				GraphEdge newEdge = new GraphEdge(newEdgeId, xml, newName, this);
				edgeMap.put(newEdgeId, newEdge);
				this.edges.add(newEdge);
			}
			this.idToEdge = edgeMap;
		}
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		DirectedGraph clone = null;
		if (groups.size() > 0) {
			clone = new DirectedGraph(this.graphName, users, groups.get(0));
			for (int i = 1; i < groups.size(); i++) {
				clone.addGroup(groups.get(i));
			}
		} else {
			clone = new DirectedGraph(this.graphName, users, "Metafora");
		}

		clone.nodeCounter = nodeCounter;
		clone.edgeCounter = edgeCounter;
		clone.width = width;
		clone.height = height;

		synchronized (clone.idToNode) {
			clone.idToNode = Collections
					.synchronizedMap(new HashMap<String, GraphNode>());
			clone.idToNode.putAll(this.idToNode);
		}
		synchronized (clone.idToEdge) {
			clone.idToEdge = Collections
					.synchronizedMap(new HashMap<String, GraphEdge>());
			clone.idToEdge.putAll(this.idToEdge);
		}
		clone.nodes = new Vector<GraphNode>();
		clone.edges = new Vector<GraphEdge>();
		return clone;
	}
}
