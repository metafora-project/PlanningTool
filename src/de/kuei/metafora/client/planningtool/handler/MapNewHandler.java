package de.kuei.metafora.client.planningtool.handler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.PlanningTool;
import de.kuei.metafora.client.planningtool.PlanningToolWidget;
import de.kuei.metafora.client.planningtool.gui.ClientErrorHandler;
import de.kuei.metafora.client.planningtool.serverlink.GraphLink;
import de.kuei.metafora.client.planningtool.serverlink.GraphLinkAsync;
import de.kuei.metafora.client.planningtool.util.InputFilter;

public class MapNewHandler implements ClickHandler {

	// i18n
	final static Languages language = GWT.create(Languages.class);
	private final GraphLinkAsync graphLink = GWT.create(GraphLink.class);

	public MapNewHandler() {
	}

	@Override
	public void onClick(ClickEvent event) {
		// TODO: no prompts!
		String input = Window
				.prompt(language.EnterNewPlanName()+" ", "unnamed");

		final String newName = InputFilter.filterString(input);

		if ((newName == null) || (newName.equals("unnamed"))
				|| (newName.isEmpty())) {
			Window.alert(language.CreationFailed());
		} else {
			graphLink.createMap(newName, PlanningTool.getToken(),
					PlanningTool.getUsers(), PlanningTool.getGroup(),
					PlanningTool.getChallengeId(),
					PlanningTool.getChallengeName(),
					PlanningTool.getConnectionId(),
					new AsyncCallback<Boolean>() {

						@Override
						public void onSuccess(Boolean result) {
							if (result.booleanValue()) {
								PlanningToolWidget.getInstance()
										.newMap(newName);
							} else
								Window.alert(language.PlanAlreadyExists());
						}

						@Override
						public void onFailure(Throwable caught) {
							ClientErrorHandler.showErrorMessage(caught);
						}
					});
		}
	}

}
