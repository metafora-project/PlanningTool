package de.kuei.metafora.client.planningtool.gui.owner;

import java.util.HashMap;
import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LayoutPanel;

import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.PlanningTool;
import de.kuei.metafora.client.planningtool.PlanningToolWidget;
import de.kuei.metafora.client.planningtool.gui.ClientErrorHandler;
import de.kuei.metafora.client.planningtool.serverlink.GraphInit;
import de.kuei.metafora.client.planningtool.serverlink.GraphInitAsync;
import de.kuei.metafora.client.planningtool.serverlink.UsersGroupLink;
import de.kuei.metafora.client.planningtool.serverlink.UsersGroupLinkAsync;

public class UserList extends LayoutPanel {

	private final GraphInitAsync graphInit = GWT.create(GraphInit.class);
	private final UsersGroupLinkAsync usersGroups = GWT
			.create(UsersGroupLink.class);

	final static Languages language = GWT.create(Languages.class);

	private static UserList instance = null;

	public static UserList getUserList() {
		if (instance == null) {
			instance = new UserList();
		}
		return instance;
	}

	private HashMap<String, UserEntry> users;

	private UserList() {
		super();
		users = new HashMap<String, UserEntry>();

		HTML text = new HTML("<b>" + language.MapOwners() + "</b>");
		add(text);
		setWidgetLeftWidth(text, 2, Unit.PX, 90, Unit.PX);
		setWidgetTopHeight(text, 2, Unit.PX, 20, Unit.PX);
		setHeight("25px");

	}

	public boolean isOwnerIncluded(Vector<String> userList) {
		for (String user : userList) {
			if (users.containsKey(user)) {
				return true;
			}
		}
		return false;
	}

	public void updateList() {
		graphInit.getUsers(PlanningToolWidget.getInstance().getGraphName(),
				PlanningTool.getToken(), new AsyncCallback<Vector<String>>() {

					@Override
					public void onSuccess(Vector<String> result) {
						if (result != null) {
							for (String user : result) {
								addUser(user);
							}
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						ClientErrorHandler.showErrorMessage(caught);
					}
				});
	}

	public void addUser(String user) {
		if (!users.containsKey(user)) {
			final UserEntry uentry = new UserEntry(user);
			users.put(user, uentry);
			add(uentry);
			setWidgetLeftRight(uentry, 92, Unit.PX, 2, Unit.PX);
			int top = ((users.size() - 1) * 25) + 2;
			setWidgetTopHeight(uentry, top, Unit.PX, 20, Unit.PX);
			setHeight((top + 25) + "px");

			usersGroups.isUserOnline(user, PlanningTool.getToken(),
					PlanningTool.getConnectionId(), PlanningToolWidget
							.getInstance().getGraphName(),
					new AsyncCallback<Boolean>() {

						@Override
						public void onFailure(Throwable caught) {
							ClientErrorHandler.showErrorMessage(caught);
						}

						@Override
						public void onSuccess(Boolean result) {
							uentry.setOnline(result);
						}
					});
		}
	}

	public void onlineChanged(String user, boolean online) {
		if (users.containsKey(user))
			users.get(user).setOnline(online);
	}

	public void clearUsers() {
		for (String user : users.keySet()) {
			UserEntry uentry = users.get(user);
			remove(uentry);
		}
		users.clear();
	}

	public int getUserCount() {
		return users.keySet().size();
	}

}
