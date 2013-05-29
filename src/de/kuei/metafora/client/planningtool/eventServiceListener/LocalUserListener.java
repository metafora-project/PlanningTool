package de.kuei.metafora.client.planningtool.eventServiceListener;

import de.kuei.metafora.shared.event.user.GroupChangeEvent;
import de.kuei.metafora.shared.event.user.UserLoginEvent;
import de.kuei.metafora.shared.event.user.UserLogoutEvent;
import de.novanic.eventservice.client.event.listener.RemoteEventListener;

public interface LocalUserListener extends RemoteEventListener{
	
	public void userLogin(UserLoginEvent event);
	
	public void userLogout(UserLogoutEvent event);
	
	public void groupChange(GroupChangeEvent event);

}
