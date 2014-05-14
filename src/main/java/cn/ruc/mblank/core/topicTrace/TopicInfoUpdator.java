package cn.ruc.mblank.core.topicTrace;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

//import cn.ruc.mblank.db.hbn.model.*;
import cn.ruc.mblank.core.infoGenerator.topic.KeyWords;
import cn.ruc.mblank.core.infoGenerator.topic.TimeNumber;
import cn.ruc.mblank.db.hbn.HSession;
import cn.ruc.mblank.db.hbn.model.*;
import cn.ruc.mblank.util.Const;
import cn.ruc.mblank.util.TimeUtil;
import cn.ruc.mblank.util.db.Hbn;
import org.hibernate.Session;

public class TopicInfoUpdator {
	
	//should update Topic and TopicInfo
	//update TopicStatus

    private String BaseDir = "data/";
	private final int BatchSize = 10000;
	private List<TopicStatus> TStatus;
    private Session session;

    private TreeMap<Integer,Event> MemEvents = new TreeMap<Integer, Event>();
	
	public TopicInfoUpdator(){
		TStatus = new ArrayList<TopicStatus>();
        session = HSession.getSession();
	}
	
	private void getInstances(){
		String sql = "from TopicStatus as obj where obj.status = " + Const.TaskId.TopicInfoToUpdate.ordinal();
		TStatus = Hbn.getElementsFromDB(sql,0, BatchSize,session);
	}

    private void loadEvents2Mem() throws IOException, ParseException {
        BufferedReader br = new BufferedReader(new FileReader(BaseDir + "pevents"));
        String line = "";
        int num = 0;
        while((line = br.readLine()) != null){
            //id,title,time,content,imgs,number,type
            String[] its = line.split("\t");
            Event et = new Event();
            et.setId(Integer.parseInt(its[0]));
            et.setTitle(its[1]);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            et.setPubTime(sdf.parse(its[2]));
            et.setContent(its[3]);
            et.setImgs(its[4]);
            et.setNumber(Integer.parseInt(its[5]));
            et.setTopic(Integer.parseInt(its[6]));
            et.setDay(TimeUtil.getDayGMT8(et.getPubTime()));
            MemEvents.put(et.getId(),et);
            num++;
            if(num % 10000 == 0){
                System.out.println(num);
            }
        }
        br.close();
        System.out.println("memory events load ok..");
    }

	public Date getLastDateOfTopic(List<Event> scrs){
		long dt = 0;
        Date today = new Date();
        Date res = null;
		for(Event et : scrs){
			if(et.getPubTime().getTime() > dt && et.getPubTime().getTime() < today.getTime()){
                dt = et.getPubTime().getTime();
                res = et.getPubTime();
            }
		}
		return res;
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
		KeyWords kw = new KeyWords(ets);
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
        int num = 0;
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
//				Event et = Hbn.getElementFromDB(session,Event.class,etr.getEid());
                Event et = MemEvents.get(etr.getEid());
                if(et == null){
                    System.out.println(etr.getEid());
                    continue;
                }
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
            num++;
            if(num % 100 == 0){
                System.out.println(num);
            }
		}
		Hbn.updateDB(session);
        session.clear();
	}

	public static void main(String[] args) throws IOException, ParseException {
        TopicInfoUpdator tiu = new TopicInfoUpdator();
        tiu.loadEvents2Mem();
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
