package cn.ruc.mblank.core.topicTrace;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import cn.ruc.mblank.db.hbn.model.*;
import cn.ruc.mblank.db.hbn.model.*;
import cn.ruc.mblank.util.Const;
import cn.ruc.mblank.util.db.Hbn;

public class TopicInfoUpdator {
	
	//should update Topic and TopicInfo
	//update TopicStatus
	
	private final int BatchSize = 2000;
	private int KeyWordSize = 10;
	private List<TopicStatus> TStatus;
	
	public TopicInfoUpdator(){
		TStatus = new ArrayList<TopicStatus>();
		getInstances();
	}
	
	private void getInstances(){
		String sql = "from TopicStatus as obj where obj.status = " + Const.TaskId.TopicInfoToUpdate.ordinal();
        Hbn db = new Hbn();
		TStatus = db.getElementsFromDB(sql,-1, BatchSize);
	}
	
	public Date getLastDateOfTopic(List<Event> scrs){
		Date dt = new Date();
		if(scrs.size() > 0){
			dt = scrs.get(0).getPubtime();
		}else{
			return dt;
		}
		for(Event et : scrs){
			if(dt.compareTo(et.getPubtime()) < 0){
				dt = et.getPubtime();
			}
		}
		return dt;
	}
	
	public Date getStartDateOfTopic(List<Event> scrs){
		Date dt = new Date();
		if(scrs.size() > 0){
			dt = scrs.get(0).getPubtime();
		}else{
			return dt;
		}
		for(Event et : scrs){
			if(dt.compareTo(et.getPubtime()) > 0){
				dt = et.getPubtime();
			}
		}
		return dt;
	}
	
	private void updateTopic(Topic tp,TopicInfo ti,List<Event> ets){
		cn.ruc.mblank.core.infoGenerator.topic.KeyWords kw = new cn.ruc.mblank.core.infoGenerator.topic.KeyWords(ets);
		List<String> keyWords = kw.getKeyWords(KeyWordSize);
		String kwstr = cn.ruc.mblank.util.StringUtil.ListToStr(keyWords);
		tp.setKeyWords(kwstr);
		tp.setEndTime(getLastDateOfTopic(ets));
		tp.setStartTime(getStartDateOfTopic(ets));
		tp.setNumber(ets.size());
		//update topic info
		ti.setId(tp.getId());
		ti.setNumber(tp.getNumber());
		ti.setStartDay(cn.ruc.mblank.util.TimeUtil.getDayGMT8(tp.getStartTime()));
		ti.setEndDay(cn.ruc.mblank.util.TimeUtil.getDayGMT8(tp.getEndTime()));
	}
	
	
	public void runTask(){
        Hbn db = new Hbn();
		List<Topic> updateTopics = new ArrayList<Topic>();
		List<TopicInfo> updateTopicInfos = new ArrayList<TopicInfo>();
		for(TopicStatus ts : TStatus){
			String sql = "from EventTopicRelation as obj where obj.tid = " + ts.getId();
			List<EventTopicRelation> etrs = db.getElementsFromDB(sql,-1, -1);
			sql = "from Topic as obj where obj.id = " + ts.getId();
			Topic tp = db.getElementFromDB(sql);
			if(etrs == null || etrs.size() == 0 || tp == null){
				//some error....
				ts.setStatus((short)Const.TaskId.UpdatedTopicInfoFailed.ordinal());
				continue;
			}
			List<Event> events = new ArrayList<Event>();
			for(EventTopicRelation etr : etrs){
				String esql = "from Event as obj where obj.id = " + etr.getEid();
				Event et = db.getElementFromDB(esql);
				events.add(et);
			}
			sql = "from TopicInfo as obj where obj.id = " + ts.getId();
			TopicInfo ti = db.getElementFromDB(sql);
			if(ti == null){
				ti = new TopicInfo();
			}
			updateTopic(tp,ti,events);
			updateTopics.add(tp);
			updateTopicInfos.add(ti);
			ts.setStatus((short)Const.TaskId.UpdatedTopicInfoSuccess.ordinal());
		}
		db.updateDB(updateTopics);
		db.updateDB(updateTopicInfos);
		db.updateDB(TStatus);
	}
	
	public static void main(String[] args){
		while(true){
			int num = 0;
			TopicInfoUpdator tiu = new TopicInfoUpdator();
			num = tiu.TStatus.size();
			tiu.runTask();
			System.out.println("one batch ok.." + new Date());
			if(num == 0){
				try {
					System.out.println("now end of update Topic Info,sleep for:"+Const.UpdateTopicInfoSleepTime /1000 /60 +" minutes. "+new Date().toString());
					Thread.sleep(Const.UpdateTopicInfoSleepTime /60);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}	
			}
		}
	}
	
	

}
