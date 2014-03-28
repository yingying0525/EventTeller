package cn.ruc.mblank.core.topicTrace;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import cn.ruc.mblank.db.hbn.model.*;
import cn.ruc.mblank.core.infoGenerator.topic.TimeNumber;
import cn.ruc.mblank.db.hbn.HSession;
import cn.ruc.mblank.db.hbn.model.*;
import cn.ruc.mblank.util.Const;
import cn.ruc.mblank.util.db.Hbn;
import org.hibernate.Session;

public class TopicInfoUpdator {
	
	//should update Topic and TopicInfo
	//update TopicStatus
	
	private final int BatchSize = 3000;
	private List<TopicStatus> TStatus;
    private Session session;
	
	public TopicInfoUpdator(){
		TStatus = new ArrayList<TopicStatus>();
        session = HSession.getSession();
	}
	
	private void getInstances(){
		String sql = "from TopicStatus as obj where obj.status = " + Const.TaskId.TopicInfoToUpdate.ordinal();
		TStatus = Hbn.getElementsFromDB(sql,0, BatchSize,session);
	}
	
	public Date getLastDateOfTopic(List<Event> scrs){
		long dt = 0;
        Date today = new Date();
        Date res = null;
		for(Event et : scrs){
			if(et.getPubtime().getTime() > dt && et.getPubtime().getTime() < today.getTime()){
                dt = et.getPubtime().getTime();
                res = et.getPubtime();
            }
		}
		return res;
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
		List<String> keyWords = kw.getKeyWords(Const.MaxTopicKeyWordsSize);
        TimeNumber gtn = new TimeNumber(ets);
        String timeNumber = gtn.getTimeNumber();
		String kwstr = cn.ruc.mblank.util.StringUtil.ListToStr(keyWords,",");
		tp.setKeyWords(kwstr);
		tp.setEndTime(getLastDateOfTopic(ets));
		tp.setStartTime(getStartDateOfTopic(ets));
		tp.setNumber(ets.size());
        tp.setTimeNumber(timeNumber);
		//update topic info
		ti.setId(tp.getId());
		ti.setNumber(tp.getNumber());
		ti.setStartDay(cn.ruc.mblank.util.TimeUtil.getDayGMT8(tp.getStartTime()));
		ti.setEndDay(cn.ruc.mblank.util.TimeUtil.getDayGMT8(tp.getEndTime()));
	}
	
	
	public void runTask(){
        getInstances();
		List<Topic> updateTopics = new ArrayList<Topic>();
		List<TopicInfo> updateTopicInfos = new ArrayList<TopicInfo>();
		for(TopicStatus ts : TStatus){
			String sql = "from EventTopicRelation as obj where obj.tid = " + ts.getId();
			List<EventTopicRelation> etrs = Hbn.getElementsFromDB(sql,0, -1,session);
			Topic tp = Hbn.getElementFromDB(session,Topic.class,ts.getId());
			if(etrs == null || etrs.size() == 0){
				//some error....
				ts.setStatus((short)Const.TaskId.UpdatedTopicInfoFailed.ordinal());
				continue;
			}
            if(tp == null){
                tp = new Topic();
                tp.setId(ts.getId());
            }
			List<Event> events = new ArrayList<Event>();
			for(EventTopicRelation etr : etrs){
				Event et = Hbn.getElementFromDB(session,Event.class,etr.getEid());
				events.add(et);
			}
			TopicInfo ti = Hbn.getElementFromDB(session,TopicInfo.class,ts.getId());
			if(ti == null){
				ti = new TopicInfo();
			}
			updateTopic(tp,ti,events);
            session.saveOrUpdate(tp);
            session.saveOrUpdate(ti);
			ts.setStatus((short)Const.TaskId.UpdatedTopicInfoSuccess.ordinal());
		}
		Hbn.updateDB(session);
        session.clear();
	}
	
	public static void main(String[] args){
        TopicInfoUpdator tiu = new TopicInfoUpdator();
        while(true){
			tiu.runTask();
			if(tiu.TStatus.size() == 0){
				try {
					System.out.println("now end of update Topic Info,sleep for:"+Const.UpdateTopicInfoSleepTime /1000 /60 +" minutes. "+new Date().toString());
					Thread.sleep(Const.UpdateTopicInfoSleepTime /60);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}	
			}else{
                System.out.println("one batch ok.." + new Date());
            }
		}
	}
	
	

}
