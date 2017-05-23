package model.yakindu;

import java.util.ArrayList;

public class FinalState extends Vertice {
	private ArrayList<String> incomingTransitionIdList;

	public FinalState(String id, String parentRegionId) {
		super(id, parentRegionId);
		this.incomingTransitionIdList = new ArrayList<>();
	}

	public ArrayList<String> getincomingTransitionIdList() {
		return incomingTransitionIdList;
	}
}
