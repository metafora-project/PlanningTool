package de.kuei.metafora.client.planningtool.popmenu;

import com.google.gwt.user.client.Command;

import de.kuei.metafora.client.planningtool.gui.graph.DnDNode;

public class ReadyCommand implements Command {

	private DnDNode widget;

	public ReadyCommand(DnDNode widget) {
		this.widget = widget;
	}

	@Override
	public void execute() {
		widget.hidePopup();
		widget.setReady();
	}

}
