package de.kuei.metafora.server.planningtool.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.kuei.metafora.server.planningtool.StartupServlet;
import de.kuei.metafora.server.planningtool.graphData.GraphManager;
import de.kuei.metafora.server.planningtool.mysql.MysqlConnector;
import de.kuei.metafora.server.planningtool.xml.Classification;
import de.kuei.metafora.server.planningtool.xml.CommonFormatCreator;
import de.kuei.metafora.server.planningtool.xml.Role;
import de.kuei.metafora.server.planningtool.xml.XMLException;
import de.kuei.metafora.server.planningtool.xml.XMLMessageTranslator;
import de.kuei.metafora.server.planningtool.xml.XMLUtils;
import de.kuei.metafora.shared.event.graph.CenterNodeEvent;
import de.kuei.metafora.shared.event.graph.EdgeEvent;
import de.kuei.metafora.shared.event.graph.FrameworkEvent;
import de.kuei.metafora.shared.event.graph.NodeEvent;
import de.kuei.metafora.shared.event.user.GroupChangeEvent;
import de.kuei.metafora.shared.event.user.UserJoinMapEvent;
import de.kuei.metafora.shared.event.user.UserLeaveMapEvent;
import de.kuei.metafora.shared.event.user.UserLoginEvent;
import de.kuei.metafora.shared.event.user.UserLogoutEvent;
import de.kuei.metafora.shared.eventservice.EventServiceDomains;
import de.novanic.eventservice.client.event.Event;
import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.domain.DomainFactory;
import de.novanic.eventservice.service.UserTimeoutListener;
import de.novanic.eventservice.service.registry.EventRegistryFactory;
import de.novanic.eventservice.service.registry.user.UserInfo;

public class UserManager implements UserTimeoutListener {

	private static UserManager manager = null;

	public static UserManager getInstance() {
		if (manager == null) {
			manager = new UserManager();
		}
		return manager;
	}

	private Domain localUserDomain = DomainFactory
			.getDomain(EventServiceDomains.USERDOMAIN);

	private Domain groupDomain = DomainFactory
			.getDomain(EventServiceDomains.GROUPDOMAIN);

	private Map<String, String> connectionIdToMap;
	private Map<String, Vector<String>> tokenToConnectionIds;

	private Map<String, Vector<String>> tokenToUsers;
	private Map<String, String> tokenToGroup;

	private UserManager() {
		connectionIdToMap = Collections
				.synchronizedMap(new HashMap<String, String>());
		tokenToConnectionIds = Collections
				.synchronizedMap(new HashMap<String, Vector<String>>());
		tokenToGroup = Collections
				.synchronizedMap(new HashMap<String, String>());
		tokenToUsers = Collections
				.synchronizedMap(new HashMap<String, Vector<String>>());
	}

	public void startClientSession(String connectionId, String token) {
		if (!tokenToConnectionIds.containsKey(token)) {
			tokenToConnectionIds.put(token, new Vector<String>());
		}
		tokenToConnectionIds.get(token).add(connectionId);
	}

	private void clientSelectedMap(String connectionId, String map,
			String token, String groupID, Vector<String> user) {

		if (!tokenToConnectionIds.containsKey(token)) {
			tokenToConnectionIds.put(token, new Vector<String>());
		}

		if (!tokenToConnectionIds.get(token).contains(connectionId)) {
			tokenToConnectionIds.get(token).add(connectionId);
		}

		if (!tokenToUsers.containsKey(token)) {
			tokenToUsers.put(token, new Vector<String>());
		}

		for (String u : user) {
			if (!tokenToUsers.get(token).contains(u)) {
				tokenToUsers.get(token).add(u);
			}
		}

		if (!tokenToGroup.containsKey(token)) {
			tokenToGroup.put(token, groupID);
		}

		if (connectionIdToMap.containsKey(connectionId)) {
			String oldmap = connectionIdToMap.get(connectionId);
			for (String u : tokenToUsers.get(token)) {
				removeUserFromMap(groupID, u, oldmap);
			}
		}

		connectionIdToMap.put(connectionId, map);

		System.err.println("ConnectionId: " + connectionId + ". Map: " + map);

		for (String u : user) {
			addUser(token, u, groupID);
			addUserToMap(groupID, u, map);
		}

		if (!tokenToGroup.get(token).equals(groupID)) {
			groupChange(groupID, token);
		}
	}

	public void selectMap(String token, String connectionId, String map,
			Vector<String> users, String groupId, String challengeId,
			String challengeName) {

		GraphManager.createGraph(map, users, groupId);

		clientSelectedMap(connectionId, map, token, groupId, users);

		for (String user : users) {
			MysqlConnector.getInstance().saveMapHistory(token, map, user);
		}

		try {

			CommonFormatCreator creator = null;
			creator = new CommonFormatCreator(System.currentTimeMillis(),
					Classification.other, "OPEN_MAP", StartupServlet.logged);
			creator.addContentProperty("SENDING_TOOL",
					StartupServlet.sending_tool);

			for (String user : users) {
				creator.addUser(user, token, Role.originator);
			}

			creator.setObject(map, "PLANNING_TOOL_MAP");
			creator.addContentProperty("GROUP_ID", groupId);
			creator.addContentProperty("CHALLENGE_ID", challengeId);
			creator.addContentProperty("CHALLENGE_NAME", challengeName);

			if (StartupServlet.logger != null)
				StartupServlet.logger.sendMessage(creator.getDocument());
			else
				System.err.println("UserManager: logger is null!");
		} catch (XMLException exc) {
			System.err.println(exc.getMessage());
		}
	}

	public String getMapForConnectionId(String connectionId) {
		if (connectionIdToMap.containsKey(connectionId)) {
			return connectionIdToMap.get(connectionId);
		}
		return null;
	}

	public String[] getListOfMaps(Vector<String> users, String group, String ip) {
		String[] list = GraphManager.getMapnames(users, group);
		return list;
	}

	public synchronized String actionReceived(String token,
			String connectionId, String xml, String map) {
		try {
			Document doc = XMLUtils.parseXMLString(xml, true);

			// Set server time as timestamp
			Node action = doc.getElementsByTagName("action").item(0);
			action.getAttributes().getNamedItem("time")
					.setNodeValue(System.currentTimeMillis() + "");

			// Users
			NodeList users = doc.getElementsByTagName("user");
			for (int i = 0; i < users.getLength(); i++) {
				Element user = (Element) users.item(i);
				if (user.getAttribute("ip") == null) {
					System.err
							.println("WARNING: user without IP! IP changed to "
									+ token);
					user.setAttribute("ip", token);
				} else {
					if (!user.getAttribute("ip").equals(token)) {
						System.err
								.println("WARNING: user with wrong IP! IP changed to "
										+ token);
						user.setAttribute("ip", token);
					}
				}
			}

			String newxml = XMLUtils.documentToString(doc,
					"http://metafora.ku-eichstaett.de/dtd/commonformat.dtd");

			// update server data model and get ID
			String[] elem = XMLMessageTranslator.actionReceived(newxml, map);
			String elemid = null;

			if (elem != null) {
				newxml = elem[1];
				elemid = elem[0];
			}

			// sent to XMPP Channel
			try {
				if (StartupServlet.logger != null) {
					StartupServlet.logger.sendMessage(newxml);
				} else {
					System.err.println("UserManager: logger is null!");
				}

				System.err.println("UserManager: action received from "
						+ connectionId);

				if (connectionId != null) {
					sendIndicatorForAction(newxml, connectionId);
				} else {
					System.err
							.println("PlanningTool: UserManager.actionReceived(): Couldn't send indicator. ConnectionId is null.");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// send to other users
			if (map == null) {
				System.err.println("ERROR: No map for message found!\n" + xml
						+ "\n\nMessage was dropped!");
			}

			sendToUsers(newxml, map);

			return elemid;

		} catch (XMLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void sendToUsers(String newxml, String map) {
		try {
			Document doc = XMLUtils.parseXMLString(newxml, true);

			String stObject = ((Element) doc.getElementsByTagName("object")
					.item(0)).getAttributeNode("type").getNodeValue();

			Document inner = XMLUtils.parseXMLString(stObject, false);

			Domain dom = DomainFactory
					.getDomain(EventServiceDomains.GRAPHCHANGEDOMAIN);

			if (inner.getElementsByTagName("node").getLength() != 0) {

				EventRegistryFactory.getInstance().getEventRegistry()
						.addEvent(dom, new NodeEvent(newxml, map));

			} else if (inner.getElementsByTagName("edge").getLength() != 0) {

				EventRegistryFactory.getInstance().getEventRegistry()
						.addEvent(dom, new EdgeEvent(newxml, map));

			} else if (inner.getElementsByTagName("ctrl").getLength() != 0) {

				EventRegistryFactory.getInstance().getEventRegistry()
						.addEvent(dom, new FrameworkEvent(newxml, map));

			} else {

				System.err.println("Unknown message type: " + newxml
						+ "\nMessage dropped!");

			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	public void sendIndicatorForAction(String xml, String connectionId) {
		try {
			Document doc = XMLUtils.parseXMLString(xml, true);

			String classification = null;
			NodeList nActionType = doc.getElementsByTagName("actiontype");
			if (nActionType.getLength() == 1) {
				Node actiontype = nActionType.item(0);
				classification = actiontype.getAttributes()
						.getNamedItem("classification").getNodeValue();
			}

			NodeList objects = doc.getElementsByTagName("object");
			String nodeId = null;
			if (objects.getLength() == 1) {
				Node object = objects.item(0);
				Node objectid = object.getAttributes().getNamedItem("id");
				if (objectid != null) {
					nodeId = objectid.getNodeValue();
				}
			}

			NodeList list = doc.getElementsByTagName("property");

			String activityType = null;
			String groupId = null;
			String challengeId = null;
			String challengeName = null;
			String nodeText = null;
			String cardName = null;

			for (int i = 0; i < list.getLength(); i++) {
				Node elem = list.item(i);
				if (elem.getAttributes().getNamedItem("name").getNodeValue()
						.toLowerCase().equals("activity_type")) {
					activityType = elem.getAttributes().getNamedItem("value")
							.getNodeValue();
				}
				if (elem.getAttributes().getNamedItem("name").getNodeValue()
						.toLowerCase().equals("group_id")) {
					groupId = elem.getAttributes().getNamedItem("value")
							.getNodeValue();
				}
				if (elem.getAttributes().getNamedItem("name").getNodeValue()
						.toLowerCase().equals("challenge_id")) {
					challengeId = elem.getAttributes().getNamedItem("value")
							.getNodeValue();
				}
				if (elem.getAttributes().getNamedItem("name").getNodeValue()
						.toLowerCase().equals("challenge_name")) {
					challengeName = elem.getAttributes().getNamedItem("value")
							.getNodeValue();
				}
				if (elem.getAttributes().getNamedItem("name").getNodeValue()
						.toLowerCase().equals("node_text")) {
					nodeText = elem.getAttributes().getNamedItem("value")
							.getNodeValue();
				}
				if (elem.getAttributes().getNamedItem("name").getNodeValue()
						.toLowerCase().equals("name")) {
					cardName = elem.getAttributes().getNamedItem("value")
							.getNodeValue();
				}
			}

			Node nObject = doc.getElementsByTagName("object").item(0);
			String id = nObject.getAttributes().getNamedItem("id")
					.getNodeValue();
			String type = nObject.getAttributes().getNamedItem("type")
					.getNodeValue();

			NodeList users = doc.getElementsByTagName("user");
			String token = null;
			if (users.getLength() > 0
					&& users.item(0).getAttributes().getNamedItem("ip") != null)
				token = users.item(0).getAttributes().getNamedItem("ip")
						.getNodeValue();

			if ((activityType != null) && (groupId != null)
					&& (challengeId != null) && (challengeName != null)
					&& (users.getLength() > 0)) {

				CommonFormatCreator creator = null;
				if ((activityType.toLowerCase().equals("modify_state_finished"))
						|| (activityType.toLowerCase()
								.equals("modify_state_started"))) {
					creator = new CommonFormatCreator(
							System.currentTimeMillis(), Classification.modify,
							"LANDMARK", StartupServlet.logged);
					creator.addContentProperty("L2L2_TAG",
							"DISTRIBUTED_LEADERSHIP");
				} else {
					if (classification.equals("create")) {
						creator = new CommonFormatCreator(
								System.currentTimeMillis(),
								Classification.create, "INDICATOR", true);
					} else if (classification.equals("delete")) {
						creator = new CommonFormatCreator(
								System.currentTimeMillis(),
								Classification.delete, "INDICATOR", true);
					} else if (classification.equals("modify")) {
						creator = new CommonFormatCreator(
								System.currentTimeMillis(),
								Classification.modify, "INDICATOR", true);
					} else {
						creator = new CommonFormatCreator(
								System.currentTimeMillis(),
								Classification.other, "INDICATOR", true);
					}
				}
				creator.setObject(id, type);
				creator.addProperty("SENDING_TOOL", StartupServlet.sending_tool);
				creator.addContentProperty("SENDING_TOOL",
						StartupServlet.sending_tool);

				String userText = "";
				for (int i = 0; i < users.getLength(); i++) {
					String user = users.item(i).getAttributes()
							.getNamedItem("id").getNodeValue();
					String tokenValue = users.item(i).getAttributes()
							.getNamedItem("ip").getNodeValue();
					userText += user;
					if (i < users.getLength() - 1) {
						userText += ", ";
					}
					creator.addUser(user, tokenValue, Role.originator);
				}

				creator.addProperty("GROUP_ID", groupId);
				creator.addContentProperty("GROUP_ID", groupId);
				creator.addProperty("TOKEN", token);
				String map = getMapForConnectionId(connectionId);
				if (map != null) {
					creator.addProperty("MAP_NAME",
							getMapForConnectionId(connectionId));
				}
				if (nodeText != null) {
					creator.addProperty("TEXT", nodeText);
				}

				creator.addContentProperty("CHALLENGE_ID", challengeId);
				creator.addContentProperty("CHALLENGE_NAME", challengeName);

				creator.addContentProperty("ACTIVITY_TYPE", activityType);
				if (activityType.toLowerCase().equals("modify_state_started")) {
					creator.setCdataDescription(userText + " started \""
							+ cardName + "\" card " + nodeId + ".");
				} else if (activityType.toLowerCase().equals(
						"modify_state_finished")) {
					creator.setCdataDescription(userText + " finished \""
							+ cardName + "\" card " + nodeId + ".");
				} else {
					creator.setCdataDescription(userText + " performed action "
							+ activityType);
				}

				String indicator = creator.getDocument();

				try {
					if (StartupServlet.analysis != null) {
						StartupServlet.analysis.sendMessage(indicator);
					} else {
						System.err.println("UserManager: logger is null!");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (XMLException ex) {
			ex.printStackTrace();
		}
	}

	public void newMapAdded(String name, String token, Vector<String> users,
			String groupId, String challengeId, String challengeName) {
		try {
			CommonFormatCreator creator = null;
			creator = new CommonFormatCreator(System.currentTimeMillis(),
					Classification.create, "NEW_MAP", StartupServlet.logged);
			creator.addContentProperty("SENDING_TOOL",
					StartupServlet.sending_tool);

			for (String user : users) {
				creator.addUser(user, token, Role.originator);
			}

			Document innerXml = XMLUtils.createDocument();
			Element iobject = innerXml.createElement("object");
			innerXml.appendChild(iobject);

			Element ctrl = innerXml.createElement("ctrl");
			iobject.appendChild(ctrl);

			Element newmap = innerXml.createElement("NEW_MAP");
			ctrl.appendChild(newmap);
			newmap.setAttribute("name", name);
			String inner = XMLUtils
					.documentToString(innerXml,
							"http://metafora.ku-eichstaett.de/dtd/planningtoolelement.dtd");
			creator.setObject(name, inner);

			creator.addContentProperty("GROUP_ID", groupId);
			creator.addContentProperty("CHALLENGE_ID", challengeId);
			creator.addContentProperty("CHALLENGE_NAME", challengeName);

			if (StartupServlet.logger != null) {
				StartupServlet.logger.sendMessage(creator.getDocument());
			} else {
				System.err.println("UserManager: logger is null!");
			}

			sendToUsers(creator.getDocument(), null);

		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	public void mapDeleted(String name, String token, Vector<String> users,
			String groupId, String challengeId, String challengeName) {
		try {
			CommonFormatCreator creator = null;
			creator = new CommonFormatCreator(System.currentTimeMillis(),
					Classification.delete, "DELETE_MAP", StartupServlet.logged);
			creator.addContentProperty("SENDING_TOOL",
					StartupServlet.sending_tool);

			for (String user : users) {
				creator.addUser(user, token, Role.originator);
			}

			Document innerXml = XMLUtils.createDocument();
			Element iobject = innerXml.createElement("object");
			innerXml.appendChild(iobject);

			Element ctrl = innerXml.createElement("ctrl");
			iobject.appendChild(ctrl);

			Element newmap = innerXml.createElement("DELETE_MAP");
			ctrl.appendChild(newmap);
			newmap.setAttribute("name", name);
			String inner = XMLUtils
					.documentToString(innerXml,
							"http://metafora.ku-eichstaett.de/dtd/planningtoolelement.dtd");
			creator.setObject(name, inner);
			creator.addContentProperty("GROUP_ID", groupId);
			creator.addContentProperty("CHALLENGE_ID", challengeId);
			creator.addContentProperty("CHALLENGE_NAME", challengeName);

			if (StartupServlet.logger != null) {
				StartupServlet.logger.sendMessage(creator.getDocument());
			} else {
				System.err.println("UserManager: logger is null!");
			}

			sendToUsers(creator.getDocument(), null);
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	public void mapSaved(String name, String id, String link, String version,
			String ip, Vector<String> users, String groupId,
			String challengeId, String challengeName) {
		try {

			CommonFormatCreator creator = null;
			creator = new CommonFormatCreator(System.currentTimeMillis(),
					Classification.other, "MAP_VERSION_SAVED",
					StartupServlet.logged);
			creator.addContentProperty("SENDING_TOOL",
					StartupServlet.sending_tool);

			for (String user : users) {
				creator.addUser(user, ip, Role.originator);
			}
			creator.setObject(name, "");
			creator.addProperty("NAME", name);
			creator.addProperty("ID", id);
			creator.addProperty("LINK", link);
			creator.addProperty("VERSION", version);
			creator.addContentProperty("GROUP_ID", groupId);
			creator.addContentProperty("CHALLENGE_ID", challengeId);
			creator.addContentProperty("CHALLENGE_NAME", challengeName);

			if (StartupServlet.logger != null) {
				StartupServlet.logger.sendMessage(creator.getDocument());
			} else {
				System.err.println("UserManager: logger is null!");
			}

			if (StartupServlet.command != null) {
				StartupServlet.command.sendMessage(creator.getDocument());
			} else {
				System.err.println("UserManager: command is null!");
			}
		} catch (XMLException exc) {
			exc.printStackTrace();
		}
	}

	@Override
	public void onTimeout(UserInfo userInfo) {
		String connectionId = userInfo.getUserId();

		String token = null;
		for (String key : tokenToConnectionIds.keySet()) {
			if (tokenToConnectionIds.get(key).contains(connectionId)) {
				token = key;
				break;
			}
		}

		if (token != null) {
			String map = connectionIdToMap.get(connectionId);
			String group = tokenToGroup.get(token);
			if (map != null) {
				for (String user : tokenToUsers.get(token)) {
					removeUserFromMap(group, user, map);
				}
			}

			Vector<String> connectionIds = tokenToConnectionIds.get(token);
			connectionIds.remove(connectionId);

			if (connectionIds.size() == 0) {
				tokenToConnectionIds.remove(token);
				tokenToUsers.remove(token);
				tokenToGroup.remove(token);
			}
		}

		connectionIdToMap.remove(connectionId);
	}

	public void userLogin(String user, String token, String group) {
		if (tokenToConnectionIds.containsKey(token) && group != null) {

			if (tokenToGroup.containsKey(token)) {
				if (tokenToGroup.get(token).equals(group)) {
					groupChange(group, token);
				}
			} else {
				tokenToGroup.put(token, group);
			}

			addUser(token, user, group);

			for (String connectionId : tokenToConnectionIds.get(token)) {
				addUserToMap(group, user, connectionIdToMap.get(connectionId));
			}
		}
	}

	public void userLogout(String user, String token, String group) {
		if (tokenToUsers.containsKey(token)) {
			tokenToUsers.get(token).remove(user);
			removeUser(token, user, group);

			for (String connectionId : tokenToConnectionIds.get(token)) {
				removeUserFromMap(group, user,
						connectionIdToMap.get(connectionId));
			}
		}
	}

	public void groupChange(String group, String token) {
		if (tokenToGroup.containsKey(token)) {
			String oldGroup = tokenToGroup.get(token);
			if (oldGroup != null) {
				for (String user : tokenToUsers.get(token)) {
					for (String connectionId : tokenToConnectionIds.get(token)) {
						String map = connectionIdToMap.get(connectionId);
						removeUserFromMap(oldGroup, user, map);
					}
				}
			}

			tokenToGroup.put(token, group);

			EventRegistryFactory
					.getInstance()
					.getEventRegistry()
					.addEvent(localUserDomain,
							new GroupChangeEvent(group, token));

			for (String user : tokenToUsers.get(token)) {
				for (String connectionId : tokenToConnectionIds.get(token)) {
					addUserToMap(group, user,
							connectionIdToMap.get(connectionId));
				}
			}
		}
	}

	private void addUser(String token, String user, String group) {
		Vector<String> users = tokenToUsers.get(token);
		if (users == null) {
			users = new Vector<String>();
			tokenToUsers.put(token, users);
		}

		if (!users.contains(user)) {
			users.add(user);

			Event event = new UserLoginEvent(user, token, group);

			EventRegistryFactory.getInstance().getEventRegistry()
					.addEvent(localUserDomain, event);

			saveMapForUser(token, user);
		}
	}

	private void saveMapForUser(String token, String user) {
		Vector<String> connectionIds = tokenToConnectionIds.get(token);
		for (String connectionId : connectionIds) {
			String map = connectionIdToMap.get(connectionId);
			MysqlConnector.getInstance().saveMapHistory(token, map, user);
		}
	}

	private void removeUser(String token, String user, String group) {
		Vector<String> users = tokenToUsers.get(token);
		if (users != null && users.contains(user)) {
			users.remove(user);

			Event event = new UserLogoutEvent(user, token, group);

			EventRegistryFactory.getInstance().getEventRegistry()
					.addEvent(localUserDomain, event);
		}
	}

	private void addUserToMap(String group, String user, String map) {
		EventRegistryFactory.getInstance().getEventRegistry()
				.addEvent(groupDomain, new UserJoinMapEvent(user, group, map));
	}

	private void removeUserFromMap(String group, String user, String map) {
		EventRegistryFactory.getInstance().getEventRegistry()
				.addEvent(groupDomain, new UserLeaveMapEvent(user, group, map));
	}

	public boolean isUserOnline(String user, String map) {
		String token = null;
		for (String key : tokenToUsers.keySet()) {
			if (tokenToUsers.get(key).contains(user)) {
				token = key;
			}
		}

		if (token != null) {
			Vector<String> connectionIds = tokenToConnectionIds.get(token);
			if (connectionIds != null) {
				for (String connectionId : connectionIds) {
					if (map.equals(connectionIdToMap.get(connectionId))) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public Vector<String> getOnlineUsersForGroup(String group, String map) {
		// finc connectionIds for map
		Vector<String> connectionIds = new Vector<String>();
		for (String key : connectionIdToMap.keySet()) {
			if (connectionIdToMap.get(key).equals(map)) {
				connectionIds.add(key);
			}
		}

		// find tokens which matches the group and have a connection to the map
		Vector<String> tokens = new Vector<String>();
		for (String key : tokenToConnectionIds.keySet()) {
			if (group.equals(tokenToGroup.get(key))) {
				Vector<String> conIdsForToken = tokenToConnectionIds.get(key);
				for (String conId : connectionIds) {
					if (conIdsForToken.contains(conId)) {
						tokens.add(key);
					}
				}
			}
		}

		Vector<String> users = new Vector<String>();
		for (String t : tokens) {
			users.addAll(tokenToUsers.get(t));
		}

		return users;
	}

	public void handleCenterNodeEvent(CenterNodeEvent event) {
		Vector<String> connectionIds = tokenToConnectionIds.get(event
				.getToken());

		System.err.println("Center Node Event: " + event.getNodeId() + ", "
				+ event.getToken());
		if (connectionIds != null)
			System.err.println("sending to " + connectionIds.size()
					+ " clients");

		if (connectionIds != null && connectionIds.size() > 0) {
			for (String connectionId : connectionIds) {
				String map = connectionIdToMap.get(connectionId);
				if (map.equals(event.getMap())) {
					event.setConnectionId(connectionId);

					Domain dom = DomainFactory
							.getDomain(EventServiceDomains.GRAPHCHANGEDOMAIN);

					EventRegistryFactory.getInstance().getEventRegistry()
							.addEvent(dom, event);
				}
			}
		}
	}
}
