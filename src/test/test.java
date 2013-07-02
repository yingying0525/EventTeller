package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import news.crawler.article.Extractor;
import news.index.ArticleTitleIndex;

import db.data.Article;
import db.data.Url;
import db.data.Word;



public class test implements Runnable{
		
	public void calPrecision(Article at){
		System.out.println(at.getTitle() + "\t" + at.getNumber() + "\t" + at.getUrl());
		String[] ids = at.getSameurls().split(" ");
		for(String id : ids){
			if(id.length() < 2){
				continue;
			}
			String shql = "from Url as obj where obj.id = " + id;
			Url uid = util.Util.getElementFromDB(shql);
			if(uid != null){
				Extractor etor = new Extractor(uid.getUrl());
				String utitle = etor.getTitle();
				if(utitle.length() == 0){
					utitle = etor.getTitleG();
				}
				System.out.println(id + "\t" + utitle + "\t" + uid.getUrl());
			}
		}
		System.out.println("---------------");
	}
	
	public void calRecall(Article at) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter("d:\\ETT\\Recall",true));
		bw.write(at.getId() + "\t" + at.getTitle() + "\t" + at.getNumber() + "\t" + at.getSameurls() + "\n");
		ArticleTitleIndex ati = new  ArticleTitleIndex("d:\\ETT\\AI\\");
		List<String> twds = util.ChineseSplit.SplitStr(at.getTitle());
		StringBuffer sb = new StringBuffer();
		for(String twd : twds){
			if(twd.length() < 2)
				continue;
			sb.append(twd + " ");
		}  
		List<Article> sims = ati.search(sb.toString(), false,50,at.getPublishtime());
		for(Article tat : sims){
			if(tat.getId() - at.getId().intValue() == 0){
				continue;
			}
			System.out.println(at.getId() + "\t" + tat);
			double osim = util.Similarity.ContentOverlap(at.getContent(), tat.getContent());
			double ssim = getTwoArticleSim(at,tat);
			if(osim > 1 && osim != 2){
				osim = osim - 1;
			}
			if(osim < 0.2 )
				continue;
			bw.write(osim + "\t" + ssim + "\t" + tat + "\t" + tat.getTitle() + "\t" + tat.getNumber() + "\t" + "\t" + tat.getUrl() + "\n");
		}
		bw.write("---------------" + "\n");	
		bw.close();
	}
	
	public void evaluateArticlePR() throws IOException{
		List<Article> ats = new ArrayList<Article>();
		String hql = "from Article as obj where obj.publishtime >= '2013-5-24' and obj.number > 0 order by number desc";
		ats = util.Util.getElementsFromDB(hql, 30);
		///for precision
		for(Article at : ats){
//			calPrecision(at);
			calRecall(at);
		}
	}
	
	
	public double getTwoArticleSim(int ida , int idb){
		String ahql = "from Article as obj where obj.id = " + ida;
		String bhql = "from Article as obj where obj.id = " + idb;
		Article ata = util.Util.getElementFromDB(ahql);
		Article atb = util.Util.getElementFromDB(bhql);
		Map<Word,Double> wda = util.ChineseSplit.SplitStrWithPosDoubleTF(ata.getContent());
		Map<Word,Double> wdb = util.ChineseSplit.SplitStrWithPosDoubleTF(atb.getContent());
		return util.Similarity.SimilarityOfTF(wda, wdb);
	}
	
	public double getTwoArticleSim(Article ata , Article atb){
		Map<Word,Double> wda = util.ChineseSplit.SplitStrWithPosDoubleTF(ata.getContent());
		Map<Word,Double> wdb = util.ChineseSplit.SplitStrWithPosDoubleTF(atb.getContent());
		return util.Similarity.SimilarityOfTF(wda, wdb);
	}
	
	
	
	
	
	
	
	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException{
//		test ts = new test();
////		ts.evaluateArticlePR();
//		double sim = ts.getTwoArticleSim(4184, 4091);
//		System.out.println(sim);
	
		Map<String,Integer> idfs = new HashMap<String,Integer>();
		BufferedReader br = new BufferedReader(new FileReader("d:\\ett\\TDF"));
		String line = "";
		String[] tmps = br.readLine().split("\t");
		
		long total = 0;
		double avg = 0;
		while((line = br.readLine())!= null){
			String[] its = line.split("\t");
			idfs.put(its[0], Integer.valueOf(its[1]));
			avg += Integer.valueOf(its[1]);
			total++;
		}
		
		System.out.println(avg / total);
		br.close();
		
		
		news.index.TopicIndex ti = new news.index.TopicIndex("d:\\ETT\\TI\\");
		Article inat = util.Util.getArticleById("10490");
		List<Article> ats = ti.search("丁俊晖", false, 100,inat.getPublishtime());
		
		System.out.println(inat.getTitle() + "\t" + inat.getPublishtime() );
		
		System.out.print(ats.size());
		
		
		for(Article at : ats){
			Double score = util.Similarity.similarityOfEvent(inat, at, idfs, 848360);
			if(score < 0.1 ){
				continue;
			}
			System.out.println(at.getId() + "\t" + at.getTitle() + "\t" + score);
		}
	}

	
	
	@Override
	public void run() {
		
		
	}
		
}