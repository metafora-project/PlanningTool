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

public class MapSaveAsHandler implements ClickHandler {

	// i18n
	final static Languages language = GWT.create(Languages.class);
	private final GraphLinkAsync graphLink = GWT.create(GraphLink.class);

	public MapSaveAsHandler() {
	}

	@Override
	public void onClick(ClickEvent event) {
		String graphName = PlanningToolWidget.getInstance().getGraphName();

		// TODO: no prompts!
		final String newName = Window.prompt(language.EnterNewPlanName()+" ",
				graphName);
		if ((newName == null)
				|| (newName.equals(graphName) || (newName.isEmpty()))) {
			Window.alert(language.SaveFailed());
		} else {
			graphLink.saveAsMap(graphName, newName, PlanningTool.getToken(),
					PlanningTool.getUsers(), PlanningTool.getGroup(),
					PlanningTool.getChallengeId(),
					PlanningTool.getChallengeName(),
					PlanningTool.getConnectionId(),
					new AsyncCallback<Boolean>() {

						@Override
						public void onSuccess(Boolean result) {
							if (result) {
								PlanningToolWidget.getInstance().setGraphName(
										newName);
								PlanningToolWidget.getInstance().addNewMap(
										newName);
							} else {
								// TODO: no prompt
								// TODO: localize
								Window.alert(language.SavingPlanWithName()+" "
										+ newName
										+ language.FailedThereIsAlreadyAPlan());
							}

						}

						@Override
						public void onFailure(Throwable caught) {
							ClientErrorHandler.showErrorMessage(caught);
						}
					});
		}
	}
}
