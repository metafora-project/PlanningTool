package de.kuei.metafora.client.planningtool.gui.owner;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.SuggestBox;

import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.PlanningTool;
import de.kuei.metafora.client.planningtool.gui.ClientErrorHandler;
import de.kuei.metafora.client.planningtool.serverlink.GraphLink;
import de.kuei.metafora.client.planningtool.serverlink.GraphLinkAsync;

public class ShareWithGroupHandler implements ClickHandler {

	private final GraphLinkAsync graphLink = GWT.create(GraphLink.class);

	final static Languages language = GWT.create(Languages.class);

	private Button button;
	private SuggestBox field;

	private boolean selectMode = false;

	public ShareWithGroupHandler(Button button, SuggestBox field) {
		this.button = button;
		this.field = field;

		selectMode = false;
	}

	@Override
	public void onClick(ClickEvent event) {
		if (!selectMode) {
			button.setText(language.OK());
			field.setVisible(true);
			selectMode = true;
		} else {
			button.setText(language.ShareWithGroup());
			final String group = field.getText();
			field.setVisible(false);
			selectMode = false;
			if (group != null && group.length() > 0) {
				graphLink.assignGroup(PlanningTool.getUsers(), group,
						PlanningTool.getToken(),
						PlanningTool.getConnectionId(),
						new AsyncCallback<Void>() {

							@Override
							public void onSuccess(Void result) {
								GroupList.getGroupList().addGroup(group);
							}

							@Override
							public void onFailure(Throwable caught) {
								ClientErrorHandler.showErrorMessage(caught);
							}
						});
			}
		}
	}
}
