package de.kuei.metafora.server.planningtool.util;

import java.util.HashMap;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.kuei.metafora.server.planningtool.StartupServlet;
import de.kuei.metafora.server.planningtool.mysql.MysqlConnector;
import de.kuei.metafora.server.planningtool.mysql.MysqlInitConnector;
import de.kuei.metafora.server.planningtool.mysql.ServerDescription;
import de.kuei.metafora.server.planningtool.xml.Classification;
import de.kuei.metafora.server.planningtool.xml.CommonFormatCreator;
import de.kuei.metafora.server.planningtool.xml.Role;
import de.kuei.metafora.server.planningtool.xml.XMLException;
import de.kuei.metafora.server.planningtool.xml.XMLUtils;

public class NodeTypeManager {

	private static NodeTypeManager instance = null;

	public static NodeTypeManager getInstance() {
		if (instance == null) {
			instance = new NodeTypeManager();
		}
		return instance;
	}

	private String urlprefix = "http://static.metafora-project.info";

	private Vector<String> languages;

	private NodeTypeManager() {
		languages = MysqlConnector.getInstance().getNodeLanguages();
		urlprefix = StartupServlet.apache;
	}

	public String[] getNodeTypes(String shortlangname, Vector<String> users,
			String token, String groupId, String challengeName,
			String challengeId) {

		if (languages.isEmpty() || !languages.contains(shortlangname)) {
			return new String[0];
		}

		HashMap<String, Vector<String>> template = null;
		try {
			int cid = Integer.parseInt(challengeId);
			String tpl = MysqlConnector.getInstance().getChallengeTemplate(cid);
			if (tpl != null) {
				TemplateParser parser = new TemplateParser();
				parser.parseTemplate(tpl);
				template = parser.getTemplate();
			}
		} catch (Exception e) {
			System.err.println("NodeTypeManager: " + e.getMessage());
		}

		Vector<String[]> icons = MysqlConnector.getInstance()
				.getNodeTypesForLanguage(shortlangname);

		if (template != null) {
			icons = filterNodes(icons, template);
		}

		String[] xmldescs = new String[icons.size()];
		for (int i = 0; i < icons.size(); i++) {
			String[] icon = icons.get(i);

			if (!icon[0].startsWith("http:")) {
				icon[0] = urlprefix + icon[0];
			}

			if (icon[1] != null && icon[1].startsWith("<")) {
				int pos = icon[1].indexOf('>', 1);
				if (pos > 0) {
					String server = icon[1].substring(1, pos);
					Vector<ServerDescription> servers = MysqlInitConnector
							.getInstance().getServer(server);
					if (servers.size() > 0) {
						ServerDescription serverdesc = servers.firstElement();
						icon[1] = serverdesc.getServer()
								+ icon[1].substring(pos + 1, icon[1].length());

						System.err.println("Tool: " + icon[1]);
					}
				}
			}

			xmldescs[i] = getXmlForUrl(icon[0], icon[1], icon[2], icon[3], i,
					icon[4], icon[5], icon[6], users, token, groupId,
					challengeName, challengeId);
		}
		return xmldescs;
	}

	private Vector<String[]> filterNodes(Vector<String[]> icons,
			HashMap<String, Vector<String>> template) {

		Vector<String> categoryOrder = template.get("categoryorder");

		HashMap<String, HashMap<String, String[]>> cards = new HashMap<String, HashMap<String, String[]>>();
		for (String[] icon : icons) {
			HashMap<String, String[]> category = null;
			if (cards.containsKey(icon[5])) {
				category = cards.get(icon[5]);
			} else {
				category = new HashMap<String, String[]>();
				cards.put(icon[5], category);
			}

			category.put(icon[6], icon);
		}

		Vector<String[]> filteredIcons = new Vector<String[]>();
		for (String categoryId : categoryOrder) {
			Vector<String> category = template.get(categoryId);
			for (String nodeId : category) {
				HashMap<String, String[]> cat = cards.get(categoryId);
				if (cat != null) {
					String[] card = cat.get(nodeId);
					if (card != null) {
						filteredIcons.add(card);
					}
				} else {
					System.err.println("NodeTypeManager: filterNodes: "
							+ categoryId + " not found!");
				}
			}
		}

		return filteredIcons;
	}

	private String getXmlForUrl(String imageUrl, String toolUrl,
			String nameStr, String categorieStr, int index, String scalefactor,
			String cateogryId, String iconid, Vector<String> users,
			String token, String groupId, String challengeName,
			String challengeId) {

		try {
			CommonFormatCreator creator = null;
			creator = new CommonFormatCreator(System.currentTimeMillis(),
					Classification.create, "CREATE_NODE", StartupServlet.logged);
			creator.addContentProperty("SENDING_TOOL",
					StartupServlet.sending_tool);

			for (String user : users) {
				creator.addUser(user, token, Role.originator);
			}

			Document content = XMLUtils.createDocument();

			Element obj = content.createElement("object");
			content.appendChild(obj);

			Element node = content.createElement("node");
			node.setAttribute("id", "palette_node_" + index);
			obj.appendChild(node);

			Element graphics = content.createElement("graphics");
			node.appendChild(graphics);

			Element scale = content.createElement("scalefactor");
			scale.setAttribute("value", scalefactor);
			graphics.appendChild(scale);

			Element bordercolor = content.createElement("bordercolor");
			bordercolor.setAttribute("value", "#AAAAAA");
			graphics.appendChild(bordercolor);

			Element position = content.createElement("position");
			position.setAttribute("x", "100");
			position.setAttribute("y", "100");
			graphics.appendChild(position);

			Element text = content.createElement("text");
			text.setAttribute("value", "");
			node.appendChild(text);

			Element properties = content.createElement("properties");
			node.appendChild(properties);

			Element pictureurl = content.createElement("pictureurl");
			pictureurl.setAttribute("value", imageUrl);
			properties.appendChild(pictureurl);

			Element tool = content.createElement("tool");
			tool.setAttribute("value", toolUrl);
			properties.appendChild(tool);

			Element categorie = content.createElement("categorie");
			categorie.setAttribute("value", categorieStr);
			properties.appendChild(categorie);

			Element name = content.createElement("name");
			name.setAttribute("value", nameStr);
			properties.appendChild(name);
			String contentxml = XMLUtils
					.documentToString(content,
							StartupServlet.planningtoolformat);
			creator.setObject("palette_node_" + index, contentxml);

			creator.addContentProperty("GROUP_ID", groupId);
			creator.addContentProperty("CHALLENGE_ID", challengeId);
			creator.addContentProperty("CHALLENGE_NAME", challengeName);
			creator.addContentProperty("CATEGORY_ID", cateogryId);
			creator.addContentProperty("ICON_ID", iconid);

			return creator.getDocument();

		} catch (XMLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
