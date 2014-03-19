package crawler.html;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


import config.JsonConfigModel;
import db.hbn.model.Url;
import db.hbn.model.UrlStatus;
import util.Const;
import util.Log;

public class Downloader {
	
	private String SaveFolderPath;
	
	public Downloader(){
		//get save path from the Config xml
		Log.getLogger().info("Start HtmlDownloader!");
		//read Bloom filter file path from json config file
		JsonConfigModel jcm = JsonConfigModel.getConfig();
		SaveFolderPath = jcm.HtmlSavePath;
	}
	
	//get 1000 urls
	private static List<UrlStatus> getInstances(){
		List<UrlStatus> results = new ArrayList<UrlStatus>();
		String hql = "from UrlStatus as obj where obj.status = " + Const.TaskId.CrawlUrlToDB.ordinal() + "  or obj.status = -1";
		results = util.db.Hbn.getElementsFromDB(hql,2000);
		return results;
	}
	
	public void runTask(){
		List<UrlStatus> uss = getInstances();
		int number = 0;
		int failNumber = 0;
		for(UrlStatus us : uss){
			number++;
			if(number % 100 == 0){
				System.out.println("download 100 htmls...");
			}
			///get url from Url table accoding to the us.id;
			String hql = "from Url as obj where obj.id = " + us.getId();
			Url url = util.db.Hbn.getElementFromDB(hql);
			if(url == null){
				us.setStatus(us.getStatus() - 10);
				failNumber++;
				continue;				
			}
			Document doc;
			try {
				doc = Jsoup.connect(url.getUrl()).userAgent(Const.CrawlerUserAgent).timeout(2000).get();
				String html = doc.html();
				String date = util.TimeUtil.getDateStr(url.getCrawlTime());
				File folder = new File(SaveFolderPath + date);
				if(!folder.exists()){
					folder.mkdirs();
				}
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(SaveFolderPath + date + File.separator + url.getId()),Const.HtmlSaveEncode));
				bw.write(html);
				bw.close();	
				us.setStatus(Const.TaskId.DownloadUrlToHtml.ordinal());
			} catch (Exception e) {
				//can't download this url.. will update the taskStatus
				us.setStatus(us.getStatus() - 1);
				failNumber++;
			}
		}
		System.out.println("Failed + " + failNumber + "\t" + "Successed : " + (number - failNumber));
		util.db.Hbn.updateDB(uss);
	}
	
	public static void main(String[] args) {
		Downloader dw = new Downloader();
		while(true){
			dw.runTask();
			try {
				System.out.println("now end of downloader,sleep for:"+Const.DownloadArticleSleepTime/1000/60+" minutes. "+new Date().toString());
				Thread.sleep(Const.DownloadArticleSleepTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	

}