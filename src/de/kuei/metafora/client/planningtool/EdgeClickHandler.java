package de.kuei.metafora.client.planningtool;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class EdgeClickHandler implements ClickHandler{
	
	private EdgeHandler edgeHandler = null;
	
	public EdgeClickHandler(EdgeHandler edgeHandler){
		this.edgeHandler = edgeHandler;
	}
	
	@Override
	public void onClick(ClickEvent event) {
		if (event.getSource().equals(EdgeHandler.SOLID_BLUE_BUTTON)) {
			edgeHandler.edgeType = EdgeHandler.SOLID_BLUE;
			EdgeHandler.SOLID_BLUE_BUTTON.setDown(true);
			EdgeHandler.SOLID_RED_BUTTON.setDown(false);
			EdgeHandler.NO_TIP_BLACK_BUTTON.setDown(false);
		} else if (event.getSource().equals(EdgeHandler.SOLID_RED_BUTTON)) {
			edgeHandler.edgeType = EdgeHandler.SOLID_RED;
			EdgeHandler.SOLID_BLUE_BUTTON.setDown(false);
			EdgeHandler.SOLID_RED_BUTTON.setDown(true);
			EdgeHandler.NO_TIP_BLACK_BUTTON.setDown(false);
		}
		else if (event.getSource().equals(EdgeHandler.NO_TIP_BLACK_BUTTON)) {
			edgeHandler.edgeType = EdgeHandler.NO_TIP_BLACK;
			EdgeHandler.SOLID_BLUE_BUTTON.setDown(false);
			EdgeHandler.SOLID_RED_BUTTON.setDown(false);
			EdgeHandler.NO_TIP_BLACK_BUTTON.setDown(true);
		}
	}

}
