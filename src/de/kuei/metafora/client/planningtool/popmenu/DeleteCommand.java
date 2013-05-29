package de.kuei.metafora.client.planningtool.popmenu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.kuei.metafora.client.PlanningTool;
import de.kuei.metafora.client.planningtool.PlanningToolWidget;
import de.kuei.metafora.client.planningtool.gui.ClientErrorHandler;
import de.kuei.metafora.client.planningtool.gui.DnDResizePanel;
import de.kuei.metafora.client.planningtool.gui.graph.DnDNode;
import de.kuei.metafora.client.planningtool.serverlink.GraphLink;
import de.kuei.metafora.client.planningtool.serverlink.GraphLinkAsync;
import de.kuei.metafora.client.planningtool.xml.XMLBuilder;
import de.kuei.metafora.client.planningtool.xml.cfcreator.Classification;
import de.kuei.metafora.shared.ActivityType;

public class DeleteCommand implements Command {

	private final GraphLinkAsync graphChange = GWT.create(GraphLink.class);

	private DnDResizePanel panel;
	private DnDNode widget;

	public DeleteCommand(DnDResizePanel panel, DnDNode widget) {
		this.panel = panel;
		this.widget = widget;
	}

	@Override
	public void execute() {
		widget.hidePopup();

		widget.removeEdges();

		panel.removeNode(widget);

		String xml = XMLBuilder.getInstance()
				.buildNodeAction(
						Classification.delete,
						"DELETE_NODE",
						widget.getId(),
						widget.getBGColor(),
						new int[] { widget.getAbsoluteLeft(),
								widget.getAbsoluteTop() },
						widget.getDescription(), widget.getPictureUrl(),
						widget.getToolUrl(), widget.getCategory(),
						widget.getName(), widget.getScalefactor(),
						ActivityType.DELETE_NODE);
		graphChange.actionReceived(xml, PlanningTool.getToken(),
				PlanningToolWidget.getInstance().getGraphName(),
				PlanningTool.getConnectionId(), new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						ClientErrorHandler.showErrorMessage(caught);
					}

					@Override
					public void onSuccess(String result) {
					}
				});
	}
}
