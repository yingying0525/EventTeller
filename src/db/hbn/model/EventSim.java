package db.hbn.model;

public class EventSim {
	
	private int fid;
	private int sid;	
	private double score;

	public int getFid() {
		return fid;
	}

	public void setFid(int fid) {
		this.fid = fid;
	}

	public int getSid() {
		return sid;
	}

	public void setSid(int sid) {
		this.sid = sid;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
	
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof EventSim){
			return ((EventSim)obj).getFid() == this.fid;
		}else{
			return false;
		}
	}
	
	@Override
	public int hashCode(){
		return this.fid;
	}

}
