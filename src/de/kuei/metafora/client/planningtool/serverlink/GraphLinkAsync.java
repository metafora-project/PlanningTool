package de.kuei.metafora.client.planningtool.serverlink;

import java.util.Vector;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GraphLinkAsync {

	void actionReceived(String xml, String token, String map,
			String connectionId, AsyncCallback<String> callback);

	void getNodeTypes(String locale, String token, Vector<String> users,
			String groupId, String challengeId, String challengeName,
			boolean cavillag, AsyncCallback<String[]> callback);

	void saveAsMap(String mapname, String savename, String token,
			Vector<String> users, String groupId, String challengeId,
			String challengeName, String connectionId,
			AsyncCallback<Boolean> callback);

	void saveVersionMap(String mapname, String token, Vector<String> users,
			String groupId, String challengeId, String challengeName,
			AsyncCallback<String> callback);

	void selectMap(String mapname, String token, Vector<String> users,
			String groupId, String challengeId, String challengeName,
			String connectionId, AsyncCallback<Void> callback);

	void deleteMap(String mapname, String token, Vector<String> users,
			String groupId, String challengeId, String challengeName,
			String connectionId, AsyncCallback<Boolean> callback);

	void createMap(String mapname, String token, Vector<String> users,
			String groupId, String challengeId, String challengeName,
			String connectionId, AsyncCallback<Boolean> callback);

	void getMapnames(Vector<String> users, String group, String token,
			AsyncCallback<String[]> callback);

	void startClientSession(String connectionId, String token,
			AsyncCallback<Void> callback);

	void assignGroup(Vector<String> users, String group, String token,
			String connectionId, AsyncCallback<Void> callback);

	void removeGroup(String group, String token, String connectionId,
			AsyncCallback<Void> callback);

	void getGroups(String token, AsyncCallback<Vector<String>> callback);

	void getLogged(AsyncCallback<Boolean> callback);

	void getSendingTool(AsyncCallback<String> callback);

	void getMetafora(AsyncCallback<String> callback);

	void getTomcatServer(AsyncCallback<String> callback);

	void getApacheServer(AsyncCallback<String> callback);

	void getReflectionChannel(AsyncCallback<String> callback);

	void getXmppServer(AsyncCallback<String> callback);

	void getLasadName(AsyncCallback<String> callback);
}
