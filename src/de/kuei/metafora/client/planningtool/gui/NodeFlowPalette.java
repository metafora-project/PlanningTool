package de.kuei.metafora.client.planningtool.gui;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import de.kuei.metafora.client.PlanningTool;
import de.kuei.metafora.client.planningtool.PlanningToolWidget;
import de.kuei.metafora.client.planningtool.gui.graph.DnDNode;
import de.kuei.metafora.client.planningtool.serverlink.GraphLink;
import de.kuei.metafora.client.planningtool.serverlink.GraphLinkAsync;
import de.kuei.metafora.client.planningtool.xml.GUIEventCreator;
import de.kuei.metafora.client.planningtool.xml.XMLBuilder;
import de.kuei.metafora.client.planningtool.xml.cfcreator.Classification;
import de.kuei.metafora.shared.ActivityType;

public class NodeFlowPalette extends FlowPanel {

	private PickupDragController dragController;

	private final GraphLinkAsync graphChange = GWT.create(GraphLink.class);

	public NodeFlowPalette(PickupDragController dragController) {
		super();
		this.dragController = dragController;
	}

	@Override
	public boolean remove(Widget w) {
		// this method is called if a drag is started
		int index = getWidgetIndex(w);

		if (index != -1 && w instanceof DnDNode) {

			final DnDNode newnode = (DnDNode) w;

			PlanningToolWidget ptw = PlanningToolWidget.getInstance();
			if (ptw != null) {
				newnode.setEdgeHandler(ptw.getEdgeHandler());
				ptw.getEdgeHandler().noClick(newnode);
			}

			// if it is a palette item
			// clone it and add it to palette
			// for dnd palette effect
			Widget clone = ((DnDNode) w).cloneWidget();
			dragController.makeDraggable(clone);
			insert(clone, index);

			String xml = XMLBuilder.getInstance().buildNodeAction(
					Classification.create,
					"CREATE_NODE",
					"default",
					newnode.getBGColor(),
					new int[] { newnode.getAbsoluteLeft(),
							newnode.getAbsoluteTop() },
					newnode.getDescription(), newnode.getPictureUrl(),
					newnode.getToolUrl(), newnode.getCategory(),
					newnode.getName(), newnode.getScalefactor(),
					ActivityType.CREATE_NODE);

			graphChange.actionReceived(xml, PlanningTool.getToken(),
					PlanningToolWidget.getInstance().getGraphName(),
					PlanningTool.getConnectionId(),
					new AsyncCallback<String>() {

						@Override
						public void onFailure(Throwable caught) {
							ClientErrorHandler.showErrorMessage(caught);
						}

						@Override
						public void onSuccess(String result) {
							newnode.setId(result);
							GUIEventCreator.getInstance().registerNode(result,
									newnode);
						}
					});
		}
		return super.remove(w);
	}

}
