package de.kuei.metafora.client.planningtool.popmenu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.kuei.metafora.client.PlanningTool;
import de.kuei.metafora.client.planningtool.gui.ClientErrorHandler;
import de.kuei.metafora.client.planningtool.gui.graph.DnDNode;
import de.kuei.metafora.client.planningtool.serverlink.MessageSend;
import de.kuei.metafora.client.planningtool.serverlink.MessageSendAsync;

public class ShareModelCommand implements Command {

	private final MessageSendAsync commandSend = GWT.create(MessageSend.class);

	private DnDNode widget;

	public ShareModelCommand(String imageUrl, DnDNode widget) {
		this.widget = widget;
	}

	@Override
	public void execute() {
		widget.hidePopup();

		if (widget.getToolUrl() != null) {
			commandSend.sendShareCommand(PlanningTool.getUsers(),
					PlanningTool.getGroup(), PlanningTool.getChallengeId(),
					PlanningTool.getChallengeName(), PlanningTool.getToken(),
					new AsyncCallback<Void>() {

						@Override
						public void onSuccess(Void result) {
						}

						@Override
						public void onFailure(Throwable caught) {
							ClientErrorHandler.showErrorMessage(caught);
						}
					});
		}
	}

}
