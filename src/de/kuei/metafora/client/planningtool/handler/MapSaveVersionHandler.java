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

public class MapSaveVersionHandler implements ClickHandler {

	// i18n
	final static Languages language = GWT.create(Languages.class);
	private final GraphLinkAsync graphLink = GWT.create(GraphLink.class);

	public MapSaveVersionHandler() {
	}

	@Override
	public void onClick(ClickEvent event) {

		graphLink.saveVersionMap(PlanningToolWidget.getInstance()
				.getGraphName(), PlanningTool.getToken(), PlanningTool
				.getUsers(), PlanningTool.getGroup(), PlanningTool
				.getChallengeId(), PlanningTool.getChallengeName(),
				new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						// TODO: no alerts!
						if (result != null) {
							Window.alert(language.DocumentSavedWithId()+" " + result);
						} else {
							Window.alert(language.SavingVersionFailed());
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						ClientErrorHandler.showErrorMessage(caught);
					}
				});
	}

}
