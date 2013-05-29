package de.kuei.metafora.server.planningtool;

import java.util.Vector;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.kuei.metafora.client.planningtool.serverlink.GraphLink;
import de.kuei.metafora.server.planningtool.couchdb.DocUploadService;
import de.kuei.metafora.server.planningtool.graphData.DirectedGraph;
import de.kuei.metafora.server.planningtool.graphData.GraphManager;
import de.kuei.metafora.server.planningtool.mysql.MysqlConnector;
import de.kuei.metafora.server.planningtool.util.NodeTypeManager;
import de.kuei.metafora.server.planningtool.util.UserManager;

public class GraphLinkImpl extends RemoteServiceServlet implements GraphLink {

	@Override
	public String actionReceived(String xml, String token, String map,
			String connectionId) {

		System.err.println("actionReceived: " + connectionId);

		return UserManager.getInstance().actionReceived(token, connectionId,
				xml, map);
	}

	@Override
	public String[] getNodeTypes(String locale, String token,
			Vector<String> users, String groupId, String challengeId,
			String challengeName, boolean cavillag) {
		String lang = locale.substring(0, 2);

		if (!cavillag) {
			System.err.println("Update last used time of " + challengeName
					+ ".");
			MysqlConnector.getInstance().updateChallengeLastUsed(challengeId);
		} else {
			System.err.println("No update of last used for " + challengeName
					+ " because of CAViLLAG flag.");
		}

		if (lang != null) {
			return NodeTypeManager.getInstance().getNodeTypes(lang, users,
					token, groupId, challengeName, challengeId);
		} else {
			return NodeTypeManager.getInstance().getNodeTypes("en", users,
					token, groupId, challengeName, challengeId);
		}
	}

	@Override
	public void selectMap(String mapname, String token, Vector<String> users,
			String groupId, String challengeId, String challengeName,
			String connectionId) {
		System.err.println("Select map: "+mapname);
		UserManager.getInstance().selectMap(token, connectionId, mapname,
				users, groupId, challengeId, challengeName);
	}

	@Override
	public boolean saveAsMap(String mapname, String savename, String token,
			Vector<String> users, String groupId, String challengeId,
			String challengeName, String connectionId) {
		boolean answer = false;
		try {
			answer = GraphManager.saveGraph(mapname, savename);
			if (answer) {
				UserManager.getInstance().newMapAdded(savename, token, users,
						groupId, challengeId, challengeName);
				UserManager.getInstance().selectMap(token, connectionId,
						savename, users, groupId, challengeId, challengeName);
			}
		} catch (CloneNotSupportedException e) {
			System.err.println("Error in saveAsMap (GraphLinkImpl)!");
			e.printStackTrace();
		}
		return answer;
	}

	@Override
	public String saveVersionMap(String mapname, String token,
			Vector<String> users, String groupId, String challengeId,
			String challengeName) {
		try {
			// get XML from the Graph which shall be stored
			String xml = GraphManager.saveGraphAsXML(mapname);

			// create object of DocUploadService to store the String xml into
			// the CouchDB
			DocUploadService uploadService = new DocUploadService();

			// test, if the mapname already exists in the CouchDB
			String version = uploadService.testDocName(mapname);

			// store the map with newNameOfMap into CouchDB
			String docId = uploadService.uploadMap(mapname, xml);

			// if upload of map succeeded, send XMPP message in command
			// channel so the Workbench receives the ID of the graph and can
			// open it through it
			if ((docId != null) && (docId != "-1")) {
				// get link to doc
				String docLink = uploadService.getLinkToDoc(docId);
				UserManager.getInstance().mapSaved(mapname, docId, docLink,
						version, token, users, groupId, challengeId,
						challengeName);

				return docId;
			} else {
				System.err
						.println("Error when storing document to CouchDB. This document is not a map or doesn�t exist.");
			}

			// XMPPBridge.getConnection("planningcommand").sendMessage(xml);
		} catch (Exception exc) {
			System.err.println("Error in saveMap (GraphLinkImpl)!");
			exc.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean deleteMap(String mapname, String token,
			Vector<String> users, String groupId, String challengeId,
			String challengeName, String connectionId) {
		try {
			if (GraphManager.deleteGraph(mapname)) {
				UserManager.getInstance().mapDeleted(mapname, token, users,
						groupId, challengeId, challengeName);
				UserManager.getInstance().selectMap(token, connectionId,
						"default", users, groupId, challengeId, challengeName);
				return true;
			} else
				return false;
		} catch (Exception exc) {
			System.err.println("Error in GraphLinkImpl.deleteMap().");
			exc.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean createMap(String mapname, String token,
			Vector<String> users, String groupId, String challengeId,
			String challengeName, String connectionId) {
		System.err
				.println("PlanningTool: GraphLinkImpl: createMap: " + mapname);
		if (GraphManager.createGraph(mapname, users, groupId)) {
			UserManager.getInstance().newMapAdded(mapname, token, users,
					groupId, challengeId, challengeName);
			UserManager.getInstance().selectMap(token, connectionId, mapname,
					users, groupId, challengeId, challengeName);
			// creates new Map if name does not already exist
			return true;
		} else
			return false;
	}

	@Override
	public void assignGroup(Vector<String> users, String group, String token,
			String connectionId) {
		DirectedGraph graph = GraphManager.getGraph(UserManager.getInstance()
				.getMapForConnectionId(connectionId));
		if (graph.getUserCount() == 0) {
			graph.addUser(users);
		}
		graph.addGroup(group);
	}

	public String[] getMapnames(Vector<String> users, String group, String token) {
		return UserManager.getInstance().getListOfMaps(users, group, token);
	}

	public void startClientSession(String connectionId, String token) {
		UserManager.getInstance().startClientSession(connectionId, token);
	}

	@Override
	public void removeGroup(String group, String token, String connectionId) {
		GraphManager.getGraph(
				UserManager.getInstance().getMapForConnectionId(connectionId))
				.removeGroup(group);
	}

	@Override
	public Vector<String> getGroups(String token) {
		return MysqlConnector.getInstance().getGroups();
	}

	@Override
	public String getSendingTool() {
		return StartupServlet.sending_tool;
	}

	@Override
	public boolean getLogged() {
		return StartupServlet.logged;
	}

	@Override
	public String getMetafora() {
		return StartupServlet.metafora;
	}

	@Override
	public String getTomcatServer() {
		return StartupServlet.tomcatserver;
	}

	@Override
	public String getApacheServer() {
		return StartupServlet.apache;
	}

	@Override
	public String getXmppServer() {
		return StartupServlet.xmpp;
	}

	@Override
	public String getReflectionChannel() {
		return StartupServlet.reflectionChannel;
	}

	@Override
	public String getLasadName() {
		return StartupServlet.lasadName;
	}
}
