package de.kuei.metafora.shared.event.user;

import de.kuei.metafora.shared.event.interfaces.MapEvent;
import de.novanic.eventservice.client.event.Event;

public class UnshareWithGroupEvent implements Event, MapEvent{
	
	private String map;
	private String group;
	
	public UnshareWithGroupEvent(){
		
	}
	
	public UnshareWithGroupEvent(String map, String group){
		this.group = group;
		this.map = map;
	}

	public String getGroup(){
		return group;
	}
	
	public String getMap(){
		return map;
	}
	
}
