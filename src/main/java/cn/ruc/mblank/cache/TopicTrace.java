package cn.ruc.mblank.cache;

import cn.ruc.mblank.db.hbn.HSession;
import cn.ruc.mblank.db.hbn.model.Event;
import cn.ruc.mblank.db.hbn.model.EventSim;
import cn.ruc.mblank.db.hbn.model.EventStatus;
import cn.ruc.mblank.db.hbn.model.Topic;
import cn.ruc.mblank.util.Const;
import cn.ruc.mblank.util.IOReader;
import cn.ruc.mblank.util.db.Hbn;
import org.ansj.domain.Term;
import org.ansj.recognition.NatureRecognition;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.hibernate.Session;

import java.io.*;
import java.util.*;

/**
 * Created by mblank on 14-4-8.
 * Use the Word index from getElement.java, calculate the max-similar event for given event
 */
public class TopicTrace {

    private final double TotalDocNumber = 3000000;
    private String LocalTDFPath = "/data/idf";
    private String LocalNameMapPath = "/data/relations";
    private Map<String,Double> IDF = new HashMap<String,Double>();

    private double AvgIDF = 1.0;

    private static int BatchSize = 10000;
    private List<EventStatus> EStatus;
    private Session session;

    private int MaxTopicId = 0;

    private Map<String,Map<String,Map<Integer,List<Integer>>>> Index = new HashMap<String, Map<String, Map<Integer, List<Integer>>>>();


    public TopicTrace() throws IOException {
        IDF = new HashMap<String,Double>();
        EStatus = new ArrayList<EventStatus>();
        loadIDF();
        loadMap();
        session = HSession.getSession();
        MaxTopicId = Hbn.getMaxFromDB(session, Topic.class, "id");
    }

    private void getInstances(){
        String hql = "from EventStatus as obj where obj.status = " + Const.TaskId.UpdateDFSuccess.ordinal();
        EStatus = Hbn.getElementsFromDB(hql, 0, BatchSize, session);
    }

    private void loadMap() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(LocalNameMapPath));
        String line = "";
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            //p day type word id
            int day = 0;
            int id = 0;
            try{
                day = Integer.parseInt(its[1]);
                id = Integer.parseInt(its[4]);
            }catch(Exception e){

            }
            if(Index.containsKey(its[0])){
                if(Index.get(its[0]).containsKey(its[3])){
                    if(Index.get(its[0]).get(its[3]).containsKey(day)){
                        Index.get(its[0]).get(its[3]).get(day).add(id);
                    }else{
                        List<Integer> ids = new ArrayList<Integer>();
                        ids.add(id);
                        Index.get(its[0]).get(its[3]).put(day,ids);
                    }
                }else{
                    Map<Integer,List<Integer>> days = new HashMap<Integer, List<Integer>>();
                    List<Integer> ids = new ArrayList<Integer>();
                    ids.add(id);
                    days.put(day,ids);
                    Index.get(its[0]).put(its[3],days);
                }
            }else{
                Map<String,Map<Integer,List<Integer>>> tmps = new HashMap<String, Map<Integer, List<Integer>>>();
                Map<Integer,List<Integer>> days = new HashMap<Integer, List<Integer>>();
                List<Integer> ids = new ArrayList<Integer>();
                ids.add(id);
                days.put(day,ids);
                tmps.put(its[3],days);
                Index.put(its[0],tmps);
            }
        }
        br.close();
        System.out.println("init map ok...");
    }

    /**
     * load Idf to memory
     */
    private void loadIDF(){
        //load DF from df file
        try {
            IOReader reader = new IOReader(LocalTDFPath);
            String line = "";
            while((line = reader.readLine()) != null){
                String[] its = line.split("\t");
                if(its.length != 2){
                    continue;
                }
                double idf = Math.log(((double)(Integer.parseInt(its[1])) / (this.TotalDocNumber + 1.0)));
                this.AvgIDF += idf;
                IDF.put(its[0], idf);
            }
            reader.close();
            this.AvgIDF /= IDF.size();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("load idf ok...");
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

    private Set<String> getPerson(Event et){
        Set<String> res = new HashSet<String>();
        List<Term> terms = ToAnalysis.parse(et.getTitle());
        new NatureRecognition(terms).recognition(); //词性标注
        for(Term tm : terms){
            if(tm.getNatrue().natureStr.indexOf("nr") == 0 && tm.getName().length() > 1){
                res.add(tm.getName());
            }
        }
        return res;
    }

    private Set<String> getWords(Event et){
        Set<String> res = new HashSet<String>();
        List<Term> terms = ToAnalysis.parse(et.getTitle());
        new NatureRecognition(terms).recognition(); //词性标注
        for(Term tm : terms){
            if(tm.getNatrue().natureStr.indexOf("nr") == 0 || tm.getNatrue().natureStr.indexOf("ns") == 0 || tm.getNatrue().natureStr.indexOf("v") == 0 ||
                    tm.getNatrue().natureStr.indexOf("nt") == 0){
                if(tm.getName().length() > 1){
                    res.add(tm.getName());
                }
            }
        }
        return res;
    }


    private Event findMostSimEvent(Event et,Set<String> persons){
        Map<Integer,String> candidates = new HashMap<Integer,String>();
        Map<Integer,String> can_person = new HashMap<Integer,String>();
        Event mostSimEvent = null;
        double maxSimScore = -2;
        Set<String> words = getWords(et);
        for(String person : persons){
            for(String word : words){
                if(!person.equals(word)){
                    //find candidate from memory map...
                    if(Index.containsKey(person) && Index.get(person).containsKey(word)){
                        for(int day : Index.get(person).get(word).keySet()){
                            if(day > et.getDay()){
                                continue;
                            }else{
                                for(int id : Index.get(person).get(word).get(day)){
                                    if(id != et.getId()){
                                        candidates.put(id,word);
                                        can_person.put(id,person);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
//        System.out.println(et.getId() + "\t" + candidates.size());
        for(int id : candidates.keySet()){
            Event can = Hbn.getElementFromDB(session,Event.class,id);
            if(can == null || can.getPubTime().compareTo(et.getPubTime()) > 0){
                continue;
            }
            double simScore = cn.ruc.mblank.util.Similarity.similarityOfEvent(et, can, IDF, AvgIDF);
            if(simScore > maxSimScore){
                mostSimEvent = can;
                maxSimScore = simScore;
            }
        }
        if(maxSimScore > Const.MaxTopicSimNum){
            ///find sim ..
            //add to session
            EventSim es = new EventSim();
            es.setFid(et.getId());
            es.setSid(mostSimEvent.getId());
            es.setScore(maxSimScore);
            es.setMain(can_person.get(mostSimEvent.getId()));
            es.setObject(candidates.get(mostSimEvent.getId()));
            session.saveOrUpdate(es);
        }else{
            mostSimEvent = null;
        }
        return mostSimEvent;
    }

    private void runTask(){
        Date readDbStart = new Date();
        getInstances();
        Date readDbEnd = new Date();
        for(EventStatus es : EStatus){
            Event et = Hbn.getElementFromDB(session,Event.class,es.getId());
            Set<String> persons = getPerson(et);
            if(et == null || persons.size() == 0){
                es.setStatus((short)Const.TaskId.GenerateTopicFailed.ordinal());
                continue;
            }
            Event simEvent = findMostSimEvent(et,persons);
            if(simEvent == null){
                es.setStatus((short)Const.TaskId.GenerateTopicFailed.ordinal());
                continue;
            }
//            EventTopicRelation uetr = new EventTopicRelation();
//            uetr.setEid(et.getId());
//            if(simEvent == null){
//                //no sim find .. should be new Topic
//                //get max id from topic
//                Topic tp = createNewTopic(et);
//                session.saveOrUpdate(tp);
//                uetr.setTid(tp.getId());
//                TopicStatus ts = Hbn.getElementFromDB(session,TopicStatus.class,tp.getId());
//                if(ts == null){
//                    ts = new TopicStatus();
//                    ts.setId(tp.getId());
//                    session.saveOrUpdate(ts);
//                }
//                ts.setStatus((short)Const.TaskId.TopicInfoToUpdate.ordinal());
//            }else{
//                //found..update updateETRs
//                //get topic id;
//                EventTopicRelation etr = Hbn.getElementFromDB(session,EventTopicRelation.class,simEvent.getId());
//                if(etr == null){
//                    //some error....
//                    es.setStatus((short)Const.TaskId.GenerateTopicFailed.ordinal());
//                    continue;
//                }
//                uetr.setTid(etr.getTid());
//                TopicStatus ts = Hbn.getElementFromDB(session,TopicStatus.class,etr.getTid());
//                if(ts == null){
//                    ts = new TopicStatus();
//                    ts.setId(etr.getTid());
//                    session.saveOrUpdate(ts);
//                }
//                ts.setStatus((short) Const.TaskId.TopicInfoToUpdate.ordinal());
//            }
//            session.saveOrUpdate(uetr);
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

    public static void main(String[] args) throws IOException {
        TopicTrace ctt = new TopicTrace();
        while(true){
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
