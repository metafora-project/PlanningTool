package de.kuei.metafora.shared.event.graph;

import de.kuei.metafora.shared.event.interfaces.MapEvent;
import de.novanic.eventservice.client.event.Event;

public class EdgeEvent implements Event, MapEvent{
	
	private String edgeXML;
	private String map;
	
	public EdgeEvent(){
		
	}
	
	public EdgeEvent(String edgeXML, String map){
		this.edgeXML = edgeXML;
		this.map = map;
	}

	public String getEdgeXML(){
		return edgeXML;
	}

	@Override
	public String getMap() {
		return map;
	}
}
