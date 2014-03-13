package core.duplicateRemoval;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;

import config.JsonConfigModel;
import config.LocalJsonConfigReader;
import util.Const;
import db.hbn.model.Article;
import db.hbn.model.Url;
import db.hbn.model.UrlStatus;
import extractor.article.Extractor;

/**
* @PackageName:core.duplicateRemoval
* @ClassName: ArticleInfoExtractor
* @author: mblank
* @date: 2014年3月13日 上午11:14:38
* @Description: just read html from disk and extract infomation from html and do chinese split.
* @Marks: TODO
*/
public class ArticleInfoExtractor implements Runnable{
	
	private String HtmlPath;
	private String ArticleSavePath;
		
	private int BatchSize = 10000; 
	private List<UrlStatus> UStatus;
	
	public ArticleInfoExtractor(){
		String fileContent = LocalJsonConfigReader.readJsonFile(Const.SYS_JSON_CONFIG_PATH);
		JsonConfigModel jcm = JSON.parseObject(fileContent,JsonConfigModel.class);
		HtmlPath = jcm.HtmlSavePath;
		ArticleSavePath = jcm.ArticleFilePath;
		getInstances();
	}
	
	private void getInstances(){
		String hql = "from UrlStatus as obj where obj.status = " + Const.TaskId.DownloadUrlToHtml.ordinal();
		UStatus = util.Util.getElementsFromDB(hql,BatchSize);
	}
	
	public boolean checkArticle(Article at){
		return at != null && at.getTitle() != null && at.getPublishtime() != null && at.getContent() != null;
	}
	
	public void writeArticle2Disk(Article at,String sfolder){
		File folder = new File(ArticleSavePath + File.separator + sfolder);
		if(!folder.exists()){
			folder.mkdirs();
		}
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(ArticleSavePath + File.separator + sfolder + File.separator + at.getId()));
			List<String> twords = util.ChineseSplit.SplitStr(at.getTitle());
			List<String> cwords = util.ChineseSplit.SplitStr(at.getContent());
			bw.write(at.getId() + "\n");
			bw.write(at.getPublishtime().getTime() + "\n");
			for(String tw : twords){
				bw.write(tw + " ");
			}
			bw.write("\n");
			for(String cw : cwords){
				bw.write(cw + " ");
			}
			bw.write("\n");
			bw.write(at.getTitle() + "\n");
			bw.write(at.getContent() + "\n");
			bw.write(at.getImgs() + "\n");
			bw.write(at.getSource() + "\n");
			bw.close();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run() {
		int startIndex = 0;
		int endIndex = 0;
		int CurThread = Integer.parseInt(Thread.currentThread().getName());
		if(CurThread == Const.AtInfoExtractorThreadNumber){
			startIndex = CurThread * (BatchSize / Const.AtInfoExtractorThreadNumber);
			endIndex = BatchSize;
		}else{
			startIndex = CurThread * (BatchSize / Const.AtInfoExtractorThreadNumber);
			endIndex = startIndex + (BatchSize / Const.AtInfoExtractorThreadNumber);
		}
		for(int i = startIndex; i < endIndex; ++i){
			UrlStatus us = UStatus.get(i);
			String folder = util.Util.getDateStr(us.getTime());
			String filePath = HtmlPath + folder + "/" + us.getId();
			File file = new File(filePath);
			if(!file.exists()){
				us.setStatus(Const.TaskId.CantFindHtmlInDisk.ordinal());
				continue;
			}
			//get Url from db, the extractor need Url.url to recognition html model
			//TODO can optimize
			String getUrl = "from Url as obj where obj.id = " + us.getId();
			Url url = util.Util.getElementFromDB(getUrl);
			//get article from Url.url and html source
			Extractor etor = new Extractor(url,file);
			Article at = etor.getArticle();
			if(!checkArticle(at) || at.getTitle().length() < Const.MinTitleWordsCount || at.getContent().length() < Const.MinContentWordsCount){
				us.setStatus(Const.TaskId.ParseHtmlFailed.ordinal());
				continue;
			}
			//split title and content and write all to disk
			writeArticle2Disk(at,folder);
			us.setStatus(Const.TaskId.ParseHtmlSuccess.ordinal());
		}
	}
	
	public static void main(String[] args) throws InterruptedException{
		while(true){
			ArticleInfoExtractor t = new ArticleInfoExtractor();
			if(t.UStatus.size() == 0){
				System.out.println("no new html to process..now will sleep for " + Const.ArticleInfoExtractorSleepTime / 1000 / 60 + "minutes");
				Thread.sleep(Const.ArticleInfoExtractorSleepTime);
				continue;
			}
			Date totalStart = new Date();
			List<Thread> thds = new ArrayList<Thread>();
			for(Integer i = 0 ; i < Const.AtInfoExtractorThreadNumber; i++){
				Thread th = new Thread(t);
				th.setName(i.toString());
				th.start();
				thds.add(th);
			}
			for(Thread t1 : thds){
				t1.join();
			}
			Date processEnd = new Date();
			System.out.print("process time : " + (processEnd.getTime() - totalStart.getTime()));
			util.Util.updateDB(t.UStatus);
			Date updateEnd = new Date();
			System.out.print(" update db time:" + (updateEnd.getTime() - processEnd.getTime()));
			System.out.println(" " + new Date());
		}
	}






}
