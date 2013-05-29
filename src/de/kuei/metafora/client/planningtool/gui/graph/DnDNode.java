package de.kuei.metafora.client.planningtool.gui.graph;

import java.util.Vector;

import com.allen_sauer.gwt.dnd.client.HasDragHandle;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import de.kuei.metafora.client.PlanningTool;
import de.kuei.metafora.client.planningtool.EdgeHandler;
import de.kuei.metafora.client.planningtool.PlanningToolWidget;
import de.kuei.metafora.client.planningtool.gui.ClientErrorHandler;
import de.kuei.metafora.client.planningtool.gui.DescriptionConflictDialog;
import de.kuei.metafora.client.planningtool.gui.DnDResizePanel;
import de.kuei.metafora.client.planningtool.gui.graph.util.AutoSizeTextArea;
import de.kuei.metafora.client.planningtool.popmenu.DeleteMenuItem;
import de.kuei.metafora.client.planningtool.popmenu.DiscussMenuItem;
import de.kuei.metafora.client.planningtool.popmenu.ReadyMenuItem;
import de.kuei.metafora.client.planningtool.popmenu.ShareChatMenuItem;
import de.kuei.metafora.client.planningtool.popmenu.ShareModelMenuItem;
import de.kuei.metafora.client.planningtool.popmenu.StartMenuItem;
import de.kuei.metafora.client.planningtool.serverlink.GraphLink;
import de.kuei.metafora.client.planningtool.serverlink.GraphLinkAsync;
import de.kuei.metafora.client.planningtool.util.InputFilter;
import de.kuei.metafora.client.planningtool.xml.XMLBuilder;
import de.kuei.metafora.client.planningtool.xml.cfcreator.Classification;
import de.kuei.metafora.shared.ActivityType;

public class DnDNode extends VerticalPanel implements KeyPressHandler,
		MouseDownHandler, MouseUpHandler, BlurHandler, HasDragHandle,
		ContextMenuHandler {

	private String id;

	private String url;
	private String description;
	private String tool;
	private String category;
	private String name;

	private String bgColor = "#AAAAAA";

	private boolean started = false;
	private boolean ready = false;

	private final MenuBar popupMenu;
	private final PopupPanel popup;

	private Vector<PlanningEdge> outgoingEdges;
	private Vector<PlanningEdge> incommingEdges;

	private Vector<String> creators;

	private EdgeHandler edgeHandler;

	private Image image;
	private TextArea descriptionField;

	private DnDResizePanel panel;

	private final GraphLinkAsync graphChange = GWT.create(GraphLink.class);

	private String scalefactor = "100";

	private int x_old;
	private int y_old;

	private DescriptionConflictDialog dialog = null;

	public DnDNode(String color, String nodeText, String pictureUrl,
			String toolUrl, String category, String name, DnDResizePanel panel) {

		url = pictureUrl.replaceAll(" ", "%20");

		tool = toolUrl;
		this.category = category;
		this.name = name;
		this.bgColor = color;
		this.description = nodeText;

		this.panel = panel;

		image = new Image(url);
		image.setSize("100px", "100px");
		image.addMouseDownHandler(this);
		image.addMouseUpHandler(this);

		descriptionField = new AutoSizeTextArea();
		descriptionField.setText(description);

		add(image);
		add(new Label(this.name));
		add(descriptionField);

		descriptionField.addKeyPressHandler(this);
		descriptionField.addKeyPressHandler(new InputFilter(descriptionField));
		descriptionField.addBlurHandler(this);

		outgoingEdges = new Vector<PlanningEdge>();
		incommingEdges = new Vector<PlanningEdge>();

		creators = new Vector<String>();

		getElement().getStyle().setBackgroundColor(bgColor);
		getElement().getStyle().setPadding(4, Unit.PX);
		getElement().getStyle().setMargin(2, Unit.PX);

		popupMenu = new MenuBar(true);
		popupMenu.addItem(new DeleteMenuItem(DnDResizePanel.lastPanel, this));

		popupMenu.addItem(new StartMenuItem(toolUrl, this));

		popupMenu.addItem(new ReadyMenuItem(this));

		popupMenu.addItem(new DiscussMenuItem(url, this));

		popupMenu.addItem(new ShareChatMenuItem(this));

		if (tool != null && tool.length() > 0) {
			// TODO: share model menu item?
			if (tool.toLowerCase().contains("expresser")) {
				popupMenu.addItem(new ShareModelMenuItem(url, this));
			}
		}

		popup = new PopupPanel(true);
		popup.add(popupMenu);

		addDomHandler(this, ContextMenuEvent.getType());
	}

	@Override
	public Widget getDragHandle() {
		return image;
	}

	public DnDNode cloneWidget() {
		DnDNode clone = new DnDNode(bgColor, description, url, tool, category,
				name, panel);
		clone.setScalefactor(scalefactor);
		return clone;
	}

	public int getCenterX() {
		return (int) (getAbsoluteLeft() + (getOffsetWidth() / 2));
	}

	public int getCenterY() {
		return (int) (getAbsoluteTop() + (getOffsetHeight() / 2));
	}

	public Vector<PlanningEdge> getOutgoingEdges() {
		return outgoingEdges;
	}

	public boolean addEdge(PlanningEdge edge, boolean incomming) {
		if (incomming) {
			if (incommingEdges.contains(edge)) {
				incommingEdges.remove(edge);
				return false;
			} else {
				incommingEdges.add(edge);
				return true;
			}
		} else {
			if (outgoingEdges.contains(edge)) {
				outgoingEdges.remove(edge);
				return false;
			} else {
				outgoingEdges.add(edge);
				return true;
			}
		}
	}

	public void edgeStart() {
		getElement().getStyle().setBackgroundColor("#FFAABB");
	}

	public void edgeEnd() {
		getElement().getStyle().setBackgroundColor(bgColor);
	}

	public void setId(String id) {
		this.id = id;
		setTitle(id);
	}

	public String getId() {
		return id;
	}

	public int getRelativeLeft() {
		return this.getAbsoluteLeft()
				- DnDResizePanel.lastPanel.getAbsoluteLeft();
	}

	public int getRelativeTop() {
		return this.getAbsoluteTop()
				- DnDResizePanel.lastPanel.getAbsoluteTop();
	}

	public void setBGColor(String color) {
		this.bgColor = color;
		getElement().getStyle().setBackgroundColor(bgColor);

		String xml = XMLBuilder.getInstance().buildNodeAction(
				Classification.modify,
				"MODIFY_NODE",
				getId(),
				color,
				new int[] { getRelativeLeft(), getRelativeTop() },
				getDescription(),
				getPictureUrl(),
				getToolUrl(),
				getCategory(),
				getName(),
				getScalefactor(),
				(isStarted() ? ActivityType.MODIFY_STATE_STARTED
						: ActivityType.MODIFY_STATE_FINISHED));

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

	@Override
	public void onContextMenu(ContextMenuEvent event) {
		event.stopPropagation();
		event.preventDefault();
		showPopupMenu(event.getNativeEvent());
		noClick();
	}

	public void showPopupMenu(NativeEvent event) {
		popup.setPopupPosition(event.getClientX(), event.getClientY());
		popup.show();
	}

	public void hidePopup() {
		popup.hide();
		noClick();
	}

	public Vector<PlanningEdge> getIncommingEdges() {
		return incommingEdges;
	}

	public void removeEdges() {
		for (int i = outgoingEdges.size() - 1; i >= 0; i--) {
			PlanningEdge edge = outgoingEdges.get(i);
			removeEdge(edge);
			edge.getEnd().removeEdge(edge);

			String xml = XMLBuilder.getInstance().buildEdgeAction(
					Classification.delete, "DELETE_EDGE", edge.getId(),
					edge.getStart().getId(), edge.getEnd().getId(),
					edge.getEdgeType(), ActivityType.DELETE_EDGE);

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
						}
					});
		}

		for (int i = incommingEdges.size() - 1; i >= 0; i--) {
			PlanningEdge edge = incommingEdges.get(i);
			removeEdge(edge);
			edge.getStart().removeEdge(edge);

			String xml = XMLBuilder.getInstance().buildEdgeAction(
					Classification.delete, "DELETE_EDGE", edge.getId(),
					edge.getStart().getId(), edge.getEnd().getId(),
					edge.getEdgeType(), ActivityType.DELETE_EDGE);

			graphChange.actionReceived(xml, PlanningTool.getToken(),
					PlanningToolWidget.getInstance().getGraphName(),
					PlanningTool.getConnectionId(),
					new AsyncCallback<String>() {

						@Override
						public void onFailure(Throwable caught) {
							// Window.alert(caught.getMessage());
						}

						@Override
						public void onSuccess(String result) {
						}
					});
		}
	}

	public void removeEdge(PlanningEdge edge) {
		if (outgoingEdges.contains(edge))
			outgoingEdges.remove(edge);
		if (incommingEdges.contains(edge))
			incommingEdges.remove(edge);
	}

	public String getBGColor() {
		return bgColor;
	}

	public String getDescription() {
		return description;
	}

	public String getPictureUrl() {
		return url;
	}

	public String getToolUrl() {
		return tool;
	}

	public String getCategory() {
		return category;
	}

	public String getName() {
		return name;
	}

	public void addCreator(String creator) {
		creators.add(creator);
	}

	public void updateRelativeLeft(int left) {
		if (panel.getOffsetWidth() <= left + getOffsetWidth() + 20) {
			panel.resizeWithServerCall(panel.getOffsetHeight(), left
					+ getOffsetWidth() + 100);
		}
		panel.setWidgetPosition(this, left, getRelativeTop());
		panel.paintEdges();
	}

	public void updateRelativeTop(int top) {
		if (panel.getOffsetHeight() <= top + getOffsetHeight() + 20) {
			panel.resizeWithServerCall(top + getOffsetHeight() + 100,
					panel.getOffsetWidth());
		}
		panel.setWidgetPosition(this, getRelativeLeft(), top);
		panel.paintEdges();
	}

	public void updateBGColor(String color) {
		this.bgColor = color;
		getElement().getStyle().setBackgroundColor(bgColor);
	}

	public void updateDescription(String text) {
		if (description.equals(descriptionField.getText())
				|| text.equals(descriptionField.getText())) {
			// no local uncomitted changes or own update
			descriptionField.setText(text);
			description = text;
			descriptionField.getElement().getStyle()
					.setBackgroundColor("#FFFFFF");
		} else {
			if (text.equals(description)) {

				// do nothing
			} else {
				if (dialog == null) {
					dialog = new DescriptionConflictDialog(this, text);
				} else {
					dialog.setOwnText(descriptionField.getText());
					dialog.setOtherText(text);
				}

				dialog.show();
			}
		}
	}

	public boolean haveEdgeTo(String nodeId) {
		for (int i = 0; i < outgoingEdges.size(); i++) {
			if (outgoingEdges.get(i).getEndId().equals(nodeId)) {
				return true;
			}
		}
		return false;
	}

	public Vector<PlanningEdge> getEdgesTo(String nodeId) {
		Vector<PlanningEdge> edges = new Vector<PlanningEdge>();

		for (int i = 0; i < outgoingEdges.size(); i++) {
			if (outgoingEdges.get(i).getEndId().equals(nodeId)) {
				edges.add(outgoingEdges.get(i));
			}
		}
		return edges;
	}

	public void setDescription(String text) {
		this.description = text;
		this.descriptionField.setText(text);
	}

	public void setScalefactor(String scalefactor) {
		this.scalefactor = scalefactor;
		image.setSize(scalefactor + "px", scalefactor + "px");
	}

	public String getScalefactor() {
		return scalefactor;
	}

	@Override
	public void onKeyPress(KeyPressEvent event) {

		descriptionField.getElement().getStyle().setBackgroundColor("#FFD1D5");

		// I am not responsible of this code.
		// They made me write it, against my will.

		if (((int) event.getCharCode()) == 13
				|| (((int) event.getCharCode()) == 0 && ((int) event
						.getNativeEvent().getKeyCode()) == 13)) {
			if (!event.isShiftKeyDown()) {

				descriptionField.cancelKey();

				sendNewDescription(descriptionField.getText());
			}
		}
	}

	public void sendNewDescription(String text) {
		description = text;
		descriptionField.setText(text);

		String xml = XMLBuilder.getInstance().buildNodeAction(
				Classification.modify, "MODIFY_NODE", getId(), getBGColor(),
				new int[] { getRelativeLeft(), getRelativeTop() },
				getDescription(), getPictureUrl(), getToolUrl(), getCategory(),
				getName(), getScalefactor(), ActivityType.MODIFY_TEXT);

		graphChange.actionReceived(xml, PlanningTool.getToken(),
				PlanningToolWidget.getInstance().getGraphName(),
				PlanningTool.getConnectionId(), new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						ClientErrorHandler.showErrorMessage(caught);
					}

					@Override
					public void onSuccess(String result) {
						descriptionField.getElement().getStyle()
								.setBackgroundColor("#D1FFEC");
					}
				});
	}

	public void setEdgeHandler(EdgeHandler handler) {
		edgeHandler = handler;
	}

	public void noClick() {
		if (edgeHandler != null) {
			edgeHandler.noClick(this);
		}
	}

	public void wasClick() {
		if (edgeHandler != null) {
			edgeHandler.doEdge(this);
		}
	}

	public void highLight(boolean highlight) {
		if (highlight) {
			getElement().getStyle().setBorderColor("#FF0000");
			getElement().getStyle().setBorderStyle(BorderStyle.SOLID);
			getElement().getStyle().setBorderWidth(1, Unit.PX);
		} else {
			getElement().getStyle().setBorderColor("#AAAAAA");
			getElement().getStyle().setBorderStyle(BorderStyle.SOLID);
			getElement().getStyle().setBorderWidth(0, Unit.PX);
		}
	}

	public boolean testClick() {
		if (Math.abs(x_old - getCenterX()) <= 10
				&& Math.abs(y_old - getCenterY()) <= 10) {
			wasClick();
			return true;
		}
		return false;
	}

	@Override
	public void onMouseUp(MouseUpEvent event) {
		testClick();
	}

	@Override
	public void onMouseDown(MouseDownEvent event) {
		// Window.alert("DnDNode.onMouseDown()");
		x_old = getCenterX();
		y_old = getCenterY();
		event.stopPropagation();
		event.preventDefault();
	}

	public String getDescriptionFieldContent() {
		return descriptionField.getText();
	}

	public void setToolUrl(String tool) {
		if (tool != null && tool.length() > 0 && tool != "null") {
			this.tool = tool;
		}
	}

	@Override
	public void onBlur(BlurEvent event) {
		if (!description.equals(descriptionField.getText())) {
			descriptionField.getElement().getStyle()
					.setBackgroundColor("#FFD1D5");
			sendNewDescription(descriptionField.getText());
		}
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady() {
		ready = true;
		started = false;
		setBGColor("#CCFFCC");
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted() {
		ready = false;
		started = true;
		setBGColor("#FFFF00");
	}

}
