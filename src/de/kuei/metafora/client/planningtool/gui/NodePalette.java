package de.kuei.metafora.client.planningtool.gui;

import java.util.HashMap;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import de.kuei.metafora.client.planningtool.gui.graph.DnDNode;

public class NodePalette {

	private PickupDragController dragController;
	private StackLayoutPanel panel;

	private HashMap<String, FlowPanel> categories;

	public NodePalette(PickupDragController dragController,
			StackLayoutPanel panel) {
		this.dragController = dragController;
		this.panel = panel;

		dragController.setBehaviorDragStartSensitivity(10);

		categories = new HashMap<String, FlowPanel>();
	}

	public void add(Widget widget) {
		DnDNode dw = (DnDNode) widget;
		if (categories.containsKey(dw.getCategory())) {
			categories.get(dw.getCategory()).add(widget);
		} else {
			FlowPanel flow = new NodeFlowPalette(dragController);
			flow.add(widget);
			categories.put(dw.getCategory(), flow);
			ScrollPanel scroll = new ScrollPanel(flow);
			panel.add(scroll, dw.getCategory(), 25);
			panel.getHeaderWidget(scroll).getElement()
					.setClassName("planningToolStackHeader");
		}
	}

}
