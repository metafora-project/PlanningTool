package de.kuei.metafora.shared.event.user;

import de.novanic.eventservice.client.event.Event;

public class UserJoinMapEvent implements Event {

	private String user;
	private String group;
	private String map;

	public UserJoinMapEvent() {

	}

	public UserJoinMapEvent(String user, String group, String map) {
		this.user = user;
		this.group = group;
		this.map = map;
	}

	public String getUser() {
		return user;
	}

	public String getGroup() {
		return group;
	}

	public String getMap() {
		return map;
	}
}
