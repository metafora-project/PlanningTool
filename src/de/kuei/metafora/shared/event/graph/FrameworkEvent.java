package de.kuei.metafora.shared.event.graph;

import de.kuei.metafora.shared.event.interfaces.MapEvent;
import de.novanic.eventservice.client.event.Event;

public class FrameworkEvent implements Event, MapEvent {

	private String frameworkXML;
	private String map;

	public FrameworkEvent() {

	}

	public FrameworkEvent(String frameworkXML, String map) {
		this.frameworkXML = frameworkXML;
		this.map = map;
	}

	public String getFrameworkXML() {
		return frameworkXML;
	}

	@Override
	public String getMap() {
		return map;
	}
}
