package de.kuei.metafora.client.planningtool.eventServiceListener;

import de.kuei.metafora.shared.event.graph.CenterNodeEvent;
import de.kuei.metafora.shared.event.graph.EdgeEvent;
import de.kuei.metafora.shared.event.graph.FrameworkEvent;
import de.kuei.metafora.shared.event.graph.NodeEvent;
import de.novanic.eventservice.client.event.listener.RemoteEventListener;

public interface GraphListener extends RemoteEventListener {

	public void nodeEvent(NodeEvent event);

	public void edgeEvent(EdgeEvent event);

	public void frameworkEvent(FrameworkEvent event);

	public void centerNodeEvent(CenterNodeEvent event);

}
