package core.cluster;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import news.index.Index;
import news.index.TopicIndex;

import org.dom4j.Node;

import core.infoGenerator.KeyWords;

import util.Config;
import util.Const;
import util.IOReader;

import db.data.Article;
import db.data.Topic;

public class TopicTrace {
	
	
	public String IndexPath;
	public Index TopicIndex;
	public String IDFPath;
	public Map<Integer,Article> MemoryArticles ;
	public Map<Integer,Topic> MemoryTopics;
	public Map<String,Set<Article>> MemoryIndex ;
	
	
	private int TotalDocNum ;
	private Map<String,Integer> Idf ;
	
	private int MaxTopicId = 0;
	
	public TopicTrace(){
		Const.loadTaskid();
		Config cfg = new Config(Const.SYS_CONFIG_PATH);
		Node elem = cfg.selectNode("/Configs/Config[@name='TopicIndex']/Path");
		Node elemIdf = cfg.selectNode("/Configs/Config[@name='TotalDF']/Path");
		IndexPath = elem.getText();
		TopicIndex = new TopicIndex(IndexPath);
		IDFPath = elemIdf.getText();
		Idf = new HashMap<String,Integer>();
		initIdf();
		MemoryArticles = new HashMap<Integer,Article>();
		MemoryTopics = new HashMap<Integer,Topic>();
		MemoryIndex = new HashMap<String,Set<Article>>();
		String hql = "select max(id) from Topic";
		MaxTopicId = util.Util.getMaxIdFromDB(hql);
	}
	
	////get articles from article table where taskstatus = 1 (stands for only article)
	//// first cluster to topics
	
	private List<Article> getInstances(){
		List<Article> results = new ArrayList<Article>();
		String hql = "from Article as obj where obj.taskstatus = 1 and pubtime is not null order by obj.publishtime asc";
		results = util.Util.getElementsFromDB(hql,2000);
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
		System.out.println(Idf.size());
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
	
	private Set<Article> getMemoryIndexSimTopic(String text){
		List<String> words = util.ChineseSplit.SplitStr(text);
		Set<Article> results = new HashSet<Article>();
		for(String wd : words){
			if(MemoryIndex.containsKey(wd)){
				results.addAll(MemoryIndex.get(wd));
			}
		}
		return results;
	}
	
	private int getMostSimTopic(Article at){
		Map<Integer,Double> totalSims = new HashMap<Integer,Double>();
		Map<Integer,Integer> totalCount = new HashMap<Integer,Integer>();
		double maxSim = -1.0;
		int maxId = -1;
		String searchText = titleToSearchString(at.getTitle());
		List<Article> simIdsInIndex = TopicIndex.search(searchText, false, 100);
		Set<Article> simIds = getMemoryIndexSimTopic(at.getTitle());
		for(Article simId : simIdsInIndex){
			if(!simIds.contains(simId)){
				simIds.add(simId);
			}			
		}
		for(Article simId : simIds){
			double score = util.Similarity.similarityOfEvent(simId, at, Idf, TotalDocNum);
			if(totalSims.containsKey(simId.getTopicid())){
				totalSims.put(simId.getTopicid(), score + totalSims.get(simId.getTopicid()));
				totalCount.put(simId.getTopicid(), 1 + totalCount.get(simId.getTopicid()));
			}else{
				totalSims.put(simId.getTopicid(), score );
				totalCount.put(simId.getTopicid(), 1 );
			}		
		}
		Iterator<Integer> sids = totalSims.keySet().iterator();
		while(sids.hasNext()){
			int key = sids.next();
			if(totalSims.get(key) / totalCount.get(key) > maxSim){
				maxSim = totalSims.get(key) / totalCount.get(key);
				maxId = key;
			}
		}
		if(maxSim < util.Const.MaxTopicSimNum){
			maxId = -1;
		}
		return maxId;
	}
	
	public Topic updateTopicInfo(int tid,Article at){
		Topic tp = new Topic();
		if(MemoryTopics.containsKey(tid)){
			tp = MemoryTopics.get(tid);
		}else{
			String hql = "from Topic as obj where obj.id = " + tid;
			tp = util.Util.getElementFromDB(hql);
		}
		
		if(tp == null){
			return null;
		}
		tp.setTitle(tp.getTitle() + "!##!" + at.getTitle());
		tp.setArticles(tp.getArticles() + " " + at.getId());
		Date time = at.getPublishtime();
		if(tp.getStartTime().compareTo(time) > 0){
			tp.setStartTime(time);
		}
		if(tp.getEndTime().compareTo(time) < 0){
			tp.setEndTime(time);
		}
		//update keywords
		KeyWords kw = new KeyWords(tp.getTitle());
		tp.setKeyWords(kw.getTopNwords(8, 0));
		//update imgs
		tp.setImgs(tp.getImgs() + "@@@@" + at.getImgs());
		//update number
		if(tp.getNumber() == null){
			tp.setNumber(1);
		}else{
			tp.setNumber(tp.getNumber() + 1);
		}
		//update subtopic temp is 0
		tp.setSubtopic(0);
		//update summary
		//update relations
		//should add at to the relationships of topic tp
		
		return tp;
	}
	
	public Topic newTopic(Article at){
		Topic Ntopic = new Topic();
		//get the max topic id
		Iterator<Integer> ids = MemoryTopics.keySet().iterator();
		while(ids.hasNext()){
			int tid = ids.next();
			if(MaxTopicId < tid){
				MaxTopicId = tid;
			}
		}
		if(MaxTopicId < 0){
			MaxTopicId = 0;
		}
		Ntopic.setId(MaxTopicId + 1);
		Ntopic.setArticles(String.valueOf(at.getId()));
		Ntopic.setEndTime(at.getPublishtime());
		Ntopic.setStartTime(at.getPublishtime());
		Ntopic.setTitle(at.getTitle());
		Ntopic.setImgs(at.getImgs());
		KeyWords kw = new KeyWords(at.getTitle());
		Ntopic.setKeyWords(kw.getTopNwords(7, 0));
		Ntopic.setNumber(1);
		//should update summary and relations
		
		//add to memory topics
		MemoryTopics.put(Ntopic.getId(), Ntopic);
		return Ntopic;
	}
	
	
	public void updateMemoryIndex(Article at){
		List<String> words = util.ChineseSplit.SplitStr(at.getTitle());
		for(String wd : words){
			if(wd.length() < 2){
				continue;
			}
			Set<Article> ats = new HashSet<Article>();
			if(MemoryIndex.containsKey(wd)){
				ats = MemoryIndex.get(wd);
			}
			ats.add(at);
			MemoryIndex.put(wd, ats);			
		}
	}
	
	
	public void RunCluster(){
		Set<Topic> updateTopics = new HashSet<Topic>();
		List<Article> articles = getInstances();
		for(Article at : articles){
			//find most similar topic in memory and index
			int simTopicId = getMostSimTopic(at);
			Topic simTopic = new Topic();
			if(simTopicId > 0){
				simTopic = updateTopicInfo(simTopicId,at);	
				System.out.println("find sim topic ..." + simTopicId);
			}else{
				simTopic = newTopic(at);
			}
			at.setTopicid(simTopic.getId());
			at.setTaskstatus(util.Const.TASKID.get("ArticleToTopic"));
			updateTopics.add(simTopic);
			MemoryTopics.put(simTopic.getId(), simTopic);
			updateMemoryIndex(at);
		}
		//update artices
		util.Util.updateDB(articles);
		//update topics
		util.Util.updateDB(updateTopics);
		//update index
		TopicIndex ti = new TopicIndex(IndexPath);
		ti.update(articles);
	}
	
	public static void main(String[] args){
		while(true){
			TopicTrace ctt = new TopicTrace();
			ctt.RunCluster();
			try {
				System.out.println("now end of one cluster,sleep for:"+Const.ClusterToTopicSleepTime /1000 /60 +" minutes. "+new Date().toString());
				Thread.sleep(Const.ClusterToTopicSleepTime );
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
		}
	}
	
}
