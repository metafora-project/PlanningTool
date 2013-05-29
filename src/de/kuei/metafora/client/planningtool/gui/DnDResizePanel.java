package de.kuei.metafora.client.planningtool.gui;

import java.util.Vector;

import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandler;
import com.allen_sauer.gwt.dnd.client.DragStartEvent;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;

import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.PlanningTool;
import de.kuei.metafora.client.planningtool.EdgeHandler;
import de.kuei.metafora.client.planningtool.PlanningToolWidget;
import de.kuei.metafora.client.planningtool.gui.graph.DnDNode;
import de.kuei.metafora.client.planningtool.gui.graph.PlanningEdge;
import de.kuei.metafora.client.planningtool.serverlink.GraphLink;
import de.kuei.metafora.client.planningtool.serverlink.GraphLinkAsync;
import de.kuei.metafora.client.planningtool.xml.XMLBuilder;

public class DnDResizePanel extends AbsolutePanel implements DragHandler,
		ClickHandler {

	// i18n
	final static Languages language = GWT.create(Languages.class);
	public static DnDResizePanel lastPanel = null;
	private boolean resizeDrag = false;
	private Canvas edgeCanvas;
	private final GraphLinkAsync graphChange = GWT.create(GraphLink.class);
	private Button verticalResize = null;

	public DnDResizePanel() {
		super();

		lastPanel = this;

		verticalResize = new Button("+");
		verticalResize.addClickHandler(this);
		verticalResize.getElement().getStyle().setZIndex(1000);
		add(verticalResize, 0, 0);

		edgeCanvas = Canvas.createIfSupported();
		if (edgeCanvas == null) {
			Window.alert(language.BrowserDoesNotSupportHTML5Canvas());
		} else {
			edgeCanvas.setWidth(500 + "px");
			edgeCanvas.setCoordinateSpaceWidth(500);

			edgeCanvas.setHeight(500 + "px");
			edgeCanvas.setCoordinateSpaceHeight(500);

			add(edgeCanvas, 0, 0);
		}
	}

	@Override
	public void add(Widget w) {

	}

	@Override
	public void setPixelSize(int width, int height) {
		super.setPixelSize(width, height);

		edgeCanvas.setSize(width + "px", height + "px");
		edgeCanvas.setCoordinateSpaceHeight(height);
		edgeCanvas.setCoordinateSpaceWidth(width);

		Context2d context = edgeCanvas.getContext2d();
		context.setFillStyle(CssColor.make(255, 100, 200));
		context.fillRect(0, 0, width, height);

		drawEdges(null);
		// callServer();
	}

	private void callServer() {
		String xml = XMLBuilder.getInstance().buildAreasizeAction(
				edgeCanvas.getCoordinateSpaceWidth(),
				edgeCanvas.getCoordinateSpaceHeight());

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

	public void paintEdges() {
		drawEdges(null);
	}

	@Override
	public void setWidth(String width) {

		if (width.contains(".")) {
			int pos = width.indexOf('.');
			if (pos > 0) {
				width = width.substring(0, pos);
			}
		}

		if (width.contains(",")) {
			int pos = width.indexOf(',');
			if (pos > 0) {
				width = width.substring(0, pos);
			}
		}

		super.setWidth(width);
		edgeCanvas.setWidth(width + "px");

		setWidgetPosition(verticalResize,
				getOffsetWidth() - verticalResize.getOffsetWidth(), 0);

		if (width.endsWith("px")) {
			String numb = width.substring(0, width.length() - 2);
			numb = numb.trim();
			try {
				edgeCanvas.setCoordinateSpaceWidth(Integer.parseInt(numb));
				drawEdges(null);
				// callServer();
			} catch (NumberFormatException e) {
			}
		}
	}

	@Override
	public void setHeight(String height) {

		if (height.contains(".")) {
			int pos = height.indexOf('.');
			if (pos > 0) {
				height = height.substring(0, pos);
			}
		}

		if (height.contains(",")) {
			int pos = height.indexOf(',');
			if (pos > 0) {
				height = height.substring(0, pos);
			}
		}

		super.setHeight(height);
		edgeCanvas.setHeight(height + "px");
		if (height.endsWith("px")) {
			String numb = height.substring(0, height.length() - 2);
			numb = numb.trim();
			try {
				edgeCanvas.setCoordinateSpaceHeight(Integer.parseInt(numb));
				drawEdges(null);
				// callServer();
			} catch (NumberFormatException e) {
			}
		}
	}

	@Override
	public boolean remove(Widget w) {
		resizeDrag = true;
		return super.remove(w);
	}

	public void resizeWithServerCall(int x, int y) {
		setHeight(y + "px");
		setWidth(x + "px");
		callServer();
	}

	private void resize(DragEndEvent e) {
		if (resizeDrag) {
			resizeDrag = false;
			Widget w = e.getContext().draggable;

			int reqHeight = w.getOffsetHeight() + w.getAbsoluteTop()
					- getAbsoluteTop();

			if (getOffsetHeight() < reqHeight) {
				setHeight((reqHeight + 500) + "px");
				callServer();
			}
		}
	}

	@Override
	public void onDragEnd(DragEndEvent event) {
		resize(event);

		Widget w = event.getContext().draggable;
		if (w instanceof DnDNode)
			drawEdges((DnDNode) w);
		else
			drawEdges(null);
	}

	@Override
	public void onDragStart(DragStartEvent event) {

	}

	@Override
	public void onPreviewDragEnd(DragEndEvent event) throws VetoDragException {
		// TODO: fix resize problems
		resize(event);

		Widget w = event.getContext().draggable;
		if (w instanceof DnDNode)
			drawEdges((DnDNode) w);
		else
			drawEdges(null);
	}

	@Override
	public void onPreviewDragStart(DragStartEvent event)
			throws VetoDragException {

	}

	public void removeNode(DnDNode node) {
		setWidgetPosition((Widget) node, -1000, -1000);
		paintEdges();
	}

	@Override
	public void onClick(ClickEvent event) {

		int width = edgeCanvas.getCoordinateSpaceWidth();
		width += 200;

		setWidth(width + "px");

		edgeCanvas.setCoordinateSpaceWidth(width);

		edgeCanvas.setWidth(width + "px");

		callServer();
	}

	public void drawEdges(DnDNode node) {
		Context2d context = edgeCanvas.getContext2d();

		context.clearRect(0, 0, edgeCanvas.getCoordinateSpaceWidth(),
				edgeCanvas.getCoordinateSpaceHeight());

		context.setLineWidth(2);
		context.setStrokeStyle(CssColor.make(0, 0, 0));

		int width = edgeCanvas.getCoordinateSpaceWidth();
		int height = edgeCanvas.getCoordinateSpaceHeight();

		context.beginPath();
		context.moveTo(0, 0);
		context.lineTo(width, 0);
		context.lineTo(width, height);
		context.lineTo(0, height);
		context.lineTo(0, 0);
		context.closePath();
		context.stroke();

		context.setLineWidth(4);
		context.setStrokeStyle(CssColor.make(0, 0, 255));

		// search for graph nodes (ImagePaletteWidget)
		Vector<DnDNode> nodes = new Vector<DnDNode>();
		for (int i = 0; i < getWidgetCount(); i++) {
			Widget w = getWidget(i);
			if (w instanceof DnDNode) {
				nodes.add((DnDNode) w);
			}
		}

		// add draged node
		// this is necessary because node is
		// removed from container while dragging
		if (node != null) {
			nodes.add(node);
		}

		// draw edges for each node
		for (DnDNode w : nodes) {
			drawEdgesForNode(w);
		}
	}

	private void drawEdgesForNode(DnDNode w) {
		Context2d context = edgeCanvas.getContext2d();

		// get nodes which are linked from this node
		// (outgoing edges)
		Vector<PlanningEdge> other = w.getOutgoingEdges();

		// draw each edge
		for (PlanningEdge edge : other) {
			boolean noTip = false;

			if (edge.getEdgeType() == EdgeHandler.SOLID_BLUE) {
				context.setStrokeStyle(CssColor.make(0, 0, 255));
			} else if (edge.getEdgeType() == EdgeHandler.SOLID_RED) {
				context.setStrokeStyle(CssColor.make(255, 0, 0));
			} else if (edge.getEdgeType() == EdgeHandler.NO_TIP_BLACK) {
				context.setStrokeStyle(CssColor.make(0, 0, 0));
				noTip = true;
			} else {
				context.setStrokeStyle(CssColor.make(0, 0, 0));
			}

			DnDNode o = edge.getEnd();

			// calcualte tip points
			int[] tipPoints = getTip(w, o);

			// use tip point method to get start point of edge
			int[] startPoints = getTip(o, w);

			// draw edge
			context.beginPath();
			context.moveTo(startPoints[0], startPoints[1]);
			context.lineTo(tipPoints[0], tipPoints[1]);
			context.closePath();
			context.stroke();

			// draw tip
			if (!noTip) {
				context.beginPath();
				context.moveTo(tipPoints[0], tipPoints[1]);
				context.lineTo(tipPoints[2], tipPoints[3]);
				context.lineTo(tipPoints[4], tipPoints[5]);
				context.lineTo(tipPoints[0], tipPoints[1]);
				context.closePath();
				context.stroke();
			}
		}
	}

	// calculate tip points
	public int[] getTipPoints(int[] point, int[] vect) {
		// array for tip points
		// {ax, ay, bx, by, cx, cy}
		int[] tipPoints = new int[6];

		// intersection point is first tip point
		tipPoints[0] = point[0];
		tipPoints[1] = point[1];

		// calculate normalization factor
		double norm = Math.sqrt(vect[0] * vect[0] + vect[1] * vect[1]);

		// go 10px back on edge
		double hx = point[0] - (vect[0] / norm) * 10;
		double hy = point[1] - (vect[1] / norm) * 10;

		// go 6 px with 90� to edge in each direction
		// to get other tip points
		tipPoints[2] = (int) (hx + (-vect[1] / norm) * 6);
		tipPoints[3] = (int) (hy + (vect[0] / norm) * 6);

		tipPoints[4] = (int) (hx - (-vect[1] / norm) * 6);
		tipPoints[5] = (int) (hy - (vect[0] / norm) * 6);

		// transform from absolute to canvas coordinates
		// x-values
		tipPoints[0] -= getAbsoluteLeft();
		tipPoints[2] -= getAbsoluteLeft();
		tipPoints[4] -= getAbsoluteLeft();
		// y-values
		tipPoints[1] -= getAbsoluteTop();
		tipPoints[3] -= getAbsoluteTop();
		tipPoints[5] -= getAbsoluteTop();

		return tipPoints;
	}

	// calculate intersection point with box
	private int[] getTip(DnDNode s, DnDNode e) {
		// edge to linear equation
		double[] centers = getLinearEquation(s.getCenterX(), s.getCenterY(),
				e.getCenterX(), e.getCenterY());

		int[] vect = new int[2];
		int[] tipPoints = null;

		// edge direction vector
		vect[0] = e.getCenterX() - s.getCenterX();
		vect[1] = e.getCenterY() - s.getCenterY();

		// test if upper or lower box edge is relevant
		if (s.getCenterY() < e.getCenterY()) {
			// test upper box edge
			// get equation for upper box edge
			double[] box = getLinearEquation(e.getAbsoluteLeft(),
					e.getAbsoluteTop(),
					e.getAbsoluteLeft() + e.getOffsetWidth(),
					e.getAbsoluteTop());
			// calculate intersection point upper box line
			int[] point = intersectLines(centers[0], centers[1], centers[2],
					box[0], box[1], box[2]);
			if (point[0] < e.getAbsoluteLeft()) {
				// point is on left box edge
				box = getLinearEquation(e.getAbsoluteLeft(),
						e.getAbsoluteTop(), e.getAbsoluteLeft(),
						e.getAbsoluteTop() + e.getOffsetHeight());
				point = intersectLines(centers[0], centers[1], centers[2],
						box[0], box[1], box[2]);
				tipPoints = getTipPoints(point, vect);
			} else if (point[0] > e.getAbsoluteLeft() + e.getOffsetWidth()) {
				// point is on right box edge
				box = getLinearEquation(
						e.getAbsoluteLeft() + e.getOffsetWidth(),
						e.getAbsoluteTop(),
						e.getAbsoluteLeft() + e.getOffsetWidth(),
						e.getAbsoluteTop() + e.getOffsetHeight());
				point = intersectLines(centers[0], centers[1], centers[2],
						box[0], box[1], box[2]);
				tipPoints = getTipPoints(point, vect);
			} else {
				// point is on top box edge
				tipPoints = getTipPoints(point, vect);
			}
		} else if (s.getCenterY() > e.getCenterY()) {
			// test upper box edge
			double[] box = getLinearEquation(e.getAbsoluteLeft(),
					e.getAbsoluteTop() + e.getOffsetHeight(),
					e.getAbsoluteLeft() + e.getOffsetWidth(),
					e.getAbsoluteTop() + e.getOffsetHeight());
			int[] point = intersectLines(centers[0], centers[1], centers[2],
					box[0], box[1], box[2]);
			if (point[0] < e.getAbsoluteLeft()) {
				// point on left box edge
				box = getLinearEquation(e.getAbsoluteLeft(),
						e.getAbsoluteTop(), e.getAbsoluteLeft(),
						e.getAbsoluteTop() + e.getOffsetHeight());
				point = intersectLines(centers[0], centers[1], centers[2],
						box[0], box[1], box[2]);
				tipPoints = getTipPoints(point, vect);
			} else if (point[0] > e.getAbsoluteLeft() + e.getOffsetWidth()) {
				// point on right box edge
				box = getLinearEquation(
						e.getAbsoluteLeft() + e.getOffsetWidth(),
						e.getAbsoluteTop(),
						e.getAbsoluteLeft() + e.getOffsetWidth(),
						e.getAbsoluteTop() + e.getOffsetHeight());
				point = intersectLines(centers[0], centers[1], centers[2],
						box[0], box[1], box[2]);
				tipPoints = getTipPoints(point, vect);
			} else {
				// point on lower box edge
				tipPoints = getTipPoints(point, vect);
			}
		} else {
			// nodes have same y-coordinate
			if (s.getCenterX() < e.getCenterX()) {
				// calc points for left edge
				tipPoints = getTipPoints(
						new int[] {
								e.getAbsoluteLeft(),
								e.getAbsoluteTop()
										+ (int) (e.getOffsetHeight() / 2) },
						vect);
			} else if (s.getCenterX() > e.getCenterX()) {
				// calc points for right edge
				tipPoints = getTipPoints(
						new int[] {
								e.getAbsoluteLeft() + e.getOffsetWidth(),
								e.getAbsoluteTop()
										+ (int) (e.getOffsetHeight() / 2) },
						vect);
			} else {
				// both boxes have same center
				// => edge not visible
				// return canvas translated center point to avoid errors
				// edge and tip are not visible
				return new int[] { e.getCenterX() - getAbsoluteLeft(),
						e.getCenterY() - getAbsoluteTop(),
						e.getCenterX() - getAbsoluteLeft(),
						e.getCenterY() - getAbsoluteTop(),
						e.getCenterX() - getAbsoluteLeft(),
						e.getCenterY() - getAbsoluteTop() };
			}
		}
		// return points for tip
		return tipPoints;
	}

	// build linear equation for two points
	private double[] getLinearEquation(int x1, int y1, int x2, int y2) {
		double[] equ = new double[3];
		equ[0] = y2 - y1;
		equ[1] = x1 - x2;
		equ[2] = (x2 * y1) - (x1 * y2);
		return equ;
	}

	// calc instersection point
	private int[] intersectLines(double a1, double b1, double c1, double a2,
			double b2, double c2) {
		int[] inter = new int[2];

		double deter = ((a1 * b2) - (a2 * b1));
		if (deter == 0) {
			return null;
		}

		inter[0] = (int) (((b1 * c2) - (b2 * c1)) / deter);
		inter[1] = (int) (((c1 * a2) - (c2 * a1)) / deter);

		return inter;
	}

}
