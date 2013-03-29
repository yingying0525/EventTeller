package db.data;

import java.util.Date;

public class Event {
	
	private Integer id;
	private String  title;
	private String  summarywords;
	private String  summary;
	private int     day;
	private Date    time;
	private Integer number;
	private String  articles;
	private Integer taskstatus;
	private Integer updatestatus;
	private Integer topicid;
	private String  img;
	private String  imgs;
	private Integer  subtopicid;
	private Integer  province;
	
	
	
	public Integer getSubtopicid() {
		return subtopicid;
	}
	public void setSubtopicid(Integer subtopicid) {
		this.subtopicid = subtopicid;
	}
	

	public Integer getTopicid() {
		return topicid;
	}
	public void setTopicid(Integer topicid) {
		this.topicid = topicid;
	}
	public Integer getTaskstatus() {
		return taskstatus;
	}
	public void setTaskstatus(Integer taskstatus) {
		this.taskstatus = taskstatus;
	}
	public Integer getNumber() {
		return number;
	}
	public void setNumber(Integer number) {
		this.number = number;
	}
	public String getArticles() {
		return articles;
	}
	public void setArticles(String articles) {
		this.articles = articles;
	}
	public Integer getId() {
		return id;
	}
	public String getSummarywords() {
		return summarywords;
	}
	public void setSummarywords(String summarywords) {
		this.summarywords = summarywords;
	}
	public int getDay() {
		return day;
	}
	public void setDay(int day) {
		this.day = day;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
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

	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getImg() {
		return img;
	}
	public void setImg(String img) {
		this.img = img;
	}
	public Integer getProvince() {
		return province;
	}
	public void setProvince(Integer province) {
		this.province = province;
	}
	public String getImgs() {
		return imgs;
	}
	public void setImgs(String imgs) {
		this.imgs = imgs;
	}
	public Integer getUpdatestatus() {
		return updatestatus;
	}
	public void setUpdatestatus(Integer updatestatus) {
		this.updatestatus = updatestatus;
	}
	
	

}
