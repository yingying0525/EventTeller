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
import java.util.Random;
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
	public Map<String,Set<Article>> MemoryIndex ;
	
	
	private int TotalDocNum ;
	private Map<String,Integer> Idf ;
	
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
		MemoryIndex = new HashMap<String,Set<Article>>();
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
	
	private List<String> sampleIds(String ats){
		String[] ids = ats.split(" ");
		List<String> results = new ArrayList<String>(10);
		Random rd = new Random(System.currentTimeMillis());
		for(String id : ids){
			if(ids.length <= 1){
				continue;
			}
			int nextid = rd.nextInt(ids.length -1);
			if(nextid > 0){
				results.add(id);
				if(results.size() >= 10){
					break;
				}
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
			///all the articles 
			///get some sample articles to stand for the whole set
			///accelerate speed
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
	
	public void updateTopicInfo(Topic tp,Article at){
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
		tp.setKeyWords(kw.getTopNwords(7, 0));
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
	}
	
	public Topic newTopic(Topic simTopic,Article at){
		simTopic = new Topic();
		//get the max topic id
		String hql = "select max(id) from Topic";
		int maxid = util.Util.getMaxIdFromDB(hql);
		Iterator<Integer> ids = MemoryTopics.keySet().iterator();
		while(ids.hasNext()){
			int tid = ids.next();
			if(maxid < tid){
				maxid = tid;
			}
		}
		if(maxid < 0){
			maxid = 0;
		}
		simTopic.setId(maxid + 1);
		simTopic.setArticles(String.valueOf(at.getId()));
		simTopic.setEndTime(at.getPublishtime());
		simTopic.setStartTime(at.getPublishtime());
		simTopic.setTitle(at.getTitle());
		simTopic.setImgs(at.getImgs());
		KeyWords kw = new KeyWords(at.getTitle());
		simTopic.setKeyWords(kw.getTopNwords(7, 0));
		simTopic.setNumber(1);
		//should update summary and relations
		
		return simTopic;
	}
	
	
	public void updateMemory(Topic tp){
		MemoryTopics.put(tp.getId(), tp);
		List<String> words = util.ChineseSplit.SplitStr(tp.getTitle());
		for(String wd : words){
			if(wd.length() < 2){
				continue;
			}
			Set<Integer> topics = new HashSet<Integer>();
			if(MemoryIndex.containsKey(wd)){
				topics = MemoryIndex.get(wd);
			}
			topics.add(tp.getId());
			MemoryIndex.put(wd, topics);			
		}
	}
	
	
	public void RunCluster(){
		Set<Topic> updateTopics = new HashSet<Topic>();
		List<Article> articles = getInstances();
		for(Article at : articles){
			//find most similar topic in memory and index
			int simTopicId = getMostSimTopic(at);
			if(simTopicId > 0){
				updateTopicInfo(simTopic,at);	
				System.out.println("find sim topic ..." + simTopic.getId());
			}else{
				simTopic = newTopic(simTopic,at);
				System.out.println("new topic ..." + simTopic.getId());
			}
			at.setTopicid(simTopic.getId());
			at.setTaskstatus(util.Const.TASKID.get("ArticleToTopic"));
			updateTopics.add(simTopic);
			updateMemory(simTopic);
		}
		//update artices
		util.Util.updateDB(articles);
		//update topics
		util.Util.updateDB(updateTopics);
		//update index
		TopicIndex ti = new TopicIndex(IndexPath);
		ti.update(updateTopics);
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
