package cn.ruc.mblank.cache.w2v;

import cn.ruc.mblank.cache.model.Person;
import cn.ruc.mblank.cache.model.WordSims;
import cn.ruc.mblank.core.word2Vec.domain.WordNeuron;
import cn.ruc.mblank.util.Similarity;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

/**
 * Created by mblank on 14-5-14.
 */
public class CalDistance implements Runnable{
    private String BaseDir = "e:\\share\\";
    private HashMap<String,WordNeuron> Words = new HashMap<String,WordNeuron>();
    private List<WordPair> WordPairs = new ArrayList<WordPair>();
    private int MaxNumber = 30;

    @Override
    public void run() {
        //cal sim
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



    private WordSims calDistanceWithTime(String input,int day){
        WordNeuron target = Words.get(input);
        int iday = 0;
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
            double score = Similarity.simOf2Vector(target.synMap.get(iday),tmp.synMap.get(tday));
            WordPair wp = new WordPair();
            wp.wn = tmp;
            wp.score = score;
            WordPairs.add(wp);
        }
        Collections.sort(WordPairs);
        StringBuffer res = new StringBuffer();
        WordSims ws = new WordSims();
        for(int i = 0 ; i < MaxNumber; ++i){
            ws.worda = input;
            ws.wordb = WordPairs.get(i).wn.name;
            ws.score =  WordPairs.get(i).score;
//            System.out.println(WordPairs.get(i).wn.name + "\t" + WordPairs.get(i).score);
        }
        WordPairs.clear();
        return ws;
    }


    private void calSelfChange(){
        int k = 10000;
        while(k-- > 0){
            Scanner scanner = new Scanner(System.in);
            String input = scanner.next();
            if(!Words.containsKey(input)){
                System.out.println("no word found!");
            }else{
                WordNeuron wn = Words.get(input);
                int lastDay = -1;
                for(int day : wn.synMap.keySet()){
                    if(day <= 0){
                        continue;
                    }
                    if(lastDay < 0){
                        lastDay = day;
                        continue;
                    }
                    double sim = Similarity.simOf2Vector(Words.get(input).synMap.get(lastDay),Words.get(input).synMap.get(day));
                    System.out.println(day + "\t" + sim);
                    lastDay = day;
                }
            }
        }
    }

    private double simOf2PersonWithDay(WordNeuron wna, WordNeuron wnb,int day){
        int tdaya = 0;
        int tdayb = 0;
        for(int daya : wna.synMap.keySet()){
            if(tdaya == 0){
                tdaya = daya;
            }
            if(daya > day){
                break;
            }
            tdaya = daya;
        }
        for(int dayb : wnb.synMap.keySet()){
            if(tdayb == 0){
                tdayb = dayb;
            }
            if(dayb > day){
                break;
            }
            tdayb = dayb;
        }
        return Similarity.simOf2Vector(wna.synMap.get(tdaya),wnb.synMap.get(tdayb));
    }

    private void twoPersonRelation(){
        System.out.println("pls input two people, split by tab..");
        int k = 10000;
        while(k-- > 0){
            Scanner scanner = new Scanner(System.in);
            String worda = scanner.next();
            String wordb = scanner.next();
            if(Words.containsKey(worda) && Words.containsKey(wordb)){
                WordNeuron wna = Words.get(worda);
                WordNeuron wnb = Words.get(wordb);
                for(int day : wna.synMap.keySet()){
                    double sim = simOf2PersonWithDay(wna,wnb,day);
                    System.out.println(day + "\t" + sim);
                }
            }else{
                if(Words.containsKey(worda)){
                    System.out.println(wordb + "\t" + "not found!");
                }else{
                    System.out.println(worda + "\t" + "not found!");
                }
            }
        }
    }

    private void getSingleSims(){
        Scanner scanner = new Scanner(System.in);
        int k = 100;
        while(k-- > 0){
            String input = scanner.next();
            if(Words.containsKey(input)){
                System.out.println(Words.get(input).synMap.size());
                int maxDay = -1;
                int minDay = 10000000;
                for(int day : Words.get(input).synMap.keySet()){
                    if(day > maxDay){
                        maxDay = day;
                    }
                    if(day < minDay){
                        minDay = day;
                    }
                }
                System.out.println("pls input a day which between " + minDay + "\t" + maxDay);
                int lastDay = scanner.nextInt();
                calDistanceWithTime(input, lastDay);
            }else{
                System.out.println("no word found!");
            }
        }
    }

    private void getPersonGraphByTime(){
        //cal person graph for everyone in every time
        HashMap<String,Person> res = new HashMap<String, Person>();
        HashMap<String,List<WordSims>> tmps = new HashMap<String, List<WordSims>>();
        for(String name : Words.keySet()){
            StringBuffer self = new StringBuffer();
            for(int day : Words.get(name).synMap.keySet()){
                calDistanceWithTime(name,day);
            }
        }
    }



    public static void main(String[] args) throws IOException, ClassNotFoundException {
        CalDistance cd = new CalDistance();
        cd.readPersonVector();
        cd.twoPersonRelation();

    }
}
