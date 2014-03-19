package core.duplicateRemoval;

//import java.util.ArrayList;
//import index.solr.EventIndex;

import index.solr.EventIndex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
//import java.util.Map;


import java.util.Set;

import util.Const;
import util.Log;


import config.JsonConfigModel;
import db.hbn.model.Article;
import db.hbn.model.Event;
import db.hbn.model.EventInfo;
import db.hbn.model.EventStatus;
import db.hbn.model.Url;
import db.hbn.model.UrlStatus;
import extractor.article.Extractor;

public class EventDetector {
	
	
	private class SimPair{
		public int id;
		public long simhash;
		public int eventId;
	};
	
	private int BatchSize = 2000; 
	private List<UrlStatus> UStatus;
	
	private ArrayList<HashMap<Integer,List<SimPair>>> LocalSimHash;
	
	private String HtmlPath;
	private String LocalSimHashPath;
	
	public EventDetector(){
		//get save path from the Config xml
		Log.getLogger().info("Start EventDetector!");
		//read Bloom filter file path from json config file
		JsonConfigModel jcm = JsonConfigModel.getConfig();
		HtmlPath = jcm.HtmlSavePath;
		LocalSimHashPath = jcm.SimHashPath;
		initSimHashIndex();
		loadLocalSimHash();
	}
	
	private void initSimHashIndex(){
		LocalSimHash = new ArrayList<HashMap<Integer,List<SimPair>>>();
		for(int i = 0 ; i < Const.SimHashIndexNumber; i++){
			HashMap<Integer,List<SimPair>> simhash = new HashMap<Integer,List<SimPair>>();
			LocalSimHash.add(simhash);
		}
	}
	
	private void add2LocalSimHash(SimPair sp){
		byte[] bts = util.Coding.long2Byte(sp.simhash);
		for(int i = 0 ; i < Const.SimHashIndexNumber; i++ ){
			List<SimPair> tmps = new ArrayList<SimPair>();
			if(LocalSimHash.get(i).containsKey((int)bts[i])){
				tmps = LocalSimHash.get(i).get((int)bts[i]);
			}
			tmps.add(sp);
			LocalSimHash.get(i).put((int)bts[i], tmps);
		}
	}
	
	private void loadLocalSimHash(){
		try {
			BufferedReader br = new BufferedReader(new FileReader(LocalSimHashPath));
			String line;
			while((line = br.readLine()) != null){
				String[] its = line.split(" ");
				if(its.length != 3){
					continue;
				}
				SimPair sp = new SimPair();
				sp.id = Integer.parseInt(its[0]);
				sp.simhash = Long.parseLong(its[1]);
				sp.eventId = Integer.parseInt(its[2]);
				add2LocalSimHash(sp);
			}
			System.out.println("init simhash index ok...");
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void getInstances(){
		String hql = "from UrlStatus as obj where obj.status = " + Const.TaskId.DownloadUrlToHtml.ordinal() ;
		UStatus = util.db.Hbn.getElementsFromDB(hql,BatchSize);
	}
	
	private SimPair findSameInIndex(long hash, int oid){
		int MinDistance = 65;
		SimPair SameId = null;
		byte[] bts = util.Coding.long2Byte(hash);
		for(int i = 0 ; i < Const.SimHashIndexNumber; i++ ){
			if(LocalSimHash.get(i).containsKey((int)bts[i])){
				//cal the disstance
				List<SimPair> ids = LocalSimHash.get(i).get((int)bts[i]);
				for(SimPair id : ids){
					int bits = util.Coding.diffBitsOfNums(hash,id.simhash);
					if(bits < MinDistance && id.id != oid){
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
			return null;
		}
	}
	
	public Event createEvent(Article at){
		Event res = new Event();
		res.setId(at.getId());
		res.setTitle(at.getTitle());
		res.setPubTime(at.getPublishtime());
		res.setSource(at.getSource());
		res.setContent(at.getContent());
		res.setImgs(at.getImgs());
		res.setNumber(1);
		res.setDay(util.TimeUtil.getDayGMT8(at.getPublishtime()));
		if(res.getDay() <= 0){
			res.setDay(0);
		}
		return res;
	}
	
	public EventInfo createEventInfo(Article at){
		EventInfo res = new EventInfo();
		res.setId(at.getId());
		res.setDay((util.TimeUtil.getDayGMT8(at.getPublishtime())));
		res.setNumber(1);
		if(res.getDay() <= 0){
			res.setDay(0);
		}
		return res;
	}
	
	private EventStatus createEventStatus(Article at){
		EventStatus res = new EventStatus();
		res.setId(at.getId());
		res.setStatus(Const.TaskId.CreateNewEvent.ordinal());
		return res;
	}
	
	private void writeSimHash2Disk(){
		if(LocalSimHash.size() == 0){
			return;
		}
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(this.LocalSimHashPath));
			for(List<SimPair> sps : LocalSimHash.get(0).values()){
				for(SimPair sp : sps){
					bw.write(sp.id + " " + sp.simhash + " " + sp.eventId + "\n");
				}
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean checkArticle(Article at){
		return at != null && at.getTitle() != null && at.getPublishtime() != null && at.getContent() != null;
	}
	
//	private void writeDF2Disk(){
//		//write DDN
//		try {
//			BufferedWriter bw = new BufferedWriter(new FileWriter(this.LocalDDNPath));
//			for(int day : DDNMap.keySet()){
//				bw.write(day + " " + DDNMap.get(day) + "\n");
//			}
//			bw.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		//write DF
//		try {
//			BufferedWriter bw = new BufferedWriter(new FileWriter(this.LocalDFPath));
//			for(String word : DfMap.keySet()){
//				bw.write(word + "\t");
//				Map<Integer,Integer> days = DfMap.get(word);
//				for(int day : days.keySet()){
//					bw.write(day + " " + days.get(day) + "\t");
//				}
//				bw.write("\n");
//			}
//			bw.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		//write TDF
//		try {
//			BufferedWriter bw = new BufferedWriter(new FileWriter(this.LocalTDFPath));
//			for(String word : TdfMap.keySet()){
//				bw.write(word + " " + TdfMap.get(word) + "\n");
//			}
//			bw.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
	
	private boolean checkMessyCode(String text){
		//just a simple checker for messy code in chinese...
		if(text == null || text.length() < 5){
			return true;
		}
		double messyNum = 0;
		for(int i = 0 ; i < text.length(); ++i){
			if(text.charAt(i) == '?'){
				messyNum++;
			}
		}
		return messyNum / text.length() > 0.4;
	}
	
	public void runTask(){
		Date start = new Date();
		//get batchsize instances first.
		getInstances();
		Set<Event> updateSolr = new HashSet<Event>();
		HashMap<Integer,Event> updateEvents = new HashMap<Integer,Event>();
		HashMap<Integer,EventInfo> updateEventInfos = new HashMap<Integer,EventInfo>();
		List<EventStatus> updateEventStatus = new ArrayList<EventStatus>();
		//for each url , get it's filepath and parse the html to article;
		for(UrlStatus us : UStatus){
			String folder = HtmlPath + util.TimeUtil.getDateStr(us.getTime()) + "/" + us.getId();
			File file = new File(folder);
			if(!file.exists()){
				us.setStatus(Const.TaskId.CantFindHtmlInDisk.ordinal());
				continue;
			}
			//get Url from db, the extractor need Url.url to recognition html model
			//TODO can optimize
			String getUrl = "from Url as obj where obj.id = " + us.getId();
			Url url = util.db.Hbn.getElementFromDB(getUrl);
			//get article from Url.url and html source
			Extractor etor = new Extractor(url,file);
			Article at = etor.getArticle();
			if(!checkArticle(at) || checkMessyCode(at.getTitle())){
				us.setStatus(Const.TaskId.ParseHtmlFailed.ordinal());
				continue;
			}
			//get simhash of this article
			long atSimHash = util.hash.SimHash.getSimHash(at.getTitle() + at.getContent());
			if(atSimHash == 0){
				us.setStatus(Const.TaskId.ParseHtmlFailed.ordinal());
				continue;
			}
			SimPair mostSame = findSameInIndex(atSimHash,url.getId());
			if(mostSame != null){
				//find same ..
				Event oldEvent = null;
				//first find in memory
				if(updateEvents.containsKey(mostSame.eventId)){
					oldEvent = updateEvents.get(mostSame.eventId);
				}else{
					//get Event from db and update imgs,pubtime
					String ehql = "from Event as obj where obj.id = " + mostSame.eventId;
					oldEvent = util.db.Hbn.getElementFromDB(ehql);
				}
				if(oldEvent == null){
					us.setStatus(Const.TaskId.ParseHtmlFailed.ordinal());
					continue;
				}
				int day = oldEvent.getDay();
				if(at.getImgs() != null && at.getImgs().length() > 0){
					oldEvent.setImgs(oldEvent.getImgs() + " " + at.getImgs());
				}
				if(oldEvent.getPubTime().compareTo(at.getPublishtime()) > 0){
					oldEvent.setPubTime(at.getPublishtime());
					day = util.TimeUtil.getDayGMT8(at.getPublishtime());
				}
				if(day > 100000 || day <= 0){
					System.out.println("error " + day + "\t" + us.getId());
					us.setStatus(Const.TaskId.ParseHtmlFailed.ordinal());
					continue;
				}
				oldEvent.setDay(day);
				oldEvent.setNumber(oldEvent.getNumber() + 1);
				updateSolr.add(oldEvent);
				//update EventInfo.number
				EventInfo ei = null;
				//first find in memory..then try to read from db.
				if(updateEventInfos.containsKey(mostSame.eventId)){
					ei = updateEventInfos.get(mostSame.eventId);
				}else{
					String ehql = "from EventInfo as obj where obj.id = " + oldEvent.getId();
					ei = util.db.Hbn.getElementFromDB(ehql);
				}
				if(ei == null){
					us.setStatus(Const.TaskId.ParseHtmlFailed.ordinal());
					continue;
				}
				ei.setDay(day);
				ei.setTopic(us.getTopic());
				ei.setNumber(ei.getNumber() + 1);
				//add to update set
				updateEvents.put(mostSame.eventId,oldEvent);
				updateEventInfos.put(mostSame.eventId,ei);
				//add to localsimhash
				SimPair nsp = new SimPair();
				nsp.id = at.getId();
				nsp.simhash = atSimHash;
				nsp.eventId = mostSame.eventId;
				add2LocalSimHash(nsp);
			}else{
				//can't find same.
				//new event 
				Event newEvent = createEvent(at);
				newEvent.setTopic(us.getTopic());
				EventInfo newEventInfo = createEventInfo(at);
				newEventInfo.setTopic(us.getTopic());
				EventStatus newEventStatus = createEventStatus(at);			
				//add to update set
				updateEvents.put(newEvent.getId(), newEvent);
				updateEventInfos.put(newEvent.getId(),newEventInfo);
				updateEventStatus.add(newEventStatus);
				updateSolr.add(newEvent);	
				//add to localsimhash
				SimPair nsp = new SimPair();
				nsp.id = newEvent.getId();
				nsp.simhash = atSimHash;
				nsp.eventId = newEvent.getId();
				add2LocalSimHash(nsp);
			}	
			us.setStatus(Const.TaskId.CreateNewEvent.ordinal());
		}
		Date writeEnd = new Date();
		//update solr
		EventIndex upei = new EventIndex();
		upei.update(updateSolr);
		//update Event,EventInfo,EventStatus,UrlStatus
		
		Date updateStart = new Date();
		
		util.db.Hbn.updateDB(updateEvents.values());
		util.db.Hbn.updateDB(updateEventInfos.values());
		util.db.Hbn.updateDB(updateEventStatus);
		util.db.Hbn.updateDB(UStatus);
		
		//write simhash to disk
		//TODO shoudl append batch set to end of the file, but now for simple just write the whole map out...
		writeSimHash2Disk();
		Date updateEnd = new Date();		
		System.out.println("algorithm time " + (writeEnd.getTime() - start.getTime()));
		System.out.println("write disk time " + (updateStart.getTime() - writeEnd.getTime()));
		System.out.println("updatedb time " + (updateEnd.getTime() - updateStart.getTime()));
		System.out.println("event detector batch ok .. " + BatchSize);
		//finish for one batch...
	}
	
	
	public static void main(String[] args){
		EventDetector ed = new EventDetector();
		while(true){
			ed.runTask();
			try {
				System.out.println("now end of event detector ,sleep for:"+Const.EventDetectorSleepTime/1000/60+" minutes. "+new Date().toString());
				Thread.sleep(Const.EventDetectorSleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
