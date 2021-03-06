package de.kuei.metafora.shared.event.user;

import de.kuei.metafora.shared.event.interfaces.TokenEvent;
import de.novanic.eventservice.client.event.Event;

public class UserLogoutEvent implements Event, TokenEvent {

	private String user;
	private String token;
	private String group;

	public UserLogoutEvent() {

	}

	public UserLogoutEvent(String user, String token, String group) {
		this.user = user;
		this.token = token;
		this.group = group;
	}

	public String getUser() {
		return user;
	}

	public String getGroup() {
		return group;
	}

	@Override
	public String getToken() {
		return token;
	}

}
