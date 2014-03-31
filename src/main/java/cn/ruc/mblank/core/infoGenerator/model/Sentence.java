package cn.ruc.mblank.core.infoGenerator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mblank on 14-3-31.
 */
public class Sentence {

    private List<Word> CurWords;
    private double CurScore;
    private int CurLength;
    private String CurOriginal;
    private int CurIndex;

    public String getCurOriginal() {
        return CurOriginal;
    }

    public void setCurOriginal(String curOriginal) {
        CurOriginal = curOriginal;
    }

    public int getCurIndex() {
        return CurIndex;
    }

    public void setCurIndex(int curIndex) {
        CurIndex = curIndex;
    }

    public Sentence(){
        CurWords = new ArrayList<Word>();
    }

    public List<Word> getCurWords() {
        return CurWords;
    }

    public void setCurWords(List<Word> curWords) {
        CurWords = curWords;
    }

    public double getCurScore() {
        return CurScore;
    }

    public void setCurScore(double curScore) {
        CurScore = curScore;
    }

    public int getCurLength(){
        if(CurWords == null){
            return 0;
        }
        return CurWords.size();
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof  Sentence){
            return ((Sentence)obj).hashCode() == this.hashCode();
        }
        return false;
    }

    @Override
    public int hashCode(){
        if(CurWords == null){
            return 0;
        }
        int res = 0;
        for(Word wd : CurWords){
            res = res * 31 + wd.hashCode();
        }
        return res;
    }
}