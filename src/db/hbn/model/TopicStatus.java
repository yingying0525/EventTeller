package db.hbn.model;

public class TopicStatus {
	
	private int id;
	private int status;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof TopicStatus){
			return ((TopicStatus)obj).getId() == this.id;
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		return id;
	}
	
	

}
