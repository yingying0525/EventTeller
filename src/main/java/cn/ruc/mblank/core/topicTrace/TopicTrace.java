package cn.ruc.mblank.core.topicTrace;


import cn.ruc.mblank.db.hbn.HSession;
import cn.ruc.mblank.db.hbn.model.*;
import cn.ruc.mblank.index.solr.EventIndex;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.ruc.mblank.config.JsonConfigModel;
import cn.ruc.mblank.util.Const;
import cn.ruc.mblank.util.IOReader;
import cn.ruc.mblank.util.db.Hbn;
import org.hibernate.Session;

public class TopicTrace {
	
	
	private static int BatchSize = 10000;
	private List<EventStatus> EStatus;
	private Session session;

    private long TotalDocumentCount = 0;
	private double AvgWordIDF = 1.0;
	private Map<String,Double> IDF;
	private int MaxTopicId = 0;
	private String LocalTDFPath;
	private String LocalDDNPath;


	
	public TopicTrace(){
		JsonConfigModel jcm = JsonConfigModel.getConfig();		
		LocalTDFPath = jcm.LocalTDFPath;
		LocalDDNPath = jcm.LocalDDNPath;
		IDF = new HashMap<String,Double>();
		EStatus = new ArrayList<EventStatus>();
		loadIDF();
        session = HSession.getSession();
        MaxTopicId = Hbn.getMaxFromDB(session, Topic.class, "id");
	}
	
	private void getInstances(){
		String hql = "from EventStatus as obj where obj.status = " + Const.TaskId.UpdateDFSuccess.ordinal();
		EStatus = Hbn.getElementsFromDB(hql,0,BatchSize,session);
	}
	
	/**
	 * load Idf to memory
	 */
	private void loadIDF(){
		//load total document count from ddn
		try {
			this.TotalDocumentCount = 0;
			IOReader reader = new IOReader(LocalDDNPath);
			String line = "";
			while((line = reader.readLine()) != null){
				String[] its = line.split("\t");
				if(its.length != 2){
					continue;
				}
				this.TotalDocumentCount += Integer.parseInt(its[1]);
			}
			reader.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//load DF from df file
		try {
			IOReader reader = new IOReader(LocalTDFPath);
			String line = "";
			while((line = reader.readLine()) != null){
				String[] its = line.split("\t");
				if(its.length != 2){
					continue;
				}
				double idf = Math.log(((double)(Integer.parseInt(its[1])) / (this.TotalDocumentCount + 1.0)));
				this.AvgWordIDF += idf;
				IDF.put(its[0], idf);
			}
			reader.close();
			this.AvgWordIDF /= IDF.size();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String titleToSearchString(String title){
		StringBuffer result = new StringBuffer("");
		List<String> words = cn.ruc.mblank.util.ChineseSplit.SplitStr(title);
		for(String word : words){
			if(word.length() >= 1){
				result.append(" " + word);
			}
		}
		return result.toString();
	}

	private Event findMostSimEvent(Event scr){
		Event mostSimEvent = null;
		double maxSimScore = -2;
		EventIndex ei = new EventIndex();
		List<Event> ets =  ei.queryEvents("et_title:" + titleToSearchString(scr.getTitle()), 0, Const.MaxNeighborEventCount, null, "");
		for(Event candidate : ets){
			if(candidate == null || candidate.getPubTime().compareTo(scr.getPubTime()) > 0 || candidate.getId() == scr.getId()){
				continue;
			}
            double simScore = cn.ruc.mblank.util.Similarity.similarityOfEvent(scr, candidate, IDF, AvgWordIDF);
            if(simScore > maxSimScore){
                mostSimEvent = candidate;
                maxSimScore = simScore;
            }
        }
		if(maxSimScore > Const.MaxTopicSimNum){
			///find sim ..
			//add to session
			EventSim es = new EventSim();
			es.setFid(scr.getId());
			es.setSid(mostSimEvent.getId());
			es.setScore(maxSimScore);
			session.saveOrUpdate(es);
		}else{
			mostSimEvent = null;
		}
		return mostSimEvent;
	}
	
	
	private Topic createNewTopic(Event et){
		Topic res = new Topic();
		res.setId(++MaxTopicId);
		res.setEndTime(et.getPubTime());
		res.setStartTime(et.getPubTime());
		res.setNumber(1);
		res.setSummary(et.getContent().substring(0,Math.min(10000,et.getContent().length())));
		res.setKeyWords(et.getTitle());
        res.setTimeNumber("");
		return res;
	}
	
	private void runTask(){
		Date readDbStart = new Date();
		getInstances();
		Date readDbEnd = new Date();
		for(EventStatus es : EStatus){
			Event et = Hbn.getElementFromDB(session,Event.class,es.getId());
			if(et == null){
				es.setStatus((short)Const.TaskId.GenerateTopicFailed.ordinal());
				continue;
			}
			Event simEvent = findMostSimEvent(et);
			EventTopicRelation uetr = new EventTopicRelation();
			uetr.setEid(et.getId());
			if(simEvent == null){
				//no sim find .. should be new Topic
				//get max id from topic
				Topic tp = createNewTopic(et);
				session.saveOrUpdate(tp);
				uetr.setTid(tp.getId());
				TopicStatus ts = Hbn.getElementFromDB(session,TopicStatus.class,tp.getId());
                if(ts == null){
                    ts = new TopicStatus();
                    ts.setId(tp.getId());
                    session.saveOrUpdate(ts);
                }
				ts.setStatus((short)Const.TaskId.TopicInfoToUpdate.ordinal());
			}else{
				//found..update updateETRs
				//get topic id;
				EventTopicRelation etr = Hbn.getElementFromDB(session,EventTopicRelation.class,simEvent.getId());
				if(etr == null){
					//some error....
                    es.setStatus((short)Const.TaskId.GenerateTopicFailed.ordinal());
					continue;
				}
				uetr.setTid(etr.getTid());
				TopicStatus ts = Hbn.getElementFromDB(session,TopicStatus.class,etr.getTid());
                if(ts == null){
                    ts = new TopicStatus();
                    ts.setId(etr.getTid());
                    session.saveOrUpdate(ts);
                }
				ts.setStatus((short) Const.TaskId.TopicInfoToUpdate.ordinal());
			}
			session.saveOrUpdate(uetr);
			es.setStatus((short)Const.TaskId.GenerateTopicSuccess.ordinal());
		}
		Date algoEnd = new Date();
		//update event-topic table
        Hbn.updateDB(session);
        session.clear();
		Date updateDBEnd = new Date();
		System.out.println("read db time spent.. " + (readDbEnd.getTime() - readDbStart.getTime()) / 1000);
		System.out.println("algorithm time spent.. " + (algoEnd.getTime() - readDbEnd.getTime()) / 1000);
		System.out.println("update db time spent .. " + (updateDBEnd.getTime() - algoEnd.getTime()) / 1000 );		
	}
	
	
	public static void main(String[] args){
		while(true){
			TopicTrace ctt = new TopicTrace();
			ctt.runTask();
			System.out.println("one batch ok for Topic Tracing.." + new Date() + "\t" +  ctt.EStatus.size());
			if(ctt.EStatus.size() == 0 ){
				try {
					System.out.println("now end of one cluster,sleep for:"+Const.ClusterToTopicSleepTime /1000 /60 +" minutes. "+new Date().toString());
					Thread.sleep(Const.ClusterToTopicSleepTime /60);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}			
			}
		}
	}
	
}
