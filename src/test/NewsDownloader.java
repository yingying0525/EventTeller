package test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import db.data.Article;
import db.data.Url;

public class NewsDownloader implements Runnable{

	String EPath = "E:\\ET\\exists";
	String UPath = "E:\\ET\\urls";
	String OPath = "E:\\ET\\htmls\\";
	static int TN = 24; 
	
	Set<String> exists = new HashSet<String>();
	List<String> urls = new ArrayList<String>();
	
	public void initExist(){
		try {
			for(int i = 1; i <= TN ;i ++){
				File nfile = new File(EPath + "_" + i);
				if(!nfile.exists()){
					nfile.createNewFile();
				}
				util.IOReader reader = new util.IOReader(EPath + "_" + i);
				String line = "";
				while((line = reader.readLine())!= null){
					exists.add(line);
				}
				reader.close();
			}
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public void initUrl(){
		try {
			util.IOReader reader = new util.IOReader(UPath);
			String line = "";
			while((line = reader.readLine())!= null){
				if(!exists.contains(line)){
					urls.add(line);
				}
			}
			reader.close();
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	
	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		
		int curname = Integer.valueOf(Thread.currentThread().getName());
		int perThNum = urls.size() / TN;
		if(urls.size() % TN != 0 && curname == TN){
			///last thread add the left items
			perThNum += urls.size() % TN;
		}
		List<String> downs = new ArrayList<String>();
		for(int i = 0; i < perThNum; i ++){
			int curItem = (curname - 1) * urls.size() / TN + i;
			String[] its = urls.get(curItem).split("\t");
			if(its.length != 2){
				continue;
			}
			extractor.article.Extractor etor = new extractor.article.Extractor(its[1]);
			Url nurl = new Url();
			nurl.setUrl(its[1]);
			nurl.setId(Integer.valueOf(its[0]));
			Article at = etor.getArticleFromUrl(nurl);
			try {
				try{
					BufferedWriter bw = new BufferedWriter(new FileWriter(OPath + its[0]));
					bw.write(at.getUrl() + "\n");
					bw.write(at.getTitle() + "\n");
					bw.write(at.getPublishtime().toLocaleString() + "\n");
					bw.write(at.getSource() + "\n");
					bw.write(at.getContent() + "\n");
					bw.write(at.getImgs() + "\n");
					bw.close();
					downs.add(urls.get(curItem));
				}catch(Exception e){
					
				}
				if(downs.size() >= 100){
					System.out.println(curname + "\t" + downs.size());
					BufferedWriter sbw = new BufferedWriter(new FileWriter(EPath + "_" + curname,true));
					for(String tmp : downs){
						sbw.write(tmp + "\n");
					}
					sbw.close();
					downs.clear();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public static void main(String[] args){
		NewsDownloader nd = new NewsDownloader();
		nd.initExist();
		nd.initUrl();
		for(int i = 1 ;i <= TN ;i ++){
			Thread td = new Thread(nd,"" + i);
			td.start();
		}
	}

}
