package de.kuei.metafora.client.planningtool.serverlink;

import java.util.Vector;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("graphinit")
public interface GraphInit extends RemoteService{

	public String[] getNodes(String graph, String token);
	public String[] getEdges(String graph, String token);
	public int[] getSize(String graph, String token);
	
	public Vector<String> getUsers(String graph, String token);
	public Vector<String> getGroups(String graph, String token);
}
