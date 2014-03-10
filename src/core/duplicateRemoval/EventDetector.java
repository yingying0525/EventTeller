package core.duplicateRemoval;

//import java.util.ArrayList;
import java.io.File;
import java.util.List;






import util.Const;
import util.Log;

import com.alibaba.fastjson.JSON;

import config.JsonConfigModel;
import config.LocalJsonConfigReader;
//import db.hbn.model.Url;
import db.hbn.model.UrlStatus;

public class EventDetector {
	
	
	private int BatchSize = 2000; 
	private List<UrlStatus> UStatus;
	
	private String HtmlPath;
	
	public EventDetector(){
		//get save path from the Config xml
		Log.getLogger().info("Start HtmlDownloader!");
		//read Bloom filter file path from json config file
		String fileContent = LocalJsonConfigReader.readJsonFile(Const.SYS_JSON_CONFIG_PATH);
		JsonConfigModel jcm = JSON.parseObject(fileContent,JsonConfigModel.class);
		HtmlPath = jcm.HtmlSavePath;
	}
	
	private void getInstances(){
		String hql = "from UrlStatus as obj where obj.status = 1";
		UStatus = util.Util.getElementsFromDB(hql,BatchSize);
	}
	
	public void runTask(){
		//get batchsize instances first.
		getInstances();
		//for each url , get it's filepath and parse the html to article;
		for(UrlStatus us : UStatus){
			String folder = HtmlPath + util.Util.getDateStr(us.getTime()) + "/" + us.getId();
			File file = new File(folder);
			if(!file.exists()){
				us.setStatus(Const.TaskId.CantFindHtmlInDisk.ordinal());
				continue;
			}
//			Document doc = Jsoup.parse(file, Const.HtmlSaveEncode);
//			Extractor();
		}
	}

}
