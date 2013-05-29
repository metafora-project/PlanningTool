package de.kuei.metafora.client.planningtool.serverlink;

import java.util.Vector;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface UsersGroupLinkAsync {

	void getOnlineUsersForGroup(String group, String token,
			String connectionId, String map,
			AsyncCallback<Vector<String>> callback);

	void isUserOnline(String user, String token, String connectionId,
			String map, AsyncCallback<Boolean> callback);

}
