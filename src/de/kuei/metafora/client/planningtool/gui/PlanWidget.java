package de.kuei.metafora.client.planningtool.gui;

import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.PlanningTool;
import de.kuei.metafora.client.planningtool.PlanningToolWidget;
import de.kuei.metafora.client.planningtool.gui.owner.GroupList;
import de.kuei.metafora.client.planningtool.gui.owner.GroupUserList;
import de.kuei.metafora.client.planningtool.gui.owner.ShareWithGroupHandler;
import de.kuei.metafora.client.planningtool.gui.owner.UserList;
import de.kuei.metafora.client.planningtool.handler.MapDeleteHandler;
import de.kuei.metafora.client.planningtool.handler.MapNewHandler;
import de.kuei.metafora.client.planningtool.handler.MapSaveAsHandler;
import de.kuei.metafora.client.planningtool.handler.MapSaveVersionHandler;
import de.kuei.metafora.client.planningtool.serverlink.GraphInit;
import de.kuei.metafora.client.planningtool.serverlink.GraphInitAsync;
import de.kuei.metafora.client.planningtool.serverlink.GraphLink;
import de.kuei.metafora.client.planningtool.serverlink.GraphLinkAsync;

public class PlanWidget extends FlowPanel {

	// object needed to implement i18n through Languages interface
	final static Languages language = GWT.create(Languages.class);

	private final GraphInitAsync graphInit = GWT.create(GraphInit.class);
	private final GraphLinkAsync graphLink = GWT.create(GraphLink.class);

	private String graphName;
	private final ListBox maps = new ListBox();

	/**
	 * Oracle for group selection while sharing the map with other groups.
	 */
	private MultiWordSuggestOracle groupOracle;

	private GroupUserList users;

	public PlanWidget(String graphName) {
		this.graphName = graphName;

		users = new GroupUserList();

		buildGui();
	}

	public void setGraphName(String graphName) {
		this.graphName = graphName;
		GroupList.getGroupList().updateGraphName();
		addNewMap(graphName);
		selectMap(graphName);
	}

	private void buildGui() {
		VerticalPanel vpanel = new VerticalPanel();

		LayoutPanel mapsPanel = new LayoutPanel();
		mapsPanel.setWidth("100%");

		HTML text = new HTML("<b>" + language.File().toUpperCase() + ":</b>");
		mapsPanel.add(text);
		mapsPanel.setWidgetLeftWidth(text, 2, Unit.PX, 40, Unit.PX);
		mapsPanel.setWidgetTopHeight(text, 6, Unit.PX, 20, Unit.PX);

		graphLink.getMapnames(PlanningTool.getUsers(), PlanningTool.getGroup(),
				PlanningTool.getToken(), new AsyncCallback<String[]>() {

					@Override
					public void onFailure(Throwable caught) {
						ClientErrorHandler.showErrorMessage(caught);
					}

					@Override
					public void onSuccess(String[] result) {
						for (int i = 0; i < result.length; i++) {
							maps.addItem(result[i]);
							if (result[i].equals(graphName))
								maps.setSelectedIndex(i);
						}
					}
				});

		maps.setVisibleItemCount(1);
		maps.setWidth("95%");
		mapsPanel.add(maps);
		mapsPanel.setWidgetLeftRight(maps, 45, Unit.PX, 2, Unit.PX);
		mapsPanel.setWidgetTopHeight(maps, 2, Unit.PX, 25, Unit.PX);

		Button open = new Button(language.MenuItemOpen());
		open.setWidth("95%");
		open.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				String mapname = maps.getItemText(maps.getSelectedIndex());
				PlanningToolWidget.getInstance().openMap(mapname);

			}
		});
		mapsPanel.add(open);
		mapsPanel.setWidgetLeftRight(open, 45, Unit.PX, 2, Unit.PX);
		mapsPanel.setWidgetTopHeight(open, 28, Unit.PX, 25, Unit.PX);

		mapsPanel.setHeight("60px");

		UserList ownersPanel = UserList.getUserList();
		ownersPanel.setWidth("100%");

		GroupList groupsPanel = GroupList.getGroupList();
		groupsPanel.setWidth("100%");

		graphInit.getUsers(this.graphName, PlanningTool.getToken(),
				new AsyncCallback<Vector<String>>() {

					@Override
					public void onFailure(Throwable caught) {
						ClientErrorHandler.showErrorMessage(caught);
					}

					@Override
					public void onSuccess(Vector<String> result) {
						for (String user : result) {
							UserList.getUserList().addUser(user);
						}
					}
				});

		graphInit.getGroups(this.graphName, PlanningTool.getToken(),
				new AsyncCallback<Vector<String>>() {

					@Override
					public void onFailure(Throwable caught) {
						ClientErrorHandler.showErrorMessage(caught);
					}

					@Override
					public void onSuccess(Vector<String> result) {
						for (String group : result) {
							GroupList.getGroupList().addGroup(group);
						}
					}
				});

		groupOracle = new MultiWordSuggestOracle();
		graphLink.getGroups(PlanningTool.getToken(),
				new AsyncCallback<Vector<String>>() {

					@Override
					public void onFailure(Throwable caught) {
						ClientErrorHandler.showErrorMessage(caught);
					}

					@Override
					public void onSuccess(Vector<String> result) {
						for (String group : result) {
							groupOracle.add(group);
						}
					}
				});

		LayoutPanel sharePanel = new LayoutPanel();
		sharePanel.setWidth("100%");

		text = new HTML("<b>" + language.ShareWithGroup().toUpperCase()
				+ "</b>");
		sharePanel.add(text);
		sharePanel.setWidgetLeftRight(text, 2, Unit.PX, 2, Unit.PX);
		sharePanel.setWidgetTopHeight(text, 2, Unit.PX, 20, Unit.PX);

		text = new HTML(language.Choose());
		sharePanel.add(text);
		sharePanel.setWidgetLeftWidth(text, 2, Unit.PX, 50, Unit.PX);
		sharePanel.setWidgetTopHeight(text, 26, Unit.PX, 20, Unit.PX);

		SuggestBox groupName = new SuggestBox(groupOracle);
		sharePanel.add(groupName);
		groupName.setWidth("95%");
		sharePanel.setWidgetLeftRight(groupName, 55, Unit.PX, 5, Unit.PX);
		sharePanel.setWidgetTopHeight(groupName, 25, Unit.PX, 25, Unit.PX);

		Button shareMap = new Button(language.Share());
		shareMap.setWidth("95%");
		shareMap.addClickHandler(new ShareWithGroupHandler(shareMap, groupName));
		sharePanel.add(shareMap);
		sharePanel.setWidgetLeftRight(shareMap, 55, Unit.PX, 5, Unit.PX);
		sharePanel.setWidgetTopHeight(shareMap, 52, Unit.PX, 25, Unit.PX);

		sharePanel.setHeight("80px");

		LayoutPanel buttonPanel = new LayoutPanel();
		buttonPanel.setWidth("100%");

		Button saveas = new Button(language.MenuItemSaveAs());
		saveas.setWidth("95%");
		saveas.addClickHandler(new MapSaveAsHandler());
		buttonPanel.add(saveas);
		buttonPanel.setWidgetLeftRight(saveas, 2, Unit.PX, 2, Unit.PX);
		buttonPanel.setWidgetTopHeight(saveas, 2, Unit.PX, 25, Unit.PX);

		Button save = new Button(language.MenuItemSaveVersion());
		save.setWidth("95%");
		save.addClickHandler(new MapSaveVersionHandler());
		buttonPanel.add(save);
		buttonPanel.setWidgetLeftRight(save, 2, Unit.PX, 2, Unit.PX);
		buttonPanel.setWidgetTopHeight(save, 29, Unit.PX, 25, Unit.PX);

		Button newMap = new Button(language.MenuItemNewMap());
		newMap.setWidth("95%");
		newMap.addClickHandler(new MapNewHandler());
		buttonPanel.add(newMap);
		buttonPanel.setWidgetLeftRight(newMap, 2, Unit.PX, 2, Unit.PX);
		buttonPanel.setWidgetTopHeight(newMap, 56, Unit.PX, 25, Unit.PX);

		// deletes a map
		Button deleteMap = new Button(language.MenuItemDelete());
		deleteMap.setWidth("95%");
		deleteMap.addClickHandler(new MapDeleteHandler());
		buttonPanel.add(deleteMap);
		buttonPanel.setWidgetLeftRight(deleteMap, 2, Unit.PX, 2, Unit.PX);
		buttonPanel.setWidgetTopHeight(deleteMap, 83, Unit.PX, 25, Unit.PX);

		buttonPanel.setHeight("115px");

		vpanel.setWidth("100%");

		vpanel.add(mapsPanel);
		vpanel.add(ownersPanel);
		vpanel.add(buttonPanel);
		vpanel.add(groupsPanel);
		vpanel.add(sharePanel);

		add(new ScrollPanel(vpanel));
		selectMap(graphName);
	}

	public void deleteMap(String name) {
		for (int i = 0; i < maps.getItemCount(); i++) {
			if (maps.getItemText(i).equals(name)) {
				maps.removeItem(i);
				return;
			}
		}
	}

	public void addNewMap(String name) {
		for (int i = 0; i < maps.getItemCount(); i++) {
			if (maps.getItemText(i).equals(name))
				return;
		}
		maps.addItem(name);
	}

	private void selectMap(String name) {
		addNewMap(name);

		for (int i = 0; i < maps.getItemCount(); i++) {
			if (maps.getItemText(i).equals(name)) {
				maps.setSelectedIndex(i);
				return;
			}
		}
	}

	public String[] getHeader() {
		String[] header = new String[2];

		header[0] = graphName;

		header[1] = "";

		for (String user : users.getUsers()) {
			header[1] += user + ", ";
		}
		if (header[1].length() > 2)
			header[1] = header[1].substring(0, header[1].length() - 2);

		return header;
	}

	public GroupUserList getGroupUserList() {
		return users;
	}

	public void groupSwitch() {
		users.groupSwitch();

		maps.clear();

		graphLink.getMapnames(PlanningTool.getUsers(), PlanningTool.getGroup(),
				PlanningTool.getToken(), new AsyncCallback<String[]>() {

					@Override
					public void onFailure(Throwable caught) {
						ClientErrorHandler.showErrorMessage(caught);
					}

					@Override
					public void onSuccess(String[] result) {
						for (int i = 0; i < result.length; i++) {
							addNewMap(result[i]);
							if (result[i].equals(graphName))
								maps.setSelectedIndex(i);
						}
					}
				});

	}
}
