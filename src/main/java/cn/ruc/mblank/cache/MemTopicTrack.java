package cn.ruc.mblank.cache;

import cn.ruc.mblank.db.hbn.model.Event;
import cn.ruc.mblank.db.hbn.model.EventSim;
import cn.ruc.mblank.db.hbn.model.EventStatus;
import cn.ruc.mblank.db.hbn.model.Topic;
import cn.ruc.mblank.util.Const;
import cn.ruc.mblank.util.IOReader;
import cn.ruc.mblank.util.TimeUtil;
import cn.ruc.mblank.util.db.Hbn;
import org.ansj.domain.Term;
import org.ansj.recognition.NatureRecognition;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.hibernate.Session;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by mblank on 2014/5/6.
 */
public class MemTopicTrack implements Runnable{

    private String BaseDir = "data/";
    private TreeMap<Integer,Event> MemEvents = new TreeMap<Integer, Event>();
    private List<Integer> MemIds = new ArrayList<Integer>();

    private final double TotalDocNumber = 3000000;
    private String LocalTDFPath = BaseDir + "idf";
    private String LocalNameMapPath = BaseDir + "MemTopicIndex";
    private Map<String,Double> IDF = new HashMap<String,Double>();

    private static final int ThreadNum = 30;


    private double AvgIDF = 1.0;
//    private static int BatchSize = 2000;
//    private int MaxTopicId = 0;


    private List<EventSim> MemEventSims = new ArrayList<EventSim>();
    private Map<String,Map<String,TreeMap<Integer,List<Integer>>>> MemIndex = new HashMap<String, Map<String, TreeMap<Integer, List<Integer>>>>();

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
        for(int id : MemEvents.keySet()){
            MemIds.add(id);
        }
        br.close();
        System.out.println("memory events load ok..");
    }

    private void loadIndex() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(LocalNameMapPath));
        String line = "";
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            Map<String,TreeMap<Integer,List<Integer>>> twords = new HashMap<String, TreeMap<Integer, List<Integer>>>();
            int wn = Integer.parseInt(its[1]);
            //read words
            for(int i = 0 ; i < wn; ++i){
                line = br.readLine();
                if(line == null){
                    break;
                }
                String[] words = line.split("\t");
                TreeMap<Integer,List<Integer>> tdays = new TreeMap<Integer, List<Integer>>();
                int dn = Integer.parseInt(words[1]);
                for(int j = 0 ; j < dn; ++j){
                    line = br.readLine();
                    String[] ids = line.split("\t");
                    int day = Integer.parseInt(ids[0]);
                    String[] sids = ids[1].split(" ");
                    List<Integer> nids = new ArrayList<Integer>();
                    for(int k = 0; k< sids.length; ++k){
                        int id = Integer.parseInt(sids[k]);
                        nids.add(id);
                    }
                    tdays.put(day,nids);
                }
                twords.put(words[0],tdays);
            }
            MemIndex.put(its[0],twords);
        }
        System.out.println("Memory Index load ok....");
    }

    private Event findMostSimEvent(Event et){
        Map<Integer,String> candidates = new HashMap<Integer,String>();
        Map<Integer,String> can_person = new HashMap<Integer,String>();
        Event mostSimEvent = null;
        double maxSimScore = -2;
        List<Term> terms = ToAnalysis.parse(et.getTitle());
        new NatureRecognition(terms).recognition();
        HashSet<String> nchecks = new HashSet<String>();
        for(Term person : terms){
            if(person.getName().length() < 2 || person.getNatrue().natureStr.indexOf("nr") < 0 || nchecks.contains(person.getName())){
                continue;
            }
            nchecks.add(person.getName());
            HashSet<String> vchecks = new HashSet<String>();
            for(Term word : terms){
                if(word.getName().equals(person.getName()) || vchecks.contains(word.getName()) || (word.getNatrue().natureStr.indexOf("n") < 0 &&
                    word.getNatrue().natureStr.indexOf("v") < 0)) {
                    continue;
                }
                vchecks.add(word.getName());
                //find candidate from memory map...
                if(MemIndex.containsKey(person.getName()) && MemIndex.get(person.getName()).containsKey(word.getName())){
                    for(int day : MemIndex.get(person.getName()).get(word.getName()).keySet()){
                        if(day > et.getDay()){
                            break;
                        }
                        for(int id : MemIndex.get(person.getName()).get(word.getName()).get(day)){
                            if(id != et.getId()){
                                candidates.put(id,word.getName());
                                can_person.put(id,person.getName());
                            }
                        }
                    }
                }
            }
        }
        for(int id : candidates.keySet()){
            Event can = MemEvents.get(id);
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
            MemEventSims.add(es);
        }else{
            mostSimEvent = null;
        }
        return mostSimEvent;
    }

    private void runCluster() throws IOException, ParseException {
        loadEvents2Mem();
        loadIDF();
        loadIndex();


    }

    private void writeEventSim2Disk() throws IOException {
        System.out.println("start to write Event Sim to disk");
        //write memory event sims to disk
        BufferedWriter bw = new BufferedWriter(new FileWriter(BaseDir + "EventSims"));
        for(EventSim es : MemEventSims){
            bw.write(es.getFid() + "\t" + es.getSid() + "\t" + es.getScore() + "\t" + es.getMain() + "\t" + es.getObject() + "\n");
        }
        bw.close();
    }

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        MemTopicTrack mtt = new MemTopicTrack();
        mtt.loadEvents2Mem();
        mtt.loadIDF();
        mtt.loadIndex();
        List<Thread> joins = new ArrayList<Thread>();
        for(int i = 0 ; i < ThreadNum; ++i){
            Thread td = new Thread(mtt);
            td.setName("" + i);
            td.start();
            joins.add(td);
        }
        for(Thread td : joins){
            td.join();
        }
        mtt.writeEventSim2Disk();
    }

    @Override
    public void run() {
        int num = 0;
        int curThread = Integer.parseInt(Thread.currentThread().getName());
        int Batch = MemEvents.size() / ThreadNum;
        int tailNum = 0;
        if(curThread == ThreadNum - 1){
            tailNum = MemEvents.size() - Batch * ThreadNum;
        }
        int start = curThread * Batch;
        int end = start + Batch + tailNum;
        for(int i = start; i < end; ++i){
            Event curEvent = MemEvents.get(MemIds.get(i));
            findMostSimEvent(curEvent);
            num++;
            if(num % 1000 == 0){
                System.out.println(curThread + "\t" + num/(double)Batch);
            }
        }
    }
}
