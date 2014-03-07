package core.duplicateRemoval;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Node;

import util.Config;
import util.Const;
import db.hbn.model.AER;
import db.hbn.model.Article;
import db.hbn.model.Event;
import db.hbn.model.Url;
import extractor.article.Extractor;

public class NewsDupRemoval {
	
	public static int BatchSize = 100;
	public static int SimHashNum = 8;
	
	private static String HtmlPath = "";
	private static String HashIndexPath = "";
	
	///for hash index maps
	private HashMap<Integer,Long> IdSimHash;
	private HashMap<Integer,Article> IdAts;
	private ArrayList<HashMap<Integer,List<Integer>>> SimHashIndex;
	
	
	private void initSimHashIndex(int n){
		SimHashIndex = new ArrayList<HashMap<Integer,List<Integer>>>();
		for(int i = 0 ; i < n; i++){
			HashMap<Integer,List<Integer>> simhash = new HashMap<Integer,List<Integer>>();
			SimHashIndex.add(simhash);
		}
	}
	
	public NewsDupRemoval(){
		Config cfg = new Config(Const.SYS_CONFIG_PATH);
		Node elm = cfg.selectNode("/Configs/Config[@name='HtmlSavePath']/Path");
		HtmlPath = elm.getText();
		elm = cfg.selectNode("/Configs/Config[@name='HashIndexPath']/Path");
		HashIndexPath = elm.getText();
		IdAts = new HashMap<Integer,Article>();
		IdSimHash = new HashMap<Integer,Long>();
		initSimHashIndex(SimHashNum);
		try {
			loadHashIndex();
		} catch (IOException e) {
			System.out.println("load SimHash Index failed...");
			System.exit(-1);
		}
	}
	
	private void loadHashIndex() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(HashIndexPath));
		String line = "";
		while((line = br.readLine()) != null){
			String[] its = line.split("\t");
			int id = Integer.parseInt(its[0]);
			long hash = Long.parseLong(its[1]);
			IdSimHash.put(id , hash);
			//will load SimHashNum 
			add2SimHashIndex(id,hash);
		}
		br.close();
	}
	
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
			IdAts.put(url.getId(), at);
			return getSimHashFromArticle(at);
		}
	}
	
	public Article getArticleFromUrl(Url url) throws IOException{
		String filePath = HtmlPath + util.Util.getDateStr(url.getCrawlTime()) + File.separator + url.getId();
		File html = new File(filePath);
		if(!html.exists()){
			///update db taskstatus to -1;
			url.setTaskStatus(-1);
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
			Article art = etor.getArticleFromUrl();
			url.setTaskStatus(2);
			return art;
		}
	}
	
	public long getSimHashFromArticle(Article art){
		String content = art.getContent() + " " + art.getTitle();
		util.SimHash simhash = new util.SimHash();
		return simhash.getSimHash(content);
	}
	
	
	private int findSameInIndex(long hash, int oid){
		int MinDistance = 65;
		int SameId = -1;
		byte[] bts = util.Util.long2Byte(hash);
		for(int i = 0 ; i < SimHashNum; i++ ){
			if(SimHashIndex.get(i).containsKey((int)bts[i])){
				//cal the disstance
				List<Integer> ids = SimHashIndex.get(i).get((int)bts[i]);
				for(int id : ids){
					int bits = util.Util.diffBitsOfNums(hash,IdSimHash.get(id));
					if(bits < MinDistance && id != oid){
						MinDistance = bits;
						SameId = id;
					}
				}
			}
		}
		if(MinDistance <= util.Const.MinSimHashBitNumber){
			//find the same article
			return SameId;
		}else{
			//not find
			return -1;
		}
	}
	
	
	
	private void add2SimHashIndex(int id, long hash){
		byte[] bts = util.Util.long2Byte(hash);
		for(int i = 0 ; i < SimHashNum; i++ ){
			List<Integer> tmps = new ArrayList<Integer>();
			if(SimHashIndex.get(i).containsKey((int)bts[i])){
				tmps = SimHashIndex.get(i).get((int)bts[i]);
			}
			tmps.add(id);
			SimHashIndex.get(i).put((int)bts[i], tmps);
		}
	}
	
	private void writeSimHash2Disk(HashMap<Integer,Long> scrs) throws IOException{
		//write update array to disk
		BufferedWriter bw = new BufferedWriter(new FileWriter(HashIndexPath,true));
		for(int id : scrs.keySet()){
			bw.write(id + "\t" + scrs.get(id) + "\n");
		}
		bw.close();
	}
	
	
	private Event newEvent(Url instance){
		Article at = IdAts.get(instance.getId());
		Event et = new Event();
		et.setId(instance.getId());
		et.setTitle(at.getTitle());
		et.setContent(at.getContent());
		if(at.getImgs() == null){
			at.setImgs("");
		}
		et.setImgs(at.getImgs());
		et.setNumber(1);
		et.setPubTime(at.getPublishtime());
		et.setSource(at.getSource());
		et.setSubTopic(instance.getSubtopicId());
		et.setTaskStatus(1);
		return et;
	}
	
	
	public int runJob() throws IOException{
		HashMap<Integer,Long> updateSimHash = new HashMap<Integer,Long>();
		HashMap<Integer,Event> updateEvents = new HashMap<Integer,Event>();
		List<AER> newAERs = new ArrayList<AER>();
		List<Url> instances = getInstances();
		for(Url instance : instances){
			long hash = getSimHashFromUrl(instance);
			int sameId = findSameInIndex(hash,instance.getId());
			AER aer = new AER();
			aer.setArticleId(instance.getId());
			if(sameId != -1){
				Event cet = new Event();
				if(updateEvents.containsKey(sameId)){
					cet = updateEvents.get(sameId);
				}else{
					String hql = "from Event as obj where obj.id = " + sameId;
					cet = util.Util.getElementFromDB(hql);
					if(cet == null){
						continue;
					}					
				}
				//update number
				cet.setNumber(cet.getNumber() + 1);
				Article cat = IdAts.get(instance.getId());
				if(cat.getImgs() != null && cat.getImgs().length() > 5){
					cet.setImgs(cet.getImgs() + "!##!" + cat.getImgs());
				}
				updateEvents.put(sameId, cet);
				aer.setEventId(sameId);		
			}else{
				//add to update array
				updateSimHash.put(instance.getId(), hash);
				//add to simhash index
				add2SimHashIndex(instance.getId(),hash);
				//
				IdSimHash.put(instance.getId(), hash);
				//add to new Events
				Event et = newEvent(instance);				
				updateEvents.put(instance.getId(), et);
				//add to AER update
				aer.setEventId(0);
			}
			newAERs.add(aer);
		}
		//write new hash to disk
		writeSimHash2Disk(updateSimHash);
		//update DB
		util.Util.updateDB(instances);
		util.Util.updateDB(updateEvents.values());
		util.Util.updateDB(newAERs);
		System.out.println("update db ok..." + instances.get(instances.size() - 1).getId());
		return instances.size();
	}
	
	
	public static void main(String[] args) throws IOException, InterruptedException{
		NewsDupRemoval ndr = new NewsDupRemoval();
		while(true){
			Date startT = new Date();
			int left = ndr.runJob();
			Date endT = new Date();
			System.out.println((endT.getTime() - startT.getTime()) / 1000 / 60.0 );
			ndr.IdAts = new HashMap<Integer,Article>();		
			if(left  == 0){
				Thread.sleep(1000 * 60 * 20);
				System.out.println("no article to clustering...sleep for 20 minutes");
			}
		}
	}

}
