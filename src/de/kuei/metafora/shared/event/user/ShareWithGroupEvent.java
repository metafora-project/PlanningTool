package de.kuei.metafora.shared.event.user;

import de.kuei.metafora.shared.event.interfaces.MapEvent;
import de.novanic.eventservice.client.event.Event;

public class ShareWithGroupEvent implements Event, MapEvent {

	private String mapname;
	private String group;

	public ShareWithGroupEvent() {

	}

	public ShareWithGroupEvent(String map, String group) {
		this.mapname = map;
		this.group = group;
	}

	public String getGroup() {
		return group;
	}

	public String getMap() {
		return mapname;
	}
}
