package cn.ruc.mblank.cache.model;

/**
 * Created by mblank on 14-5-16.
 */
public class WordSims implements Comparable<WordSims>{
    public String worda;
    public String wordb;
    public double score;


    @Override
    public int hashCode(){
        return worda.hashCode() * 37 + wordb.hashCode() * 31;
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof WordSims){
            return ((WordSims)obj).hashCode() == this.hashCode();
        }else{
            return false;
        }
    }

    @Override
    public int compareTo(WordSims o) {
        if(o.score > this.score){
            return 1;
        }else{
            return -1;
        }
    }
}
