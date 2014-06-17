package cn.ruc.mblank.cache.crawler.url;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by mblank on 2014/6/17.
 */
public class MUrl implements Serializable{

    public int id;
    public String title;
    public Date crawlTime;
    public String url;
    public int subTopic;
    public int level;
    public String webSite;

    @Override
    public int hashCode(){
        return this.url.hashCode();
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof MUrl){
            return ((MUrl)obj).hashCode() == this.hashCode();
        }else{
            return false;
        }
    }

}
