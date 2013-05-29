package de.kuei.metafora.client.planningtool.serverlink;

import java.util.Vector;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MessageSendAsync {

	void sendCommand(String xml, String token, AsyncCallback<Void> callback);

	void sendShareCommand(Vector<String> users, String groupId, String challengeId, String challengeName, String token, AsyncCallback<Void> callback);

	void sendLogAction(String xml, String token, AsyncCallback<Void> callback);

	public void sendToLogAndCommand(String xml, String token,
			AsyncCallback<Void> callback);

	void sendToChatCommand(String graphname, String nodeid, String url,
			String local, String token, Vector<String> users, String groupId,
			String challengeId, String challengeName, String viewurl,
			String text,
			AsyncCallback<Void> callback);
}
