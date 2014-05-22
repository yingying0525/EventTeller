package cn.ruc.mblank.word;

import cn.ruc.mblank.util.Similarity;

import java.io.*;
import java.util.*;

/**
 * Created by mblank on 2014/5/1.
 */
public class getPersonGraphs {

    public String BaseDir = "data/";
    private int VectorSize = 100;
    private String VectorPath = BaseDir + "pvectors";
    private List<word> Words = new ArrayList<word>();
    private HashMap<String,word> WordMaps = new HashMap<String,word>();

    class word implements Comparable<word>{
        public String name;
        public double[] vector;
        public double score;

        @Override
        public int compareTo(word o) {
            if(o.score > this.score){
                return 1;
            }else if(o.score < this.score){
                return -1;
            }else{
                return 0;
            }
        }
    }

    class SimPair{
        public String worda;
        public String wordb;
        public double score;
        public int level;

        @Override
        public int hashCode(){
            return worda.hashCode() * 31 + wordb.hashCode()*31;
        }

        @Override
        public boolean equals(Object obj){
            if(obj instanceof SimPair){
               return ((SimPair)obj).hashCode() == this.hashCode();
            }else{
                return false;
            }
        }
    }



    public void loadWord() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(VectorPath));
        String line = "";
        while((line = br.readLine())!=null){
            String[] its = line.split("\t");
            //name vector number
            word wd = new word();
            wd.name = its[0];
            int num = Integer.parseInt(its[2]);
            double[] vector = new double[VectorSize];
            String[] dbs = its[1].split(" ");
            for(int i = 0 ; i < VectorSize; ++i){
                vector[i] = Double.parseDouble(dbs[i]);
            }
            wd.vector = vector;
            Words.add(wd);
            WordMaps.put(wd.name,wd);
            for(int k = 0 ; k < num; ++k){
                br.readLine();
            }
        }
        System.out.println("load ok...  " + Words.size());
        br.close();
    }

    private List<word> getMostSimTopN(word cur,int N){
        List<word> res = new ArrayList<word>();
        for(word wd : Words){
            if(wd.name.equals(cur.name)){
                wd.score = -11111100;
            }else{
                double sim = Similarity.simOf2Vector(wd.vector,cur.vector);
                wd.score = sim;
            }
        }
        Collections.sort(Words);
        for(int i = 0; i < N; ++i){
            res.add(Words.get(i));
        }
        return res;
    }

    public void getWordDistance(){
        int k = 100;
        int top = 20;
        while(k-- > 0){
            Scanner sc = new Scanner(System.in);
            String name = sc.next();
            if(WordMaps.containsKey(name)){
                word cur = WordMaps.get(name);
                for(word wd : Words){
                    if(wd.name.equals(name)){
                        wd.score = -11111100;
                    }else{
                        double sim = Similarity.simOf2Vector(wd.vector,cur.vector);
                        wd.score = sim;
                    }
                }
                Collections.sort(Words);
                for(int j = 0; j < top; ++j){
                    System.out.println(Words.get(j).name + "\t" + Words.get(j).score);
                }
            }else{
                System.out.println("can't find this word..");
            }
        }
    }

    private void findMostSimN() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(BaseDir + "psims"));
        int N = 20;
        int num = 0;
        for(String word : WordMaps.keySet()){
            List<word> sims = getMostSimTopN(WordMaps.get(word),N);
            bw.write(word + "\t");
            for(word sim : sims){
                bw.write(sim.name + "," + sim.score + "\t");
            }
            bw.write("\n");
            num++;
            if(num % 100 == 0){
                System.out.println(num);
            }
        }
    }

    private void getPersonGraph() throws IOException {
        HashMap<String,List<word>> sims = new HashMap<String, List<word>>();
        HashMap<String,Set<SimPair>> pairs = new HashMap<String, Set<SimPair>>();
        BufferedReader br = new BufferedReader(new FileReader(BaseDir + "psims"));
        String line = "";
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            List<word> words = new ArrayList<word>();
            for(int i = 1; i < its.length; ++i){
                word wd = new word();
                String[] subs = its[i].split(",");
                wd.name = subs[0];
                wd.score = Double.parseDouble(subs[1]);
                words.add(wd);
            }
            sims.put(its[0],words);
        }
        br.close();
        System.out.println("Load sims ok....");
        for(String name : sims.keySet()){
            List<word> words = sims.get(name);
            Set<SimPair> ps = new HashSet<SimPair>();
            for(word wd : words){
                SimPair sp = new SimPair();
                sp.worda = name;
                sp.wordb = wd.name;
                sp.score = wd.score;
                ps.add(sp);
                if(!sims.containsKey(wd.name)){
                    continue;
                }
                List<word> subs = sims.get(wd.name);
                for(word sb : subs){
                    SimPair sup = new SimPair();
                    sup.worda = wd.name;
                    sup.wordb = sb.name;
                    sup.score = sb.score;
                    ps.add(sup);
                }
            }
            pairs.put(name,ps);
        }
        System.out.println("get graph ok...");
        BufferedWriter bw = new BufferedWriter(new FileWriter(BaseDir + "pgraph"));
        for(String name : pairs.keySet()){
            bw.write(name + "\t");
            for(SimPair sp : pairs.get(name)){
                bw.write(sp.worda + "," + sp.wordb + "," + sp.score + "\t");
            }
            bw.write("\n");
        }
        bw.close();
        System.out.println("ok...");
    }





    public static void main(String[] args) throws IOException {
        getPersonGraphs cd = new getPersonGraphs();
        cd.getPersonGraph();
    }
}
