package model.yakindu;

public abstract class Vertice {
	private String id;

	public Vertice(String id) {
		super();
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public static String createId() {
		String id = new Integer(((int) Math.floor(Math.random() * 10e8))).toString();
		return id;
	}

}
