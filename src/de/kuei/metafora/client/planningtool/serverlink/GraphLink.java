package de.kuei.metafora.client.planningtool.serverlink;

import java.util.Vector;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("graphlink")
public interface GraphLink extends RemoteService {

	public String actionReceived(String xml, String token, String map,
			String connectionId);

	// alphabetisch sortiert nach (2)Kategorie + (1)Name
	public String[] getNodeTypes(String locale, String token,
			Vector<String> users, String groupId, String challengeId,
			String challengeName, boolean cavillag);

	public void selectMap(String mapname, String token, Vector<String> users,
			String groupId, String challengeId, String challengeName,
			String connectionId);

	public boolean saveAsMap(String mapname, String savename, String token,
			Vector<String> users, String groupId, String challengeId,
			String challengeName, String connectionId);

	public String saveVersionMap(String mapname, String token,
			Vector<String> users, String groupId, String challengeId,
			String challengeName);

	public boolean deleteMap(String mapname, String token,
			Vector<String> users, String groupId, String challengeId,
			String challengeName, String connectionId);

	public boolean createMap(String mapname, String token,
			Vector<String> users, String groupId, String challengeId,
			String challengeName, String connectionId);

	public String[] getMapnames(Vector<String> users, String group, String token);

	public void startClientSession(String connectionId, String token);

	public void assignGroup(Vector<String> users, String group, String token,
			String connectionId);

	public void removeGroup(String group, String token, String connectionId);

	public Vector<String> getGroups(String token);

	public String getSendingTool();
	
	public String getLasadName();

	public boolean getLogged();

	public String getMetafora();

	public String getTomcatServer();

	public String getApacheServer();

	public String getXmppServer();

	public String getReflectionChannel();

}
