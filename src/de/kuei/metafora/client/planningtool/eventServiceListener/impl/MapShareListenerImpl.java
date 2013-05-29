package de.kuei.metafora.client.planningtool.eventServiceListener.impl;

import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.PlanningTool;
import de.kuei.metafora.client.planningtool.PlanningToolWidget;
import de.kuei.metafora.client.planningtool.eventServiceListener.MapShareListener;
import de.kuei.metafora.client.planningtool.gui.ClientErrorHandler;
import de.kuei.metafora.client.planningtool.gui.owner.GroupList;
import de.kuei.metafora.client.planningtool.gui.owner.UserList;
import de.kuei.metafora.client.planningtool.serverlink.GraphInit;
import de.kuei.metafora.client.planningtool.serverlink.GraphInitAsync;
import de.kuei.metafora.shared.event.user.ShareWithGroupEvent;
import de.kuei.metafora.shared.event.user.UnshareWithGroupEvent;
import de.novanic.eventservice.client.event.Event;
import de.novanic.eventservice.client.event.filter.EventFilter;

public class MapShareListenerImpl implements MapShareListener {

	// i18n
	final static Languages language = GWT.create(Languages.class);
	private final GraphInitAsync graphInit = GWT.create(GraphInit.class);

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
		if (anEvent instanceof ShareWithGroupEvent) {
			shareWithGroup((ShareWithGroupEvent) anEvent);
		} else if (anEvent instanceof UnshareWithGroupEvent) {
			unshareWithGroup((UnshareWithGroupEvent) anEvent);
		}
	}

	@Override
	public void shareWithGroup(ShareWithGroupEvent event) {
		if (PlanningToolWidget.getInstance().getGraphName()
				.equals(event.getMap())) {
			GroupList.getGroupList().addGroup(event.getGroup());
			UserList.getUserList().updateList();
		} else {
			if (PlanningTool.getGroup().equals(event.getGroup())) {
				PlanningToolWidget.getInstance().addNewMap(event.getMap());
			}
		}
	}

	@Override
	public void unshareWithGroup(UnshareWithGroupEvent event) {
		final String graph = event.getMap();

		if (PlanningTool.getGroup().equals(event.getGroup())) {
			if (PlanningToolWidget.getInstance().getGraphName().equals(graph)) {
				if (!UserList.getUserList().isOwnerIncluded(
						PlanningTool.getUsers())) {
					PlanningToolWidget.getInstance().deleteMap(graph);
					Window.resizeTo(100, 100);
					Window.alert(language.MapNotSharedAnyLongerWithGroup());
				}
			} else {
				graphInit.getUsers(graph, PlanningTool.getToken(),
						new AsyncCallback<Vector<String>>() {

							@Override
							public void onFailure(Throwable caught) {
								ClientErrorHandler.showErrorMessage(caught);
							}

							@Override
							public void onSuccess(Vector<String> result) {
								boolean remove = true;
								for (String user : result) {
									if (PlanningTool.getUsers().contains(user)) {
										remove = false;
									}
								}

								if (remove) {
									PlanningToolWidget.getInstance().deleteMap(
											graph);
								}
							}
						});

			}
		}
	}

}
