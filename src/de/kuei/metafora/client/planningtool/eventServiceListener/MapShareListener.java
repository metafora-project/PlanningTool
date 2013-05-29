package de.kuei.metafora.client.planningtool.eventServiceListener;

import de.kuei.metafora.shared.event.user.ShareWithGroupEvent;
import de.kuei.metafora.shared.event.user.UnshareWithGroupEvent;
import de.novanic.eventservice.client.event.listener.RemoteEventListener;

public interface MapShareListener extends RemoteEventListener{
	
	public void shareWithGroup(ShareWithGroupEvent event);
	
	public void unshareWithGroup(UnshareWithGroupEvent event);

}
