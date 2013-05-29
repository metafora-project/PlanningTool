package de.kuei.metafora.client.planningtool.serverlink;

import java.util.Vector;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("command")
public interface MessageSend extends RemoteService {

	public void sendCommand(String xml, String token);

	public void sendShareCommand(Vector<String> users, String groupId,
			String challengeId, String challengeName, String token);

	public void sendLogAction(String xml, String token);

	public void sendToLogAndCommand(String xml, String token);

	public void sendToChatCommand(String graphname, String nodeid, String url,
			String local, String token, Vector<String> users, String groupId,
			String challengeId, String challengeName, String viewurl,
			String text);
}
