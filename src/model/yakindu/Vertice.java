package model.yakindu;

public abstract class Vertice {
	private String id;
	private String parentRegionId;

	public Vertice(String id, String parentRegionId) {
		super();
		this.id = id;
		this.parentRegionId = parentRegionId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParentRegionId() {
		return parentRegionId;
	}

	public void setParentRegionId(String parentRegionId) {
		this.parentRegionId = parentRegionId;
	}
	
	public static String createId() {
		String id = new Integer(((int) Math.floor(Math.random() * 10e8))).toString();
		return id;
	}

}
