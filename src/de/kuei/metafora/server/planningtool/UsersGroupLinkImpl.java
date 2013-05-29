package de.kuei.metafora.server.planningtool;

import java.util.Vector;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.kuei.metafora.client.planningtool.serverlink.UsersGroupLink;
import de.kuei.metafora.server.planningtool.util.UserManager;

public class UsersGroupLinkImpl extends RemoteServiceServlet implements
		UsersGroupLink {

	@Override
	public boolean isUserOnline(String user, String token, String connectionId,
			String map) {
		return UserManager.getInstance().isUserOnline(user, map);
	}

	@Override
	public Vector<String> getOnlineUsersForGroup(String group, String token,
			String connectionId, String map) {
		return UserManager.getInstance().getOnlineUsersForGroup(group, map);
	}

}
