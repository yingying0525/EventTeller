package cn.ruc.mblank.cache;

import cn.ruc.mblank.db.hbn.model.Topic;
import cn.ruc.mblank.index.solr.TopicIndex;
import cn.ruc.mblank.index.solr.model.WebTopic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mblank on 2014/5/9.
 */
public class updateTopicIndex {

    private String BaseDir = "data/";
    private HashMap<Integer,StringBuffer> Tids = new HashMap<Integer, StringBuffer>();

    private void loadRelations() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(BaseDir + "relations"));
        String line = "";
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            int ida = Integer.parseInt(its[1]);
            if(Tids.containsKey(ida)){
                Tids.get(ida).append(" " + its[0]);
            }else{
                StringBuffer tmp = new StringBuffer();
                tmp.append(its[0]);
                Tids.put(ida,tmp);
            }
        }
        br.close();
        System.out.println("load relations ok...");
    }

    private void updateTopic() throws IOException, ParseException {
        BufferedReader br = new BufferedReader(new FileReader(BaseDir + "topicsfull"));
        String line = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        List<WebTopic> wts = new ArrayList<WebTopic>();
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            WebTopic wt = new WebTopic();
            Topic tp = new Topic();
            tp.setId(Integer.parseInt(its[0]));
            tp.setKeyWords(its[1]);
            tp.setSummary(its[2]);
            tp.setStartTime(sdf.parse(its[3]));
            tp.setEndTime(sdf.parse(its[4]));
            tp.setNumber(Integer.parseInt(its[5]));
            tp.setMain(its[7]);
            tp.setObject(its[8]);
            wt.tp = tp;
            wt.ids = Tids.get(tp.getId()).toString();
            wts.add(wt);
        }
        TopicIndex ti = new TopicIndex();
        ti.deleteAll();
        ti.update(wts);
        System.out.println("update index ok....");
    }

    public static void main(String[] args) throws IOException, ParseException {
        updateTopicIndex uti = new updateTopicIndex();
        uti.loadRelations();
        uti.updateTopic();
    }
}
