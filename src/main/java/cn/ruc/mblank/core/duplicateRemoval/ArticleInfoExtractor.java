package cn.ruc.mblank.core.duplicateRemoval;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.ruc.mblank.db.hbn.model.Article;
import cn.ruc.mblank.db.hbn.model.Url;
import cn.ruc.mblank.db.hbn.model.UrlStatus;
import cn.ruc.mblank.util.db.Hbn;
import com.alibaba.fastjson.JSON;

import cn.ruc.mblank.config.JsonConfigModel;
import cn.ruc.mblank.config.LocalJsonConfigReader;
import cn.ruc.mblank.util.Const;
import cn.ruc.mblank.extractor.article.Extractor;

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
        Hbn db = new Hbn();
		UStatus = db.getElementsFromDB(hql,-1,BatchSize);
		BatchSize = UStatus.size();
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
			List<String> twords = cn.ruc.mblank.util.ChineseSplit.SplitStr(at.getTitle());
			List<String> cwords = cn.ruc.mblank.util.ChineseSplit.SplitStr(at.getContent());
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
        Hbn db = new Hbn();
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
			String folder = cn.ruc.mblank.util.TimeUtil.getDateStr(us.getTime());
			String filePath = HtmlPath + folder + "/" + us.getId();
			File file = new File(filePath);
			if(!file.exists()){
				us.setStatus((short)Const.TaskId.CantFindHtmlInDisk.ordinal());
				continue;
			}
			//get Url from db, the extractor need Url.url to recognition html model
			//TODO can optimize
			String getUrl = "from Url as obj where obj.id = " + us.getId();
			Url url = db.getElementFromDB(getUrl);
			if(url == null){
				us.setStatus((short)Const.TaskId.ParseHtmlFailed.ordinal());
				continue;
			}
			//get article from Url.url and html source
			Extractor etor = new Extractor(url,file);
			Article at = etor.getArticle();
			if(!checkArticle(at) || at.getTitle().length() < Const.MinTitleWordsCount || at.getContent().length() < Const.MinContentWordsCount){
				us.setStatus((short)Const.TaskId.ParseHtmlFailed.ordinal());
				continue;
			}
			//split title and content and write all to disk
			writeArticle2Disk(at,folder);
			us.setStatus((short)Const.TaskId.ParseHtmlSuccess.ordinal());
		}
	}
	
	public static void main(String[] args) throws InterruptedException{
		while(true){
			ArticleInfoExtractor t = new ArticleInfoExtractor();
			if(t.UStatus.size() < Const.MinArticleToProcess){
				System.out.println(t.UStatus.size());
				System.out.println("not enough new html to process..now will sleep for " + Const.ArticleInfoExtractorSleepTime / 1000 / 60 + " minutes");
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
            Hbn db = new Hbn();
			db.updateDB(t.UStatus);
			Date updateEnd = new Date();
			System.out.print(" update db time:" + (updateEnd.getTime() - processEnd.getTime()));
			System.out.println(" " + new Date());
		}
	}






}
