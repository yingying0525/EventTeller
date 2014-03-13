package config;

public class JsonConfigModel {
	
	public String HtmlSavePath;
	public String SimHashPath;
	public String SolrIndexURI;
	public String UrlsBloomFilterFilePath;
	public String LocalDFPath;
	public String LocalTDFPath;
	public String ArticleFilePath;
	
	
	
	
	
	public String toString(){
		return HtmlSavePath + "\t" + 
				SimHashPath + "\t" + 
				SolrIndexURI + "\t" + 
				UrlsBloomFilterFilePath + "\t" +
				LocalDFPath + "\t" + 
				LocalTDFPath;
	}

}
