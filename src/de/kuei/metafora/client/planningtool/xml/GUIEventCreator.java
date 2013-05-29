package de.kuei.metafora.client.planningtool.xml;

import java.util.Collection;
import java.util.HashMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.planningtool.PlanningToolWidget;
import de.kuei.metafora.client.planningtool.gui.graph.DnDNode;
import de.kuei.metafora.client.planningtool.gui.graph.PlanningEdge;

public class GUIEventCreator {

	// i18n
	final static Languages language = GWT.create(Languages.class);
	private static GUIEventCreator instance = null;

	public static GUIEventCreator getInstance() {
		if (instance == null) {
			instance = new GUIEventCreator();
		}
		return instance;
	}

	private HashMap<String, DnDNode> idToNode;
	private HashMap<String, DnDNode> idToPalette;
	private HashMap<String, PlanningEdge> idToEdge;

	private GUIEventCreator() {
		idToNode = new HashMap<String, DnDNode>();
		idToPalette = new HashMap<String, DnDNode>();
		idToEdge = new HashMap<String, PlanningEdge>();
	}

	public int[] getNodePosition(String nodeId) {
		if (idToNode.containsKey(nodeId)) {
			DnDNode node = idToNode.get(nodeId);
			int[] point = new int[] { node.getCenterX(), node.getCenterY() };
			return point;
		}
		return null;
	}

	public DnDNode getNode(String nodeId) {
		return idToNode.get(nodeId);
	}

	public void createPaletteNode(String nodeId, String color, int[] position,
			String nodeText, String pictureUrl, String toolUrl,
			String category, String name, String[] creators, String graphName,
			String scalefactor) {

		PlanningToolWidget ptw = PlanningToolWidget.getInstance();

		if (ptw == null)
			return;

		DnDNode node = new DnDNode(color, nodeText, pictureUrl, toolUrl,
				category, name, ptw.getDropArea());
		node.setId(nodeId);
		node.setScalefactor(scalefactor);
		for (String creator : creators) {
			node.addCreator(creator);
		}

		idToPalette.put(node.getId(), node);

		ptw.addPaletteNode(node);
	}

	public void createNode(String nodeId, String color, int[] position,
			String nodeText, String pictureUrl, String toolUrl,
			String category, String name, String[] creators, String scalefactor) {

		if (!nodeId.startsWith(PlanningToolWidget.getInstance().getGraphName())) {
			return;
		}

		DnDNode node = null;

		node = new DnDNode(color, nodeText, pictureUrl, toolUrl, category,
				name, PlanningToolWidget.getInstance().getDropArea());
		PlanningToolWidget ptw = PlanningToolWidget.getInstance();
		if (ptw != null) {
			node.setEdgeHandler(ptw.getEdgeHandler());
		}

		node.setId(nodeId);
		node.setScalefactor(scalefactor);

		for (String creator : creators) {
			node.addCreator(creator);
		}

		if (idToNode.containsKey(nodeId)) {
			deleteNode(nodeId);
		}

		idToNode.put(node.getId(), node);

		PlanningToolWidget.getInstance().addNode(node);

		node.updateRelativeLeft(position[0]);
		node.updateRelativeTop(position[1]);
	}

	public void updateNode(String nodeId, String color, int[] position,
			String nodeText, String pictureUrl, String toolUrl,
			String categorie, String name, String scalefactor) {

		DnDNode node = idToNode.get(nodeId);

		if (node != null) {
			node.updateBGColor(color);
			node.updateDescription(nodeText);
			node.updateRelativeLeft(position[0]);
			node.updateRelativeTop(position[1]);
			node.setScalefactor(scalefactor);
			node.setToolUrl(toolUrl);
			Window.setStatus("New Tool URL for " + nodeId + ": " + toolUrl);
		} else {
			Window.setStatus("Node not found: " + nodeId);
		}
	}

	public void deleteNode(String nodeId) {
		DnDNode node = idToNode.get(nodeId);
		if (node != null) {
			PlanningToolWidget.getInstance().removeNode(node);

			idToNode.remove(nodeId);
		}
	}

	public void createEdge(String edgeId, String startNodeId, String endNodeId,
			String type, String label, String[] creators) {

		DnDNode start = idToNode.get(startNodeId);
		DnDNode end = idToNode.get(endNodeId);

		if (start != null && end != null) {
			PlanningEdge edge = new PlanningEdge(start, end, type);
			edge.setId(edgeId);

			if (idToEdge.containsKey(edgeId)) {
				deleteEdge(edgeId);
			}

			idToEdge.put(edgeId, edge);

			PlanningToolWidget.getInstance().addEdge(edge);
		}
	}

	public void updateEdge(String edgeId, String startNodeId, String endNodeId,
			String type) {
		PlanningEdge edge = idToEdge.get(edgeId);
		if (edge != null) {
			edge.updateEdge(type);

			PlanningToolWidget.getInstance().paintEdges();
		}
	}

	public void deleteEdge(String edgeId) {
		PlanningEdge edge = idToEdge.get(edgeId);
		if (edge != null) {
			PlanningToolWidget.getInstance().removeEdge(edge);

			idToEdge.remove(edgeId);

		}
	}

	public void setAreaSize(int width, int height) {
		PlanningToolWidget.getInstance().setSize(width, height);
	}

	public void registerNode(String nodeId, DnDNode node) {
		if (idToNode.containsKey(nodeId)) {
			deleteNode(nodeId);
		}
		idToNode.put(nodeId, node);
	}

	public void registerEdge(String edgeId, PlanningEdge edge) {
		if (idToEdge.containsKey(edgeId)) {
			deleteNode(edgeId);
		}
		idToEdge.put(edgeId, edge);
	}

	public void deleteAll(String graphName) {
		Collection<PlanningEdge> values = idToEdge.values();
		for (PlanningEdge edge : values) {
			PlanningToolWidget.getInstance().removeEdge(edge);
		}
		idToEdge.clear();

		Collection<DnDNode> valuesNode = idToNode.values();
		for (DnDNode node : valuesNode) {
			PlanningToolWidget.getInstance().removeNode(node);
		}
		idToNode.clear();
	}

	public void updateIdsByMapRename(String oldGraphName, String newGraphName) {
		Collection<PlanningEdge> values = idToEdge.values();
		for (PlanningEdge edge : values) {
			String edgeId = edge.getId();
			String newEdgeId = edgeId.replaceFirst(oldGraphName, newGraphName);
			idToEdge.put(newEdgeId, edge);
			edge.setId(newEdgeId);
			idToEdge.remove(edgeId);
		}

		Collection<DnDNode> valuesNode = idToNode.values();
		for (DnDNode node : valuesNode) {
			String nodeId = node.getId();
			String newNodeId = nodeId.replaceFirst(oldGraphName, newGraphName);
			idToNode.put(newNodeId, node);
			node.setId(newNodeId);
			idToNode.remove(nodeId);
		}
	}

	public void areaSizeChanged(String xval, String yval) {

		if (xval.contains(".")) {
			int pos = xval.indexOf('.');
			if (pos > 0) {
				xval = xval.substring(0, pos);
			}
		}

		if (xval.contains(",")) {
			int pos = xval.indexOf(',');
			if (pos > 0) {
				xval = xval.substring(0, pos);
			}
		}

		if (yval.contains(".")) {
			int pos = yval.indexOf('.');
			if (pos > 0) {
				yval = yval.substring(0, pos);
			}
		}

		if (yval.contains(",")) {
			int pos = yval.indexOf(',');
			if (pos > 0) {
				yval = yval.substring(0, pos);
			}
		}

		xval = xval.replaceAll("[^0-9]", "");
		yval = yval.replaceAll("[^0-9]", "");

		int x, y;
		try {
			x = Integer.parseInt(xval);
			y = Integer.parseInt(yval);
			setAreaSize(x, y);
		} catch (NumberFormatException ex) {
			Window.alert("GUIEventCreator: "+language.InvalidNewAreaSize()+" x:" + xval
					+ ", y:" + yval);
		}
	}

	public void newMapAdded(String name) {
		PlanningToolWidget ptw = PlanningToolWidget.getInstance();
		if (ptw != null)
			ptw.addNewMap(name);
	}

	public void deleteMap(String name) {
		PlanningToolWidget ptw = PlanningToolWidget.getInstance();
		if (ptw != null) {
			ptw.deleteMap(name);
			ptw.openMap("default");
		}
	}

}
