package de.kuei.metafora.client.planningtool.eventServiceListener.impl;

import de.kuei.metafora.client.PlanningTool;
import de.kuei.metafora.client.planningtool.PlanningToolWidget;
import de.kuei.metafora.client.planningtool.eventServiceListener.LocalUserListener;
import de.kuei.metafora.shared.event.user.GroupChangeEvent;
import de.kuei.metafora.shared.event.user.UserLoginEvent;
import de.kuei.metafora.shared.event.user.UserLogoutEvent;
import de.novanic.eventservice.client.event.Event;
import de.novanic.eventservice.client.event.filter.EventFilter;

public class LocalUserListenerImpl implements LocalUserListener {

	private EventFilter filter;

	public void setFilter(EventFilter filter) {
		this.filter = filter;
	}

	@Override
	public void apply(Event anEvent) {
		if (filter != null) {
			if (!filter.match(anEvent)) {
				return;
			}
		}

		if (anEvent instanceof UserLoginEvent) {
			userLogin((UserLoginEvent) anEvent);
		} else if (anEvent instanceof UserLogoutEvent) {
			userLogout((UserLogoutEvent) anEvent);
		} else if (anEvent instanceof GroupChangeEvent) {
			groupChange((GroupChangeEvent) anEvent);
		}
	}

	@Override
	public void userLogin(UserLoginEvent event) {
		PlanningTool.addUser(event.getUser());
	}

	@Override
	public void userLogout(UserLogoutEvent event) {
		PlanningTool.removeUser(event.getUser());
	}

	@Override
	public void groupChange(GroupChangeEvent event) {
		PlanningTool.setGroup(event.getGroup());
		PlanningToolWidget.getInstance().getPlanWidget().groupSwitch();
	}
}
