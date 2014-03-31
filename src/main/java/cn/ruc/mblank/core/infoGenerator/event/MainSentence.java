package cn.ruc.mblank.core.infoGenerator.event;

import cn.ruc.mblank.core.infoGenerator.model.Sentence;
import cn.ruc.mblank.core.infoGenerator.model.Word;
import cn.ruc.mblank.db.hbn.model.Event;
import cn.ruc.mblank.util.ChineseSplit;
import cn.ruc.mblank.util.Const;
import sun.applet.Main;

import java.util.*;

/**
 * Extract Main Sentences from Event Content
 * the role of summary
 * Created by mblank on 14-3-31.
 */
public class MainSentence {

    private Event CurEvent;
    private List<Sentence> CurSens;
    private Map<String,Integer> CurWordTF;

    //parameters
    private final double alpha = 0.25;
    private final double beta = 0.25;
    private final double gama = 0.25;
    private final double theta = 0.25;

    /**
     * construct from Event
     * @param et
     */
    public MainSentence(Event et){
        CurEvent = et;
        CurWordTF = new HashMap<String, Integer>();
        getSentences();
    }

    /**
     * construct from str content
     * @param content
     */
    public MainSentence(String content){

    }

    private void add2TFMap(List<Word> wds){
        for(Word wd : wds){
            if(CurWordTF.containsKey(wd.getName())){
                CurWordTF.put(wd.getName(),CurWordTF.get(wd.getName()) + 1);
            }else{
                CurWordTF.put(wd.getName(),1);
            }
        }
    }

    private void updateSenSocre(){
        Set<String> tSet = new HashSet<String>();
        //update tf score in words
        // score is calculated by word overlap rate with title words;
        int index = 0;
        for(Sentence sen : CurSens){
            double score = 0.0;
            for(Word wd : sen.getCurWords()){
                if(index == 0){
                    //in title sentence;
                    tSet.add(wd.getName());
                }else{
                    if(tSet.contains(wd.getName())){
                        score++;
                    }
                }
                if(CurWordTF.containsKey(wd.getName())){
                    wd.setTf(CurWordTF.get(wd.getName()));
                }
            }
            score = score / sen.getCurWords().size();
            if(index == 0){
                // for title sentence
                score = 1.0;
            }
            sen.setCurScore(score);
            ++index;
        }
    }

    private void getSentences(){
        if(CurEvent == null || CurEvent.getTitle() == null || CurEvent.getTitle().length() == 0){
            return;
        }
        CurSens = new ArrayList<Sentence>();
        //update tf info..
        //TODO update idf info and score
        //for title
        List<Word> twords = ChineseSplit.SplitStrWithPos(CurEvent.getTitle());
        //add to sentences
        Sentence tsen = new Sentence();
        tsen.setCurWords(twords);
        tsen.setCurIndex(0);
        tsen.setCurOriginal(CurEvent.getTitle());
        CurSens.add(tsen);
        add2TFMap(twords);
        //for content
        String[] paras = CurEvent.getContent().split(Const.ParagraphSeperator);
        if(paras.length <= 1){
            //for event with no structure paragraph extracted
            //try to split content with full stop
            paras = CurEvent.getContent().split(" 　　");
        }
       //for sentence index
        int index = 0;
        for(String pa : paras){
            //filter some short paragraph
            if(pa.length() < Const.MinParagraphLength || filter(pa)){
                continue;
            }
            ++index;
            List<Word> pwords = ChineseSplit.SplitStrWithPos(pa);
            add2TFMap(pwords);
            Sentence psen = new Sentence();
            psen.setCurWords(pwords);
            psen.setCurIndex(index);
            psen.setCurOriginal(pa);
            CurSens.add(psen);
        }
        updateSenSocre();
        Collections.sort(CurSens,new SentenceComparator());
    }

    class SentenceComparator implements Comparator<Sentence>{
        @Override
        public int compare(Sentence s1,Sentence s2){
            if(s1.getCurScore() > s2.getCurScore()){
                return -1;
            }else if(s1.getCurScore() == s2.getCurScore()){
                return 0;
            }else{
                return 1;
            }
        }
    }

    /**
     * some rule based filter for sentence..
     * @param org
     * @return
     */
    private boolean filter(String org){
        if(org.indexOf("原标题") >= 0){
            return true;
        }
        return false;
    }

    public List<Sentence> getMainSens(int N){
        List<Sentence> res = new ArrayList<Sentence>();
        for(Sentence sen : CurSens){
            if(N-- > 0){
                res.add(sen);
            }
        }
        return res;
    }










}
