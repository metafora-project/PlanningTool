package de.kuei.metafora.client.planningtool.gui.graph;

public class PlanningEdge {

	private DnDNode end;
	private DnDNode start;
	private String edgeType;
	private String id = null;

	public PlanningEdge(DnDNode start, DnDNode end, String edgetype) {
		this.start = start;
		this.end = end;
		this.edgeType = edgetype;
	}

	public DnDNode getStart() {
		return start;
	}

	public DnDNode getEnd() {
		return end;
	}

	public String getEdgeType() {
		return edgeType;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof PlanningEdge) {
			PlanningEdge other = (PlanningEdge) o;
			if (other.getStart().equals(getStart())
					&& other.getEnd().equals(getEnd())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "Edge: " + start.getId() + ", " + end.getId();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		if (this.id == null) {
			this.id = id;
		}
	}

	public void updateEdge(String edgeType) {
		this.edgeType = edgeType;
	}

	public String getEndId() {
		return end.getId();
	}
}
