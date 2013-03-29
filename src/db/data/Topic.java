package db.data;

import java.util.Date;

public class Topic {
	
	private Integer id;
	private String  title;
	private String  keywords;
	private Date    time;
	private Date    startTime;
	private Date    endTime;
	private Integer number;
	private String  events;
	private Integer updatestate;
	private String  imgs;
	private Integer  subtopicid;
	
	
	
	public Integer getSubtopicid() {
		return subtopicid;
	}
	public void setSubtopicid(Integer subtopicid) {
		this.subtopicid = subtopicid;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getKeywords() {
		return keywords;
	}
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public Integer getNumber() {
		return number;
	}
	public void setNumber(Integer number) {
		this.number = number;
	}
	public String getEvents() {
		return events;
	}
	public void setEvents(String events) {
		this.events = events;
	}
	public Integer getUpdatestate() {
		return updatestate;
	}
	public void setUpdatestate(Integer updatestate) {
		this.updatestate = updatestate;
	}
	public String getImgs() {
		return imgs;
	}
	public void setImgs(String imgs) {
		this.imgs = imgs;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	
	
}
