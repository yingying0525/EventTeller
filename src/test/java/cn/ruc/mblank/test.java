package cn.ruc.mblank;

import cn.ruc.mblank.db.hbn.HSession;
import cn.ruc.mblank.db.hbn.model.Url;
import cn.ruc.mblank.db.hbn.model.UrlStatus;
import cn.ruc.mblank.mq.Sender;
import cn.ruc.mblank.util.Const;
import cn.ruc.mblank.util.db.Hbn;
import org.hibernate.Session;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by mblank on 14-5-15.
 */

class CUrl{
    public String url;
    public String name;

    @Override
    public int hashCode(){
        return url.hashCode();
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof  CUrl){
            return ((CUrl)obj).hashCode() == this.hashCode();
        }else{
            return false;
        }
    }
}


public class test {

    public static void main(String[] args) throws IOException {


       String url = "http://news.qq.com/world_index.shtml";
        Document doc = Jsoup.connect(url).userAgent(Const.CrawlerUserAgent).get();
        Elements nodes = doc.getElementsByTag("a");
        for(Element node : nodes){

            System.out.println(node.absUrl("href"));
        }
    }
}
