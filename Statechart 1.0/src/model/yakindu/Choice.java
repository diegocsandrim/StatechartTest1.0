package model.yakindu;

import java.util.ArrayList;

public class Choice extends Vertice {
	private ArrayList<String> incomingTransitionIdList;
	private ArrayList<String> outgoingTransitionIdList;


	public Choice(String id, String parentRegionId) {
		super(id, parentRegionId);
		this.incomingTransitionIdList = new ArrayList<String>();
		this.outgoingTransitionIdList = new ArrayList<String>();
	}

	public ArrayList<String> getincomingTransitionIdList() {
		return incomingTransitionIdList;
	}

	public ArrayList<String> getoutgoingTransitionIdList() {
		return outgoingTransitionIdList;
	}
}
