package de.kuei.metafora.client.planningtool.popmenu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.MenuItem;

import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.planningtool.gui.graph.DnDNode;

public class ShareModelMenuItem extends MenuItem {
	// object needed to implement i18n through Languages interface
	final static Languages language = GWT.create(Languages.class);

	public ShareModelMenuItem(String imageUrl, DnDNode widget) {
		super(language.ShareModelRightClickCardMenu(), true,
				new ShareModelCommand(imageUrl, widget));
	}

}
