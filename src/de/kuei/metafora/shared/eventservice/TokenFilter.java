package de.kuei.metafora.shared.eventservice;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

import de.kuei.metafora.shared.event.interfaces.TokenEvent;
import de.novanic.eventservice.client.event.Event;
import de.novanic.eventservice.client.event.filter.EventFilter;

public class TokenFilter implements EventFilter, Serializable, IsSerializable{

	private static final long serialVersionUID = -6128528432210930932L;
	
	private String token;
	
	public TokenFilter(String token){
		this.token = token;
	}
	
	@Override
	public boolean match(Event anEvent) {
		if(anEvent instanceof TokenEvent){
			TokenEvent tokenevent = (TokenEvent)anEvent;
			if(token.equals(tokenevent.getToken())){
				return true;
			}else{
				return false;
			}
		}
		return true;
	}

}
