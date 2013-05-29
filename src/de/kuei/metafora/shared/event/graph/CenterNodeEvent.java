package de.kuei.metafora.shared.event.graph;

import de.kuei.metafora.shared.event.interfaces.MapEvent;
import de.novanic.eventservice.client.event.Event;

public class CenterNodeEvent implements Event, MapEvent {

	private String nodeId;
	private String map;
	private String token;
	private String connectionId;

	public CenterNodeEvent() {

	}

	public CenterNodeEvent(String nodeId, String map, String token) {
		this.nodeId = nodeId;
		this.map = map;
		this.token = token;
	}

	public String getNodeId() {
		return nodeId;
	}

	public String getMap() {
		return map;
	}

	public String getToken() {
		return token;
	}

	public String getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}
}
