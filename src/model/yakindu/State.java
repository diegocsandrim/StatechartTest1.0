package model.yakindu;

import java.util.ArrayList;

public class State extends Vertice {
	private String name;
	private String childRegionId;
	private ArrayList<String> incomingTransitionIdList;
	private ArrayList<String> outgoingTransitionIdList;


	public State(String id, String name, String parentRegionId, String childRegionId) {
		super(id, parentRegionId);
		this.name = name;
		this.childRegionId = childRegionId;
		this.incomingTransitionIdList = new ArrayList<String>();
		this.outgoingTransitionIdList = new ArrayList<String>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getChildRegionId() {
		return childRegionId;
	}

	public void setChildRegionId(String childRegionId) {
		this.childRegionId = childRegionId;
	}

	public ArrayList<String> getincomingTransitionIdList() {
		return incomingTransitionIdList;
	}

	public ArrayList<String> getoutgoingTransitionIdList() {
		return outgoingTransitionIdList;
	}
}
