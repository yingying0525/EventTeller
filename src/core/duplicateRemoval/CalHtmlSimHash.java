package core.duplicateRemoval;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import db.hbn.model.Article;
import db.hbn.model.Url;
import extractor.article.Extractor;

public class CalHtmlSimHash {
	
	
	public static int BatchSize = 1000;
	public String HtmlPath = "\\\\10.77.30.73\\eventteller-htmls\\";
	public String OutSimHashPath = "d://ett/simhash";
	
	
	//get 2000 urls
	private static List<Url> getInstances(){
		List<Url> results = new ArrayList<Url>();
		String hql = "from Url as obj where obj.taskStatus = 1 order by obj.id asc";
		results = util.Util.getElementsFromDB(hql,BatchSize);
		return results;
	}
	
	public long getSimHashFromUrl(Url url) throws IOException{
		Article at = getArticleFromUrl(url);
		if(at == null){
			return -1;
		}else{
			return getSimHashFromArticle(at);
		}
	}
	
	public Article getArticleFromUrl(Url url) throws IOException{
		String filePath = HtmlPath + util.Util.getDateStr(url.getCrawlTime()) + File.separator + url.getId();
		File html = new File(filePath);
		if(!html.exists()){
			///update db taskstatus to -1;
//			url.setTaskStatus(-1);
			return null;
		}else{
			BufferedReader br = new BufferedReader(new FileReader(html));
			StringBuffer source = new StringBuffer();
			String line = "";
			while((line = br.readLine()) != null){
				source.append(line);
			}
			br.close();
			Extractor etor = new Extractor(url,source.toString());
			try{
				Article art = etor.getArticleFromUrl();
				if(art.getPublishtime() == null || art.getContent().length() < 30 || art.getTitle().length() < 5){
//					url.setTaskStatus(-3);
					return null;
				}
//				url.setTaskStatus(2);
				return art;
			}catch(Exception e){
//				url.setTaskStatus(-3);
				return null;
			}
		}
	}
	
	public long getSimHashFromArticle(Article art){
		String content = art.getContent() + " " + art.getTitle();
		util.SimHash simhash = new util.SimHash();
		return simhash.getSimHash(content);
	}
	
	
	public int runJob() throws IOException{
		List<Url> urls = getInstances();
		BufferedWriter bw = new BufferedWriter(new FileWriter(OutSimHashPath,true));
		for(Url url : urls){
			Article at = getArticleFromUrl(url);
			long hash = 0;
			if(at == null){
				continue;
			}else{
				 hash = getSimHashFromArticle(at);
			}
			if(hash == -1){
				continue;
			}
			bw.write(url.getId() + "\t" + hash + "\t" + at.toString() + "\n");
		}
		util.Util.updateDB(urls);	
		bw.close();
		Date end = new Date();
		System.out.println("write 2000 url ok.. "  + end.toString() );
		return urls.size();
	}
	
	public static void main(String[] args) throws IOException, InterruptedException{
		CalHtmlSimHash csh = new CalHtmlSimHash();
		csh.HtmlPath = args[0];
		csh.OutSimHashPath = args[1];
		while(true){
			int left = csh.runJob();
			if(left == 0){
				System.out.println("sleep for 10 minutes");
				Thread.sleep(1000 * 60 * 10);
			}
		}
	}
	

}
