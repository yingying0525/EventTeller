package cn.ruc.mblank.event;

import cn.ruc.mblank.db.hbn.model.Event;
import cn.ruc.mblank.util.TimeUtil;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by mblank on 14-4-24.
 */
public class RemoveDul {

    private String EventPath = "";
    private HashMap<Integer,String> TitleChecks = new HashMap<Integer,String>();
    private HashMap<String,String> SameEventMap = new HashMap<String,String>();
    private HashMap<String,Integer> EventCount = new HashMap<String,Integer>();
    private HashMap<Integer,Event> memEvent = new HashMap<Integer,Event>();

    private int hashCode(List<Term> scrs){
        int res = 0;
        for(Term tm : scrs){
            res += res * 251 + tm.getName().hashCode() * 31;
        }
        return res;
    }

    private void remove() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(EventPath));
        BufferedWriter bw = new BufferedWriter(new FileWriter(EventPath + "_clean"));
        String line = "";
        int num  = 0;
        int failCount = 0;
        int failContent = 0;
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            if(its.length != 7){
                failCount++;
                continue;
            }
//            if(its[3].length() < 20){
//                failContent++;
//                System.out.println(its[3]);
//                continue;
//            }
//            bw.write(line + "\n");
//
            int number = 0;
            try{
                number = Integer.parseInt(its[5]);
            }catch (Exception e){
                continue;
            }
            MyStaticValue.userLibrary = "h:\\other\\extdic.txt";
            List<Term> terms = ToAnalysis.parse(its[1]);
            int hash = hashCode(terms);
            if(TitleChecks.containsKey(hash)){
                String orgid = TitleChecks.get(hash);
                SameEventMap.put(its[0],orgid);
                EventCount.put(orgid,EventCount.get(orgid) + number);
            }else{
                TitleChecks.put(hash,its[0]);
                EventCount.put(its[0],number);
            }
            num++;
            if(num % 10000 == 0){
                System.out.println(num + "\t" + SameEventMap.size() + "\t" + EventCount.size());
            }
        }
        writeSame2Disk();
        System.out.println("write ok....");
    }

    private void writeSame2Disk() throws IOException {
        String SameEventPath = "H:\\other\\sameEventIds";
        String EventCountPath = "h:\\other\\eventCount";
        //SameEvent
        BufferedWriter bw = new BufferedWriter(new FileWriter(SameEventPath));
        for(String id : SameEventMap.keySet()){
            bw.write(id + "\t" + SameEventMap.get(id) + "\n");
        }
        bw.close();
        //EventCount
        BufferedWriter ecbw = new BufferedWriter(new FileWriter(EventCountPath));
        for(String id : EventCount.keySet()){
            ecbw.write(id + "\t" + EventCount.get(id) + "\n");
        }
        ecbw.close();
    }

    private void loadMemEvent() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(EventPath));
        String line = "";
        int num = 0;
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            if(its.length != 7){
                continue;
            }
            int id = 0;
            int subTopic = 2;
            int number = 0;
            Date date = new Date();
            try{
                id = Integer.parseInt(its[0]);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                date = sdf.parse(its[2]);
                number = Integer.parseInt(its[5]);
                subTopic = Integer.parseInt(its[6]);
            }catch (Exception e){
                continue;
            }
            Event et = new Event();
            et.setId(id);
            et.setTitle(its[1]);
            et.setPubTime(date);
            et.setContent(its[3]);
            et.setImgs(its[4]);
            et.setNumber(number);
            et.setTopic(subTopic);
            memEvent.put(id,et);
            num++;
            if(num % 10000 == 0){
                System.out.println(num);
            }
        }
        br.close();
        System.out.println("load events ok...");
    }


    private void generateNewEvent() throws IOException {
        HashMap<Integer,Integer> SameMap = new HashMap<Integer, Integer>();
        String SameEventPath = "/tmp/sameEventIds";
        //load
        BufferedReader sbr = new BufferedReader(new FileReader(SameEventPath));
        String line = "";
        while((line = sbr.readLine()) != null){
            String[] its = line.split("\t");
            try{
                int id = Integer.parseInt(its[0]);
                int count = Integer.parseInt(its[1]);
                SameMap.put(id,count);
            }catch (Exception e){
                continue;
            }
        }
        sbr.close();
        System.out.println("load map ok...");
        //generate new
        for(int id : SameMap.keySet()){
            int val = SameMap.get(id);
            if(memEvent.containsKey(val)){
                Event valet = memEvent.get(val);
                //update content
                if(valet.getContent().length() < 20 && memEvent.containsKey(id) && memEvent.get(id).getContent().length() > 20){
                    valet.setContent(memEvent.get(id).getContent());
                    if(memEvent.get(id).getImgs() != null && memEvent.get(id).getImgs().length() > 5){
                        valet.setImgs(valet.getImgs() + "!##!" + memEvent.get(id).getImgs());
                    }
                }
                //update number
                valet.setNumber(valet.getNumber() + 1);
            }
        }
        System.out.println("combine event ok....");
        //write to disk
        BufferedWriter bw = new BufferedWriter(new FileWriter("/tmp/new_events.sql"));
        for(int id : memEvent.keySet()){
            Event et = memEvent.get(id);
            if(SameMap.containsKey(id)){
                continue;
            }
//            int day = TimeUtil.getDayGMT8(et.getPubTime());
            //write and split by "\t"
            bw.write(et.getId() + "\t");
            bw.write(et.getTitle() + "\t");
            bw.write(et.getPubTime() + "\t");
            bw.write(et.getContent() + "\t");
            bw.write(et.getImgs() + "\t");
            bw.write(et.getNumber() + "\t");
            bw.write(et.getTopic() + "\n");
//            bw.write(day + "\n");
        }
        bw.close();
        System.out.println("write ok....");
    }

    public static void main(String[] args) throws IOException {
        RemoveDul rd = new RemoveDul();
//        rd.EventPath = "h:\\other\\events.sql";
        rd.EventPath = args[0];
        rd.loadMemEvent();
        rd.generateNewEvent();


    }

}
