package model.yakindu;

import java.util.ArrayList;

public class State extends Vertice {
	private String name;
	private ArrayList<String> incomingTransitionIdList;

	public State(String id, String name) {
		super(id);
		this.name = name;
		this.incomingTransitionIdList = new ArrayList<String>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<String> getIncomingTransitionList() {
		return incomingTransitionIdList;
	}
}
