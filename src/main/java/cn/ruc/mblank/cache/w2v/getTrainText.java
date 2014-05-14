package cn.ruc.mblank.cache.w2v;

import cn.ruc.mblank.db.hbn.model.Event;
import cn.ruc.mblank.util.TimeUtil;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;
import org.apache.solr.client.solrj.SolrRequest;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by mblank on 14-5-14.
 */
public class getTrainText {
    private String BaseDir = "data/";
    private TreeMap<Integer,List<Event>> MemEvents = new TreeMap<Integer, List<Event>>();

    private void loadEvents() throws IOException, ParseException {
        BufferedReader br = new BufferedReader(new FileReader(BaseDir + "events.sql"));
        String line = "";
        int num = 0;
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            //id,title,time,content,images,number,topic
            Event et = new Event();
            et.setId(Integer.parseInt(its[0]));
            et.setTitle(its[1]);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            et.setPubTime(sdf.parse(its[2]));
            et.setContent(its[3]);
            et.setImgs(its[4]);
            et.setNumber(Integer.parseInt(its[5]));
            et.setTopic(Integer.parseInt(its[6]));
            int day = TimeUtil.getDayGMT8(et.getPubTime());
            et.setDay(day);
            if(MemEvents.containsKey(day)){
                MemEvents.get(day).add(et);
            }else{
                List<Event> events = new ArrayList<Event>();
                events.add(et);
                MemEvents.put(day,events);
            }
            num++;
            if(num % 10000 == 0){
                System.out.println(num);
            }
        }
        br.close();
        System.out.println("load Events to MEm ok...");
    }

    private void writeEvent2Disk() throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BaseDir + "events.sql.object"));
        for(int day : MemEvents.keySet()){
            for(Event et : MemEvents.get(day)){
                oos.writeObject(et);
            }
        }
        // for eof mark
        oos.writeObject(null);
        oos.close();
        System.out.println("write events to disk ok...");
    }

    private void generateTrainText() throws IOException {
        MyStaticValue.userLibrary = BaseDir + "extdic";
        BufferedWriter bw = new BufferedWriter(new FileWriter(BaseDir + "newTrainText"));
        for(int day : MemEvents.keySet()){
            bw.write(day + "\t");
            for(Event et : MemEvents.get(day)){
                List<Term> tms = ToAnalysis.parse(et.getTitle() + " " + et.getContent());
                for(Term tm : tms){
                    bw.write(tm.getName() + " ");
                }
            }
            bw.write("\n");
        }
        bw.close();
        System.out.println("write train text ok....");
    }

    public static void main(String[] args) throws IOException, ParseException {
        getTrainText gtt = new getTrainText();
        gtt.loadEvents();
        gtt.writeEvent2Disk();
        gtt.generateTrainText();
    }

}
