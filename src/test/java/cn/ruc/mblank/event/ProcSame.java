package cn.ruc.mblank.event;

import cn.ruc.mblank.db.hbn.model.Article;
import org.ansj.domain.Term;
import org.ansj.recognition.NatureRecognition;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;

import java.io.*;
import java.util.HashMap;
import java.util.List;

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

    private void runTask() throws IOException, ClassNotFoundException {
        File folder = new File(BaseDir);
        File[] files = folder.listFiles();
        int num = 0;
        for(File file : files){
            num++;
            if(num > MaxProcDay){
                break;
            }
            System.out.println(file.getName());
            //try to process news
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            Article at = null;
            int objnum = 0;
            while((at = (Article) ois.readObject()) != null){
                objnum++;
                if(objnum % 1000 == 0){
                    System.out.println(objnum);
                }
                int hash = selfHashCode(at.getTitle());
                if(hash == 0){
                    System.out.println("error" + at.getTitle());
                    continue;
                }
                if(TitleHash.containsKey(hash)){
                    int id = TitleHash.get(hash);
                    SameNews.get(id).add(at);
                }
            }
            ois.close();
        }
        System.out.println("all process hash ok..start to cal the three p & r..");

    }

    private static void main(String[] args) throws IOException, ClassNotFoundException {
        ProcSame ps = new ProcSame();
        ps.runTask();
    }


}
