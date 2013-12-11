package de.kuei.metafora.server.planningtool;

import java.util.Vector;

import javax.servlet.http.HttpServlet;

import de.kuei.metafora.server.planningtool.couchdb.DocUploadService;
import de.kuei.metafora.server.planningtool.mysql.ChannelDescription;
import de.kuei.metafora.server.planningtool.mysql.MysqlConnector;
import de.kuei.metafora.server.planningtool.mysql.MysqlInitConnector;
import de.kuei.metafora.server.planningtool.mysql.ServerDescription;
import de.kuei.metafora.server.planningtool.xmpp.XMLInput;
import de.kuei.metafora.server.planningtool.xmpp.XMPPListener;
import de.kuei.metafora.xmppbridge.xmpp.NameConnectionMapper;
import de.kuei.metafora.xmppbridge.xmpp.ServerConnection;
import de.kuei.metafora.xmppbridge.xmpp.XmppMUC;
import de.kuei.metafora.xmppbridge.xmpp.XmppMUCManager;
import de.novanic.eventservice.service.registry.EventRegistryFactory;
import de.novanic.eventservice.service.registry.user.UserManager;
import de.novanic.eventservice.service.registry.user.UserManagerFactory;

public class StartupServlet extends HttpServlet {

	public static String planninggraphformat = "http://static.metafora-project.info/dtd/planningtoolgraph.dtd";
	public static String commonformat = "http://static.metafora-project.info/dtd/commonformat.dtd";
	public static String planningtoolformat = "http://static.metafora-project.info/dtd/planningtoolelement.dtd";
	public static String sending_tool = "PLANNING_TOOL";
	public static String lasadName = "LASAD";
	public static boolean logged = true;
	public static String metafora = "METAFORA";
	public static String tomcatserver = "https://metafora-project.de";
	public static String apache = "http://metafora-project.de";
	public static String xmpp = "metafora-project.de";
	public static String reflectionChannel = "analysis";

	public static XmppMUC logger;
	public static XmppMUC analysis;
	public static XmppMUC command;

	public void init() {
		System.err.println("PlanningTool StartupServlet init...");

		UserManager manager = UserManagerFactory.getInstance().getUserManager(
				EventRegistryFactory.getInstance().getEventRegistry()
						.getConfiguration());
		manager.getUserActivityScheduler().addTimeoutListener(
				de.kuei.metafora.server.planningtool.util.UserManager
						.getInstance());

		System.err.println("Timeout listener registerd....");

		MysqlInitConnector.getInstance().loadData("PlanningTool");

		System.err.println("Loading mysql init parameter....");

		sending_tool = MysqlInitConnector.getInstance().getParameter(
				"SENDING_TOOL");

		lasadName = MysqlInitConnector.getInstance().getParameter("LASAD");

		System.err.println("PlanningTool: Lasad receiving tool: " + lasadName);

		reflectionChannel = MysqlInitConnector.getInstance().getParameter(
				"ReflectionChannel");

		metafora = MysqlInitConnector.getInstance().getParameter("METAFORA");

		if (MysqlInitConnector.getInstance().getParameter("logged")
				.toLowerCase().equals("false")) {
			logged = false;
		}

		ServerDescription apacheServer = MysqlInitConnector.getInstance()
				.getAServer("apache");

		apache = apacheServer.getServer();

		System.err.println("SENDING_TOOL: " + sending_tool);
		System.err.println("METAFORA: " + metafora);
		System.err.println("logged: " + logged);

		System.err.println("Loading mysql init server data...");

		ServerDescription couchDBServer = MysqlInitConnector.getInstance()
				.getAServer("couchdb");
		ServerDescription tomcatServer = MysqlInitConnector.getInstance()
				.getAServer("tomcat");

		StartupServlet.tomcatserver = tomcatServer.getServer();

		System.err.println("Config DocUploadService...");

		// config DocUploadService
		DocUploadService.tomcatserver = tomcatServer.getServer();

		DocUploadService.server = couchDBServer.getServer();
		DocUploadService.user = couchDBServer.getUser();
		DocUploadService.password = couchDBServer.getPassword();

		System.err.println("Config XMPP...");

		// configure xmpp
		Vector<ServerDescription> xmppServers = MysqlInitConnector
				.getInstance().getServer("xmpp");

		// should be the same server but different accounts
		if (xmppServers.size() > 0)
			xmpp = xmppServers.firstElement().getServer();

		for (ServerDescription xmppServer : xmppServers) {
			System.err.println("XMPP server: " + xmppServer.getServer());
			System.err.println("XMPP user: " + xmppServer.getUser());
			System.err.println("XMPP password: " + xmppServer.getPassword());
			System.err.println("XMPP device: " + xmppServer.getDevice());
			System.err.println("Modul: " + xmppServer.getModul());

			System.err.println("Starting XMPP connection...");

			NameConnectionMapper.getInstance().createConnection(
					xmppServer.getConnectionName(), xmppServer.getServer(),
					xmppServer.getUser(), xmppServer.getPassword(),
					xmppServer.getDevice());

			if (xmppServer.getConnectionName().equals("PlanningToolInput")) {
				System.err.println("PlanningToolInput found");
				NameConnectionMapper.getInstance()
						.getConnection(xmppServer.getConnectionName())
						.addPacketListener(new XMLInput());
			} else {
				NameConnectionMapper.getInstance()
						.getConnection(xmppServer.getConnectionName())
						.addPacketListener(new XMPPListener());
			}

			NameConnectionMapper.getInstance()
					.getConnection(xmppServer.getConnectionName()).login();
		}

		Vector<ChannelDescription> channels = MysqlInitConnector.getInstance()
				.getXMPPChannels();

		for (ChannelDescription channeldesc : channels) {
			ServerConnection connection = NameConnectionMapper.getInstance()
					.getConnection(channeldesc.getConnectionName());

			if (connection == null) {
				System.err.println("StartupServlet: Unknown connection: "
						+ channeldesc.getUser());
				continue;
			}

			System.err.println("Joining channel " + channeldesc.getChannel()
					+ " as " + channeldesc.getAlias());

			XmppMUC muc = XmppMUCManager.getInstance().getMultiUserChat(
					channeldesc.getChannel(), channeldesc.getAlias(),
					connection);
			muc.join(0);

			if (channeldesc.getChannel().equals("logger")) {
				System.err.println("StartupServlet: logger configured.");
				logger = muc;
			} else if (channeldesc.getChannel().equals("analysis")) {
				System.err.println("StartupServlet: analysis configured.");
				analysis = muc;
			} else if (channeldesc.getChannel().equals("command")) {
				System.err.println("StartupServlet: command configured.");
				command = muc;
			}
		}

		System.err.println("Config MySQL...");

		// init mysql
		ServerDescription mysqlServer = MysqlInitConnector.getInstance()
				.getAServer("mysql");

		MysqlConnector.url = "jdbc:mysql://" + mysqlServer.getServer()
				+ "/metafora?useUnicode=true&characterEncoding=UTF-8";
		MysqlConnector.user = mysqlServer.getUser();
		MysqlConnector.password = mysqlServer.getPassword();
		MysqlConnector.getInstance().loadAllNetsFromDatabase();
	}

	@Override
	public void destroy() {
		Vector<ServerDescription> xmppServers = MysqlInitConnector
				.getInstance().getServer("xmpp");

		for (ServerDescription xmppServer : xmppServers) {
			NameConnectionMapper.getInstance()
					.getConnection(xmppServer.getConnectionName()).disconnect();
		}
	}
}
