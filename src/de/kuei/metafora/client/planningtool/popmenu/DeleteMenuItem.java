package de.kuei.metafora.client.planningtool.popmenu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.MenuItem;

import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.planningtool.gui.DnDResizePanel;
import de.kuei.metafora.client.planningtool.gui.graph.DnDNode;

public class DeleteMenuItem extends MenuItem{
	// object needed to implement i18n through Languages interface
	final static Languages language = GWT.create(Languages.class);
	
	public DeleteMenuItem(DnDResizePanel panel, DnDNode widget){
		super(language.DeleteRightClickCardMenu(), true, new DeleteCommand(panel, widget));
	}
	
}
