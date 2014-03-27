package cn.ruc.mblank.core.duplicateRemoval;

import cn.ruc.mblank.db.hbn.HSession;
import cn.ruc.mblank.db.hbn.model.*;
import cn.ruc.mblank.index.solr.EventIndex;

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


import java.util.Set;

import cn.ruc.mblank.util.Const;
import cn.ruc.mblank.util.Log;


import cn.ruc.mblank.config.JsonConfigModel;
import cn.ruc.mblank.extractor.article.Extractor;
import cn.ruc.mblank.util.db.Hbn;
import org.hibernate.Session;

public class EventDetector {
	
	
	private class SimPair{
		public int id;
		public long simhash;
		public int eventId;
	}
	
	private int BatchSize = 2000;
	private List<UrlStatus> UStatus;
    private Session Session;
	
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
        Session = HSession.getSession();
	}
	
	private void initSimHashIndex(){
		LocalSimHash = new ArrayList<HashMap<Integer,List<SimPair>>>();
		for(int i = 0 ; i < Const.SimHashIndexNumber; i++){
			HashMap<Integer,List<SimPair>> simhash = new HashMap<Integer,List<SimPair>>();
			LocalSimHash.add(simhash);
		}
	}
	
	private void add2LocalSimHash(SimPair sp){
		byte[] bts = cn.ruc.mblank.util.Coding.long2Byte(sp.simhash);
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
		UStatus = Hbn.getElementsFromDB(hql,0,BatchSize,Session);
	}
	
	private SimPair findSameInIndex(long hash, int oid){
		int MinDistance = 65;
		SimPair SameId = null;
		byte[] bts = cn.ruc.mblank.util.Coding.long2Byte(hash);
		for(int i = 0 ; i < Const.SimHashIndexNumber; i++ ){
			if(LocalSimHash.get(i).containsKey((int)bts[i])){
				//cal the disstance
				List<SimPair> ids = LocalSimHash.get(i).get((int)bts[i]);
				for(SimPair id : ids){
					int bits = cn.ruc.mblank.util.Coding.diffBitsOfNums(hash,id.simhash);
					if(bits < MinDistance && id.id != oid){
						MinDistance = bits;
						SameId = id;
					}
				}
			}
		}
		if(MinDistance <= cn.ruc.mblank.util.Const.MinSimHashBitNumber){
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
		res.setPubtime(at.getPublishtime());
		res.setSource(at.getSource());
		res.setContent(at.getContent());
		res.setImgs(at.getImgs());
		res.setNumber(1);
		res.setDay(cn.ruc.mblank.util.TimeUtil.getDayGMT8(at.getPublishtime()));
		if(res.getDay() <= 0){
			res.setDay(0);
		}
		return res;
	}
	
	public EventInfo createEventInfo(Article at){
		EventInfo res = new EventInfo();
		res.setId(at.getId());
		res.setDay((cn.ruc.mblank.util.TimeUtil.getDayGMT8(at.getPublishtime())));
		res.setNumber(1);
		if(res.getDay() <= 0){
			res.setDay(0);
		}
		return res;
	}
	
	private EventStatus createEventStatus(Article at){
		EventStatus res = new EventStatus();
		res.setId(at.getId());
		res.setStatus((short)Const.TaskId.CreateNewEvent.ordinal());
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
	
	public int runTask(){
		Date start = new Date();
		//get batchsize instances first.
		getInstances();
		Set<Event> updateSolr = new HashSet<Event>();
		//for each url , get it's filepath and parse the html to article;
		for(UrlStatus us : UStatus){
			String folder = HtmlPath + cn.ruc.mblank.util.TimeUtil.getDateStr(us.getTime()) + "/" + us.getId();
			File file = new File(folder);
			if(!file.exists()){
				us.setStatus((short)Const.TaskId.CantFindHtmlInDisk.ordinal());
				continue;
			}
			//get Url from db, the extractor need Url.url to recognition html model
			//TODO can optimize
			Url url = Hbn.getElementFromDB(Session,Url.class,us.getId());
			//get article from Url.url and html source
			Extractor etor = new Extractor(url,file);
			Article at = etor.getArticle();
			if(!checkArticle(at) || checkMessyCode(at.getTitle())){
				us.setStatus((short)Const.TaskId.ParseHtmlFailed.ordinal());
				continue;
			}
			//get simhash of this article
			long atSimHash = cn.ruc.mblank.util.hash.SimHash.getSimHash(at.getTitle() + at.getContent());
			if(atSimHash == 0){
				us.setStatus((short)Const.TaskId.ParseHtmlFailed.ordinal());
				continue;
			}
			SimPair mostSame = findSameInIndex(atSimHash,url.getId());
			if(mostSame != null){
				//find same ..
				Event oldEvent;
				oldEvent = Hbn.getElementFromDB(Session,Event.class,mostSame.eventId);
				if(oldEvent == null){
					us.setStatus((short)Const.TaskId.ParseHtmlFailed.ordinal());
					continue;
				}
				int day = oldEvent.getDay();
				if(at.getImgs() != null && at.getImgs().length() > 0){
					oldEvent.setImgs(oldEvent.getImgs() + " " + at.getImgs());
				}
				if(oldEvent.getPubtime().compareTo(at.getPublishtime()) > 0){
					oldEvent.setPubtime(at.getPublishtime());
					day = cn.ruc.mblank.util.TimeUtil.getDayGMT8(at.getPublishtime());
				}
				if(day > 100000 || day <= 0){
					System.out.println("error " + day + "\t" + us.getId());
					us.setStatus((short)Const.TaskId.ParseHtmlFailed.ordinal());
					continue;
				}
				oldEvent.setDay(day);
				oldEvent.setNumber(oldEvent.getNumber() + 1);
				updateSolr.add(oldEvent);
				//update EventInfo.number
				EventInfo ei;
				ei = Hbn.getElementFromDB(Session,EventInfo.class,oldEvent.getId());
				if(ei == null){
					us.setStatus((short)Const.TaskId.ParseHtmlFailed.ordinal());
					continue;
				}
				ei.setDay(day);
				ei.setTopic(us.getTopic());
				ei.setNumber(ei.getNumber() + 1);
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
				//add to session
                Session.saveOrUpdate(newEvent);
                Session.saveOrUpdate(newEventInfo);
                Session.saveOrUpdate(newEventStatus);
				updateSolr.add(newEvent);
				//add to localsimhash
				SimPair nsp = new SimPair();
				nsp.id = newEvent.getId();
				nsp.simhash = atSimHash;
				nsp.eventId = newEvent.getId();
				add2LocalSimHash(nsp);
			}	
			us.setStatus((short)Const.TaskId.CreateNewEvent.ordinal());
		}
		Date writeEnd = new Date();
		//update solr
		EventIndex upei = new EventIndex();
		upei.update(updateSolr);
		//update Event,EventInfo,EventStatus,UrlStatus
		Date updateStart = new Date();
        Hbn.updateDB(Session);
        Session.clear();
		//write simhash to disk
		//TODO should append batch set to end of the file, but now for simple just write the whole map out...
		writeSimHash2Disk();
		Date updateEnd = new Date();		
		System.out.println("algorithm time " + (writeEnd.getTime() - start.getTime()));
		System.out.println("write disk time " + (updateStart.getTime() - writeEnd.getTime()));
		System.out.println("updatedb time " + (updateEnd.getTime() - updateStart.getTime()));
		System.out.println("event detector batch ok .. " + BatchSize);
		//finish for one batch...
		return UStatus.size();
	}
	
	
	public static void main(String[] args){
        EventDetector ed = new EventDetector();
        while(true){
			int num = ed.runTask();
			if(num == 0){
				try {
					System.out.println("now end of event detector ,sleep for:"+Const.EventDetectorSleepTime/1000/60+" minutes. "+new Date().toString());
					Thread.sleep(Const.EventDetectorSleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
