package core.topicTrace;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import util.Const;
import db.hbn.model.Event;
import db.hbn.model.EventTopicRelation;
import db.hbn.model.Topic;
import db.hbn.model.TopicInfo;
import db.hbn.model.TopicStatus;

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
		TStatus = util.db.Hbn.getElementsFromDB(sql, BatchSize);
	}
	
	public Date getLastDateOfTopic(List<Event> scrs){
		Date dt = new Date();
		if(scrs.size() > 0){
			dt = scrs.get(0).getPubTime();
		}else{
			return dt;
		}
		for(Event et : scrs){
			if(dt.compareTo(et.getPubTime()) < 0){
				dt = et.getPubTime();
			}
		}
		return dt;
	}
	
	public Date getStartDateOfTopic(List<Event> scrs){
		Date dt = new Date();
		if(scrs.size() > 0){
			dt = scrs.get(0).getPubTime();
		}else{
			return dt;
		}
		for(Event et : scrs){
			if(dt.compareTo(et.getPubTime()) > 0){
				dt = et.getPubTime();
			}
		}
		return dt;
	}
	
	private void updateTopic(Topic tp,TopicInfo ti,List<Event> ets){
		core.infoGenerator.topic.KeyWords kw = new core.infoGenerator.topic.KeyWords(ets);
		List<String> keyWords = kw.getKeyWords(KeyWordSize);
		String kwstr = util.StringUtil.ListToStr(keyWords);
		tp.setKeyWords(kwstr);
		tp.setEndTime(getLastDateOfTopic(ets));
		tp.setStartTime(getStartDateOfTopic(ets));
		tp.setNumber(ets.size());
		//update topic info
		ti.setId(tp.getId());
		ti.setNumber(tp.getNumber());
		ti.setStartDay(util.TimeUtil.getDayGMT8(tp.getStartTime()));
		ti.setEndDay(util.TimeUtil.getDayGMT8(tp.getEndTime()));
	}
	
	
	public void runTask(){
		List<Topic> updateTopics = new ArrayList<Topic>();
		List<TopicInfo> updateTopicInfos = new ArrayList<TopicInfo>();
		for(TopicStatus ts : TStatus){
			String sql = "from EventTopicRelation as obj where obj.tid = " + ts.getId();
			List<EventTopicRelation> etrs = util.db.Hbn.getElementsFromDB(sql, -1);
			sql = "from Topic as obj where obj.id = " + ts.getId();
			Topic tp = util.db.Hbn.getElementFromDB(sql);
			if(etrs == null || etrs.size() == 0 || tp == null){
				//some error....
				ts.setStatus(Const.TaskId.UpdatedTopicInfoFailed.ordinal());
				continue;
			}
			List<Event> events = new ArrayList<Event>();
			for(EventTopicRelation etr : etrs){
				String esql = "from Event as obj where obj.id = " + etr.getEid();
				Event et = util.db.Hbn.getElementFromDB(esql);
				events.add(et);
			}
			sql = "from TopicInfo as obj where obj.id = " + ts.getId();
			TopicInfo ti = util.db.Hbn.getElementFromDB(sql);
			if(ti == null){
				ti = new TopicInfo();
			}
			updateTopic(tp,ti,events);
			updateTopics.add(tp);
			updateTopicInfos.add(ti);
			ts.setStatus(Const.TaskId.UpdatedTopicInfoSuccess.ordinal());
		}
		util.db.Hbn.updateDB(updateTopics);
		util.db.Hbn.updateDB(updateTopicInfos);
		util.db.Hbn.updateDB(TStatus);
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
