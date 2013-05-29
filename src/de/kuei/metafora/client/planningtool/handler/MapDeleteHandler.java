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

public class MapDeleteHandler implements ClickHandler {

	// i18n
	final static Languages language = GWT.create(Languages.class);
	private final GraphLinkAsync graphLink = GWT.create(GraphLink.class);

	public MapDeleteHandler() {
	}

	@Override
	public void onClick(ClickEvent event) {
		// TODO: better alternative to prompt
		final String answer = Window.prompt(
				language.DoYouReallyWantToDeleteThisPlan(),
				language.EnterYourAnswer());
		final String name = PlanningToolWidget.getInstance().getGraphName();

		if (answer.equals("yes")) {
			graphLink.deleteMap(name, PlanningTool.getToken(),
					PlanningTool.getUsers(), PlanningTool.getGroup(),
					PlanningTool.getChallengeId(),
					PlanningTool.getChallengeName(),
					PlanningTool.getConnectionId(),
					new AsyncCallback<Boolean>() {
						@Override
						public void onSuccess(Boolean result) {
							if (result.booleanValue()) {
								PlanningToolWidget.getInstance()
										.deleteMap(name);
							} else
								Window.alert(language.DeletingPlanFailed());
						}

						@Override
						public void onFailure(Throwable caught) {
							ClientErrorHandler.showErrorMessage(caught);
						}
					});
		} else
			Window.alert(language.DeletingPlanCanceled());
	}

}
