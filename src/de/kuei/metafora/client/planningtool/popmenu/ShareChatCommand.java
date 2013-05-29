package de.kuei.metafora.client.planningtool.popmenu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.kuei.metafora.client.PlanningTool;
import de.kuei.metafora.client.planningtool.PlanningToolWidget;
import de.kuei.metafora.client.planningtool.gui.ClientErrorHandler;
import de.kuei.metafora.client.planningtool.gui.graph.DnDNode;
import de.kuei.metafora.client.planningtool.serverlink.MessageSend;
import de.kuei.metafora.client.planningtool.serverlink.MessageSendAsync;

public class ShareChatCommand implements Command {

	private final MessageSendAsync commandSend = GWT.create(MessageSend.class);

	private DnDNode widget;

	public ShareChatCommand(DnDNode widget) {
		this.widget = widget;
	}

	@Override
	public void execute() {
		widget.hidePopup();

		PlanningToolWidget ptw = PlanningToolWidget.getInstance();
		String url = null;
		String viewurl = widget.getPictureUrl();

		String text = widget.getName();
		if (widget.getDescription() != null
				&& widget.getDescription().length() > 0) {
			text += " (" + widget.getDescription() + ")";
		}
		text += " Plan: " + ptw.getGraphName();

		url = PlanningTool.getTomcatServer() + "/planningtoolsolo/?centerNode="
				+ URL.encode(widget.getId());

		commandSend.sendToChatCommand(ptw.getGraphName(), widget.getId(), url,
				LocaleInfo.getCurrentLocale().getLocaleName(),
				PlanningTool.getToken(), PlanningTool.getUsers(),
				PlanningTool.getGroup(), PlanningTool.getChallengeId(),
				PlanningTool.getChallengeName(), viewurl, text,
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
