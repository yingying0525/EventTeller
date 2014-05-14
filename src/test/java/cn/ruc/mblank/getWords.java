package cn.ruc.mblank;

import cn.ruc.mblank.core.infoGenerator.model.Word;
import cn.ruc.mblank.index.solr.WordIndex;
import cn.ruc.mblank.index.solr.model.WebWord;
import cn.ruc.mblank.util.ChineseSplit;
import cn.ruc.mblank.util.Const;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by mblank on 14-4-2.
 */
public class getWords {


    private void getTYWord() throws IOException {
        String path = "d:\\ETT\\tianyi";
        String outPath = "d:\\ETT\\tianyiwords";

        BufferedWriter bw = new BufferedWriter(new FileWriter(outPath));
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line = "";
        int num = 0;
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            if(its.length < 4){
                continue;
            }
            List<String> twords = ChineseSplit.SplitStr(its[2] + " " + its[3]);
            if(twords.size() > 0){
                bw.write(its[1] + "\t");
            }
            for(String wd : twords){
                bw.write(wd + " ");
            }
            bw.write("\n");
            num++;
            if(num % 10000 == 0){
                System.out.println(num);
            }
        }
        br.close();
    }

    private boolean checkWordNature(Word wd){
        if(wd.getNature().indexOf("n") == 0 || wd.getNature().indexOf("v") == 0){
            return true;
        }
        return false;
    }

    private void getEventWord() throws IOException {
        String path = "h:\\other\\events.sql";
        String outPath = "h\\other\\wordRelated";
        HashMap<String,HashMap<String,HashMap<String,Integer>>> RelatedWords = new HashMap<String, HashMap<String, HashMap<String, Integer>>>();
        BufferedWriter bw = new BufferedWriter(new FileWriter(outPath));
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line = "";
        int num = 0;
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            if(its.length != 7){
                continue;
            }
            List<Word> words = ChineseSplit.SplitStrWithPos(its[1]);
            for(Word word : words){
                if(!checkWordNature(word)){
                    continue;
                }
                for(Word tmp : words){
                    if(tmp.equals(word) || !checkWordNature(tmp)){
                        continue;
                    }
//                    if()
                }
            }
            num++;
            if(num % 10000 == 0){
                System.out.println(num);
            }
        }
        br.close();
        bw.close();
    }

    private static String processWordTime(String time){
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


    public static  void main(String[] args) throws IOException {
//
        WordIndex wi = new WordIndex();
        wi.deleteAll();
        String path = "h:\\other\\relatedWords";
        String timePath = "h:\\other\\wordTime";

        HashMap<String,String> timelineMap = new HashMap<String, String>();
        BufferedReader br = new BufferedReader(new FileReader(timePath));
        String line = "";
        while((line = br.readLine()) != null){
            WebWord word = new WebWord();
            String[] its = line.split("\t");
            if(its.length != 2){
                continue;
            }
            timelineMap.put(its[0],processWordTime(its[1]));
        }
        List<WebWord> res = new ArrayList<WebWord>();
        br = new BufferedReader(new FileReader(path));
        int num  = 0;
        while((line = br.readLine()) != null){
            num++;
            String[] sts = line.split("\t");
            if(timelineMap.containsKey(sts[0])){
                WebWord wword = new WebWord();
                wword.setName(sts[0]);
                wword.setWordTimeNumber(timelineMap.get(sts[0]));
                StringBuffer tmp = new StringBuffer();
                for(int i = 1; i < sts.length;++i){
                    tmp.append(sts[i] + "\t");
                }
                wword.setRelatedWords(tmp.toString());
                res.add(wword);
                if(num % 10000 == 0){
                    System.out.println(num);
                    wi.update(res);
                    res.clear();
                }
            }
        }
        System.out.println("read file ok.. start to write index");
        wi.update(res);
        br.close();
    }
}
