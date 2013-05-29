package de.kuei.metafora.client.planningtool.gui.owner;

import com.google.gwt.user.client.ui.HTML;

public class UserEntry extends HTML {

	private String userName;

	public UserEntry(String username) {
		super(username);

		this.userName = username;
	}

	public void setOnline(boolean online) {
		if (online) {
			setHTML(userName + " <span style='color: #008000;'>(online)</span>");
		} else {
			setHTML(userName);
		}
	}

}
