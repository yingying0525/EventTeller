package db.hbn.model;

import java.util.Date;

public class Event {

	
	
	private int id;
	private String title;
	private Date pubTime;
	private String content;
	private String source;
	private String imgs;
	private int number;
	private int taskStatus;
	private int subTopic;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Date getPubTime() {
		return pubTime;
	}
	public void setPubTime(Date pubTime) {
		this.pubTime = pubTime;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getImgs() {
		return imgs;
	}
	public void setImgs(String imgs) {
		this.imgs = imgs;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public int getTaskStatus() {
		return taskStatus;
	}
	public void setTaskStatus(int taskStatus) {
		this.taskStatus = taskStatus;
	}
	public int getSubTopic() {
		return subTopic;
	}
	public void setSubTopic(int subTopic) {
		this.subTopic = subTopic;
	}
	
	@Override
	public String toString(){
		StringBuffer res = new StringBuffer();
		res.append(this.id + "@##@");
		res.append(this.title + "@##@");
		res.append(this.pubTime + "@##@");
		res.append(this.content + "@##@");
		res.append(this.imgs + "@##@");
		res.append(this.number + "@##@");
		res.append(this.subTopic);
		return res.toString();
	}
	
	
	
}
