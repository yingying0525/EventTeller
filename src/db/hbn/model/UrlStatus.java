package db.hbn.model;

import java.util.Date;

public class UrlStatus {
	
	private int id;
	private int status;
	private int topic;
	private Date time;
	
	
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
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public int getTopic() {
		return topic;
	}
	public void setTopic(int topic) {
		this.topic = topic;
	}
	
	
	public boolean equals(Object obj){
		if(obj instanceof UrlStatus){
			return ((UrlStatus)obj).getId() == id;
		}else{
			return false;
		}
	}
	
	
	

}
