package cn.ruc.mblank.config;

import cn.ruc.mblank.util.Const;

import com.alibaba.fastjson.JSON;

public class JsonConfigModel {
	
	public String HtmlSavePath;
	public String SimHashPath;
	public String SolrIndexURI;
    public String SolrWordIndexURI;
	public String UrlsBloomFilterFilePath;
	public String LocalDFPath;
	public String LocalTDFPath;
	public String LocalDDNPath;
	public String ArticleFilePath;
	
	
	public String toString(){
		return HtmlSavePath + "\t" + 
				SimHashPath + "\t" + 
				SolrIndexURI + "\t" + 
				UrlsBloomFilterFilePath + "\t" +
				LocalDFPath + "\t" + 
				LocalTDFPath;
	}
	
	
	public static JsonConfigModel getConfig(){

		String fileContent = LocalJsonConfigReader.readJsonFile(Const.SYS_JSON_CONFIG_PATH);
		JsonConfigModel jcm = JSON.parseObject(fileContent,JsonConfigModel.class);
		return jcm;
	}

}
