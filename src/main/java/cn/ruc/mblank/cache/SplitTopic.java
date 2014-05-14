package cn.ruc.mblank.cache;

import cn.ruc.mblank.db.hbn.HSession;
import cn.ruc.mblank.db.hbn.model.EventTopicRelation;
import cn.ruc.mblank.db.hbn.model.Topic;
import cn.ruc.mblank.db.hbn.model.TopicInfo;
import cn.ruc.mblank.db.hbn.model.TopicStatus;
import cn.ruc.mblank.util.Const;
import org.hibernate.Session;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mblank on 14-4-9.
 */
public class SplitTopic {

    class pair{
        public String fid;
        public String sid;
    }
    private String BaseDir = "data/";

    private final String MapPath = BaseDir + "EventSims";
    private Map<String,List<pair>> MemoryMap = new HashMap<String,List<pair>>();
    private List<pair> Mains = new ArrayList<pair>();
    private List<List<String>> Classes = new ArrayList<List<String>>();

    private int MaxTopicId = 1;

    private Session session;

    public SplitTopic(){
        session = HSession.getSession();
    }

    private void loadMap() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(MapPath));
        String line = "";
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            if(its.length != 5){
                continue;
            }
            String key = its[3] + "\t" + its[4];
            pair pi = new pair();
            pi.fid = its[0];
            pi.sid = its[1];
            if(MemoryMap.containsKey(key)){
                MemoryMap.get(key).add(pi);
            }else{
                List<pair> ps = new ArrayList<pair>();
                ps.add(pi);
                MemoryMap.put(key,ps);
            }
        }
        br.close();
        System.out.println("load map ok.....");
    }

    private void getTopicFromMap(){
        for(String key : MemoryMap.keySet()){
            String[] its = key.split("\t");
            pair psi = new pair();
            psi.fid = its[0];
            psi.sid = its[1];
            int startClass = 0;
            List<pair> ps = MemoryMap.get(key);
            Map<String,Integer> clss = new HashMap<String, Integer>();
            for(pair pi : ps){
                if(clss.containsKey(pi.sid)){
                    clss.put(pi.fid,clss.get(pi.sid));
                }else{
                    clss.put(pi.fid,startClass);
                    clss.put(pi.sid,startClass);
                    startClass++;
                }
            }
            //get inverse list from classes
            List<List<String>> tmps = new ArrayList<List<String>>();
            for(int i = 0 ; i < startClass; ++i){
                List<String> tmp = new ArrayList<String>();
                tmps.add(tmp);
            }
            for(String id : clss.keySet()){
                tmps.get(clss.get(id)).add(id);
            }
            for(List<String> tmp : tmps){
                Classes.add(tmp);
                Mains.add(psi);
            }
        }
        System.out.println(Classes.size());
    }

    private void buildDB(){
        int index = 0;
        for(List<String> clss : Classes){
            for(String cls : clss){
                EventTopicRelation etr = new EventTopicRelation();
                etr.setEid(Integer.parseInt(cls));
                etr.setTid(MaxTopicId);
                session.saveOrUpdate(etr);
            }
            pair pi = Mains.get(index++);
            //update tp main object
            Topic tp = new Topic();
            tp.setId(MaxTopicId);
            tp.setMain(pi.fid);
            tp.setObject(pi.sid);
            session.saveOrUpdate(tp);
            //update topicinfo main object
            TopicInfo ti = new TopicInfo();
            ti.setId(MaxTopicId);
            ti.setMain(pi.fid);
            ti.setObject(pi.sid);
            session.saveOrUpdate(ti);

            TopicStatus ts = new TopicStatus();
            ts.setId(MaxTopicId);
            ts.setStatus((short) Const.TaskId.TopicInfoToUpdate.ordinal());
            session.saveOrUpdate(ts);
            MaxTopicId++;
            if(MaxTopicId % 1000 == 0){
                session.beginTransaction().commit();
                System.out.print("update batch ... ok ");
                System.out.println(MaxTopicId + "\t" + Classes.size());
            }
        }
        session.beginTransaction().commit();
        session.close();
        System.out.println("update db ok ....");
    }


    public static void main(String[] args) throws IOException {
        SplitTopic st = new SplitTopic();
        st.loadMap();
        st.getTopicFromMap();
//        st.buildDB();
    }





}
