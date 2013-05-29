package de.kuei.metafora.client.planningtool.eventServiceListener.impl;

import de.kuei.metafora.client.PlanningTool;
import de.kuei.metafora.client.planningtool.PlanningToolWidget;
import de.kuei.metafora.client.planningtool.eventServiceListener.GroupListener;
import de.kuei.metafora.client.planningtool.gui.owner.GroupList;
import de.kuei.metafora.shared.event.user.UserJoinMapEvent;
import de.kuei.metafora.shared.event.user.UserLeaveMapEvent;
import de.novanic.eventservice.client.event.Event;
import de.novanic.eventservice.client.event.filter.EventFilter;

public class GroupListenerImpl implements GroupListener {

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

		if (anEvent instanceof UserJoinMapEvent) {
			userJoinGroup((UserJoinMapEvent) anEvent);
		} else if (anEvent instanceof UserLeaveMapEvent) {
			userLeaveGroup((UserLeaveMapEvent) anEvent);
		}
	}

	@Override
	public void userJoinGroup(UserJoinMapEvent event) {
		if (PlanningToolWidget.getInstance().getGraphName()
				.equals(event.getMap())) {
			GroupList.getGroupList().userJoinGroup(event.getUser(),
					event.getGroup());
			if (PlanningTool.getGroup().equals(event.getGroup()))
				PlanningToolWidget.getInstance().getPlanWidget()
						.getGroupUserList().addUser(event.getUser());
		}
	}

	@Override
	public void userLeaveGroup(UserLeaveMapEvent event) {
		if (PlanningToolWidget.getInstance().getGraphName()
				.equals(event.getMap())) {
			GroupList.getGroupList().userLeaveGroup(event.getUser(),
					event.getGroup());
			if (PlanningTool.getGroup().equals(event.getGroup()))
				PlanningToolWidget.getInstance().getPlanWidget()
						.getGroupUserList().removeUser(event.getUser());
		}
	}
}
