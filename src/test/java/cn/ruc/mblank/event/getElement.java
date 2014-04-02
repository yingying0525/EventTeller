package cn.ruc.mblank.event;

import cn.ruc.mblank.core.infoGenerator.model.Word;
import cn.ruc.mblank.db.hbn.HSession;
import cn.ruc.mblank.db.hbn.model.Event;
import cn.ruc.mblank.util.ChineseSplit;
import cn.ruc.mblank.util.Const;
import cn.ruc.mblank.util.IOReader;
import cn.ruc.mblank.util.db.Hbn;
import org.hibernate.Session;

import java.io.*;
import java.util.*;

/**
 * Created by mblank on 14-4-1.
 */
public class getElement {

    /**
     * map structure:
     * word,day,nature,word,id
     */
    private Map<String,TreeMap<Integer,Map<String,Map<String,Set<Integer>>>>> Persons = new HashMap<String,TreeMap<Integer,Map<String,Map<String,Set<Integer>>>>>();


    private String OutPath = "e:\\share\\persons";


    private void updateMap(List<Word> words,int day,int id){
        Set<Word> persons = new HashSet<Word>();
        for(Word wd : words){
            if(wd.getNature().contains("nr")){
                //person ...
                persons.add(wd);
            }
        }
        for(Word ps : persons){
            TreeMap<Integer,Map<String,Map<String,Set<Integer>>>> tmps = null;
            if(!Persons.containsKey(ps.getName())){
                tmps = new TreeMap<Integer, Map<String, Map<String, Set<Integer>>>>();
                Persons.put(ps.getName(),tmps);
            }else{
                tmps = Persons.get(ps.getName());
            }
            for(Word wd : words){
                if(wd.equals(ps)){
                    continue;
                }
                Map<String,Map<String,Set<Integer>>> natrues = null;
                if(tmps.containsKey(day)){
                    natrues = tmps.get(day);
                }else{
                    natrues = new HashMap<String, Map<String, Set<Integer>>>();
                    tmps.put(day,natrues);
                }
                Map<String,Set<Integer>> wds;
                if(natrues.containsKey(wd.getNature())){
                    wds = natrues.get(wd.getNature());
                }else{
                    wds = new HashMap<String,Set<Integer>>();
                    natrues.put(wd.getNature(),wds);
                }
                if(wds.containsKey(wd.getName())){
                    wds.get(wd.getName()).add(id);

                }else{
                    Set<Integer> ids = new HashSet<Integer>();
                    ids.add(id);
                    wds.put(wd.getName(),ids);
                }
            }
        }
    }

    /**
     * format
     * Word,DayCount
     * Day1,NatureCount
     * Nature1,WordCount
     * W1 id1,id2....
     * W2 id1,id2....
     */
    private void writeMap2Disk(){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(OutPath));
            for(String word : Persons.keySet()){
                bw.write(word + "\t" + Persons.get(word).size() + "\n");
                for(int day : Persons.get(word).keySet()){
                    bw.write(day + "\t" + Persons.get(word).get(day).size() + "\n");
                    for(String nature : Persons.get(word).get(day).keySet()){
                        bw.write(nature + "\t" + Persons.get(word).get(day).get(nature).size() + "\n");
                        for(String target : Persons.get(word).get(day).get(nature).keySet()){
                            bw.write(target + "\t");
                            int index = 0;
                            for(Integer id : Persons.get(word).get(day).get(nature).get(target)){
                                if(index == 0){
                                    bw.write(id.toString());
                                }else{
                                    bw.write("," + id.toString() );
                                }
                                index++;
                            }
                            bw.write("\n");
                        }
                    }
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void runTask(){
        int BatchSize = 10000;
        Session session = HSession.getSession();
        int start = 0;
        String sql = "from Event as obj";
        for(int i = 0 ; ;++i){
            long batch = System.currentTimeMillis();
            List<Event> events = Hbn.getElementsFromDB(sql, BatchSize * i, BatchSize, session);
            if(events.size() == 0){
                break;
            }
            for(Event et : events){
                String title = et.getTitle();
                int day = et.getDay();
                List<Word> words = ChineseSplit.SplitStrWithPos(title);
                updateMap(words,day,et.getId());
            }
            long end = System.currentTimeMillis();
            System.out.println(i + "\t" + (end - batch) + "\t" + Persons.size());
            session.clear();
            if(i % 20 == 0){
                writeMap2Disk();
            }
        }
        writeMap2Disk();
        HSession.closeSession();
    }

    public static void main(String[] args){
        getElement ge = new getElement();
        ge.runTask();
    }
}
