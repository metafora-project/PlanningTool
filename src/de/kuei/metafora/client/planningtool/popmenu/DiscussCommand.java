package de.kuei.metafora.client.planningtool.popmenu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.kuei.metafora.client.PlanningTool;
import de.kuei.metafora.client.planningtool.gui.ClientErrorHandler;
import de.kuei.metafora.client.planningtool.gui.graph.DnDNode;
import de.kuei.metafora.client.planningtool.serverlink.MessageSend;
import de.kuei.metafora.client.planningtool.serverlink.MessageSendAsync;
import de.kuei.metafora.client.planningtool.xml.XMLBuilder;

public class DiscussCommand implements Command {

	private final MessageSendAsync commandSend = GWT.create(MessageSend.class);

	private String imageUrl = null;
	private DnDNode widget;

	public DiscussCommand(String imageUrl, DnDNode widget) {
		this.widget = widget;
		this.imageUrl = imageUrl;
	}

	@Override
	public void execute() {
		widget.hidePopup();

		if (imageUrl != null) {
			String xml = XMLBuilder.getInstance().buildDiscussAction(imageUrl,
					widget.getId());

			commandSend.sendToLogAndCommand(xml, PlanningTool.getToken(),
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
