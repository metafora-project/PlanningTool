package de.kuei.metafora.shared.eventservice;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

import de.kuei.metafora.shared.event.interfaces.MapEvent;
import de.novanic.eventservice.client.event.Event;
import de.novanic.eventservice.client.event.filter.EventFilter;

public class MapFilter implements EventFilter, Serializable, IsSerializable {

	private static final long serialVersionUID = 5329204757912098098L;

	private String mapname;

	public MapFilter(String mapname) {
		this.mapname = mapname;
	}

	@Override
	public boolean match(Event anEvent) {
		if (anEvent instanceof MapEvent) {
			MapEvent mapevent = (MapEvent) anEvent;
			if (mapname.equals(mapevent.getMap()) || mapevent.getMap() == null) {
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

}
