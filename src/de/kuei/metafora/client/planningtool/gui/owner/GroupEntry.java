package de.kuei.metafora.client.planningtool.gui.owner;

import java.util.HashMap;
import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LayoutPanel;

import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.PlanningTool;
import de.kuei.metafora.client.planningtool.PlanningToolWidget;
import de.kuei.metafora.client.planningtool.gui.ClientErrorHandler;
import de.kuei.metafora.client.planningtool.serverlink.UsersGroupLink;
import de.kuei.metafora.client.planningtool.serverlink.UsersGroupLinkAsync;

public class GroupEntry extends LayoutPanel implements ClickHandler {

	// i18n
	final static Languages language = GWT.create(Languages.class);
	private final UsersGroupLinkAsync usersGroups = GWT
			.create(UsersGroupLink.class);

	private Button remove;
	private String groupName;
	private Vector<String> users;
	private HashMap<String, UserEntry> userEntries;
	private HTML groupHTML;

	public GroupEntry(String group, boolean removeable) {
		super();

		users = new Vector<String>();
		userEntries = new HashMap<String, UserEntry>();

		groupName = group;

		groupHTML = new HTML(group + " [0] ");
		add(groupHTML);
		updateGroupHtml();

		remove = new Button("X");
		remove.setWidth("100%");
		remove.setHeight("100%");
		remove.addClickHandler(this);
		remove.setTitle(language.UnshareWithGroup());
		add(remove);
		if (!removeable) {
			remove.setVisible(false);
		}

		setSize("100%", "30px");

		setWidgetLeftRight(groupHTML, 0, Unit.PX, 29, Unit.PX);
		setWidgetTopHeight(groupHTML, 5, Unit.PX, 22, Unit.PX);

		setWidgetRightWidth(remove, 2, Unit.PX, 25, Unit.PX);
		setWidgetTopHeight(remove, 0, Unit.PX, 25, Unit.PX);

		usersGroups.getOnlineUsersForGroup(groupName, PlanningTool.getToken(),
				PlanningTool.getConnectionId(), PlanningToolWidget
						.getInstance().getGraphName(),
				new AsyncCallback<Vector<String>>() {

					@Override
					public void onSuccess(Vector<String> result) {
						if (result != null) {
							for (String user : result) {
								userJoin(user);
							}
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						ClientErrorHandler.showErrorMessage(caught);
					}
				});
	}

	public void updateGroupHtml() {
		if (groupName.equals(PlanningTool.getGroup())) {
			groupHTML.setHTML("<b>" + groupName + " [" + users.size()
					+ "] </b>");
		} else {
			groupHTML.setHTML(groupName + " [" + users.size() + "] ");
		}
	}

	private void updateUserEntries() {

		int yoffset = 30;
		int indent = 20;

		for (String user : users) {
			final UserEntry entry = userEntries.get(user);
			setWidgetLeftRight(entry, indent, Unit.PX, 2, Unit.PX);
			setWidgetTopHeight(entry, yoffset, Unit.PX, 20, Unit.PX);
			yoffset += 20;

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
							entry.setOnline(result);
						}
					});
		}

		setHeight((yoffset + 5) + "px");
	}

	public String getGroupName() {
		return groupName;
	}

	public void userJoin(String user) {
		if (!users.contains(user)) {
			users.add(user);

			UserEntry entry = new UserEntry(user);
			userEntries.put(user, entry);
			add(entry);

			updateGroupHtml();

			updateToolTipText();

			updateUserEntries();
		}
	}

	private void updateToolTipText() {
		String text = "";

		for (String user : users) {
			text += (user + ", ");
		}
		if (text.length() > 2) {
			text = text.substring(0, text.length() - 2);
		}
		setTitle(text);
	}

	public void userLeave(String user) {
		if (users.contains(user)) {
			users.remove(user);

			UserEntry entry = userEntries.get(user);
			remove(entry);
			userEntries.remove(user);

			updateGroupHtml();

			updateToolTipText();

			updateUserEntries();
		}
	}

	public void setRemoveable(boolean removeable) {
		if (removeable) {
			remove.setVisible(true);
		} else {
			remove.setVisible(false);
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		GroupList.getGroupList().removeGroup(this);
	}
}
