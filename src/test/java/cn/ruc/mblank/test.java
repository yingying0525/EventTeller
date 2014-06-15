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


        Set<CUrl> urls = new HashSet<CUrl>();
        String url = "http://6660409.blog.163.com/blog/static/472419692013229103322946/";
        Document doc = Jsoup.connect(url).userAgent(Const.CrawlerUserAgent).timeout(5000).get();
        Elements els = doc.getElementsByTag("a");
        for(Element el : els){
            Elements nodes = el.getElementsByTag("font");
            if(nodes.size() != 1 || nodes.get(0).attr("color") == null||!nodes.get(0).attr("color").equals("#474486")){
                continue;
            }
            String turl = el.attr("href");
            String text = el.getElementsByTag("font").get(0).text();
            if(turl.equals(text)){
                continue;
            }
            System.out.println(turl + "\t" + text);
            CUrl curl = new CUrl();
            curl.url = turl;
            curl.name = text;
            urls.add(curl);
        }
        System.out.println(urls.size());
        BufferedWriter bw = new BufferedWriter(new FileWriter("d:\\urls"));
        for(CUrl curl : urls){
            bw.write(curl.url + "\t" + curl.name + "\n");
        }
        bw.close();
    }
}
