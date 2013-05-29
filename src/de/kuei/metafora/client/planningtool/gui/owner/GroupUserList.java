package de.kuei.metafora.client.planningtool.gui.owner;

import java.util.Vector;

import com.google.gwt.core.client.GWT;

import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.PlanningTool;

public class GroupUserList {

	final static Languages language = GWT.create(Languages.class);

	private Vector<String> users;

	public GroupUserList() {
		super();

		users = new Vector<String>();

		updateList();
	}

	public void setGraphName(String graphName) {
		clearUsers();
		updateList();
	}

	public Vector<String> getUsers() {
		return users;
	}

	public void updateList() {
		for (String user : PlanningTool.getUsers()) {
			addUser(user);
		}
	}

	public void addUser(String user) {
		if (!users.contains(user)) {
			users.add(user);
		}
	}

	public void removeUser(String user) {
		if (users.contains(user)) {
			users.remove(user);
		}
	}

	public void groupSwitch() {
		clearUsers();
		updateList();
	}

	public void clearUsers() {
		users.clear();
	}
}
