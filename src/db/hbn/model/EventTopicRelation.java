package db.hbn.model;

public class EventTopicRelation {
	
	private int eid;
	private int tid;
	
	public int getEid() {
		return eid;
	}
	public void setEid(int eid) {
		this.eid = eid;
	}
	public int getTid() {
		return tid;
	}
	public void setTid(int tid) {
		this.tid = tid;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof EventTopicRelation){
			return ((EventTopicRelation)obj).getEid() == this.eid;
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		return eid;
	}
	
	
	

}
