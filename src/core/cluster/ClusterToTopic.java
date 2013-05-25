package core.cluster;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import news.index.Index;
import news.index.TopicIndex;

import org.dom4j.Node;

import util.Config;
import util.Const;
import util.IOReader;

import db.data.Article;
import db.data.Topic;
import db.data.Word;

public class ClusterToTopic {
	
	
	public String IndexPath;
	public Index TopicIndex;
	public String IDFPath;
	
	
	private int TotalDocNum ;
	private Map<String,Integer> Idf ;
	
	ClusterToTopic(){
		Const.loadTaskid();
		Config cfg = new Config(Const.SYS_CONFIG_PATH);
		Node elem = cfg.selectNode("/Configs/Config[@name='TopicIndex']/Path");
		Node elemIdf = cfg.selectNode("/Configs/Config[@name='TotalDF']/Path");
		IndexPath = elem.getText();
		TopicIndex = new TopicIndex(IndexPath);
		IDFPath = elemIdf.getText();
		Idf = new HashMap<String,Integer>();
		initIdf();
	}
	
	////get articles from article table where taskstatus = 1 (stands for only article)
	//// first cluster to topics
	
	private List<Article> getInstances(){
		List<Article> results = new ArrayList<Article>();
		String hql = "from Article as obj where obj.taskstatus = 1 and pubtime is not null order by obj.publishtime asc";
		results = util.Util.getElementsFromDB(hql,500);
		return results;
	}
	
	/**
	 * load Idf to memory
	 */
	private void initIdf(){
		try {
			IOReader reader = new IOReader(IDFPath);
			String line = "";
			line = reader.readLine();
			if(line == null || line.length() == 0 || line.split("\t").length != 2){
				return;
			}
			String[] its = line.split("\t");
			TotalDocNum = Integer.valueOf(its[0]);
			while((line = reader.readLine())!=null){
				String[] vls = line.split("\t");
				Idf.put(vls[0], Integer.valueOf(vls[1]));
			}
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * get the search text from title
	 * @param title
	 * @return
	 */
	private String titleToSearchString(String title){
		StringBuffer result = new StringBuffer("");
		List<String> words = util.ChineseSplit.SplitStr(title);
		for(String word : words){
			if(word.length() >= 2){
				result.append(" " + word);
			}
		}
		return result.toString();
	}
	
	private int getMostSimId(Article at){
		double maxSim = -1.0;
		int maxId = -1;
		String searchText = titleToSearchString(at.getTitle());
		List<Integer> simIds = TopicIndex.search(searchText, false, 50);
		for(Integer simId : simIds){
			double totalSim = 0.0;
			String hql = "from Topic as obj where obj.id = " + simId;
			Topic tp = util.Util.getElementFromDB(hql);
			String[] simAts = tp.getArticles().split(" ");
			if(simAts.length == 0){
				continue;
			}
			Map<Word, Double> wordsA = util.ChineseSplit.SplitStrWithPosDoubleTF(at.getContent() + at.getTitle());
			for(String simAt : simAts){
				Article tat = util.Util.getArticleById(simAt);
				Map<Word, Double> wordsB = util.ChineseSplit.SplitStrWithPosDoubleTF(tat.getContent() + tat.getTitle());
				double score = util.Similarity.SimilarityWithIDF(wordsA, wordsB, Idf, TotalDocNum);
				totalSim += score;
			}
			if(totalSim / simAts.length > maxSim){
				maxSim = totalSim / simAts.length;
				maxId = simId;
			}			
		}
		if(maxSim < util.Const.MaxTopicSimNum){
			maxId = -1;
		}
		return maxId;
	}
	
	public void RunCluster(){
		List<Article> articles = getInstances();
		for(Article at : articles){
			int simId = getMostSimId(at);
			if(simId > 0){
				at.setEventid(simId);
				at.setTaskstatus(util.Const.TASKID.get("ArticleToTopic"));
			}
		}
		///now should update index and db
		
		//update db
		
		//update index
		
	}
	
}
