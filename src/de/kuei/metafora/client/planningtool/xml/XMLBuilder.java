package de.kuei.metafora.client.planningtool.xml;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;

import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.PlanningTool;
import de.kuei.metafora.client.planningtool.PlanningToolWidget;
import de.kuei.metafora.client.planningtool.xml.cfcreator.Classification;
import de.kuei.metafora.client.planningtool.xml.cfcreator.CommonFormatCreator;
import de.kuei.metafora.client.planningtool.xml.cfcreator.Role;
import de.kuei.metafora.shared.ActivityType;

public class XMLBuilder {

	// i18n
	final static Languages language = GWT.create(Languages.class);
	private static XMLBuilder instance = null;

	public static XMLBuilder getInstance() {
		if (instance == null) {
			instance = new XMLBuilder();
		}
		return instance;
	}

	/**
	 * @param users
	 *            enthält Paare von eindeutiger ID (in 0. Koordinate) und IP (in
	 *            1. Koordinate)
	 */
	private XMLBuilder() {
	}

	/**
	 * Erzeugt einen XML-wohlgeformten String entsprechend einem
	 * "action"-Element im commonformat.dtd mit einem node-"object"
	 * 
	 * @param classification
	 * @param actionType
	 * @param nodeId
	 * @param color
	 * @param position
	 * @param nodeText
	 * @param pictureUrl
	 * @param toolUrl
	 * @param categorie
	 * @param name
	 * @return XML-String eines "action"-Elements im commonformat.dtd mit dem
	 *         type aus buildXmlForNode
	 */
	public String buildNodeAction(Classification classification,
			String actionType, String nodeId, String color, int[] position,
			String nodeText, String pictureUrl, String toolUrl,
			String categorie, String name, String scalefactor,
			ActivityType activityType) {

		CommonFormatCreator cfcreator = null;
		cfcreator = new CommonFormatCreator(System.currentTimeMillis(),
				classification, actionType, PlanningTool.getLogged());
		cfcreator.addContentProperty("SENDING_TOOL",
				PlanningTool.getSendingTool());

		String objectType = buildXmlForNode(nodeId, color, position, nodeText,
				pictureUrl, toolUrl, categorie, name, scalefactor);

		cfcreator.setObject(nodeId, objectType);
		for (String user : PlanningTool.getUsers()) {
			cfcreator.addUser(user, PlanningTool.getToken(), Role.originator);
		}
		cfcreator.addContentProperty("GROUP_ID", PlanningTool.getGroup());
		cfcreator.addContentProperty("CHALLENGE_ID",
				PlanningTool.getChallengeId());
		cfcreator.addContentProperty("CHALLENGE_NAME",
				PlanningTool.getChallengeName());
		cfcreator.addContentProperty("ACTIVITY_TYPE", activityType.toString());
		if ((activityType.equals(ActivityType.MODIFY_STATE_FINISHED))
				|| (activityType.equals(ActivityType.MODIFY_STATE_STARTED))) {
			cfcreator.addContentProperty("NAME", name.toString());
		}
		if (activityType.equals(ActivityType.MODIFY_TEXT)) {
			cfcreator.addContentProperty("NODE_TEXT", nodeText);
		}

		return cfcreator.getDocument();
	}

	/**
	 * Erzeugt einen XML-wohlgeformten String entsprechend einem
	 * "action"-Element im commonformat.dtd mit einem edge-"object"
	 * 
	 * @param classification
	 * @param actionType
	 * @param edgeId
	 * @param startId
	 * @param endId
	 * @param type
	 * @param label
	 * @param activityType
	 * @return XML-String eines "action"-Elements im commonformat.dtd mit dem
	 *         type aus buildXmlForEdge
	 */
	public String buildEdgeAction(Classification classification,
			String actionType, String edgeId, String startId, String endId,
			String type, ActivityType activityType) {

		String objectType = buildXmlForEdge(edgeId, startId, endId, type);

		CommonFormatCreator cfcreator = null;
		cfcreator = new CommonFormatCreator(System.currentTimeMillis(),
				classification, actionType, PlanningTool.getLogged());
		cfcreator.addContentProperty("SENDING_TOOL",
				PlanningTool.getSendingTool());

		cfcreator.setObject(edgeId, objectType);

		for (String user : PlanningTool.getUsers()) {
			cfcreator.addUser(user, PlanningTool.getToken(), Role.originator);
		}
		cfcreator.addContentProperty("GROUP_ID", PlanningTool.getGroup());
		cfcreator.addContentProperty("CHALLENGE_ID",
				PlanningTool.getChallengeId());
		cfcreator.addContentProperty("CHALLENGE_NAME",
				PlanningTool.getChallengeName());
		cfcreator.addContentProperty("ACTIVITY_TYPE", activityType.toString());

		return cfcreator.getDocument();
	}

	public String buildAreasizeAction(int width, int height) {

		String objectType = buildXmlForAreasizeControl(width, height);

		CommonFormatCreator cfcreator = null;
		cfcreator = new CommonFormatCreator(System.currentTimeMillis(),
				Classification.other, "UPDATE_FRAMEWORK",
				PlanningTool.getLogged());
		cfcreator.addContentProperty("SENDING_TOOL",
				PlanningTool.getSendingTool());

		cfcreator.setObject(PlanningToolWidget.getInstance().getGraphName(),
				objectType);

		return cfcreator.getDocument();
	}

	private String buildXmlForAreasizeControl(int width, int height) {

		String w = null, h = null;
		w = width + "";
		h = height + "";

		if (w.contains(".")) {
			w = w.substring(0, w.indexOf("."));
		}
		if (w.contains(",")) {
			w = w.substring(0, w.indexOf(","));
		}

		if (h.contains(".")) {
			h = h.substring(0, h.indexOf("."));
		}
		if (w.contains(",")) {
			h = h.substring(0, h.indexOf(","));
		}

		Document doc = XMLParser.parse("<object></object>");

		Element ctrl = (Element) (doc.getDocumentElement()).appendChild(doc
				.createElement("ctrl"));

		Element areasize = (Element) ctrl.appendChild(doc
				.createElement("areasize"));
		areasize.setAttribute("x", "" + w);
		areasize.setAttribute("y", "" + h);

		return doc.toString();
	}

	/**
	 * Erzeugt einen String entsprechend einem node-"object" in der
	 * planningtoolgraph.dtd
	 * 
	 * @param nodeId
	 *            ID des Knoten im Planning Tool
	 * @param color
	 *            Farbe des Knoten im Planning Tool
	 * @param position
	 *            Position des Knoten im Planning Tool: x-Koordinate in 0 und
	 *            y-Koordinate in 1
	 * @param nodeText
	 *            Knotentext im Planning Tool
	 * @param pictureUrl
	 * @param toolUrl
	 * @param categorie
	 * @param name
	 * @return xml-konformer String eines "object" wie in planningtoolgraph.dtd
	 */
	private String buildXmlForNode(String nodeId, String color, int[] position,
			String nodeText, String pictureUrl, String toolUrl,
			String categorie, String name, String scalefactor) {

		Document doc = XMLParser.parse("<object></object>");

		Element elNode = (Element) (doc.getDocumentElement()).appendChild(doc
				.createElement("node"));
		elNode.setAttribute("id", nodeId);
		Element elGraphics = (Element) elNode.appendChild(doc
				.createElement("graphics"));

		Element elScalefactor = (Element) elGraphics.appendChild(doc
				.createElement("scalefactor"));
		elScalefactor.setAttribute("value", scalefactor);

		Element elBordercolor = (Element) elGraphics.appendChild(doc
				.createElement("bordercolor"));
		elBordercolor.setAttribute("value", color);
		Element elPosition = (Element) elGraphics.appendChild(doc
				.createElement("position"));
		elPosition.setAttribute("x", "" + position[0]);
		elPosition.setAttribute("y", "" + position[1]);

		if (!nodeText.equals("")) {
			Element elText = (Element) elNode.appendChild(doc
					.createElement("text"));
			elText.setAttribute("value", nodeText);
		}

		Element elProperties = (Element) elNode.appendChild(doc
				.createElement("properties"));
		if (!pictureUrl.equals("")) {
			Element elPictureUrl = (Element) elProperties.appendChild(doc
					.createElement("pictureurl"));
			elPictureUrl.setAttribute("value", pictureUrl);
		}
		if (!toolUrl.equals("")) {
			Element elTool = (Element) elProperties.appendChild(doc
					.createElement("tool"));
			elTool.setAttribute("value", toolUrl);
		}
		Element elCategorie = (Element) elProperties.appendChild(doc
				.createElement("categorie"));
		elCategorie.setAttribute("value", categorie);
		Element elName = (Element) elProperties.appendChild(doc
				.createElement("name"));
		elName.setAttribute("value", name);

		return doc.toString();
	}

	/**
	 * Erzeugt einen String entsprechend einem edge-"object" in der
	 * planningtoolgraph.dtd
	 * 
	 * @param kantenId
	 *            ID der Kante im Planning Tool
	 * @param startKnotenId
	 *            Startknoten der Kante im Planning Tool
	 * @param endKnotenId
	 *            Endknoten der Kante im Planning Tool
	 * @param type
	 * @param label
	 * @return xml-konformer String eines edge-"object" wie in
	 *         planningtoolgraph.dtd
	 */
	private String buildXmlForEdge(String kantenId, String startKnotenId,
			String endKnotenId, String type) {
		Document doc = XMLParser.parse("<object></object>");

		Element elEdge = (Element) (doc.getDocumentElement()).appendChild(doc
				.createElement("edge"));
		elEdge.setAttribute("id", kantenId);

		Element elStart = (Element) elEdge.appendChild(doc
				.createElement("start"));
		elStart.setAttribute("value", startKnotenId);
		Element elEnd = (Element) elEdge.appendChild(doc.createElement("end"));
		elEnd.setAttribute("value", endKnotenId);
		Element elType = (Element) elEdge
				.appendChild(doc.createElement("type"));
		elType.setAttribute("value", type);

		return doc.toString();
	}

	public String buildDiscussAction(String imageUrl, String nodeId) {
		CommonFormatCreator creator = null;
		try {
			creator = new CommonFormatCreator(System.currentTimeMillis(),
					Classification.other, "CREATE_REFERABLE_OBJECT",
					PlanningTool.getLogged());
			creator.setObject("0", "SHARE");
			creator.addProperty("OBJECT_HOME_TOOL",
					PlanningTool.getSendingTool());
			// Base64 base = new Base64();
			String graph64 = URL.encode(PlanningToolWidget.getInstance()
					.getGraphName());
			// base.encodeStringForUrl(PlanningToolWidget
			// .getInstance().getGraphName());
			creator.addProperty("REFERENCE_URL", PlanningTool.getTomcatServer()
					+ "/planningtoolsolo/?map=" + graph64 + "&centerNode="
					+ URL.encode(nodeId));
			creator.addContentProperty("SENDING_TOOL",
					PlanningTool.getSendingTool());
			creator.addContentProperty("RECEIVING_TOOL",
					PlanningTool.getLasadName());

			for (String localUser : PlanningTool.getUsers()) {
				creator.addUser(localUser, PlanningTool.getToken(),
						Role.originator);
			}

			creator.addProperty("TEXT", language.HereIsMyModel()+" "
					+ PlanningToolWidget.getInstance().getGraphName());
			for (String user : PlanningTool.getUsers()) {
				String allUsers = "";
				if (allUsers.equals(""))
					allUsers = user;
				else
					allUsers += "|" + user;
				creator.addProperty("CREATOR", allUsers);
			}

			if (imageUrl.startsWith("http://")) {
				creator.addProperty("VIEW_URL", imageUrl);
			} else {
				creator.addProperty("VIEW_URL", GWT.getHostPageBaseURL()
						+ imageUrl);
			}

			creator.addContentProperty("GROUP_ID", PlanningTool.getGroup());
			creator.addContentProperty("CHALLENGE_ID",
					PlanningTool.getChallengeId());
			creator.addContentProperty("CHALLENGE_NAME",
					PlanningTool.getChallengeName());
		} catch (Exception e) {
		}
		return creator.getDocument();
	}

	public String buildToolAction(String url, String id) {
		CommonFormatCreator creator = null;
		creator = new CommonFormatCreator(System.currentTimeMillis(),
				Classification.other, "DISPLAY_STATE_URL",
				PlanningTool.getLogged());
		for (String user : PlanningTool.getUsers()) {
			creator.addUser(user, PlanningTool.getToken(), Role.originator);
		}
		creator.addContentProperty("RECEIVING_TOOL", PlanningTool.getMetafora());
		creator.addContentProperty("SENDING_TOOL",
				PlanningTool.getSendingTool());

		creator.setObject("0", "ELEMENT");
		creator.addProperty("NODE_ID", id);
		creator.addProperty("REFERENCE_URL", url);
		creator.addContentProperty("GROUP_ID", PlanningTool.getGroup());
		creator.addContentProperty("CHALLENGE_ID",
				PlanningTool.getChallengeId());
		creator.addContentProperty("CHALLENGE_NAME",
				PlanningTool.getChallengeName());
		return creator.getDocument();
	}

}
