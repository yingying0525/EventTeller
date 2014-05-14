package cn.ruc.mblank.app.web;

import cn.ruc.mblank.core.infoGenerator.model.Word;
import cn.ruc.mblank.util.HttpServerUtil;
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

    public WordHttpHandler() throws IOException {
        loadMemRelatedWords();
        loadMemWordTime();
    }

    private String BaseDir = "data/";
    private final String WordTimePath = BaseDir + "wordTime";
    private final String RelatedWordPath = BaseDir + "relatedWords";
    private HashMap<String,String> MemWordTimeMap = new HashMap<String, String>();
    private HashMap<String,TreeMap<Integer,HashMap<String,HashMap<String,HashMap<Integer,Integer>>>>> MemRelatedWords = new HashMap<String, TreeMap<Integer, HashMap<String, HashMap<String, HashMap<Integer, Integer>>>>>();

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

    private void loadMemWordTime() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(WordTimePath));
        String line = "";
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            if(its.length != 2){
                continue;
            }
            MemWordTimeMap.put(its[0],processWordTime(its[1]));
        }
        System.out.println("load WordTime to memory.. " + MemWordTimeMap.size());
        br.close();
    }

    private void loadMemRelatedWords() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(RelatedWordPath));
        String line = "";
        int num = 0;
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            if(its.length < 2){
                continue;
            }
            TreeMap<Integer,HashMap<String,HashMap<String,HashMap<Integer,Integer>>>> days = new TreeMap<Integer, HashMap<String, HashMap<String, HashMap<Integer, Integer>>>>();
            for(int i = 1; i< its.length ; ++i){
                String[] sdays = its[i].split(" ");
                int day = Integer.parseInt(sdays[0]);
                HashMap<String,HashMap<String,HashMap<Integer,Integer>>> natures = new HashMap<String, HashMap<String, HashMap<Integer, Integer>>>();
                for(int j = 1; j<sdays.length; ++j){
                    String[] stypes = sdays[j].split(",");
                    HashMap<String,HashMap<Integer,Integer>> related = new HashMap<String, HashMap<Integer, Integer>>();
                    for(int k = 1; k < stypes.length; ++k){
                        String[] swbs = stypes[k].split(";");
                        HashMap<Integer,Integer> nids = new HashMap<Integer, Integer>();
                        for(int l = 1; l < swbs.length; ++l){
                            String[] sids = swbs[l].split("[|]");
                            nids.put(Integer.parseInt(sids[0]),Integer.parseInt(sids[1]));
                        }
                        related.put(swbs[0],nids);
                    }
                    natures.put(stypes[0],related);
                }
                days.put(day,natures);
            }
            MemRelatedWords.put(its[0], days);
            num++;
            if(num % 10000 == 0){
                System.out.println(num);
            }
        }
        br.close();
        System.out.println("load related word ok....");
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

    private String getRelatedWord(String input,int startDay,int endDay,int topN){
        StringBuffer res = new StringBuffer();
        HashMap<String,HashMap<String,Integer>> numbers = new HashMap<String, HashMap<String, Integer>>();
        HashMap<String,HashSet<Integer>> ids = new HashMap<String, HashSet<Integer>>();
        for(int day : MemRelatedWords.get(input).keySet()){
            if(day > endDay || day < startDay){
                continue;
            }
            for(String type : MemRelatedWords.get(input).get(day).keySet()){
                if(!type.equals("nr") && !type.equals("ns") && !type.equals("v")){
                    continue;
                }
                for(String wb : MemRelatedWords.get(input).get(day).get(type).keySet()){
                    int total = 0;
                    for(int id :  MemRelatedWords.get(input).get(day).get(type).get(wb).keySet()){
                        int val = MemRelatedWords.get(input).get(day).get(type).get(wb).get(id);
                        total+=val;
                        if(ids.containsKey(wb)){
                            if(ids.get(wb).size() > 20){
                                continue;
                            }
                            ids.get(wb).add(id);
                        }else{
                            HashSet<Integer> tids = new HashSet<Integer>();
                            tids.add(id);
                            ids.put(wb,tids);
                        }
                    }
                    if(numbers.containsKey(type)){
                        if(numbers.get(type).containsKey(wb)){
                            numbers.get(type).put(wb,numbers.get(type).get(wb) + total);
                        }else{
                            numbers.get(type).put(wb,total);
                        }
                    }else{
                        HashMap<String,Integer> tnum = new HashMap<String, Integer>();
                        tnum.put(wb,total);
                        numbers.put(type,tnum);
                    }
                }
            }
        }
        //sort and get topN word add result to res
        for(String type : numbers.keySet()){
            List<wordNumber> words = new ArrayList<wordNumber>();
            for(String word : numbers.get(type).keySet()){
                wordNumber wn = new wordNumber();
                wn.name = word;
                wn.count = numbers.get(type).get(word);
                words.add(wn);
            }
            //sort words
            Collections.sort(words);
            //add to res..
            res.append(type + " ");
            for(int i = 0 ; i < topN; ++i){
                if(i == words.size()){
                    break;
                }
                res.append(words.get(i).name + ",");
                for(Integer id : ids.get(words.get(i).name)){
                    res.append(id + ";");
                }
                res.append(" ");
            }
            res.append("\t");
        }
        System.out.println(res.toString());
        return res.toString();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            String responseMsg = "";
            Map<String, String> paramers = HttpServerUtil.parseParamers(httpExchange);
            String function = paramers.get("function");
            String input = paramers.get("input");
            System.out.println(input + "\t" + function);
            if(function.equals("word")){
                if(MemWordTimeMap.containsKey(input)){
                    responseMsg = MemWordTimeMap.get(input).toString();
                }else{
                    responseMsg = "404";
                }
            }else if(function.equals("relatedWord")){
                int start = Integer.parseInt(paramers.get("startDay"));
                int end = Integer.parseInt(paramers.get("endDay"));
                int topN = Integer.parseInt(paramers.get("topN"));
                if(MemRelatedWords.containsKey(input)){
                   responseMsg = getRelatedWord(input,start,end,topN);
                }else{
                    responseMsg = "404";
                }
            }
            System.out.println(input);
            HttpServerUtil.writeToClient(httpExchange, URLEncoder.encode(responseMsg, "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            try {
                HttpServerUtil.writeToClient(httpExchange, "");
            } catch (IOException e1) {
                System.out.println("eee");
            }
        } finally {
            httpExchange.close();
        }
    }





}
