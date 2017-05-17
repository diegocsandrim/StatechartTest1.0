package model.yakindu;

import java.util.ArrayList;

public abstract class Vertice {
	private String id;
	private ArrayList<String> outgoingTransitionIdList;

	public Vertice(String id) {
		super();
		this.id = id;
		this.outgoingTransitionIdList = new ArrayList<String>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ArrayList<String> getOutgoingTransitionList() {
		return outgoingTransitionIdList;
	}

	public static String createId() {
		String id = new Integer(((int) Math.floor(Math.random() * 10e8))).toString();
		return id;
	}

}
