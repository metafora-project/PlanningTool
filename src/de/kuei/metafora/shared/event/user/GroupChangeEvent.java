package de.kuei.metafora.shared.event.user;

import de.kuei.metafora.shared.event.interfaces.TokenEvent;
import de.novanic.eventservice.client.event.Event;

public class GroupChangeEvent implements Event, TokenEvent {

	private String group;
	private String token;

	public GroupChangeEvent() {

	}

	public GroupChangeEvent(String group, String token) {
		this.group = group;
		this.token = token;
	}

	public String getGroup() {
		return group;
	}

	@Override
	public String getToken() {
		return token;
	}

}
