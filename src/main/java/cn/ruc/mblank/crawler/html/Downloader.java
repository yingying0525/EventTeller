package cn.ruc.mblank.crawler.html;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.ruc.mblank.db.hbn.HSession;
import cn.ruc.mblank.db.hbn.model.Url;
import cn.ruc.mblank.db.hbn.model.UrlStatus;
import cn.ruc.mblank.util.db.Hbn;
import org.hibernate.Session;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


import cn.ruc.mblank.config.JsonConfigModel;
import cn.ruc.mblank.util.Const;
import cn.ruc.mblank.util.Log;

public class Downloader {
	
	private String SaveFolderPath;
    private int BatchSize = 1000;

    private List<UrlStatus> Instances;
	
	public Downloader(){
		//get save path from the Config xml
		Log.getLogger().info("Start HtmlDownloader!");
		//read Bloom filter file path from json config file
		JsonConfigModel jcm = JsonConfigModel.getConfig();
		SaveFolderPath = jcm.HtmlSavePath;
	}

    /**
     * get instances from db;
     */
	private void getInstances(Session session){
		String hql = "from UrlStatus as obj where obj.status = 0 or obj.status = -1";
        Instances = Hbn.getElementsFromDB(hql,0,BatchSize,session);
//        Instances = Hbn.getElementsFromDBC(Session,UrlStatus.class,(short)-1,(short)0,BatchSize);
        int maxId = Hbn.getMaxFromDB(session,UrlStatus.class,"id");
        System.out.println(Instances.size() + "\t" + maxId);
	}

    private void writeHtml2Disk(Url url,String html){
        String date = cn.ruc.mblank.util.TimeUtil.getDateStr(url.getCrawltime());
        File folder = new File(SaveFolderPath + date);
        if(!folder.exists()){
            folder.mkdirs();
        }
        try {
            BufferedWriter bw = null;
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(SaveFolderPath + date + File.separator + url.getId()), Const.HtmlSaveEncode));
            bw.write(html);
            bw.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public void runTask(){
        Session session = HSession.getSession();
        getInstances(session);
        int number = 0;
		int failNumber = 0;
        if(Instances.size() == 0){
            return;
        }
		for(UrlStatus us : Instances){
			number++;
			if(number % 200 == 0){
				System.out.println("download 200 htmls..." + "\t" + us.getId());
			}
			Url url = Hbn.getElementFromDB(session,Url.class,us.getId());
			if(url == null){
				us.setStatus((short)(us.getStatus() - 10));
				failNumber++;
				continue;				
			}
			try {
                Document doc = Jsoup.connect(url.getUrl()).userAgent(Const.CrawlerUserAgent).timeout(2000).get();
				String html = doc.html();
                writeHtml2Disk(url,html);
				us.setStatus((short)Const.TaskId.DownloadUrlToHtml.ordinal());
			} catch (Exception e) {
				//can't download this url.. will update the taskStatus
				us.setStatus((short)(us.getStatus() - 1));
				failNumber++;
			}
		}
		System.out.println("Failed + " + failNumber + "\t" + "Successed : " + (number - failNumber) + "\t" + Instances.get(0).getId() + "\t" + new Date());
		Hbn.updateDB(session);
        HSession.closeSession();
	}
	
	public static void main(String[] args) {
        while(true){
            Downloader dw = new Downloader();
            dw.runTask();
            try {
                System.out.println("now end of downloader,sleep for:" + Const.DownloadArticleSleepTime / 1000 / 60 + " minutes. " + new Date().toString());
                Thread.sleep(Const.DownloadArticleSleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
		}
	}
	
	
	

}