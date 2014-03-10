package crawler.url.filter;

public class UrlFilter {
	
	private String[] str_filter;
	private int url_avg_len ;
	private int title_avg_len;

	public int getUrl_avg_len() {
		return url_avg_len;
	}

	public void setUrl_avg_len(int url_avg_len) {
		this.url_avg_len = url_avg_len;
	}

	public int getTitle_avg_len() {
		return title_avg_len;
	}

	public void setTitle_avg_len(int title_avg_len) {
		this.title_avg_len = title_avg_len;
	}

	public String[] getStr_filter() {
		return str_filter;
	}

	public void setStr_filter(String[] str_filter) {
		this.str_filter = str_filter;
	}

}
