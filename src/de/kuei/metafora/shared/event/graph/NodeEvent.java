package de.kuei.metafora.shared.event.graph;

import de.kuei.metafora.shared.event.interfaces.MapEvent;
import de.novanic.eventservice.client.event.Event;

public class NodeEvent implements Event, MapEvent {

	private String nodeXML;
	private String map;

	public NodeEvent() {

	}

	public NodeEvent(String nodeXML, String map) {
		this.nodeXML = nodeXML;
		this.map = map;
	}

	public String getNodeXML() {
		return nodeXML;
	}

	public String getMap() {
		return map;
	}

}
