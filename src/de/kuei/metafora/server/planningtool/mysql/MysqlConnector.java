package de.kuei.metafora.server.planningtool.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import de.kuei.metafora.server.planningtool.graphData.DirectedGraph;
import de.kuei.metafora.server.planningtool.graphData.GraphManager;

public class MysqlConnector {

	public static String url = "jdbc:mysql://metafora.ku-eichstaett.de/metafora?useUnicode=true&characterEncoding=UTF-8";
	public static String user = "user";
	public static String password = "password";

	private static MysqlConnector instance = null;

	public static synchronized MysqlConnector getInstance() {
		if (instance == null) {
			instance = new MysqlConnector();
		}
		return instance;
	}

	private Connection connection;

	private MysqlConnector() {
		try {
			Class.forName("com.mysql.jdbc.Driver");

			try {
				System.err.println("Try to connect to " + MysqlConnector.url
						+ " as " + MysqlConnector.user + " identified by "
						+ MysqlConnector.password);

				connection = DriverManager.getConnection(MysqlConnector.url,
						MysqlConnector.user, MysqlConnector.password);
			} catch (SQLException e) {
				e.printStackTrace();
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void updateChallengeLastUsed(String challengeId) {
		long time = System.currentTimeMillis();

		String sql = "UPDATE challenge set lastUsed = '" + time
				+ "' where challengeId = " + challengeId;
		try {
			PreparedStatement pst = getConnection().prepareStatement(sql);
			pst.executeUpdate();
		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
	}

	public void loadAllNetsFromDatabase() {
		Vector<String> graphNames = new Vector<String>();

		try {
			PreparedStatement pst = getConnection().prepareStatement(
					"SELECT graphName FROM DirectedGraph");
			ResultSet rs = pst.executeQuery();

			if (rs.first()) {
				do {
					String graphName = rs.getString("graphName");
					if (!graphNames.contains(graphName)) {
						graphNames.add(graphName);
					}
				} while (rs.next());
			}

			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		for (String graphName : graphNames) {
			GraphManager.graphFromDB(graphName);
		}

	}

	public Vector<String> getGroups() {
		Vector<String> groups = new Vector<String>();

		try {
			PreparedStatement pst = getConnection().prepareStatement(
					"SELECT name FROM Teams");
			ResultSet rs = pst.executeQuery();

			rs.first();

			do {
				String group = rs.getString("name");
				if (!groups.contains(group))
					groups.add(group);
			} while (rs.next());

			rs.close();
			pst.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		return groups;
	}

	public Vector<String> getMapNames(Vector<String> users, String group) {
		String sqlUser = "SELECT graphName, name FROM DirectedGraph JOIN (SELECT MapOwners.MapId, MapOwners.UserId, Users.name FROM MapOwners JOIN Users ON MapOwners.UserId = Users.id) AS Owners ON DirectedGraph.graphId = Owners.MapId WHERE name IN (";
		for (int i = 0; i < users.size(); i++) {
			sqlUser += "?";
			if (i < users.size() - 1) {
				sqlUser += ", ";
			}
		}
		sqlUser += ")";

		Vector<String> maps = new Vector<String>();

		try {
			System.err.println("MysqlConnector.getMapNames(): sqlUser: "
					+ sqlUser);
			PreparedStatement pst = getConnection().prepareStatement(sqlUser);
			for (int j = 0; j < users.size(); j++) {
				pst.setString(j + 1, users.get(j));
			}
			ResultSet rs = pst.executeQuery();

			if (rs.first()) {
				do {
					String graphName = rs.getString("graphName");
					if (!maps.contains(graphName))
						maps.add(graphName);
				} while (rs.next());
			}

			rs.close();
			pst.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		try {
			PreparedStatement pst = getConnection()
					.prepareStatement(
							"SELECT graphName, name FROM DirectedGraph JOIN (SELECT MapTeams.MapId, MapTeams.TeamId, Teams.name FROM MapTeams JOIN Teams ON MapTeams.TeamId = Teams.id) AS Teams ON DirectedGraph.graphId = Teams.MapId WHERE name = ?");
			pst.setString(1, group);
			ResultSet rs = pst.executeQuery();

			if (rs.first()) {
				do {
					String graphName = rs.getString("graphName");
					if (!maps.contains(graphName))
						maps.add(graphName);
				} while (rs.next());
			}

			rs.close();
			pst.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		return maps;
	}

	public synchronized DirectedGraph loadNetFromDatabase(String graphName) {
		int graphId, width, height, nodeCounter, edgeCounter;
		DirectedGraph graph = null;

		try {
			PreparedStatement pst = getConnection().prepareStatement(
					"SELECT * FROM DirectedGraph WHERE graphName LIKE ?");
			pst.setString(1, graphName);
			ResultSet rs = pst.executeQuery();

			if (getRowCount(rs) == 0) {
				System.err.println("DirectedGraph " + graphName
						+ " not found on database.");
				rs.close();
				pst.close();
				return null;
			} else if (getRowCount(rs) > 1) {
				System.err.println("Multiple graphs with name " + graphName
						+ " found into database!");
			}

			if (rs.first()) {
				graphId = rs.getInt("graphId");
				width = rs.getInt("width");
				height = rs.getInt("height");
				nodeCounter = rs.getInt("nodeCounter");
				edgeCounter = rs.getInt("edgeCounter");
			} else {
				return null;
			}

			rs.close();
			pst.close();

			pst = getConnection()
					.prepareStatement(
							"SELECT MapOwners.MapId, MapOwners.UserId, Users.name FROM MapOwners JOIN Users ON MapOwners.UserId = Users.id WHERE MapOwners.MapId = ?");
			pst.setInt(1, graphId);
			rs = pst.executeQuery();

			Vector<String> users = new Vector<String>();

			if (rs.first()) {
				do {
					users.add(rs.getString("name"));
				} while (rs.next());
			}

			rs.close();
			pst.close();

			pst = getConnection()
					.prepareStatement(
							"SELECT MapTeams.MapId, MapTeams.TeamId, Teams.name FROM MapTeams JOIN Teams ON MapTeams.TeamId = Teams.id WHERE MapTeams.MapId = ?");
			pst.setInt(1, graphId);
			rs = pst.executeQuery();

			Vector<String> groups = new Vector<String>();

			if (rs.first()) {
				do {
					groups.add(rs.getString("name"));
				} while (rs.next());
			}

			String group = "Metafora";
			if (groups.size() > 0) {
				group = groups.firstElement();
			}

			graph = new DirectedGraph(graphName, width, height, nodeCounter,
					edgeCounter, users, group);

			for (int i = 1; i < groups.size(); i++) {
				graph.addGroupFromDatabase(groups.get(i));
			}

			graph.areaSizeChanged(width, height);

			rs.close();
			pst.close();

			pst = getConnection().prepareStatement(
					"SELECT * FROM GraphNode WHERE graphId = ?");
			pst.setInt(1, graphId);
			rs = pst.executeQuery();

			if (rs.first()) {
				String id = null;
				String xml = null;

				do {
					id = rs.getString("id");
					xml = rs.getString("xml");

					graph.nodeFromDB(id, xml);
				} while (rs.next());
			}

			rs.close();
			pst.close();

			pst = getConnection().prepareStatement(
					"SELECT * FROM GraphEdge WHERE graphId = ?");
			pst.setInt(1, graphId);
			rs = pst.executeQuery();

			if (rs.first()) {
				String id = null;
				String xml = null;

				do {
					id = rs.getString("id");
					xml = rs.getString("xml");

					graph.edgeFromDB(id, xml);
				} while (rs.next());
			}

			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return graph;
	}

	private Connection getConnection() {
		try {
			if (connection == null || !connection.isValid(5)) {
				connection = DriverManager.getConnection(MysqlConnector.url,
						MysqlConnector.user, MysqlConnector.password);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return connection;
	}

	public synchronized Vector<String> getNodeLanguages() {
		try {
			Vector<String> languages = new Vector<String>();

			PreparedStatement pst = getConnection().prepareStatement(
					"SELECT * FROM language");
			ResultSet rs = pst.executeQuery();

			while (rs.next()) {
				String lang = rs.getString("short");
				languages.add(lang);
			}

			rs.close();
			pst.close();

			return languages;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public boolean hasIconRelative() {
		boolean answer = false;
		try {
			PreparedStatement pst = getConnection().prepareStatement(
					"show tables");
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				if (rs.getString(1).equals("iconrelative")) {
					answer = true;
					break;
				}
			}

			rs.close();
			pst.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return answer;
	}

	public synchronized String getChallengeTemplate(int challengeId) {
		String template = null;

		try {
			PreparedStatement pst = getConnection().prepareStatement(
					"SELECT template FROM challenge WHERE challengeId = ?");
			pst.setInt(1, challengeId);
			ResultSet rs = pst.executeQuery();

			int count = getRowCount(rs);

			if (count > 0) {
				rs.first();
				template = rs.getString("template");
			}

			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return template;
	}

	public synchronized Vector<String[]> getNodeTypesForLanguage(
			String shortname) {
		try {
			PreparedStatement pst = getConnection()
					.prepareStatement(
							"SELECT * FROM language JOIN (SELECT TEMP2.*, languagecategory.word as langcat, languagecategory.language as langlang,  languagecategory.scalesize, languagecategory.`order` FROM (SELECT languagecategory.category, languagecategory.language, languagecategory.word, category.scalesize, category.`order` FROM languagecategory JOIN category ON category.id = languagecategory.category) AS languagecategory JOIN (SELECT temp.*, language.short, language.name as langlong FROM language JOIN (SELECT icon.id  AS 'iconid', icon.pictureurl, icon.toolurl, icon.name, icon.category, languagename.language, languagename.word FROM icon JOIN languagename ON icon.name = languagename.name) AS temp ON language.id = temp.language WHERE language.short LIKE ?) AS TEMP2 ON TEMP2.category = languagecategory.category) as TEMP3 on TEMP3.langlang = language.id WHERE language.short LIKE ? ORDER BY `order` ASC");
			pst.setString(1, shortname);
			pst.setString(2, shortname);

			if (hasIconRelative()) {
				pst.clearBatch();
				pst = getConnection()
						.prepareStatement(
								"SELECT * FROM language JOIN (SELECT TEMP2.*, languagecategory.word as langcat, languagecategory.language as langlang,  languagecategory.scalesize, languagecategory.`order` FROM (SELECT languagecategory.category, languagecategory.language, languagecategory.word, category.scalesize, category.`order` FROM languagecategory JOIN category ON category.id = languagecategory.category) AS languagecategory JOIN (SELECT temp.*, language.short, language.name as langlong FROM language JOIN (SELECT iconrelative.id AS 'iconid', iconrelative.pictureurl, iconrelative.toolurl, iconrelative.name, iconrelative.category, languagename.language, languagename.word FROM iconrelative JOIN languagename ON iconrelative.name = languagename.name) AS temp ON language.id = temp.language WHERE language.short LIKE ?) AS TEMP2 ON TEMP2.category = languagecategory.category) as TEMP3 on TEMP3.langlang = language.id WHERE language.short LIKE ? ORDER BY `order` ASC");
				pst.setString(1, shortname);
				pst.setString(2, shortname);
			}

			ResultSet rs = pst.executeQuery();

			Vector<String[]> icons = new Vector<String[]>();

			while (rs.next()) {

				String[] icon = new String[7];

				icon[0] = rs.getString("pictureurl");
				icon[1] = rs.getString("toolurl");
				icon[2] = rs.getString("word");
				icon[3] = rs.getString("langcat");
				icon[4] = rs.getString("scalesize");
				icon[5] = rs.getString("category");
				icon[6] = rs.getString("iconid");

				icons.add(icon);
			}

			rs.close();
			pst.close();

			return icons;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	private int getRowCount(ResultSet rs) {
		int rowCount = 0;
		try {
			if (rs.last()) {
				rowCount = rs.getRow();
				rs.first();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rowCount;
	}

	private int getTeamId(String group) {
		int teamId = -1;

		try {
			PreparedStatement pst = getConnection().prepareStatement(
					"SELECT id FROM Teams WHERE name LIKE ?");
			pst.setString(1, group);
			ResultSet rs = pst.executeQuery();

			if (getRowCount(rs) == 0) {
				System.err
						.println("Group " + group + " not found on database.");
				rs.close();
				pst.close();
				return -1;
			} else if (getRowCount(rs) > 1) {
				System.err.println("Multiple groups with name " + group
						+ " found into database!");
			}

			if (rs.first()) {
				teamId = rs.getInt("id");
			}

			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return teamId;
	}

	private int getUserId(String user) {
		int userId = -1;

		try {
			PreparedStatement pst = getConnection().prepareStatement(
					"SELECT id FROM Users WHERE name LIKE ?");
			pst.setString(1, user);
			ResultSet rs = pst.executeQuery();

			if (getRowCount(rs) == 0) {
				System.err.println("User " + user + " not found on database.");
				rs.close();
				pst.close();
				return -1;
			} else if (getRowCount(rs) > 1) {
				System.err.println("Multiple users with name " + user
						+ " found into database!");
			}

			if (rs.first()) {
				userId = rs.getInt("id");
			}

			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return userId;
	}

	private int getGraphId(String graphName) {
		int graphId = -1;

		try {
			PreparedStatement pst = getConnection().prepareStatement(
					"SELECT graphId FROM DirectedGraph WHERE graphName LIKE ?");
			pst.setString(1, graphName);
			ResultSet rs = pst.executeQuery();

			if (getRowCount(rs) == 0) {
				System.err.println("DirectedGraph " + graphName
						+ " not found on database.");
				rs.close();
				pst.close();
				return -1;
			} else if (getRowCount(rs) > 1) {
				System.err.println("Multiple graphs with name " + graphName
						+ " found into database!");
			}

			if (rs.first()) {
				graphId = rs.getInt("graphId");
			}

			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return graphId;
	}

	private int getNodeId(String graphName, String nodeId) {
		int graphId = getGraphId(graphName);

		if (graphId == -1) {
			System.err.println("Graph " + graphName + " not found!");
			return -1;
		}

		int id = -1;

		try {
			PreparedStatement pst = getConnection()
					.prepareStatement(
							"SELECT nodeId FROM GraphNode WHERE id LIKE ? AND graphId=?");
			pst.setString(1, nodeId);
			pst.setInt(2, graphId);
			ResultSet rs = pst.executeQuery();

			if (getRowCount(rs) == 0) {
				System.err.println("GraphNode " + nodeId
						+ " not found on database.");
				rs.close();
				pst.close();
				return -1;
			}

			if (rs.first()) {
				id = rs.getInt("nodeId");
			}

			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return id;
	}

	private int getEdgeId(String graphName, String edgeId) {
		int graphId = getGraphId(graphName);

		if (graphId == -1) {
			System.err.println("Graph " + graphName + " not found!");
			return -1;
		}

		int id = -1;

		try {
			PreparedStatement pst = getConnection()
					.prepareStatement(
							"SELECT edgeId FROM GraphEdge WHERE id LIKE ? AND graphId=?");
			pst.setString(1, edgeId);
			pst.setInt(2, graphId);
			ResultSet rs = pst.executeQuery();

			if (getRowCount(rs) == 0) {
				System.err.println("GraphEdge " + edgeId
						+ " not found on database.");
				rs.close();
				pst.close();
				return -1;
			} else if (getRowCount(rs) > 1) {
				System.err.println("Multiple edges with name " + edgeId
						+ " found into database!");
			}

			if (rs.first()) {
				id = rs.getInt("edgeId");
			}

			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return id;
	}

	public synchronized void addGroup(String graphName, String group) {
		int graphId = getGraphId(graphName);
		int groupId = getTeamId(group);

		if (groupId == -1 || graphId == -1) {
			System.err
					.println("PlanningTool: MysqlConnector.addGroup: groupId or graphId not found!");
			return;
		}

		try {
			PreparedStatement pst = getConnection().prepareStatement(
					"INSERT INTO MapTeams(MapId, TeamId) VALUES (?, ?)");
			pst.setInt(1, graphId);
			pst.setInt(2, groupId);
			pst.execute();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized void removeGroup(String graphName, String group) {
		int graphId = getGraphId(graphName);
		int groupId = getTeamId(group);

		if (groupId == -1 || graphId == -1) {
			System.err
					.println("PlanningTool: MysqlConnector.removeGroup: groupId or graphId not found!");
			return;
		}

		try {
			PreparedStatement pst = getConnection().prepareStatement(
					"DELETE FROM MapTeams WHERE MapId = ? AND TeamId = ?");
			pst.setInt(1, graphId);
			pst.setInt(2, groupId);
			pst.execute();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized void addUser(String graphName, String user) {
		int graphId = getGraphId(graphName);
		int userId = getUserId(user);

		if (userId == -1 || graphId == -1) {
			System.err
					.println("PlanningTool: MysqlConnector.addGroup: userId or graphId not found!");
			return;
		}

		try {
			PreparedStatement pst = getConnection().prepareStatement(
					"INSERT INTO MapOwners(MapId, UserId) VALUES (?, ?)");
			pst.setInt(1, graphId);
			pst.setInt(2, userId);
			pst.execute();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized void createGraph(String graphName, int width,
			int height, int nodeCounter, int edgeCounter, Vector<String> users,
			String group) {

		if (getGraphId(graphName) != -1) {
			updateGraph(graphName, width, height, nodeCounter, edgeCounter);
			return;

		}

		try {
			PreparedStatement pst = getConnection()
					.prepareStatement(
							"INSERT INTO DirectedGraph(graphName, width, height, nodeCounter, edgeCounter) VALUES (?, ?, ?, ?, ?)");
			pst.setString(1, graphName);
			pst.setInt(2, width);
			pst.setInt(3, height);
			pst.setInt(4, nodeCounter);
			pst.setInt(5, edgeCounter);
			pst.execute();
			pst.close();

			for (String user : users) {
				addUser(graphName, user);
			}

			addGroup(graphName, group);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized void updateGraph(String graphName, int width,
			int height, int nodeCounter, int edgeCounter) {
		int graphId = getGraphId(graphName);

		if (graphId == -1)
			return;

		try {
			PreparedStatement pst = getConnection()
					.prepareStatement(
							"UPDATE DirectedGraph SET width=?, height=?, nodeCounter=?, edgeCounter=? WHERE graphId=?");
			pst.setInt(1, width);
			pst.setInt(2, height);
			pst.setInt(3, nodeCounter);
			pst.setInt(4, edgeCounter);
			pst.setInt(5, graphId);
			pst.executeUpdate();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized void deleteGraph(String graphName) {
		int graphId = getGraphId(graphName);

		if (graphId == -1)
			return;

		try {
			PreparedStatement pst = getConnection().prepareStatement(
					"DELETE FROM DirectedGraph WHERE graphId=?");
			pst.setInt(1, graphId);
			pst.execute();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized void createNode(String graphName, String id,
			String xml, String innerXml) {
		int graphId = getGraphId(graphName);

		if (graphId == -1)
			return;

		try {
			PreparedStatement pst = getConnection()
					.prepareStatement(
							"INSERT INTO GraphNode(graphId, id, xml, innerXml) VALUES (?, ?, ?, ?)");
			pst.setInt(1, graphId);
			pst.setString(2, id);
			pst.setString(3, xml);
			pst.setString(4, innerXml);
			pst.execute();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized void updateNode(String graphName, String id,
			String xml, String innerXml) {
		int graphId = getGraphId(graphName);

		if (graphId == -1)
			return;

		int nodeId = getNodeId(graphName, id);

		if (nodeId == -1)
			return;

		try {
			PreparedStatement pst = getConnection()
					.prepareStatement(
							"UPDATE GraphNode SET xml=?, innerXml=?  WHERE graphId=? AND nodeId=?");
			pst.setString(1, xml);
			pst.setString(2, innerXml);
			pst.setInt(3, graphId);
			pst.setInt(4, nodeId);
			pst.executeUpdate();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized void deleteNode(String graphName, String id) {
		int graphId = getGraphId(graphName);

		if (graphId == -1)
			return;

		int nodeId = getNodeId(graphName, id);

		if (nodeId == -1)
			return;

		try {
			PreparedStatement pst = getConnection().prepareStatement(
					"DELETE FROM GraphNode WHERE graphId=? AND nodeId=?");
			pst.setInt(1, graphId);
			pst.setInt(2, nodeId);
			pst.executeUpdate();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized void createEdge(String graphName, String id,
			String xml, String innerXml, String startNode, String endNode) {

		int graphId = getGraphId(graphName);

		if (graphId == -1)
			return;

		int startId = getNodeId(graphName, startNode);

		if (startId == -1)
			return;

		int endId = getNodeId(graphName, endNode);

		if (endId == -1)
			return;

		try {
			PreparedStatement pst = getConnection()
					.prepareStatement(
							"INSERT INTO GraphEdge(graphId, id, xml, innerXml, startNode, endNode) VALUES (?, ?, ?, ?, ?, ?)");
			pst.setInt(1, graphId);
			pst.setString(2, id);
			pst.setString(3, xml);
			pst.setString(4, innerXml);
			pst.setInt(5, startId);
			pst.setInt(6, endId);
			pst.execute();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized void updateEdge(String graphName, String id,
			String xml, String innerXml) {
		int graphId = getGraphId(graphName);

		if (graphId == -1)
			return;

		int edgeId = getEdgeId(graphName, id);

		if (edgeId == -1)
			return;

		try {
			PreparedStatement pst = getConnection()
					.prepareStatement(
							"UPDATE GraphEdge SET xml=?, innerXml=?  WHERE graphId=? AND edgeId=?");
			pst.setString(1, xml);
			pst.setString(2, innerXml);
			pst.setInt(3, graphId);
			pst.setInt(4, edgeId);
			pst.executeUpdate();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized void deleteEdge(String graphName, String id) {
		int graphId = getGraphId(graphName);

		if (graphId == -1)
			return;

		int edgeId = getEdgeId(graphName, id);

		if (edgeId == -1)
			return;

		try {
			PreparedStatement pst = getConnection().prepareStatement(
					"DELETE FROM GraphEdge WHERE graphId=? AND edgeId=?");
			pst.setInt(1, graphId);
			pst.setInt(2, edgeId);
			pst.executeUpdate();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void saveMapHistory(String token, String map, String user) {
		boolean alreadySaved = false;

		String sqlCheck = "SELECT * FROM `PlanHistory` WHERE `map` LIKE '"
				+ map + "'  AND `token` LIKE '" + token
				+ "' AND `username` LIKE '" + user + "'";

		try {
			PreparedStatement pst = getConnection().prepareStatement(sqlCheck);
			ResultSet rs = pst.executeQuery();
			if (rs.first()) {
				alreadySaved = true;
			}
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (!alreadySaved) {
			long now = System.currentTimeMillis();

			String sql = "INSERT INTO  `PlanHistory` (`map`, `token`, `username`, `time`) VALUES ('"
					+ map + "',  '" + token + "', '" + user + "', " + now + ")";
			try {
				PreparedStatement pst = getConnection().prepareStatement(sql);
				pst.execute();
				pst.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
