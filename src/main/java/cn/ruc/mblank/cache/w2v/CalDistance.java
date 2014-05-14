package cn.ruc.mblank.cache.w2v;

import cn.ruc.mblank.core.word2Vec.domain.WordNeuron;
import cn.ruc.mblank.util.Similarity;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

/**
 * Created by mblank on 14-5-14.
 */
public class CalDistance {
    private String BaseDir = "data/";
    private HashMap<String,WordNeuron> Words = new HashMap<String,WordNeuron>();
    private List<WordPair> WordPairs = new ArrayList<WordPair>();
    private int MaxNumber = 30;

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
        }
        ois.close();
        System.out.println("read word vector ok...");
    }



    private void calDistanceWithTime(String input,int day){
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
        for(int i = 0 ; i < MaxNumber; ++i){
            System.out.println(WordPairs.get(i).wn.name + "\t" + WordPairs.get(i).score);
        }
        WordPairs.clear();
    }



    public static void main(String[] args) throws IOException, ClassNotFoundException {
        CalDistance cd = new CalDistance();
        cd.readPersonVector();
        Scanner scanner = new Scanner(System.in);
        int k = 100;
        while(k-- > 0){
            String input = scanner.next();
            if(cd.Words.containsKey(input)){
                System.out.println(cd.Words.get(input).synMap.size());
                int maxDay = -1;
                int minDay = 10000000;
                for(int day : cd.Words.get(input).synMap.keySet()){
                    if(day > maxDay){
                        maxDay = day;
                    }
                    if(day < minDay){
                        minDay = day;
                    }
                }
                System.out.println("pls input a day which between " + minDay + "\t" + maxDay);
                int lastDay = scanner.nextInt();
                cd.calDistanceWithTime(input, lastDay);
            }else{
                System.out.println("no word found!");
            }
        }
    }
}
