package news.model;

public class NewsPage {
	
	private String Tag;
	private String Id;
	private String TagClass;
	private String SubTag;
	private int	SubTagIndex;
	private String LinkAtt;
	private String TextAtt;
	private String Format;
	private String BaseUrl;
	
	
	
	public String getTag() {
		return Tag;
	}
	public void setTag(String tag) {
		Tag = tag;
	}
	public String getId() {
		return Id;
	}
	public void setId(String id) {
		Id = id;
	}

	public String getSubTag() {
		return SubTag;
	}
	public void setSubTag(String subTag) {
		SubTag = subTag;
	}
	public int getSubTagIndex() {
		return SubTagIndex;
	}
	public void setSubTagIndex(int subTagIndex) {
		SubTagIndex = subTagIndex;
	}
	public String getLinkAtt() {
		return LinkAtt;
	}
	public void setLinkAtt(String linkAtt) {
		LinkAtt = linkAtt;
	}
	public String getTextAtt() {
		return TextAtt;
	}
	public void setTextAtt(String textAtt) {
		TextAtt = textAtt;
	}
	public String getFormat() {
		return Format;
	}
	public void setFormat(String format) {
		Format = format;
	}
	public String getBaseUrl() {
		return BaseUrl;
	}
	public void setBaseUrl(String baseUrl) {
		BaseUrl = baseUrl;
	}
	public String getTagClass() {
		return TagClass;
	}
	public void setTagClass(String tagClass) {
		TagClass = tagClass;
	}
	
	
	
	

}
