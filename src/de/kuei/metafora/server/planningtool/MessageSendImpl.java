package de.kuei.metafora.server.planningtool;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Vector;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.kuei.metafora.client.planningtool.serverlink.MessageSend;
import de.kuei.metafora.server.planningtool.xml.Classification;
import de.kuei.metafora.server.planningtool.xml.CommonFormatCreator;
import de.kuei.metafora.server.planningtool.xml.Role;
import de.kuei.metafora.server.planningtool.xml.XMLException;
import de.kuei.metafora.server.planningtool.xml.XMLUtils;

public class MessageSendImpl extends RemoteServiceServlet implements
		MessageSend {

	@Override
	public void sendCommand(String xml, String token) {
		xml = xml.replaceAll("[$][iI][pP]", token);

		if (StartupServlet.command != null) {
			StartupServlet.command.sendMessage(xml);
		} else {
			System.err.println("MessageSendImpl: command is null!");
		}
	}

	@Override
	public void sendShareCommand(Vector<String> users, String groupId,
			String challengeId, String challengeName, String token) {
		CommonFormatCreator creator;
		try {
			creator = new CommonFormatCreator(System.currentTimeMillis(),
					Classification.other, "SHARE_THIS_MODEL",
					StartupServlet.logged);
			creator.addContentProperty("SENDING_TOOL",
					StartupServlet.sending_tool);

			for (String user : users) {
				creator.addUser(user, token, Role.originator);
			}
			creator.addContentProperty("GROUP_ID", groupId);
			creator.addContentProperty("CHALLENGE_ID", challengeId);
			creator.addContentProperty("CHALLENGE_NAME", challengeName);

			if (StartupServlet.logger != null) {
				StartupServlet.logger.sendMessage(creator.getDocument());
			} else {
				System.err.println("MessageSendImpl: command is null!");
			}

			if (StartupServlet.command != null) {
				StartupServlet.command.sendMessage(creator.getDocument());
			} else {
				System.err.println("MessageSendImpl: command is null!");
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendLogAction(String xml, String token) {
		xml = xml.replaceAll("[$][iI][pP]", token);

		String newxml = null;
		Document doc;
		try {
			doc = XMLUtils.parseXMLString(xml, true);
			// Set server time as timestamp
			Node action = doc.getElementsByTagName("action").item(0);
			action.getAttributes().getNamedItem("time")
					.setNodeValue(System.currentTimeMillis() + "");
			newxml = XMLUtils.documentToString(doc,
					StartupServlet.commonformat);
		} catch (XMLException e) {
			e.printStackTrace();
		}

		if (StartupServlet.logger != null) {
			StartupServlet.logger.sendMessage(newxml);
		} else {
			System.err.println("MessageSendImpl: logger is null!");
		}
	}

	@Override
	public void sendToLogAndCommand(String xml, String token) {
		xml = xml.replaceAll("[$][iI][pP]", token);

		String newxml = null;
		Document doc;
		try {
			doc = XMLUtils.parseXMLString(xml, true);
			// Set server time as timestamp
			Node action = doc.getElementsByTagName("action").item(0);
			action.getAttributes().getNamedItem("time")
					.setNodeValue(System.currentTimeMillis() + "");
			newxml = XMLUtils.documentToString(doc,
					StartupServlet.commonformat);
		} catch (XMLException e) {
			e.printStackTrace();
		}

		if (StartupServlet.logger != null) {
			StartupServlet.logger.sendMessage(newxml);
		} else {
			System.err.println("MessageSendImpl: logger is null!");
		}

		if (StartupServlet.command != null) {
			StartupServlet.command.sendMessage(newxml);
		} else {
			System.err.println("MessageSendImpl: command is null!");
		}
	}

	/*
	 * <?xml version="1.0" encoding="utf-8" standalone="no"?> <action
	 * time="1326909617639"> <actiontype classification="USER_INTERACTION"
	 * succeeded="UNKNOWN" type="OPEN_MAP"/> <user id="all users with this IP"
	 * role="controller"/> <object id="default"/> </action>
	 */
	@Override
	public void sendToChatCommand(String graphname, String nodeid, String url,
			String local, String token, Vector<String> users, String groupId,
			String challengeId, String challengeName, String viewurl,
			String text) {
		try {
			CommonFormatCreator creator = new CommonFormatCreator(
					System.currentTimeMillis(), Classification.create,
					"CREATE_REFERABLE_OBJECT", StartupServlet.logged);
			creator.setObject("0", "REFERABLE_OBJECT");
			creator.addProperty("OBJECT_HOME_TOOL", StartupServlet.sending_tool);
			creator.addContentProperty("SENDING_TOOL",
					StartupServlet.sending_tool);
			creator.addContentProperty("RECEIVING_TOOL",
					StartupServlet.metafora);

			for (String user : users) {
				creator.addUser(user, token, Role.originator);
			}

			// Base64 base = new Base64();
			// String encgname = base.encodeStringForUrl(graphname);

			String encgname = null;
			try {
				encgname = URLEncoder.encode(graphname, "UTF8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			/*
			 * if (url.contains("map")) { int spos = url.indexOf("map"); spos--;
			 * int epos = url.indexOf("&", spos + 1); if (epos == -1) { url =
			 * url.substring(0, spos); } else { url = url.substring(0, spos) +
			 * url.substring(epos, url.length()); } }
			 */

			if (url.contains("?")) {
				url += "&ptMap=" + encgname;
			} else {
				url += "?ptMap=" + encgname;
			}

			creator.addProperty("TEXT", text);
			creator.addProperty("VIEW_URL", viewurl);
			creator.addProperty("REFERENCE_URL", url);

			creator.addContentProperty("GROUP_ID", groupId);
			creator.addContentProperty("CHALLENGE_ID", challengeId);
			creator.addContentProperty("CHALLENGE_NAME", challengeName);

			if (StartupServlet.logger != null) {
				StartupServlet.logger.sendMessage(creator.getDocument());
			} else {
				System.err.println("MessageSendImpl: logger is null!");
			}

			if (StartupServlet.command != null) {
				StartupServlet.command.sendMessage(creator.getDocument());
			} else {
				System.err.println("MessageSendImpl: command is null!");
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	public static void sendStateSavedLandmark(String nodeId, String url,
			String graphName, String user, String homeTool, String groupId,
			String challengeId, String challengeName) {
		// send landmark to analysis channel
		Document doc;
		try {

			doc = XMLUtils.createDocument();
			Element action = doc.createElement("action");
			action.setAttribute("time", System.currentTimeMillis() + "");
			doc.appendChild(action);

			Element actiontype = doc.createElement("actiontype");
			actiontype.setAttribute("type", "LANDMARK");
			actiontype.setAttribute("classification", "create");
			actiontype.setAttribute("logged", StartupServlet.logged + "");
			action.appendChild(actiontype);

			Element userElement = doc.createElement("user");
			userElement.setAttribute("id", user);
			userElement.setAttribute("role", "originator");
			action.appendChild(userElement);

			Element object = doc.createElement("object");
			object.setAttribute("id", nodeId);
			object.setAttribute("type", "PLANNING_TOOL_NODE");
			action.appendChild(object);

			Element properties = doc.createElement("properties");
			object.appendChild(properties);

			Element property;
			if (url != null) {
				property = doc.createElement("property");
				properties.appendChild(property);
				property.setAttribute("name", "RESOURCE_URL");
				property.setAttribute("value", url);
			}

			if (graphName != null) {
				property = doc.createElement("property");
				properties.appendChild(property);
				property.setAttribute("name", "PLANNING_TOOL_MAP");
				property.setAttribute("value", graphName);
			}

			property = doc.createElement("property");
			properties.appendChild(property);
			property.setAttribute("name", "TOOL");
			property.setAttribute("value", homeTool);

			Element content = doc.createElement("content");
			action.appendChild(content);

			String cardName = "";
			if (homeTool.toLowerCase().contains("LASAD".toLowerCase()))
				cardName = "Discussion";
			else if (homeTool.toLowerCase().contains(
					"PLANNING_TOOL".toLowerCase()))
				cardName = "Planning Tool";
			else if (homeTool.toLowerCase().contains("PIKI".toLowerCase()))
				cardName = "PiKi";
			else if (homeTool.toLowerCase().contains("JUGGLER".toLowerCase()))
				cardName = "3d Juggler";
			else if (homeTool.toLowerCase().contains("MATH".toLowerCase()))
				cardName = "3d Math";
			else if (homeTool.toLowerCase().contains("SUS_CITY".toLowerCase()))
				cardName = "Sus City";
			else if (homeTool.toLowerCase().contains("EXPRESSER".toLowerCase()))
				cardName = "eXpresser";

			Element description = doc.createElement("description");
			CDATASection cdata = doc.createCDATASection(user + " started exploring the \""
					+ cardName + "\" resource card " + nodeId + ".");
			description.appendChild(cdata);
			content.appendChild(description);

			Element contentProperties = doc.createElement("properties");
			content.appendChild(contentProperties);

			Element property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "LANDMARK_TYPE");
			property2.setAttribute("value", "ACTIVITY");

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "L2L2_TAG");
			property2.setAttribute("value", "MUTUAL_ENGAGEMENT");
			
			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "ACTIVITY_TYPE");
			property2.setAttribute("value", "resource state saved");

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "SENDING_TOOL");
			property2.setAttribute("value", StartupServlet.sending_tool);

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "GROUP_ID");
			property2.setAttribute("value", groupId);

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "CHALLENGE_ID");
			property2.setAttribute("value", challengeId);

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "CHALLENGE_NAME");
			property2.setAttribute("value", challengeName);

			String xmlXMPPMessage = XMLUtils.documentToString(doc,
					StartupServlet.commonformat);

			if (StartupServlet.analysis != null) {
				StartupServlet.analysis.sendMessage(xmlXMPPMessage);
			} else {
				System.err.println("MessageSendImpl: analysis is null!");
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}
}
