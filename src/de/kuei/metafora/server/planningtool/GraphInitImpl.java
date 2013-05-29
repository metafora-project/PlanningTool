package de.kuei.metafora.server.planningtool;

import java.util.Vector;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.kuei.metafora.client.planningtool.serverlink.GraphInit;
import de.kuei.metafora.server.planningtool.graphData.DirectedGraph;
import de.kuei.metafora.server.planningtool.graphData.GraphManager;

public class GraphInitImpl extends RemoteServiceServlet implements GraphInit {

	@Override
	public String[] getNodes(String map, String token) {
		DirectedGraph graph = GraphManager.getGraph(map);
		return graph.getNodes();
	}

	@Override
	public String[] getEdges(String map, String token) {
		DirectedGraph graph = GraphManager.getGraph(map);
		return graph.getEdges();
	}

	@Override
	public int[] getSize(String map, String token) {
		DirectedGraph graph = GraphManager.getGraph(map);
		return graph.getSize();
	}

	@Override
	public Vector<String> getUsers(String map, String token) {
		return GraphManager.getGraph(map).getUsers();
	}

	@Override
	public Vector<String> getGroups(String map, String token) {
		return GraphManager.getGraph(map).getGroups();
	}
}
