package de.kuei.metafora.client.planningtool.serverlink;

import java.util.Vector;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GraphInitAsync {

	void getEdges(String graph, String token, AsyncCallback<String[]> callback);

	void getNodes(String graph, String token, AsyncCallback<String[]> callback);
	
	void getSize(String graph, String token, AsyncCallback<int[]> callback);

	void getGroups(String graph, String token, AsyncCallback<Vector<String>> callback);

	void getUsers(String graph, String token, AsyncCallback<Vector<String>> callback);

}
