package model.yakindu;

public class Transition {
	private String id;
	private String sourceVerticeId;
	private String targetVerticeId;
	private String specification;

	public Transition(String id) {
		this(id, null, null, null);
	}
	
	public Transition(String id, String sourceVerticeId, String targetVerticeId, String specification) {
		super();
		this.id = id;
		this.sourceVerticeId = sourceVerticeId;
		this.specification = specification;
		this.targetVerticeId = targetVerticeId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSourceVerticeId() {
		return sourceVerticeId;
	}

	public void setSourceVerticeId(String verticeId) {
		this.sourceVerticeId = verticeId;
	}

	public String getTargetVerticeId() {
		return targetVerticeId;
	}

	public void setTargetVerticeId(String target) {
		this.targetVerticeId = target;
	}

	public String getSpecification() {
		return specification;
	}

	public void setSpecification(String specification) {
		this.specification = specification;
	}
	
	@Override
	public int hashCode() {
		return this.id.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		
		if(obj.getClass().getName() != Transition.class.getName()){
			return false;
		}
		
		return ((Transition)obj).getId().equals(this.id);
	}
}
