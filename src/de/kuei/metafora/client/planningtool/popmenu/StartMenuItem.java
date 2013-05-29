package de.kuei.metafora.client.planningtool.popmenu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.MenuItem;

import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.planningtool.gui.graph.DnDNode;

public class StartMenuItem extends MenuItem{
	// object needed to implement i18n through Languages interface
	final static Languages language = GWT.create(Languages.class);
	
	public StartMenuItem(String toolUrl, DnDNode widget){
		super(language.Start(), true, new StartCommand(toolUrl, widget));
	}
}
