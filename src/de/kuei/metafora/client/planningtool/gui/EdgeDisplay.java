package de.kuei.metafora.client.planningtool.gui;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.LayoutPanel;

import de.kuei.metafora.client.planningtool.EdgeHandler;

public class EdgeDisplay extends LayoutPanel {

	private Canvas picture;
	private String type;
	private DnDResizePanel resizePanel;

	public EdgeDisplay(int width, int height, String type, DnDResizePanel panel) {
		super();

		this.resizePanel = panel;
		this.type = type;

		picture = Canvas.createIfSupported();

		picture.setWidth(width + "px");
		picture.setCoordinateSpaceWidth(width);

		picture.setHeight(height + "px");
		picture.setCoordinateSpaceHeight(height);

		add(picture);
		setWidgetLeftRight(picture, 0, Unit.PX, 0, Unit.PX);
		setWidgetTopBottom(picture, 0, Unit.PX, 0, Unit.PX);

		drawPicture();
	}

	private void drawPicture() {
		Context2d context = picture.getContext2d();

		boolean noTip = false;

		context.setLineWidth(4);

		if (type == EdgeHandler.SOLID_BLUE) {
			context.setStrokeStyle(CssColor.make(0, 0, 255));
		} else if (type == EdgeHandler.SOLID_RED) {
			context.setStrokeStyle(CssColor.make(255, 0, 0));
		} else if (type == EdgeHandler.NO_TIP_BLACK) {
			context.setStrokeStyle(CssColor.make(0, 0, 0));
			noTip = true;
		} else {
			context.setStrokeStyle(CssColor.make(0, 0, 0));
		}

		// calcualte tip points
		int[] point = new int[2];
		point[1] = picture.getCoordinateSpaceHeight() / 2;
		point[0] = picture.getCoordinateSpaceWidth() - 5;

		int[] vect = new int[] { 1, 0 };

		int[] tipPoints = resizePanel.getTipPoints(point, vect);

		// use tip point method to get start point of edge
		int[] startPoints = new int[2];
		startPoints[0] = 5;
		startPoints[1] = point[1];

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
