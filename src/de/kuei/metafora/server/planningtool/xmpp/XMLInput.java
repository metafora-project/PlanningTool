package de.kuei.metafora.server.planningtool.xmpp;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.kuei.metafora.server.planningtool.util.UserManager;
import de.kuei.metafora.server.planningtool.xml.XMLUtils;

public class XMLInput implements PacketListener {

	@Override
	public void processPacket(Packet packet) {
		System.err.println("Packet received.");
		if (packet instanceof Message) {
			Message message = (Message) packet;
			System.err.println("Packet is Message: " + message.getBody());

			String messageText = message.getBody();
			messageText = messageText.replaceAll("\n", "");

			if (messageText
					.matches(".*[aA][cC][tT][iI][oO][nN].*[tT][iI][mM][eE].*")) {
				System.err.println("Message seems to be an action.");

				try {
					XMLUtils.parseXMLString(messageText, true);
					System.err
							.println("Test parsing with validiating successfull.");
				} catch (Exception e) {
					System.err.println(e.getMessage());
					System.err.println("Try to repair message...");
					messageText = tryToRepairMessage(messageText);
					System.err.println("Repaired message: " + messageText);
				}

				String map = null;
				String token = "ThisIsATestToken";

				try {
					Document doc = XMLUtils.parseXMLString(messageText, false);
					System.err
							.println("Test parsing without validiating successfull.");

					NodeList nodes = doc.getElementsByTagName("user");
					if (nodes.getLength() > 0) {
						Node node = nodes.item(0);
						String role = node.getAttributes().getNamedItem("role")
								.getNodeValue();
						if(role != null && role.toLowerCase().equals("originator")){
							String ip = node.getAttributes().getNamedItem("ip")
									.getNodeValue();
							if(ip != null){
								token = ip;
								System.err.println("Token found: "+token);
							}
						}
					}
					
					nodes = doc.getElementsByTagName("object");
					if (nodes.getLength() > 0) {
						Node node = nodes.item(0);
						String id = node.getAttributes().getNamedItem("id")
								.getNodeValue();
						System.err.println("Node id: " + id);

						int pos = id.lastIndexOf("_");
						if (pos > 0) {
							pos = id.lastIndexOf('_', pos - 1);
							map = id.substring(0, pos);
							System.err.println("Map found: " + map);
						}

					} else {
						System.err.println("No object found!");
					}

					if (map != null) {
						System.err.println("Insert action into system");
						UserManager.getInstance().actionReceived(
								token, null, messageText, map);
					}
				} catch (Exception e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	private String tryToRepairMessage(String message) {
		int pos = -1;
		boolean searchEnd = false;

		do {
			pos = message.indexOf('\'', pos + 1);

			if (pos > 0) {
				if (searchEnd) {
					char c = message.charAt(pos + 1);
					if (c == ' ' || c == '/' || c == '?' || c == '>') {
						searchEnd = false;
					} else {
						message = message.substring(0, pos)
								+ message.substring(pos + 1, message.length());
					}
				} else {
					char c = message.charAt(pos - 1);
					if (c == '=' || c == ' ') {
						searchEnd = true;
					}
				}
			}
		} while (pos > 0);

		return message;
	}

}
