package de.kuei.metafora.client.planningtool.serverlink;

import java.util.Vector;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("usersgroups")
public interface UsersGroupLink extends RemoteService {

	public boolean isUserOnline(String user, String token, String connectionId,
			String map);

	public Vector<String> getOnlineUsersForGroup(String group, String token,
			String connectionId, String map);
}
