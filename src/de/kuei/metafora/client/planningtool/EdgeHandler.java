package de.kuei.metafora.client.planningtool;

import java.util.Vector;

import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandler;
import com.allen_sauer.gwt.dnd.client.DragStartEvent;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.PlanningTool;
import de.kuei.metafora.client.planningtool.gui.ClientErrorHandler;
import de.kuei.metafora.client.planningtool.gui.DnDResizePanel;
import de.kuei.metafora.client.planningtool.gui.graph.DnDNode;
import de.kuei.metafora.client.planningtool.gui.graph.PlanningEdge;
import de.kuei.metafora.client.planningtool.serverlink.GraphLink;
import de.kuei.metafora.client.planningtool.serverlink.GraphLinkAsync;
import de.kuei.metafora.client.planningtool.xml.GUIEventCreator;
import de.kuei.metafora.client.planningtool.xml.XMLBuilder;
import de.kuei.metafora.client.planningtool.xml.cfcreator.Classification;
import de.kuei.metafora.shared.ActivityType;

public class EdgeHandler implements DragHandler {

	// object needed to implement i18n through Languages interface
	final static Languages language = GWT.create(Languages.class);

	private final GraphLinkAsync graphChange = GWT.create(GraphLink.class);

	public static final String SOLID_BLUE = "SOLID_BLUE";
	public static final String SOLID_RED = "SOLID_RED";
	public static final String NO_TIP_BLACK = "NO_TIP_BLACK";

	public static ToggleButton SOLID_BLUE_BUTTON = new ToggleButton(language.Select());

	public static ToggleButton SOLID_RED_BUTTON = new ToggleButton(language.Select());

	public static ToggleButton NO_TIP_BLACK_BUTTON = new ToggleButton(language.Select());

	private DnDResizePanel edgeLayer;

	public String edgeType = SOLID_RED;

	// reference to start node for edge
	private DnDNode start = null;

	public EdgeHandler(DnDResizePanel edgelayer) {
		this.edgeLayer = edgelayer;

		SOLID_BLUE_BUTTON.addClickHandler(new EdgeClickHandler(this));
		SOLID_RED_BUTTON.addClickHandler(new EdgeClickHandler(this));
		NO_TIP_BLACK_BUTTON.addClickHandler(new EdgeClickHandler(this));

		SOLID_BLUE_BUTTON.setDown(false);
		SOLID_RED_BUTTON.setDown(true);
		NO_TIP_BLACK_BUTTON.setDown(false);

		edgeType = SOLID_RED;
	}

	public void noClick(DnDNode w) {
		start = null;
		w.edgeEnd();
	}

	// if there was a drag remove selection for start edge
	public void wasDrag(DnDNode widget) {
		start = null;

		String xml = XMLBuilder.getInstance()
				.buildNodeAction(
						Classification.modify,
						"MODIFY_NODE",
						widget.getId(),
						widget.getBGColor(),
						new int[] { widget.getRelativeLeft(),
								widget.getRelativeTop() },
						widget.getDescription(), widget.getPictureUrl(),
						widget.getToolUrl(), widget.getCategory(),
						widget.getName(), widget.getScalefactor(),
						ActivityType.MODIFY_POSITION);

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

		widget.edgeEnd();
	}

	public void setEdgeType(String type) {
		this.edgeType = type;
	}

	// handle edge build events
	public void doEdge(DnDNode widget) {
		if (start == null) {
			start = widget;
			start.edgeStart();
		} else {
			if (start.equals(widget) || widget == null) {
				// there can not be an edge without end or to the same node
				start.edgeEnd();
				start = null;
				return;
			}

			Vector<PlanningEdge> edges = null;

			if (start.haveEdgeTo(widget.getId())) {
				edges = start.getEdgesTo(widget.getId());
			}

			final PlanningEdge edge = new PlanningEdge(start, widget, edgeType);
			boolean add = start.addEdge(edge, false);
			widget.addEdge(edge, true);

			final String startid = start.getId();
			final String endid = widget.getId();

			start.edgeEnd();

			start = null;

			if (add) {
				String xml = XMLBuilder.getInstance().buildEdgeAction(
						Classification.create, "CREATE_EDGE", "null", startid,
						endid, edgeType, ActivityType.CREATE_EDGE);

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
								if (result != null) {
									edge.setId(result);
									GUIEventCreator.getInstance().registerEdge(
											result, edge);
								}
							}
						});

			} else if (edges.size() > 0) {
				for (int i = 0; i < edges.size(); i++) {
					PlanningEdge e = edges.get(i);

					String xml = XMLBuilder.getInstance().buildEdgeAction(
							Classification.delete, "DELETE_EDGE", e.getId(),
							e.getStart().getId(), e.getEnd().getId(),
							e.getEdgeType(), ActivityType.DELETE_EDGE);

					graphChange.actionReceived(xml, PlanningTool.getToken(),
							PlanningToolWidget.getInstance().getGraphName(),
							PlanningTool.getConnectionId(),
							new AsyncCallback<String>() {

								@Override
								public void onFailure(Throwable caught) {
									Window.alert(caught.getMessage());
								}

								@Override
								public void onSuccess(String result) {
								}
							});
				}
			}

			edgeLayer.drawEdges(start);
			edgeLayer.drawEdges(widget);
		}
	}

	public void addEdge(PlanningEdge edge) {
		if (edge.getStart().haveEdgeTo(edge.getEndId())) {
			return;
		}

		edge.getStart().addEdge(edge, false);
		edge.getEnd().addEdge(edge, true);
		edgeLayer.drawEdges(edge.getStart());
		edgeLayer.drawEdges(edge.getEnd());
	}

	public void removeEdge(PlanningEdge edge) {
		if (!edge.getStart().haveEdgeTo(edge.getEndId())) {
			return;
		}

		edge.getStart().addEdge(edge, false);
		edge.getEnd().addEdge(edge, true);
		edgeLayer.drawEdges(edge.getStart());
		edgeLayer.drawEdges(edge.getEnd());
	}

	@Override
	public void onDragEnd(DragEndEvent event) {
		Widget w = event.getContext().draggable;
		if (w instanceof DnDNode) {
			DnDNode img = (DnDNode) w;
			if (!img.testClick()) {
				wasDrag(img);
			}
		}
	}

	@Override
	public void onDragStart(DragStartEvent event) {
	}

	@Override
	public void onPreviewDragEnd(DragEndEvent event) throws VetoDragException {
	}

	@Override
	public void onPreviewDragStart(DragStartEvent event)
			throws VetoDragException {
	}
}