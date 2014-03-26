package cn.ruc.mblank.crawler.url;



import java.util.Map;


import cn.ruc.mblank.crawler.url.filter.UrlFilter;

public class WebSite {
	
	public String SiteName ;
	public Map<String,Integer> Sites;
	public Map<String,UrlFilter> filters;



	public Map<String, UrlFilter> getFilters() {
		return filters;
	}

	public void setFilters(Map<String, UrlFilter> filters) {
		this.filters = filters;
	}

	public void setSiteName(String siteName) {
		SiteName = siteName;
	}

	public void setSites(Map<String,Integer> sites) {
		Sites = sites;
	}
	public String getSiteName() {
		return SiteName;
	}

	public Map<String,Integer> getSites() {
		return Sites;
	}
	
	

}
