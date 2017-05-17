package model.yakindu;

import java.util.ArrayList;

public class Entry extends Vertice {
	private ArrayList<String> outgoingTransitionIdList;

	public Entry(String id) {
		super(id);
		this.outgoingTransitionIdList = new ArrayList<>();
	}

	public ArrayList<String> getoutgoingTransitionIdList() {
		return outgoingTransitionIdList;
	}
}
