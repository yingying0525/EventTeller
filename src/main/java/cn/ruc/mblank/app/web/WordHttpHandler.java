package cn.ruc.mblank.app.web;

import cn.ruc.mblank.cache.model.Person;
import cn.ruc.mblank.cache.model.WordSims;
import cn.ruc.mblank.core.infoGenerator.model.Word;
import cn.ruc.mblank.core.word2Vec.domain.WordNeuron;
import cn.ruc.mblank.util.HttpServerUtil;
import cn.ruc.mblank.util.Similarity;
import com.alibaba.fastjson.JSONObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import love.cq.util.IOUtil;
import love.cq.util.StringUtil;
import org.ansj.app.keyword.KeyWordComputer;
import org.ansj.app.keyword.Keyword;
import org.ansj.app.summary.SummaryComputer;
import org.ansj.app.summary.TagContent;
import org.ansj.app.summary.pojo.Summary;
import org.ansj.domain.Term;
import org.ansj.recognition.NatureRecognition;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by mblank on 14-4-21.
 */
public class WordHttpHandler implements HttpHandler {

    private HashMap<String,WordNeuron> Words = new HashMap<String,WordNeuron>();
    private List<WordPair> WordPairs = new ArrayList<WordPair>();

    public WordHttpHandler() throws IOException, ClassNotFoundException {
        readPersonVector();
    }

    private String BaseDir = "data/";
    private final String WordTimePath = BaseDir + "wordTime";
    private final String RelatedWordPath = BaseDir + "relatedWords";

    private String processWordTime(String time){
        StringBuffer res = new StringBuffer();
        String[] its = time.split(" ");
        TreeMap<Integer,Integer> dates = new TreeMap<Integer, Integer>();
        for(String it : its){
            String[] subs = it.split(",");
            int date = Integer.parseInt(subs[0]);
            int ct = Integer.parseInt(subs[1]);
            dates.put(date,ct);
        }
        int start = 0;
        for(int key : dates.keySet()){
            if(key <= 15760){
                continue;
            }
            if(start == 0){
                start = key;
            }
            if(key > start){
                for(int i = start + 1; i < key; ++i){
                    res.append(i + ","+"0" + " ");
                }
                start = key;
            }
            res.append(start + "," + dates.get(start) + " ");
        }
        return res.toString();
    }

    private void readPersonVector() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(BaseDir + "personVectors"));
        WordNeuron wn = null;
        int num = 0;
        while((wn = (WordNeuron) ois.readObject()) != null){
            Words.put(wn.name, wn);
            num++;
            if(num % 1000 == 0){
                System.out.println(num);
            }
        }
        ois.close();
        System.out.println("read word vector ok..." + "\t" + num);
    }





    class wordNumber implements Comparable<wordNumber>{
        public String name;
        public int count;
        public int compareTo(wordNumber other){
            if(other.count > this.count){
                return 1;
            }else{
                return -1;
            }
        }
    }

    class WordPair implements Comparable<WordPair>{
        public WordNeuron wn;
        public double score;
        @Override
        public int compareTo(WordPair o) {
            if(o.score > this.score){
                return 1;
            }else{
                return -1;
            }
        }
    }

    private  List<WordSims> calDistanceWithTime(String input,int day){
        WordNeuron target = Words.get(input);
        int iday = 0;
        int MaxNumber = 10;
        //find most resent day to input day
        for(int id : target.synMap.keySet()){
            if(id > day){
                break;
            }
            iday = id;
        }
        for(String key : Words.keySet()){
            WordNeuron tmp = Words.get(key);
            if(tmp.name.equals(input)){
                continue;
            }
            int tday = 0;
            for(int sday : tmp.synMap.keySet()){
                if(sday > day){
                    break;
                }
                //find the most resent day..
                tday = sday;
            }
            double score = Similarity.simOf2Vector(target.synMap.get(iday), tmp.synMap.get(tday));
            WordPair wp = new WordPair();
            wp.wn = tmp;
            wp.score = score;
            WordPairs.add(wp);
        }
        Collections.sort(WordPairs);
        List<WordSims> res = new ArrayList<WordSims>();
        for(int i = 0 ; i < MaxNumber; ++i){
            WordSims ws = new WordSims();
            ws.worda = input;
            ws.wordb = WordPairs.get(i).wn.name;
            ws.score =  WordPairs.get(i).score;
            res.add(ws);
        }
        WordPairs.clear();
        return res;
    }

    private String getTimePGraph(String name,String persent){
        Person ps = new Person();
        int day = 0;
        //get the day according to persent
        WordNeuron current = Words.get(name);
        double per = Integer.parseInt(persent) / 100.0;
        int index = Math.min((int) (per * current.synMap.size()), current.synMap.size());
        int num = 0;
        for(int tday : current.synMap.keySet()){
            day = tday;
            num++;
            if(num == index){
                break;
            }
        }
        System.out.println(day);
        //first get first level graph
        List<WordSims> firsts = calDistanceWithTime(name,day);
        ps.relations.addAll(firsts);
        //get second level
        for(WordSims ws : firsts){
            List<WordSims> second = calDistanceWithTime(ws.wordb,day);
            ps.relations.addAll(second);
        }
        System.out.println(ps.relations.size());
        String res = JSONObject.toJSONString(ps);
        if(res == null){
            res = "404";
        }
        return res;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            String responseMsg = "";
            Map<String, String> paramers = HttpServerUtil.parseParamers(httpExchange);
            if(paramers == null){
                responseMsg = "404";
            }else{
                String function = paramers.get("function");
                String input = paramers.get("input");
                String persent = paramers.get("persent");
                if(function.equals("timePersonGraph")){
                    if(Words.containsKey(input)){
                        responseMsg = getTimePGraph(input,persent);
                    }else{
                        responseMsg = "404";
                    }
                }
                System.out.println(input + "\t --- " + responseMsg);
            }
            HttpServerUtil.writeToClient(httpExchange, URLEncoder.encode(responseMsg, "utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
            try {
                HttpServerUtil.writeToClient(httpExchange, "404");
            } catch (IOException e1) {
                System.out.println("eee");
            }
        } finally {
            httpExchange.close();
        }
    }





}
