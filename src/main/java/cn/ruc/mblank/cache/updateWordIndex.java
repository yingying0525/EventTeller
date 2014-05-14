package cn.ruc.mblank.cache;

import cn.ruc.mblank.index.solr.WordIndex;
import cn.ruc.mblank.index.solr.model.WebWord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mblank on 2014/5/4.
 */
public class updateWordIndex {

    class pair{
        public String ea;
        public String eb;
        public Double score;
    }

    private String BaseDir = "data/";

    private HashMap<String,String> TopicWords = new HashMap<String, String>();
    HashMap<String,HashMap<String,List<pair>>> idMaps = new HashMap<String, HashMap<String, List<pair>>>();
    HashMap<String,String> titleMaps = new HashMap<String, String>();

    private void loadTitleMap() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(BaseDir + "events.sql"));
        String line = "";
        while((line = br.readLine()) != null ){
            String[] its = line.split("\t");
            titleMaps.put(its[0],its[1]);
        }
        br.close();
        System.out.println("load title map ok...");
    }

    private void loadTopicWords() throws IOException {
        BufferedReader tbr = new BufferedReader(new FileReader(BaseDir + "topicInfos"));
        String line = "";
        while((line = tbr.readLine()) != null){
            String[] its = line.split("\t");
            String[] tubs = its[1].split(";");
            StringBuffer tmps = new StringBuffer();
            for(String tub : tubs){
                String[] subs = tub.split(",");
                if(idMaps.containsKey(its[0]) && idMaps.get(its[0]).containsKey(subs[0])){
                    StringBuffer tmp = new StringBuffer();
                    for(pair pi : idMaps.get(its[0]).get(subs[0])){
                        tmp.append(pi.ea + "|" + pi.eb + "|" + pi.score + "@##@");
                    }
                    tmps.append(tub + "," + tmp.toString() + ";");
                }
            }
            TopicWords.put(its[0], tmps.toString());
        }
        tbr.close();
        System.out.println("load topic word ok..." + "\t" + TopicWords.size());
    }

    private void loadEventSims() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(BaseDir + "EventSims"));
        String line = "";
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            String key = its[3];
            pair pi = new pair();
            pi.ea = its[0];
            pi.eb = its[1];
            pi.score = Double.parseDouble(its[2]);
            String atitle = titleMaps.get(its[0]);
            String btitle = titleMaps.get(its[1]);
            if(atitle != null){
                pi.ea += "!--!" + atitle;
            }else{
                pi.ea += "!--!" + "null";
            }
            if(btitle != null){
                pi.eb += "!--!" + btitle;
            }else{
                pi.eb += "!--!" + "null";
            }
            if(idMaps.containsKey(key)){
                if(idMaps.get(key).containsKey(its[4])){
                    idMaps.get(key).get(its[4]).add(pi);
                }else{
                    List<pair> ids = new ArrayList<pair>();
                    ids.add(pi);
                    idMaps.get(key).put(its[4],ids);
                }
            }else{
                List<pair> ids = new ArrayList<pair>();
                ids.add(pi);
                HashMap<String,List<pair>> tmps = new HashMap<String, List<pair>>();
                tmps.put(its[4],ids);
                idMaps.put(key,tmps);
            }
        }
        System.out.println("load keys ok..." + "\t" + idMaps.size());
    }

    private void update() throws IOException {
        BufferedReader tbr = new BufferedReader(new FileReader(BaseDir + "word_timeline"));
        String line = "";
        HashMap<String,WebWord> words = new HashMap<String, WebWord>();
        while((line = tbr.readLine()) != null){
            String[] its = line.split("\t");
            WebWord word = new WebWord();
            word.setName(its[0]);
            word.setWordTimeNumber(its[1]);
            words.put(its[0],word);
        }
        System.out.println("load word timeline ok...");
        tbr = new BufferedReader(new FileReader(BaseDir + "relatedWords"));
        while((line = tbr.readLine()) != null){
            String[] its = line.split("\t");
            StringBuffer tmp = new StringBuffer();
            for(int i = 1; i < its.length; ++i){
                tmp.append(its[i] + "\t");
            }
            if(words.containsKey(its[0])){
                words.get(its[0]).setRelatedWords(tmp.toString());
            }
        }
        System.out.println("load related word ok...");
        tbr = new BufferedReader(new FileReader(BaseDir + "pgraph"));
        while((line = tbr.readLine()) != null){
            String[] its = line.split("\t");
            StringBuffer tmp = new StringBuffer();
            for(int i = 1; i < its.length; ++i){
                tmp.append(its[i] + "\t");
            }
            if(words.containsKey(its[0])){
                words.get(its[0]).setPersonGraph(tmp.toString());
            }
        }
        System.out.println("load person graph ok...");
        WordIndex wi = new WordIndex();
        wi.deleteAll();
        List<WebWord> updates = new ArrayList<WebWord>();
        int fail =0;
        for(String key : words.keySet()){
            if(TopicWords.containsKey(key)){
                words.get(key).setTopicRelatedWords(TopicWords.get(key));
                updates.add(words.get(key));
            }else{
                fail++;
            }
        }
        wi.update(updates);
        System.out.println("update ok..." + "\t" + fail);
    }

    public static void main(String[] args) throws IOException {
        updateWordIndex uwi = new updateWordIndex();
        uwi.loadTitleMap();
        uwi.loadEventSims();
        uwi.loadTopicWords();
        uwi.update();
    }
}
