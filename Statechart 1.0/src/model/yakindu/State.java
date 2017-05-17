package model.yakindu;

import java.util.ArrayList;

public class State extends Vertice {
	private String name;
	private ArrayList<String> incomingTransitionIdList;
	private ArrayList<String> outgoingTransitionIdList;


	public State(String id, String name) {
		super(id);
		this.name = name;
		this.incomingTransitionIdList = new ArrayList<String>();
		this.outgoingTransitionIdList = new ArrayList<String>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<String> getincomingTransitionIdList() {
		return incomingTransitionIdList;
	}

	public ArrayList<String> getoutgoingTransitionIdList() {
		return outgoingTransitionIdList;
	}
}
