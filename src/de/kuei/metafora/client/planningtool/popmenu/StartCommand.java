package de.kuei.metafora.client.planningtool.popmenu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.kuei.metafora.client.PlanningTool;
import de.kuei.metafora.client.planningtool.PlanningToolWidget;
import de.kuei.metafora.client.planningtool.gui.ClientErrorHandler;
import de.kuei.metafora.client.planningtool.gui.graph.DnDNode;
import de.kuei.metafora.client.planningtool.serverlink.MessageSend;
import de.kuei.metafora.client.planningtool.serverlink.MessageSendAsync;
import de.kuei.metafora.client.planningtool.xml.XMLBuilder;

public class StartCommand implements Command {

	private final MessageSendAsync commandSend = GWT.create(MessageSend.class);

	private DnDNode widget;

	public StartCommand(String toolUrl, DnDNode widget) {
		this.widget = widget;
	}

	@Override
	public void execute() {
		widget.hidePopup();

		String toolUrl = widget.getToolUrl();

		widget.setStarted();

		if (toolUrl != null && toolUrl.length() > 0 && toolUrl != "null") {

			String nodeid = widget.getId();
			String map = PlanningToolWidget.getInstance().getGraphName();

			// Base64 base = new Base64();
			// nodeid = base.encodeStringForUrl(nodeid);
			// map = base.encodeStringForUrl(map);

			nodeid = URL.encode(nodeid);
			map = URL.encode(map);

			// add ptNodeId and ptMap only to URL if not already there
			// -> eXpresser resource card
			String url = toolUrl;
			if (!(toolUrl.contains("ptNodeId"))) {
				if (toolUrl.contains("?")) {
					url += "&ptNodeId=" + nodeid;
				} else {
					url += "?ptNodeId=" + nodeid;
				}
			}

			if (!(toolUrl.contains("ptMap"))) {
				url += "&ptMap=" + map;
			}

			String xml = XMLBuilder.getInstance().buildToolAction(url,
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
