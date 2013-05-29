package de.kuei.metafora.client.planningtool;

import java.util.Vector;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.PlanningTool;
import de.kuei.metafora.client.planningtool.eventServiceListener.impl.GraphListenerImpl;
import de.kuei.metafora.client.planningtool.gui.ClientErrorHandler;
import de.kuei.metafora.client.planningtool.gui.DnDResizePanel;
import de.kuei.metafora.client.planningtool.gui.EdgeDisplay;
import de.kuei.metafora.client.planningtool.gui.NodePalette;
import de.kuei.metafora.client.planningtool.gui.PlanHeaderWidget;
import de.kuei.metafora.client.planningtool.gui.PlanWidget;
import de.kuei.metafora.client.planningtool.gui.graph.DnDNode;
import de.kuei.metafora.client.planningtool.gui.graph.PlanningEdge;
import de.kuei.metafora.client.planningtool.gui.owner.GroupList;
import de.kuei.metafora.client.planningtool.gui.owner.UserList;
import de.kuei.metafora.client.planningtool.serverlink.GraphInit;
import de.kuei.metafora.client.planningtool.serverlink.GraphInitAsync;
import de.kuei.metafora.client.planningtool.serverlink.GraphLink;
import de.kuei.metafora.client.planningtool.serverlink.GraphLinkAsync;
import de.kuei.metafora.client.planningtool.xml.GUIEventCreator;

public class PlanningToolWidget extends LayoutPanel implements ClickHandler {

	private static GraphLinkAsync graphLinkStatic = GWT.create(GraphLink.class);

	public static String map = "unknown";

	/**
	 * reference to the current planningtool instance
	 */
	private static PlanningToolWidget ptWidget = null;

	// object needed to implement i18n through Languages interface
	final static Languages language = GWT.create(Languages.class);

	public static PlanningToolWidget createInstance(String map) {

		PlanningToolWidget.map = map;

		if (ptWidget == null) {
			graphLinkStatic.createMap(map, PlanningTool.getToken(),
					PlanningTool.getUsers(), PlanningTool.getGroup(),
					PlanningTool.getChallengeId(),
					PlanningTool.getChallengeName(),
					PlanningTool.getConnectionId(),
					new AsyncCallback<Boolean>() {

						@Override
						public void onFailure(Throwable caught) {
							ClientErrorHandler.showErrorMessage(caught);
						}

						@Override
						public void onSuccess(Boolean result) {
						}
					});

			graphLinkStatic.selectMap(map, PlanningTool.getToken(),
					PlanningTool.getUsers(), PlanningTool.getGroup(),
					PlanningTool.getChallengeId(),
					PlanningTool.getChallengeName(),
					PlanningTool.getConnectionId(), new AsyncCallback<Void>() {

						@Override
						public void onFailure(Throwable caught) {
							ClientErrorHandler.showErrorMessage(caught);
						}

						@Override
						public void onSuccess(Void result) {

						}
					});

			ptWidget = new PlanningToolWidget(map);
		}
		return ptWidget;
	}

	public static PlanningToolWidget getInstance() {
		return ptWidget;
	}

	private final GraphInitAsync graphInit = GWT.create(GraphInit.class);
	private final GraphLinkAsync graphLink = GWT.create(GraphLink.class);

	// DnD-Controller for directed graph and palette
	private final PickupDragController dragController;

	private StackLayoutPanel menuLayoutPanel;
	private final DnDResizePanel dropArea;
	private SplitLayoutPanel mainLayout;
	private Button reflectionToolButton;
	private Frame reflectionTool;
	private ScrollPanel dropScroll;
	private boolean isReflectionVisible = false;
	private int reflectionToolHeight = 200;

	private NodePalette nodes;

	private EdgeHandler edgeHandler;

	private PlanWidget file;
	private PlanHeaderWidget planHeader;

	private String graphName;
	private DnDNode centeredNode = null;

	protected PlanningToolWidget(String graphname) {
		this.graphName = graphname;

		menuLayoutPanel = new StackLayoutPanel(Unit.PX);
		dropArea = new DnDResizePanel();

		edgeHandler = new EdgeHandler(dropArea);

		dragController = new PickupDragController(dropArea, true);
		dragController.addDragHandler(dropArea);
		dragController.addDragHandler(edgeHandler);

		file = buildFileWidget();
		ScrollPanel scroll = new ScrollPanel(file);

		planHeader = new PlanHeaderWidget(graphname, "");
		menuLayoutPanel.add(scroll, planHeader, 50);
		planHeader.getElement().setClassName("planningToolPlanHeader");
		updateFileHeader();

		buildEdgeWidget();
		buildNodeWidget();

		String reflectionUrl = PlanningTool.getApacheServer()
				+ "/reflectiontool/?groupid=" + PlanningTool.getGroup()
				+ "&challengeid=" + PlanningTool.getChallengeId() + "&server="
				+ PlanningTool.getXMPPServer() + "&channel="
				+ PlanningTool.getReflectionChannel() + "&tomcat="
				+ PlanningTool.getTomcatServer() + "&token="
				+ PlanningTool.getToken();
		if (PlanningTool.getUsers() != null
				&& PlanningTool.getUsers().size() > 0) {
			reflectionUrl += "&user=" + PlanningTool.getUsers().firstElement();
		}

		reflectionTool = new Frame(reflectionUrl);
		reflectionTool.setSize("100%", "100%");

		mainLayout = new SplitLayoutPanel(4) {
			@Override
			public void onResize() {
				super.onResize();
				PlanningToolWidget.getInstance().updateButtonPosition();
			}
		};
		mainLayout.addEast(menuLayoutPanel, 180);
		mainLayout.addSouth(reflectionTool, 1);

		dropScroll = new ScrollPanel(dropArea);
		mainLayout.add(dropScroll);

		add(mainLayout);
		setWidgetLeftWidth(mainLayout, 0, Unit.PX, 100, Unit.PCT);
		setWidgetTopHeight(mainLayout, 0, Unit.PX, 100, Unit.PCT);

		reflectionToolButton = new Button(language.ReflectionTool());
		reflectionToolButton.getElement().setClassName("reflectionToolButton");
		add(reflectionToolButton);
		setWidgetBottomHeight(reflectionToolButton, 3, Unit.PX, 25, Unit.PX);
		setWidgetLeftWidth(reflectionToolButton, 10, Unit.PX, 150, Unit.PX);
		reflectionToolButton.addClickHandler(this);

		initGraph();

	}

	/**
	 * This method updates the file StackPanel header in case of user of map
	 * changes.
	 */
	public void updateFileHeader() {
		String[] header = file.getHeader();
		planHeader.setPlanName(header[0]);
		planHeader.setUserText(header[1]);
	}

	public void initGraph() {

		graphInit.getSize(graphName, PlanningTool.getToken(),
				new AsyncCallback<int[]>() {

					@Override
					public void onFailure(Throwable caught) {
						dropArea.setPixelSize(1000, 1000);
						ClientErrorHandler.showErrorMessage(caught);
					}

					@Override
					public void onSuccess(int[] result) {
						dropArea.setPixelSize(result[0], result[1]);
					}
				});

		final PlanningToolWidget thisWidget = this;

		graphInit.getNodes(this.graphName, PlanningTool.getToken(),
				new AsyncCallback<String[]>() {

					@Override
					public void onFailure(Throwable caught) {
						ClientErrorHandler.showErrorMessage(caught);
					}

					@Override
					public void onSuccess(String[] result) {
						for (int i = 0; i < result.length; i++) {
							GraphListenerImpl.getInstance()
									.parseNode(result[i]);
						}
						thisWidget.initGraphEdges();
					}
				});

	}

	private void initGraphEdges() {
		graphInit.getEdges(this.graphName, PlanningTool.getToken(),
				new AsyncCallback<String[]>() {

					@Override
					public void onFailure(Throwable caught) {
						ClientErrorHandler.showErrorMessage(caught);
					}

					@Override
					public void onSuccess(String[] result) {
						for (int i = 0; i < result.length; i++) {
							GraphListenerImpl.getInstance()
									.parseEdge(result[i]);
						}
						dropArea.paintEdges();

						centerNode();
					}
				});
	}

	public void centerNode() {
		if (centeredNode != null) {
			centeredNode.highLight(false);
			centeredNode = null;
		}

		String nodeId = PlanningTool.getCenterNode();

		if (nodeId != null) {
			int[] position = GUIEventCreator.getInstance().getNodePosition(
					nodeId);

			if (position != null) {
				int xscroll = position[0] - (dropScroll.getOffsetWidth() / 2)
						+ dropScroll.getHorizontalScrollPosition();
				int yscroll = position[1] - (dropScroll.getOffsetHeight() / 2)
						+ dropScroll.getVerticalScrollPosition();

				dropScroll.setVerticalScrollPosition(yscroll);
				dropScroll.setHorizontalScrollPosition(xscroll);

				DnDNode node = GUIEventCreator.getInstance().getNode(nodeId);
				if (node != null) {
					node.highLight(true);
					centeredNode = node;
				}
			}
		}
	}

	public void buildNodeWidget() {
		nodes = new NodePalette(dragController, menuLayoutPanel);

		graphLink.getNodeTypes(LocaleInfo.getCurrentLocale().getLocaleName(),
				PlanningTool.getToken(), PlanningTool.getUsers(),
				PlanningTool.getGroup(), PlanningTool.getChallengeId(),
				PlanningTool.getChallengeName(), PlanningTool.isCavillag(),
				new AsyncCallback<String[]>() {

					@Override
					public void onFailure(Throwable caught) {
						ClientErrorHandler.showErrorMessage(caught);
					}

					@Override
					public void onSuccess(String[] result) {
						for (int i = 0; i < result.length; i++) {
							GraphListenerImpl.getInstance().parsePaletteNode(
									result[i], graphName);
						}
					}
				});
	}

	public void buildEdgeWidget() {
		VerticalPanel vpanel = new VerticalPanel();

		HTML text = new HTML(language.SelectConnectorType());
		vpanel.add(text);

		EdgeDisplay isNextDisplay = new EdgeDisplay(80, 25,
				EdgeHandler.SOLID_RED, dropArea);
		EdgeDisplay isNeededForDisplay = new EdgeDisplay(80, 25,
				EdgeHandler.SOLID_BLUE, dropArea);
		EdgeDisplay isLinkedToDisplay = new EdgeDisplay(80, 25,
				EdgeHandler.NO_TIP_BLACK, dropArea);

		LayoutPanel layout = new LayoutPanel();
		text = new HTML("<b>" + language.IsNext() + "</b>");
		layout.add(text);
		layout.setWidgetLeftRight(text, 2, Unit.PX, 2, Unit.PX);
		layout.setWidgetTopHeight(text, 5, Unit.PX, 20, Unit.PX);

		layout.add(isNextDisplay);
		layout.setWidgetLeftWidth(isNextDisplay, 5, Unit.PX, 80, Unit.PX);
		layout.setWidgetTopHeight(isNextDisplay, 25, Unit.PX, 25, Unit.PX);

		EdgeHandler.SOLID_RED_BUTTON.getElement().getStyle()
				.setTextAlign(TextAlign.CENTER);
		layout.add(EdgeHandler.SOLID_RED_BUTTON);
		layout.setWidgetLeftRight(EdgeHandler.SOLID_RED_BUTTON, 85, Unit.PX, 2,
				Unit.PX);
		layout.setWidgetTopHeight(EdgeHandler.SOLID_RED_BUTTON, 25, Unit.PX,
				25, Unit.PX);

		layout.setHeight("55px");
		vpanel.add(layout);

		layout = new LayoutPanel();
		text = new HTML("<b>" + language.IsNeededFor() + "</b>");
		layout.add(text);
		layout.setWidgetLeftRight(text, 2, Unit.PX, 2, Unit.PX);
		layout.setWidgetTopHeight(text, 5, Unit.PX, 20, Unit.PX);

		layout.add(isNeededForDisplay);
		layout.setWidgetLeftWidth(isNeededForDisplay, 5, Unit.PX, 80, Unit.PX);
		layout.setWidgetTopHeight(isNeededForDisplay, 25, Unit.PX, 25, Unit.PX);

		EdgeHandler.SOLID_BLUE_BUTTON.getElement().getStyle()
				.setTextAlign(TextAlign.CENTER);
		layout.add(EdgeHandler.SOLID_BLUE_BUTTON);
		layout.setWidgetLeftRight(EdgeHandler.SOLID_BLUE_BUTTON, 85, Unit.PX,
				2, Unit.PX);
		layout.setWidgetTopHeight(EdgeHandler.SOLID_BLUE_BUTTON, 25, Unit.PX,
				25, Unit.PX);

		layout.setHeight("55px");
		vpanel.add(layout);

		layout = new LayoutPanel();
		text = new HTML("<b>" + language.IsLinkedTo() + "</b>");
		layout.add(text);
		layout.setWidgetLeftRight(text, 2, Unit.PX, 2, Unit.PX);
		layout.setWidgetTopHeight(text, 5, Unit.PX, 20, Unit.PX);

		layout.add(isLinkedToDisplay);
		layout.setWidgetLeftWidth(isLinkedToDisplay, 5, Unit.PX, 80, Unit.PX);
		layout.setWidgetTopHeight(isLinkedToDisplay, 25, Unit.PX, 25, Unit.PX);

		EdgeHandler.NO_TIP_BLACK_BUTTON.getElement().getStyle()
				.setTextAlign(TextAlign.CENTER);
		layout.add(EdgeHandler.NO_TIP_BLACK_BUTTON);
		layout.setWidgetLeftRight(EdgeHandler.NO_TIP_BLACK_BUTTON, 85, Unit.PX,
				2, Unit.PX);
		layout.setWidgetTopHeight(EdgeHandler.NO_TIP_BLACK_BUTTON, 25, Unit.PX,
				25, Unit.PX);

		layout.setHeight("55px");
		vpanel.add(layout);

		vpanel.setWidth("100%");

		ScrollPanel scroll = new ScrollPanel(vpanel);
		menuLayoutPanel.add(scroll, language.Connectors(), 25);
		menuLayoutPanel.getHeaderWidget(scroll).getElement()
				.setClassName("planningToolStackHeader");
	}

	/**
	 * create GUI for file Management (save (map name), new map)
	 */
	public PlanWidget buildFileWidget() {
		return new PlanWidget(graphName);
	}

	public String getGraphName() {
		return graphName;
	}

	/**
	 * This method updates the name of the current map in case of a new map or a
	 * map save.
	 * 
	 * @param newName
	 *            name of the new map
	 */
	public void setGraphName(String newName) {
		PlanningToolWidget.map = newName;
		graphName = newName;
		file.setGraphName(newName);
		PlanningTool.updateMap(newName);
		GUIEventCreator.getInstance().updateIdsByMapRename(graphName, newName);
		updateFileHeader();

		if (centeredNode != null) {
			centeredNode.highLight(false);
			centeredNode = null;
		}
	}

	public Widget getWidget() {
		return this;
	}

	public void newMap(String name) {
		GUIEventCreator.getInstance().deleteAll(graphName);
		setGraphName(name);
	}

	public void deleteMap(String name) {
		GUIEventCreator.getInstance().deleteAll(name);

		updateFileHeader();

		file.deleteMap(name);
	}

	public void openMap(String name) {
		final String mapname = name;
		graphLink.selectMap(name, PlanningTool.getToken(),
				PlanningTool.getUsers(), PlanningTool.getGroup(),
				PlanningTool.getChallengeId(), PlanningTool.getChallengeName(),
				PlanningTool.getConnectionId(), new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						ClientErrorHandler.showErrorMessage(caught);
					}

					@Override
					public void onSuccess(Void result) {
						newMap(mapname);
						initGraph();

						UserList.getUserList().clearUsers();
						GroupList.getGroupList().clearGroups();

						graphInit.getUsers(mapname, PlanningTool.getToken(),
								new AsyncCallback<Vector<String>>() {

									@Override
									public void onFailure(Throwable caught) {
										ClientErrorHandler
												.showErrorMessage(caught);
									}

									@Override
									public void onSuccess(Vector<String> result) {
										for (String user : result) {
											UserList.getUserList()
													.addUser(user);
										}
									}
								});

						graphInit.getGroups(mapname, PlanningTool.getToken(),
								new AsyncCallback<Vector<String>>() {

									@Override
									public void onFailure(Throwable caught) {
										ClientErrorHandler
												.showErrorMessage(caught);
									}

									@Override
									public void onSuccess(Vector<String> result) {
										for (String group : result) {
											GroupList.getGroupList().addGroup(
													group);
										}
									}
								});
					}

				});
	}

	public void paintEdges() {
		dropArea.paintEdges();
	}

	public void addNode(DnDNode node) {

		if (node instanceof Widget) {
			dragController.makeDraggable((Widget) node);
			dropArea.add((Widget) node, node.getCenterX(), node.getCenterY());
		}
	}

	public void removeNode(DnDNode node) {
		node.removeEdges();
		dropArea.removeNode(node);
	}

	public void addEdge(PlanningEdge edge) {
		edgeHandler.addEdge(edge);
	}

	public void removeEdge(PlanningEdge edge) {
		edgeHandler.removeEdge(edge);
	}

	public void addPaletteNode(DnDNode node) {
		if (node instanceof Widget) {
			nodes.add((Widget) node);
			dragController.makeDraggable((Widget) node);
		}
	}

	public void setSize(int width, int height) {
		dropArea.setPixelSize(width, height);
	}

	public DnDResizePanel getDropArea() {
		return dropArea;
	}

	public void addNewMap(String name) {
		file.addNewMap(name);
	}

	public EdgeHandler getEdgeHandler() {
		return edgeHandler;
	}

	@Override
	public void onClick(ClickEvent event) {
		if (!isReflectionVisible) {
			mainLayout.setWidgetSize(reflectionTool, reflectionToolHeight);
			isReflectionVisible = true;
			setWidgetBottomHeight(reflectionToolButton, reflectionToolHeight,
					Unit.PX, 25, Unit.PX);
			setWidgetLeftWidth(reflectionToolButton, 10, Unit.PX, 150, Unit.PX);
		} else {
			mainLayout.setWidgetSize(reflectionTool, 1);
			isReflectionVisible = false;
			setWidgetBottomHeight(reflectionToolButton, 3, Unit.PX, 25, Unit.PX);
			setWidgetLeftWidth(reflectionToolButton, 10, Unit.PX, 150, Unit.PX);
		}
	}

	public void updateButtonPosition() {
		if (isReflectionVisible)
			reflectionToolHeight = reflectionTool.getOffsetHeight();
		setWidgetBottomHeight(reflectionToolButton,
				reflectionTool.getOffsetHeight() - 3, Unit.PX, 25, Unit.PX);
		setWidgetLeftWidth(reflectionToolButton, 10, Unit.PX, 150, Unit.PX);
	}

	public PlanWidget getPlanWidget() {
		return file;
	}
}
