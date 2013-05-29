package de.kuei.metafora.client.planningtool.gui.owner;

import java.util.HashMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.PlanningTool;
import de.kuei.metafora.client.planningtool.gui.ClientErrorHandler;
import de.kuei.metafora.client.planningtool.serverlink.GraphLink;
import de.kuei.metafora.client.planningtool.serverlink.GraphLinkAsync;

public class GroupList extends VerticalPanel {

	private final GraphLinkAsync graphLink = GWT.create(GraphLink.class);

	final static Languages language = GWT.create(Languages.class);

	private static GroupList instance = null;

	public static GroupList getGroupList() {
		if (instance == null) {
			instance = new GroupList();
		}
		return instance;
	}

	private HashMap<String, GroupEntry> groups;

	private GroupList() {
		super();
		groups = new HashMap<String, GroupEntry>();
		add(new HTML("<b>" + language.SharedWithGroups().toUpperCase() + "</b>"));
	}

	public void updateGraphName() {
		for (String group : groups.keySet()) {
			groups.get(group).updateGroupHtml();
		}
	}

	public void addGroup(String group) {
		if (!groups.containsKey(group)) {
			GroupEntry groupEntry;
			if (groups.keySet().size() == 0) {
				groupEntry = new GroupEntry(group, false);
			} else {
				groupEntry = new GroupEntry(group, true);
				for (String key : groups.keySet()) {
					groups.get(key).setRemoveable(true);
				}
			}

			groups.put(group, groupEntry);
			add(groupEntry);

			if (group.equals(PlanningTool.getGroup())) {
				for (String user : PlanningTool.getUsers()) {
					userJoinGroup(user, PlanningTool.getGroup());
				}
			}
		}
	}

	public void userJoinGroup(String user, String group) {
		if (group != null && groups.containsKey(group)) {
			groups.get(group).userJoin(user);
		}
	}

	public void userLeaveGroup(String user, String group) {
		if (group != null && groups.containsKey(group)) {
			groups.get(group).userLeave(user);
		}
	}

	public void removeGroup(String group) {
		if (groups.containsKey(group)) {
			if (groups.keySet().size() > 1) {
				GroupEntry groupEntry = groups.get(group);

				remove(groupEntry);
				groups.remove(group);

				if (groups.keySet().size() == 1) {
					for (String key : groups.keySet()) {
						groups.get(key).setRemoveable(false);
					}
				}
			}
		}
	}

	public void removeGroup(GroupEntry groupEntry) {
		final String groupName = groupEntry.getGroupName();

		graphLink.removeGroup(groupEntry.getGroupName(),
				PlanningTool.getToken(), PlanningTool.getConnectionId(),
				new AsyncCallback<Void>() {

					@Override
					public void onSuccess(Void result) {
						removeGroup(groupName);
					}

					@Override
					public void onFailure(Throwable caught) {
						ClientErrorHandler.showErrorMessage(caught);
					}
				});
	}

	public void clearGroups() {
		for (String key : groups.keySet()) {
			GroupEntry ge = groups.get(key);
			remove(ge);
		}
		groups.clear();
	}
}
