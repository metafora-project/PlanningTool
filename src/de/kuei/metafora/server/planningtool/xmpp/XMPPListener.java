package de.kuei.metafora.server.planningtool.xmpp;

import java.net.URLDecoder;
import java.util.Collection;
import java.util.Date;
import java.util.Vector;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.packet.DelayInfo;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.kuei.metafora.server.planningtool.MessageSendImpl;
import de.kuei.metafora.server.planningtool.graphData.DirectedGraph;
import de.kuei.metafora.server.planningtool.graphData.GraphManager;
import de.kuei.metafora.server.planningtool.graphData.GraphNode;
import de.kuei.metafora.server.planningtool.util.UserManager;
import de.kuei.metafora.server.planningtool.xml.XMLException;
import de.kuei.metafora.server.planningtool.xml.XMLUtils;
import de.kuei.metafora.shared.event.graph.CenterNodeEvent;
import de.kuei.metafora.shared.event.graph.NodeEvent;
import de.kuei.metafora.shared.eventservice.EventServiceDomains;
import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.domain.DomainFactory;
import de.novanic.eventservice.service.registry.EventRegistryFactory;

public class XMPPListener implements PacketListener {

	private Domain graphChangeDomain = DomainFactory
			.getDomain(EventServiceDomains.GRAPHCHANGEDOMAIN);

	public void newMessage(String user, String message, String chat, Date time) {
		try {

			// do not handle logger messages
			if (chat.contains("logger"))
				return;

			// do not handle logger messages
			if (chat.contains("analysis"))
				return;

			try {
				message = message.replaceAll("\n", "");
			} catch (Exception e) {
				System.err
						.println("Planning Tool XMPPListener.newMessage(): exception: "
								+ e.toString());
			}

			if (message == null)
				return;

			if (message
					.matches(".*[aA][cC][tT][iI][oO][nN].*[tT][iI][mM][eE].*")) {
				// handle as action
				try {
					Document doc = XMLUtils.parseXMLString(message, false);

					String classification = null;
					String type = null;
					NodeList ats = doc.getElementsByTagName("actiontype");
					if (ats.getLength() == 1) {
						Node actiontype = ats.item(0);
						classification = actiontype.getAttributes()
								.getNamedItem("classification")
								.getTextContent();
						type = actiontype.getAttributes().getNamedItem("type")
								.getTextContent();
					} else {
						System.err
								.println("PlanningTool: XMPPListener: Invalid XML! "
										+ message);
					}

					boolean handleMessage = false;

					if (type != null && classification != null
							&& classification.toLowerCase().equals("modify")
							&& type.toLowerCase().equals("modify_node_url")) {
						handleMessage = true;
						System.err
								.println("PlanningTool: XMPPListener: Modify node url found!");
					} else if (type.toLowerCase().equals("login")
							|| type.toLowerCase().equals("logout")
							|| type.toLowerCase().equals("set_teamname")) {
						String userName = null;
						String token = null;

						NodeList users = doc.getElementsByTagName("user");
						for (int i = 0; i < users.getLength(); i++) {
							Node userNode = users.item(i);
							String role = userNode.getAttributes()
									.getNamedItem("role").getTextContent();
							if (role.toLowerCase().equals("originator")) {
								userName = userNode.getAttributes()
										.getNamedItem("id").getTextContent();
								token = userNode.getAttributes()
										.getNamedItem("ip").getTextContent();
								break;
							}
						}

						String groupName = null;
						NodeList properties = doc
								.getElementsByTagName("property");
						for (int i = 0; i < properties.getLength(); i++) {
							Node property = properties.item(i);
							String pname = property.getAttributes()
									.getNamedItem("name").getTextContent();
							if (pname.toLowerCase().equals("group_id")) {
								groupName = property.getAttributes()
										.getNamedItem("value").getTextContent();
								break;
							}
						}

						if (type.toLowerCase().equals("login")) {
							if (userName != null && token != null) {
								UserManager.getInstance().userLogin(userName,
										token, groupName);
							} else {
								System.err
										.println("Planning Tool XMPPListener: Invalid XML message. User or token are null");
								return;
							}
						} else if (type.toLowerCase().equals("logout")) {
							if (userName != null && token != null) {
								UserManager.getInstance().userLogout(userName,
										token, groupName);
							} else {
								System.err
										.println("Planning Tool XMPPListener: Invalid XML message. User or token are null!");
								return;
							}
						} else {
							if (groupName != null && token != null) {
								UserManager.getInstance().groupChange(
										groupName, token);
							} else {
								System.err
										.println("Planning Tool XMPPListener: Invalid XML message. GroupName or token are null!");
								return;
							}
						}
					} else if (type.toLowerCase().equals("center_node")) {

						Vector<String> tokens = new Vector<String>();

						NodeList users = doc.getElementsByTagName("user");
						for (int i = 0; i < users.getLength(); i++) {
							Node userNode = users.item(i);
							String role = userNode.getAttributes()
									.getNamedItem("role").getTextContent();
							if (role.toLowerCase().equals("receiver")) {
								String userToken = userNode.getAttributes()
										.getNamedItem("ip").getTextContent();
								if (!tokens.contains(userToken)) {
									tokens.add(userToken);
								}
							}
						}

						if (tokens.size() > 0) {
							String plan = null;
							String nodeId = null;

							NodeList properties = doc
									.getElementsByTagName("property");
							for (int i = 0; i < properties.getLength(); i++) {
								Node property = properties.item(i);
								String pname = property.getAttributes()
										.getNamedItem("name").getTextContent();
								if (pname.toLowerCase().equals(
										"planningtool_node")) {
									nodeId = property.getAttributes()
											.getNamedItem("value")
											.getTextContent();
								} else if (pname.toLowerCase().equals("plan")) {
									plan = property.getAttributes()
											.getNamedItem("value")
											.getTextContent();
								}
							}

							if (plan != null && nodeId != null) {
								for (String token : tokens) {
									CenterNodeEvent centerEvent = new CenterNodeEvent(
											nodeId, plan, token);
									UserManager.getInstance()
											.handleCenterNodeEvent(centerEvent);
								}
							}
						}

						return;
					} else {
						return;
					}

					String token = null;

					NodeList users = doc.getElementsByTagName("user");
					String localUser = null;
					Vector<String> localUsers = new Vector<String>();
					for (int i = 0; i < users.getLength(); i++) {
						Node u = users.item(i);

						Node nuserip = u.getAttributes().getNamedItem("ip");
						String ip = null;

						if (nuserip != null) {
							ip = nuserip.getTextContent();
						} else {
							System.err
									.println("PlanningTool: Invalid message! User IP is null!");
							return;
						}

						Node nuserid = u.getAttributes().getNamedItem("id");
						if (nuserid != null) {
							localUser = nuserid.getTextContent();
							localUsers.add(localUser);
						} else {
							System.err
									.println("Planning Tool XMPPListener: Invalid message! User ID is null");
							return;
						}

						if (ip != null) {
							token = ip;
						} else {
							System.err
									.println("PlanningTool: XMPPListener: modifyNodeURL: token == null");
						}
					}
					if (handleMessage && token != null) {
						NodeList objects = doc.getElementsByTagName("object");
						if (objects.getLength() == 1) {
							Node object = objects.item(0);

							Node objectid = object.getAttributes()
									.getNamedItem("id");
							String nodeid = null;

							if (objectid != null) {
								nodeid = objectid.getTextContent();
							} else {
								System.err
										.println("PlanningTool: Invalid message! ID is null!");
								return;
							}

							String map = null;
							String url = null;
							String homeTool = null;
							String groupId = null;
							String challengeId = null;
							String challengeName = null;

							NodeList properties = doc
									.getElementsByTagName("property");
							for (int i = 0; i < properties.getLength(); i++) {
								Node property = properties.item(i);
								String n = property.getAttributes()
										.getNamedItem("name").getTextContent();
								if (n.toLowerCase().equals("resource_url")) {
									url = property.getAttributes()
											.getNamedItem("value")
											.getTextContent();
								} else if (n.toLowerCase().equals(
										"planning_tool_map")) {
									map = property.getAttributes()
											.getNamedItem("value")
											.getTextContent();
								} else if (n.toLowerCase().equals(
										"sending_tool")) {
									homeTool = property.getAttributes()
											.getNamedItem("value")
											.getTextContent();
								} else if (n.toLowerCase().equals("group_id")) {
									groupId = property.getAttributes()
											.getNamedItem("value")
											.getTextContent();
								} else if (n.toLowerCase().equals(
										"challenge_id")) {
									challengeId = property.getAttributes()
											.getNamedItem("value")
											.getTextContent();
								} else if (n.toLowerCase().equals(
										"challenge_name")) {
									challengeName = property.getAttributes()
											.getNamedItem("value")
											.getTextContent();
								} else {
									System.err
											.println("PlanningTool: XMPPListener: unknown property "
													+ n);
								}
							}

							System.err
									.println("PlanningTool: XMPPListener: NodeId: "
											+ nodeid
											+ ", URL: "
											+ url
											+ ", Map: " + map);

							if (nodeid != null && map != null && url != null) {
								nodeid = URLDecoder.decode(nodeid, "UTF8");

								map = URLDecoder.decode(map, "UTF8");

								DirectedGraph graph = GraphManager
										.getGraph(map);

								if (graph != null) {
									System.err
											.println("PlanningTool: XMPPListener: NodeId: "
													+ nodeid
													+ ", Map: "
													+ map
													+ ", Graph: "
													+ graph.getName());

									GraphNode node = graph.getNodeForId(nodeid);

									if (node != null) {
										System.err
												.println("PlanningTool: XMPPListener: Update tool url. NodeId: "
														+ nodeid
														+ ", Map: "
														+ map
														+ ", Graph: "
														+ graph.getName()
														+ ", Node: "
														+ node.getId());
										node.setTool(url);

										String nodeXml = node.toXml();

										Document nodeDoc = XMLUtils
												.parseXMLString(nodeXml, false);

										Node action = nodeDoc
												.getElementsByTagName("action")
												.item(0);

										NodeList ulist = nodeDoc
												.getElementsByTagName("user");
										for (int i = 0; i < ulist.getLength(); i++) {
											Node u = ulist.item(i);
											Node parent = u.getParentNode();
											parent.removeChild(u);
										}

										for (String lUser : localUsers) {
											Element orignode = nodeDoc
													.createElement("user");
											orignode.setAttribute("id", lUser);
											orignode.setAttribute("ip", token);
											orignode.setAttribute("role",
													"originator");
											action.appendChild(orignode);
										}

										Node actiontype = nodeDoc
												.getElementsByTagName(
														"actiontype").item(0);
										actiontype.getAttributes()
												.getNamedItem("classification")
												.setNodeValue("modify");
										actiontype.getAttributes()
												.getNamedItem("type")
												.setNodeValue("MODIFY_NODE");

										action.getAttributes()
												.getNamedItem("time")
												.setNodeValue(
														System.currentTimeMillis()
																+ "");

										String modifyXml = XMLUtils
												.documentToString(nodeDoc);

										EventRegistryFactory
												.getInstance()
												.getEventRegistry()
												.addEvent(
														graphChangeDomain,
														new NodeEvent(
																modifyXml, map));

										MessageSendImpl.sendStateSavedLandmark(
												nodeid, url, map, localUser,
												homeTool, groupId, challengeId,
												challengeName);
									} else {
										System.err
												.println("PlanningTool: XMPPListener: Node not found or invalid node category. Message was dropped. \n"
														+ message);
									}
								} else {
									System.err
											.println("PlanningTool: XMPPListener: Graph not found. Message was dropped. \n"
													+ message);
								}
							} else {
								System.err
										.println("PlanningTool: XMPPListener: Invalid XML received. Message was dropped. \n"
												+ message);
							}

						} else {
							System.err
									.println("PlanningTool: XMPPListener: Invalid XML received. Message was dropped. \n"
											+ message);
						}

					}
				} catch (XMLException e) {
					System.err
							.println("PlanningTool: XMPPListener: Invalid XML received. Message was dropped. \n"
									+ message);
				}
			}
		} catch (Exception e) {
			System.err.println("Planning Tool XMPPListener.newMessage(): "
					+ e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void processPacket(Packet packet) {
		try {
			handlePacket(packet);
		} catch (Exception e) {
			System.err
					.println("PlanningTool: XMPPListener: Handle packet exception! "
							+ e.getMessage());
			e.printStackTrace();
		}
	}

	public void handlePacket(Packet packet) {
		if (packet instanceof Message) {
			Message msg = (Message) packet;

			if (msg.getBody() == null) {
				return;
			}

			String from = msg.getFrom();
			String name = "";
			String chat = from;

			int splitPos = from.indexOf('/');
			if (splitPos > 0) {
				name = from.substring(splitPos + 1, from.length());
				chat = from.substring(0, splitPos);
			}

			Date time = new Date();

			Collection<PacketExtension> extensions = packet.getExtensions();
			for (PacketExtension e : extensions) {
				if (e instanceof DelayInfo) {
					DelayInfo d = (DelayInfo) e;
					time = d.getStamp();
					break;
				} else if (e instanceof DelayInformation) {
					DelayInformation d = (DelayInformation) e;
					time = d.getStamp();
					break;
				}
			}

			newMessage(name, msg.getBody(), chat, time);
		}
	}
}
