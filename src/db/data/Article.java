package db.data;

import java.util.Date;

public class Article {
	
	private Integer id;
	private String  title;
	private String  url;
	private Date  	publishtime;
	private String  source;
	private String  content;
	private Date    crawltime;
	private Integer taskstatus;
	private String 	imgs;
	private Integer subtopic;
	private Integer number;
	private String  sameurls;
	private Integer topicid;
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
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
	public Date getPublishtime() {
		return publishtime;
	}
	public void setPublishtime(Date publishtime) {
		this.publishtime = publishtime;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Date getCrawltime() {
		return crawltime;
	}
	public void setCrawltime(Date crawltime) {
		this.crawltime = crawltime;
	}
	public Integer getTaskstatus() {
		return taskstatus;
	}
	public void setTaskstatus(Integer taskstatus) {
		this.taskstatus = taskstatus;
	}
	public String getImgs() {
		return imgs;
	}
	public void setImgs(String imgs) {
		this.imgs = imgs;
	}
	public Integer getSubtopic() {
		return subtopic;
	}
	public void setSubtopic(Integer subtopic) {
		this.subtopic = subtopic;
	}
	public Integer getNumber() {
		return number;
	}
	public void setNumber(Integer number) {
		this.number = number;
	}
	public String getSameurls() {
		return sameurls;
	}
	public void setSameurls(String sameurls) {
		this.sameurls = sameurls;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public Integer getTopicid() {
		return topicid;
	}
	public void setTopicid(Integer topicid) {
		this.topicid = topicid;
	}
	
	
	@Override
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof Article) {
        	if(((Article) anObject).getId() == this.id){
        		return true;
        	}
        }
        return false;
    }
	
	@Override
	public int hashCode(){
		return id;
	}
	
	
	
	
	
}
