package de.kuei.metafora.client.planningtool.popmenu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.MenuItem;

import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.planningtool.gui.graph.DnDNode;

public class DiscussMenuItem extends MenuItem{
	// object needed to implement i18n through Languages interface
	final static Languages language = GWT.create(Languages.class);
	
	public DiscussMenuItem(String imageUrl, DnDNode widget){
		super(language.DiscussThisRightClickCardMenu(), true, new DiscussCommand(imageUrl, widget));
	}
	
}
