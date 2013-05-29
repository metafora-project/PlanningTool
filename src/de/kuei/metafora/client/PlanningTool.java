package de.kuei.metafora.client;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import de.kuei.metafora.client.planningtool.PlanningToolWidget;
import de.kuei.metafora.client.planningtool.eventServiceListener.impl.GraphListenerImpl;
import de.kuei.metafora.client.planningtool.eventServiceListener.impl.GroupListenerImpl;
import de.kuei.metafora.client.planningtool.eventServiceListener.impl.LocalUserListenerImpl;
import de.kuei.metafora.client.planningtool.eventServiceListener.impl.MapShareListenerImpl;
import de.kuei.metafora.client.planningtool.gui.ClientErrorHandler;
import de.kuei.metafora.client.planningtool.serverlink.GraphLink;
import de.kuei.metafora.client.planningtool.serverlink.GraphLinkAsync;
import de.kuei.metafora.client.planningtool.util.UrlDecoder;
import de.kuei.metafora.shared.Base64;
import de.kuei.metafora.shared.eventservice.EventServiceDomains;
import de.kuei.metafora.shared.eventservice.MapFilter;
import de.kuei.metafora.shared.eventservice.TokenFilter;
import de.novanic.eventservice.client.ClientHandler;
import de.novanic.eventservice.client.event.RemoteEventService;
import de.novanic.eventservice.client.event.RemoteEventServiceFactory;
import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.domain.DomainFactory;

public class PlanningTool implements EntryPoint {

	// object needed to implement i18n through Languages interface
	final static Languages language = GWT.create(Languages.class);

	private final GraphLinkAsync graphLink = GWT.create(GraphLink.class);

	/**
	 * The unique token for this client
	 */
	private static String token = null;

	/**
	 * local users of this client
	 */
	private static Vector<String> users = new Vector<String>();

	/**
	 * local users group name
	 */
	private static String groupId = null;
	/**
	 * local users challenge (id)
	 */
	private static String challengeId = null;
	/**
	 * local users challenge (name)
	 */
	private static String challengeName = null;

	private static String connectionId = null;

	private static String centerNode = null;

	private static RemoteEventService remoteEventService;

	private static Domain localUserDomain = DomainFactory
			.getDomain(EventServiceDomains.USERDOMAIN);
	private static Domain graphShareDomain = DomainFactory
			.getDomain(EventServiceDomains.GRAPHSHAREDOMAIN);
	private static Domain graphChangeDomain = DomainFactory
			.getDomain(EventServiceDomains.GRAPHCHANGEDOMAIN);
	private static Domain groupDomain = DomainFactory
			.getDomain(EventServiceDomains.GROUPDOMAIN);

	private static GraphListenerImpl graphListener;
	private static LocalUserListenerImpl localUserListener;
	private static MapShareListenerImpl mapShareListener;
	private static GroupListenerImpl groupListener;

	private static String sendingTool = "PLANNING_TOOL";
	private static String lasadName = "LASAD";
	private static boolean logged = true;
	private static String tomcatserver = "https://metaforaserver.ku.de";
	private static String metafora = "METAFORA";
	private static String reflectionChannel = "analysis";
	private static String xmpp = "metaforaserver.ku.de";
	private static String apache = "http://metaforaserver.ku.de";

	private static boolean cavillag = false;

	private String map;

	private boolean apacheFlag = false;
	private boolean xmppFlag = false;
	private boolean channelFlag = false;
	private boolean connectionIdFlag = false;
	private boolean tomcatFlag = false;

	public void onModuleLoad() {
		map = UrlDecoder.getParameter("planningCard");
		if (map == null)
			map = UrlDecoder.getParameter("ptMap");
		if (map == null || map.length() == 0) {
			map = "--default";
		}

		if (map.startsWith("--")) {
			map = map.substring(2, map.length());
		} else {
			try {
				Base64 base = new Base64();
				if(base.isBaseEncoded(map)){
					map = base.decodeString(map);
				}
			} catch (Exception e) {
				// ignore
			}
		}

		PlanningTool.token = UrlDecoder.getParameter("token");

		PlanningTool.centerNode = UrlDecoder.getParameter("centerNode");

		// get users from url parameters
		String user = UrlDecoder.getParameter("user");
		PlanningTool.users.add(user);
		Map<String, List<String>> parameters = Window.Location
				.getParameterMap();
		Set<String> keySet = parameters.keySet();
		for (String key : keySet) {
			if (key.startsWith("otherUser")) {
				List<String> values = parameters.get(key);
				for (String u : values) {
					if (u != null)
						PlanningTool.users.add(URL.decode(u));
				}
			}
		}

		PlanningTool.groupId = UrlDecoder.getParameter("groupId");

		PlanningTool.challengeId = UrlDecoder.getParameter("challengeId");

		PlanningTool.challengeName = UrlDecoder.getParameter("challengeName");

		if (UrlDecoder.getParameter("cavillag") != null
				&& UrlDecoder.getParameter("cavillag").toLowerCase()
						.equals("true")) {
			PlanningTool.cavillag = true;
		}

		remoteEventService = RemoteEventServiceFactory.getInstance()
				.getRemoteEventService();

		graphLink.getSendingTool(new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				ClientErrorHandler.showErrorMessage(caught);
			}

			@Override
			public void onSuccess(String result) {
				sendingTool = result;
			}
		});

		graphLink.getLasadName(new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				ClientErrorHandler.showErrorMessage(caught);
			}

			@Override
			public void onSuccess(String result) {
				lasadName = result;
			}
		});

		graphLink.getLogged(new AsyncCallback<Boolean>() {

			@Override
			public void onFailure(Throwable caught) {
				ClientErrorHandler.showErrorMessage(caught);
			}

			@Override
			public void onSuccess(Boolean result) {
				logged = result;
			}
		});

		graphLink.getTomcatServer(new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				ClientErrorHandler.showErrorMessage(caught);
			}

			@Override
			public void onSuccess(String result) {
				tomcatserver = result;
				tomcatFlag = true;
				initGui();

			}
		});

		graphLink.getMetafora(new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				ClientErrorHandler.showErrorMessage(caught);
			}

			@Override
			public void onSuccess(String result) {
				metafora = result;
			}
		});

		graphLink.getApacheServer(new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				ClientErrorHandler.showErrorMessage(caught);
			}

			@Override
			public void onSuccess(String result) {
				apache = result;
				apacheFlag = true;
				initGui();
			}
		});

		graphLink.getXmppServer(new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				ClientErrorHandler.showErrorMessage(caught);
			}

			@Override
			public void onSuccess(String result) {
				xmpp = result;
				xmppFlag = true;
				initGui();
			}
		});

		graphLink.getReflectionChannel(new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				ClientErrorHandler.showErrorMessage(caught);
			}

			@Override
			public void onSuccess(String result) {
				reflectionChannel = result;
				channelFlag = true;
				initGui();
			}
		});

		localUserListener = new LocalUserListenerImpl();
		graphListener = GraphListenerImpl.getInstance();
		mapShareListener = new MapShareListenerImpl();
		groupListener = new GroupListenerImpl();

		localUserListener.setFilter(new TokenFilter(token));
		graphListener.setFilter(new MapFilter(map));
		mapShareListener.setFilter(new MapFilter(map));

		remoteEventService.addListener(localUserDomain, localUserListener,
				new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						Window.alert("localUserDomain: " + caught.getMessage());
					}

					@Override
					public void onSuccess(Void result) {
					}
				});

		remoteEventService.addListener(graphShareDomain, mapShareListener,
				new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						Window.alert("graphShareDomain: " + caught.getMessage());
					}

					@Override
					public void onSuccess(Void result) {
					}
				});

		remoteEventService.addListener(graphChangeDomain, graphListener,
				new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						Window.alert("graphChangeDomain: "
								+ caught.getMessage());
					}

					@Override
					public void onSuccess(Void result) {
					}
				});

		remoteEventService.addListener(groupDomain, groupListener,
				new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						Window.alert("groupDomain: " + caught.getMessage());
					}

					@Override
					public void onSuccess(Void result) {
					}
				});

		RemoteEventServiceFactory.getInstance().requestClientHandler(
				new AsyncCallback<ClientHandler>() {

					@Override
					public void onFailure(Throwable caught) {
						Window.alert("requestClientHandler: "
								+ caught.getMessage());
					}

					@Override
					public void onSuccess(ClientHandler result) {
						connectionId = result.getConnectionId();

						if (connectionId == null) {
							// TODO: find the f... bug!
							Date date = new Date();
							connectionId = "ISNOGOOD" + date.getTime()
									+ Random.nextInt();
						}

						startClientSession();
					}
				});

		// TODO: eventfilter
	}

	public void initGui() {
		if (xmppFlag && apacheFlag && channelFlag && connectionIdFlag
				&& tomcatFlag) {
			RootLayoutPanel rootLayout = RootLayoutPanel.get();

			PlanningToolWidget planning = PlanningToolWidget
					.createInstance(map);

			Widget pw = planning.getWidget();
			pw.setWidth("100%");
			pw.setHeight("100%");

			rootLayout.add(pw);
		}
	}

	public void startClientSession() {
		graphLink.startClientSession(connectionId, token,
				new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						ClientErrorHandler.showErrorMessage(caught);
					}

					@Override
					public void onSuccess(Void result) {
						connectionIdFlag = true;
						initGui();
					}
				});
	}

	public static void addUser(String user) {
		users.add(user);
		PlanningToolWidget.getInstance().updateFileHeader();
	}

	public static void removeUser(String user) {
		users.remove(user);
		PlanningToolWidget.getInstance().updateFileHeader();
	}

	public static Vector<String> getUsers() {
		return users;
	}

	public static String getToken() {
		return token;
	}

	public static void setGroup(String group) {
		groupId = group;
	}

	public static String getGroup() {
		return groupId;
	}

	public static String getChallengeName() {
		return challengeName;
	}

	public static String getChallengeId() {
		return challengeId;
	}

	public static void updateMap(String mapname) {
		mapShareListener.setFilter(new MapFilter(mapname));
		graphListener.setFilter(new MapFilter(mapname));
	}

	public static String getSendingTool() {
		return sendingTool;
	}

	public static String getLasadName() {
		return lasadName;
	}

	public static boolean getLogged() {
		return logged;
	}

	public static String getTomcatServer() {
		return tomcatserver;
	}

	public static String getMetafora() {
		return metafora;
	}

	public static String getApacheServer() {
		return apache;
	}

	public static String getXMPPServer() {
		return xmpp;
	}

	public static String getReflectionChannel() {
		return reflectionChannel;
	}

	public static String getConnectionId() {
		return connectionId;
	}

	public static boolean isCavillag() {
		return cavillag;
	}

	public static String getCenterNode() {
		return centerNode;
	}

	public static void setCenterNode(String nodeId) {
		centerNode = nodeId;
	}
}
