package core.duplicateRemoval;

//import java.util.ArrayList;
//import index.solr.EventIndex;

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
import java.util.List;

import util.Const;
import util.Log;

import com.alibaba.fastjson.JSON;

import config.JsonConfigModel;
import config.LocalJsonConfigReader;
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
//	private HashMap<Integer,Integer> TdfMap;
//	private HashMap<String,HashMap<Integer,Integer>> DfMap;
	
	private String HtmlPath;
	private String LocalSimHashPath;
//	private String LocalDFPath;
//	private String LocalTDFPath;
	
	public EventDetector(){
		//get save path from the Config xml
		Log.getLogger().info("Start HtmlDownloader!");
		//read Bloom filter file path from json config file
		String fileContent = LocalJsonConfigReader.readJsonFile(Const.SYS_JSON_CONFIG_PATH);
		JsonConfigModel jcm = JSON.parseObject(fileContent,JsonConfigModel.class);
		HtmlPath = jcm.HtmlSavePath;
		LocalSimHashPath = jcm.SimHashPath;
//		LocalDFPath = jcm.LocalDFPath;
//		LocalTDFPath = jcm.LocalTDFPath;
		initSimHashIndex();
		loadLocalSimHash();
//		loadDF();
	}
	
	private void initSimHashIndex(){
		LocalSimHash = new ArrayList<HashMap<Integer,List<SimPair>>>();
		for(int i = 0 ; i < Const.SimHashIndexNumber; i++){
			HashMap<Integer,List<SimPair>> simhash = new HashMap<Integer,List<SimPair>>();
			LocalSimHash.add(simhash);
		}
	}
	
	private void add2LocalSimHash(SimPair sp){
		byte[] bts = util.Util.long2Byte(sp.simhash);
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
	
//	private void loadDF(){
//		//should load two file, one for word-df
//		//another for day-df
//		TdfMap = new HashMap<Integer,Integer>();
//		DfMap = new HashMap<String,HashMap<Integer,Integer>>();
//		//load tdf file
//		String line;
//		try {
//			BufferedReader br = new BufferedReader(new FileReader(LocalTDFPath));
//			while((line = br.readLine()) != null){
//				String[] its = line.split(" ");
//				TdfMap.put(Integer.parseInt(its[0]), Integer.parseInt(its[1]));
//			}
//			br.close();
//		} catch (IOException e) {
//			System.out.println("can't load local TDF file.");
//		}
//		//load df file
//		try{
//			BufferedReader dbr = new BufferedReader(new FileReader(LocalDFPath));
//			while((line = dbr.readLine()) != null){
//				String[] its = line.split("\t");
//				if(its.length < 2){
//					continue;
//				}
//				HashMap<Integer,Integer> dayNumbers = new HashMap<Integer,Integer>();
//				for(int i = 1; i < its.length; i++){
//					String[] sub = its[i].split(" ");
//					if(sub.length != 2){
//						continue;
//					}
//					dayNumbers.put(Integer.parseInt(sub[0]), Integer.parseInt(sub[1]));
//				}
//				DfMap.put(its[0], dayNumbers);
//			}
//			dbr.close();
//		}catch(Exception e){
//			System.out.println("can't find local DF file");
//		}
//	}
	
	
	
	private void getInstances(){
		String hql = "from UrlStatus as obj where obj.status = 1";
		UStatus = util.Util.getElementsFromDB(hql,BatchSize);
	}
	
	private SimPair findSameInIndex(long hash, int oid){
		int MinDistance = 65;
		SimPair SameId = null;
		byte[] bts = util.Util.long2Byte(hash);
		for(int i = 0 ; i < Const.SimHashIndexNumber; i++ ){
			if(LocalSimHash.get(i).containsKey((int)bts[i])){
				//cal the disstance
				List<SimPair> ids = LocalSimHash.get(i).get((int)bts[i]);
				for(SimPair id : ids){
					int bits = util.Util.diffBitsOfNums(hash,id.simhash);
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
		return res;
	}
	
	public EventInfo createEventInfo(Article at){
		EventInfo res = new EventInfo();
		res.setId(at.getId());
		res.setDay((int)(at.getPublishtime().getTime() / 1000 / 60 / 60 / 24));
		res.setNumber(1);
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
	
	public void runTask(){
		Date start = new Date();
		//get batchsize instances first.
		getInstances();
		List<Event> updateSolr = new ArrayList<Event>();
		HashMap<Integer,Event> updateEvents = new HashMap<Integer,Event>();
		HashMap<Integer,EventInfo> updateEventInfos = new HashMap<Integer,EventInfo>();
		List<EventStatus> updateEventStatus = new ArrayList<EventStatus>();
		//for each url , get it's filepath and parse the html to article;
		for(UrlStatus us : UStatus){
			String folder = HtmlPath + util.Util.getDateStr(us.getTime()) + "/" + us.getId();
			File file = new File(folder);
			if(!file.exists()){
				us.setStatus(Const.TaskId.CantFindHtmlInDisk.ordinal());
				continue;
			}
			//get Url from db, the extractor need Url.url to recognition html model
			//TODO can optimize
			String getUrl = "from Url as obj where obj.id = " + us.getId();
			Url url = util.Util.getElementFromDB(getUrl);
			StringBuffer html = util.FileUtil.readAll(folder);
			//get article from Url.url and html source
			Extractor etor = new Extractor(url,html.toString());
			Article at = etor.getArticle();
			if(!checkArticle(at)){
				us.setStatus(Const.TaskId.ParseHtmlFailed.ordinal());
				continue;
			}
			//get simhash of this article
			long atSimHash = util.SimHash.getSimHash(at.getTitle() + at.getContent());
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
					String ehql = "from Event as obj where obj.id = " + mostSame.eventId;
					oldEvent = util.Util.getElementFromDB(ehql);
				}
				//get Event from db and update imgs,pubtime

				if(oldEvent == null){
					//TODO can't find in db and memory..some error occured.
					continue;
				}
				if(at.getImgs() != null && at.getImgs().length() > 0){
					oldEvent.setImgs(oldEvent.getImgs() + "!##!" + at.getImgs());
				}
				if(oldEvent.getPubTime().compareTo(at.getPublishtime()) > 0){
					oldEvent.setPubTime(at.getPublishtime());
				}
				//update EventInfo.number
				EventInfo ei = null;
				//first find in memory..then try to read from db.
				if(updateEventInfos.containsKey(mostSame.eventId)){
					ei = updateEventInfos.get(mostSame.eventId);
				}else{
					String ehql = "from EventInfo as obj where obj.id = " + oldEvent.getId();
					ei = util.Util.getElementFromDB(ehql);
				}
				if(ei == null){
					//TODO some error...
					continue;
				}
				if(oldEvent.getPubTime().getTime() / 1000 / 60 / 60 / 24 > 1000000){
					System.out.println(oldEvent.getId() + "\t" + oldEvent.getPubTime());
				}
				ei.setDay((int)(oldEvent.getPubTime().getTime() / 1000 / 60 / 60 / 24));
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
				EventInfo newEventInfo = createEventInfo(at);
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
		//write simhash to disk
		//TODO shoudl append batch set to end of the file, but now for simple just write the whole map out...
		writeSimHash2Disk();
		//update solr
//		EventIndex upei = new EventIndex();
//		upei.update(updateSolr);
		//update Event,EventInfo,EventStatus,UrlStatus
		
		Date updateStart = new Date();
		
		util.Util.updateDB(updateEvents.values());
		util.Util.updateDB(updateEventInfos.values());
		util.Util.updateDB(updateEventStatus);
		util.Util.updateDB(UStatus);
		
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
