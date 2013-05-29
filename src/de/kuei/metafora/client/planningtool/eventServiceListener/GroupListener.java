package de.kuei.metafora.client.planningtool.eventServiceListener;

import de.kuei.metafora.shared.event.user.UserJoinMapEvent;
import de.kuei.metafora.shared.event.user.UserLeaveMapEvent;
import de.novanic.eventservice.client.event.listener.RemoteEventListener;

public interface GroupListener extends RemoteEventListener {

	public void userJoinGroup(UserJoinMapEvent event);

	public void userLeaveGroup(UserLeaveMapEvent event);
}
