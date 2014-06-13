package cn.ruc.mblank.cache.event;

import cn.ruc.mblank.core.infoGenerator.model.Word;
import cn.ruc.mblank.db.hbn.model.Article;
import cn.ruc.mblank.util.ChineseSplit;
import cn.ruc.mblank.util.Similarity;
import cn.ruc.mblank.util.hash.SimHash;
import org.ansj.domain.Term;
import org.ansj.recognition.NatureRecognition;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by mblank on 2014/6/12.
 */
public class ProcSame {

    private HashMap<Integer,Integer>  TitleHash = new HashMap<Integer, Integer>();
    private HashMap<Integer,List<Article>> SameNews = new HashMap<Integer, List<Article>>();

    private int MaxProcDay = 20;
    private String BaseDir = "NewExtracted/";
    private String OutPath = "EXP/";

    public void ProcSame(){
        MyStaticValue.userLibrary =  "extdic";
    }


    private int selfHashCode(String scr){
        List<Term> terms = ToAnalysis.parse(scr);
        new NatureRecognition(terms).recognition();
        int res = 0;
        for(Term term : terms){
            //only cal word with length bigger than 2
            if(term.getName().length() >= 2){
                res += term.getName().hashCode() * 31;
            }
        }
        return res;
    }

    private void runTask() throws IOException, ClassNotFoundException, ParseException {
        File folder = new File(BaseDir);
        File[] files = folder.listFiles();
        int num = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String endstr = "2013-05-01";
        Date enddt = sdf.parse(endstr);
        for(File file : files){
            Date dt = sdf.parse(file.getName());
            if(dt.compareTo(enddt) > 0){
                continue;
            }
            System.out.println(file.getName());
            //try to process news
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            Article at = null;
            int objnum = 0;
            try{
                while((at = (Article) ois.readObject()) != null){
                    objnum++;
                    if(objnum % 1000 == 0){
                        System.out.println(objnum);
                    }
                    int hash = selfHashCode(at.getTitle());
                    if(hash == 0){
//                        System.out.println("error" + at.getTitle());
                        continue;
                    }
                    if(TitleHash.containsKey(hash)){
                        int id = TitleHash.get(hash);
                        SameNews.get(id).add(at);
                    }else{
                        TitleHash.put(hash,at.getId());
                        List<Article> ats = new ArrayList<Article>();
                        ats.add(at);
                        SameNews.put(at.getId(),ats);
                    }
                }
            }catch (Exception e){

            }finally {
                ois.close();
            }
        }
        System.out.println("all process hash ok..start to cal the three p & r..");
        //try to cal avg
        double overlap = calSimHash();


        System.out.println("overlap : " + overlap);

    }

    private double calTF(){
        double avg = 0;
        double size = 0;
        for(Integer id : SameNews.keySet()){
            if(SameNews.get(id).size() < 2){
                continue;
            }
            size++;
            double total = 0;
            for(int i = 0 ; i < SameNews.get(id).size(); ++i){
                if(i == 0){
                    continue;
                }
                Map<Word,Double> worda = ChineseSplit.SplitStrWithPosDoubleTF(SameNews.get(id).get(i).getContent());
                Map<Word,Double> wordb = ChineseSplit.SplitStrWithPosDoubleTF(SameNews.get(id).get(i-1).getContent());
                double sim = Similarity.SimilarityOfTF(worda,wordb);
                if(sim > 1){
                    sim = sim - 1;
                }
//                System.out.println(sim);
                total+=sim;
            }
            total = total / ( SameNews.get(id).size() - 1 );
            avg += total;
        }
        System.out.println(size);
        return avg / size;
    }

    private double calOverlap(){
        double avg = 0;
        double size = 0;
        for(Integer id : SameNews.keySet()){
            if(SameNews.get(id).size() < 2){
                continue;
            }
            size++;
            double total = 0;
            for(int i = 0 ; i < SameNews.get(id).size(); ++i){
                if(i == 0){
                    continue;
                }
                double sim = Similarity.ContentOverlap(SameNews.get(id).get(i).getTitle(),SameNews.get(id).get(i-1).getTitle());
                if(sim > 1){
                    sim = sim - 1;
                }
                System.out.println(sim);
                total+=sim;
            }
            total = total / ( SameNews.get(id).size() - 1 );
            avg += total;
        }
        System.out.println(size);
        return avg / size;
    }

    private double calSimHash(){
        double avg = 0;
        double size = 0;
        for(Integer id : SameNews.keySet()){
            if(SameNews.get(id).size() < 2){
                continue;
            }
            size++;
            double total = 0;
            for(int i = 0 ; i < SameNews.get(id).size(); ++i){
                if(i == 0){
                    continue;
                }
                double sim = SimHash.getSimHashDiffBits(SameNews.get(id).get(i).getContent(),SameNews.get(id).get(i-1).getContent());
                total+=sim;
            }
            total = total / ( SameNews.get(id).size() - 1 );
            avg += total;
        }
        System.out.println(size);
        return avg / size;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, ParseException {
        ProcSame ps = new ProcSame();
        ps.runTask();
    }


}
