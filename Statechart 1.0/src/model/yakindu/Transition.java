package model.yakindu;

public class Transition {
	private String id;
	private String verticeId;
	private String specification;
	private String target;
	
	public Transition(String id, String verticeId, String specification, String target) {
		super();
		this.id = id;
		this.verticeId = verticeId;
		this.specification = specification;
		this.target = target;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVerticeId() {
		return verticeId;
	}

	public void setVerticeId(String verticeId) {
		this.verticeId = verticeId;
	}

	public String getSpecification() {
		return specification;
	}

	public void setSpecification(String specification) {
		this.specification = specification;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
	
	
}
