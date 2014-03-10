package db.hbn.model;

import java.util.Date;
public class Url {
	
	private Integer id;
	private String  webSite;
	private String  url;
	private Date    crawlTime;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getWebSite() {
		return webSite;
	}
	public void setWebSite(String webSite) {
		this.webSite = webSite;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Date getCrawlTime() {
		return crawlTime;
	}
	public void setCrawlTime(Date crawlTime) {
		this.crawlTime = crawlTime;
	}
	
	@Override
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof Url) {
        	if(((Url) anObject).getId() == this.id){
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
