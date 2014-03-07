package crawler.html;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dom4j.Node;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import db.hbn.model.Url;
import util.Config;
import util.Const;
import util.Log;
import util.Util;

public class Downloader {
	
	private String SaveFolderPath;
	
	public Downloader(){
		//get save path from the Config xml
		Log.getLogger().info("Start HtmlDownloader!");
		Config cfg = new Config(Const.SYS_CONFIG_PATH);
		Node elm = cfg.selectNode("/Configs/Config[@name='HtmlSavePath']/Path");
		SaveFolderPath = elm.getText();
	}
	
	//get 1000 urls
	private static List<Url> getInstances(){
		List<Url> results = new ArrayList<Url>();
		String hql = "from Url as obj where obj.taskStatus = 0  or obj.taskStatus = -1 order by obj.id desc";
		results = util.Util.getElementsFromDB(hql,2000);
		return results;
	}
	
	public void runTask(){
		List<Url> urls = getInstances();
		int number = 0;
		int failNumber = 0;
		for(Url url : urls){
			number++;
			if(number % 100 == 0){
				System.out.println("download 100 htmls...");
			}
			Document doc;
			try {
				doc = Jsoup.connect(url.getUrl()).userAgent(Const.CrawlerUserAgent).timeout(2000).get();
				String html = doc.html();
				String date = Util.getDateStr(url.getCrawlTime());
				File folder = new File(SaveFolderPath + date);
				if(!folder.exists()){
					folder.mkdirs();
				}
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(SaveFolderPath + date + File.separator + url.getId()),"UTF8"));
				bw.write(html);
				bw.close();	
				url.setTaskStatus(1);
			} catch (Exception e) {
				//can't download this url.. will update the taskStatus
				url.setTaskStatus(url.getTaskStatus() - 1);
				failNumber++;
			}
		}
		System.out.println("Failed + " + failNumber);
		Util.updateDB(urls);
	}
	
	public static void main(String[] args) {
		Downloader dw = new Downloader();
		while(true){
			dw.runTask();
			try {
				System.out.println("Downloader 1000 pages ok, will sleep for 10 minutes!" + new Date());
				Thread.sleep(1000 * 60 * 10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	

}
