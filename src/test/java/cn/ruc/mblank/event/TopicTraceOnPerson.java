package cn.ruc.mblank.event;

import cn.ruc.mblank.db.hbn.HSession;
import cn.ruc.mblank.db.hbn.model.Event;
import cn.ruc.mblank.db.hbn.model.EventStatus;
import cn.ruc.mblank.util.TimeUtil;
import cn.ruc.mblank.util.db.Hbn;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.hibernate.Session;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;

/**
 * Created by mblank on 14-4-4.
 */
public class TopicTraceOnPerson {

    /**
     * load
     * get Event from db
     *
     *
     *
     */

    private void insert2DB() throws IOException {
        String path = "d:\\ETT\\events.sql";
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line = "";
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            System.out.println();
        }
        br.close();
    }

    private void getEvent() throws IOException {
        int BatchSize = 10000;
        Session session = HSession.getSession();
        int start = 0;
        BufferedWriter bw = new BufferedWriter(new FileWriter("e:\\share\\events_new_words"));
        String sql = "from Event as obj";
        for(int i = 0 ; i < 300 ;++i){
            long batch = System.currentTimeMillis();
            List<Event> events = Hbn.getElementsFromDB(sql, BatchSize * i, BatchSize, session);
            if(events.size() == 0 ){
                break;
            }
            for(Event et : events){
                String title = et.getTitle();
                int day = et.getDay();

                List<Term> terms = ToAnalysis.parse(et.getTitle() + " " + et.getContent());
                if(terms.size() > 0){
                    bw.write(et.getPubTime() + "\t");
                }
                for(Term tm : terms){
                    if(tm.getName().length() > 1){
                        bw.write(tm.getName() + " ");
                    }
                }
                bw.write("\n");
            }
            long end = System.currentTimeMillis();
            System.out.println(i + "\t" + (end - batch) );
            session.clear();
        }
        HSession.closeSession();
    }

    private long getTitleHash(String title){
        long res = 0;
        List<Term> terms = ToAnalysis.parse(title);
        for(Term tm : terms){
            res += tm.getName().hashCode() * 31;
        }
        return res;
    }

    private void insertDB() throws IOException {
        Session session = HSession.getSession();
        HashSet<String> checks = new HashSet<String>();
        String path = "D:\\ETT\\events.sql";
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line = "";
        int num = 0;
        while((line = br.readLine()) != null){
            num++;
            if(num % 10000 == 0){
                System.out.println(num);
            }
            String[] its = line.split("\t");
            if(its.length != 7 ){
                continue;
            }
            try{
//                if(checks.contains(its[1])){
//                    continue;
//                }
                Event et = new Event();
                et.setId(Integer.parseInt(its[0]));
                et.setTitle(its[1]);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                et.setPubTime(sdf.parse(its[2]));
                if(its[3].length() < 10){
                    continue;
                }
                et.setContent(its[3]);
                et.setImgs(its[4]);
                et.setTopic(Integer.parseInt(its[5]));
                et.setNumber(Integer.parseInt(its[6]));
                int day = TimeUtil.getDayGMT8(et.getPubTime());
                et.setDay(day);
//                checks.add(et.getTitle());
                session.saveOrUpdate(et);
            } catch (Exception e) {
                System.out.println("......");
            }
            if(session.getStatistics().getEntityCount() % 10000 == 0){
                session.beginTransaction().commit();
                session.clear();
                System.out.println("Start to update db..." );
            }
        }
        session.beginTransaction().commit();
        System.out.println("update ok....");
        session.close();
        br.close();
    }

    private void insertStatus() throws IOException {
        Session session = HSession.getSession();
        String path = "e:\\ids";
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line = "";
        while((line = br.readLine()) != null){
            try{
                EventStatus es = new EventStatus();
                es.setId(Integer.parseInt(line));
                es.setStatus((short)7);
                session.saveOrUpdate(es);
            }catch (Exception e){

            }
            if(session.getStatistics().getEntityCount() % 10000 == 0){
                session.beginTransaction().commit();
                session.clear();
                System.out.println("start to update db");
            }
        }
        session.beginTransaction().commit();
        session.clear();
        System.out.println("run ok...");
        br.close();

    }



    public static void main(String[] args) throws IOException {
        TopicTraceOnPerson tco = new TopicTraceOnPerson();
        tco.insertDB();
    }
}
