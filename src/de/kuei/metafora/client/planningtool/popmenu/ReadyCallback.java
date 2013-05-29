package de.kuei.metafora.client.planningtool.popmenu;

import com.google.gwt.user.client.rpc.AsyncCallback;

import de.kuei.metafora.client.planningtool.gui.graph.DnDNode;

public class ReadyCallback implements AsyncCallback<String> {

	private DnDNode widget;

	public ReadyCallback(DnDNode widget) {
		this.widget = widget;
	}

	@Override
	public void onFailure(Throwable caught) {
	}

	@Override
	public void onSuccess(String result) {
		if (result.equals("true"))
			widget.setReady();
	}

}
