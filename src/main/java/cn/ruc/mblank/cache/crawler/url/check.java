package cn.ruc.mblank.cache.crawler.url;

import cn.ruc.mblank.util.Const;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;

/**
 * Created by mblank on 2014/6/13.
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

class Class implements Comparable<Class>{
    public String name;
    public Set<String> urls = new HashSet<String>();

    @Override
    public int hashCode(){
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof Class){
            return ((Class)obj).hashCode() == this.hashCode();
        }else{
            return false;
        }
    }

    @Override
    public int compareTo(Class o) {
        if(o.urls.size() > this.urls.size()){
            return 1;
        }else{
            return -1;
        }
    }
}

public class check {

    private String BaseUrlPath = "d:\\urls";
    private List<CUrl> BaseUrls = new ArrayList<CUrl>();
    private HashMap<String,Class> ClassSet = new HashMap<String,Class>();

    private int Threshold = 10;

    private void loadBaseUrl() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(BaseUrlPath));
        String line = "";
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            CUrl curl = new CUrl();
            curl.name = its[1];
            curl.url = its[0];
            BaseUrls.add(curl);
        }
        br.close();
        System.out.println("base Url load ok...");
    }

    private void validate() throws IOException {

        int fail = 0;
        List<CUrl> success = new ArrayList<CUrl>();
        for(CUrl curl : BaseUrls){
            try{
                int ucount = 0;
                Document doc = Jsoup.connect(curl.url).userAgent(Const.CrawlerUserAgent).timeout(5000).get();
                Elements as = doc.getElementsByTag("a");
                for(Element tag : as){
                    String turl = tag.attr("href");
                    String text = tag.text();
                    if(turl == null || text == null){
                        continue;
                    }
//                    if(turl.indexOf("htm") < 0 || text.length() > 5){
//                        continue;
//                    }
                    if(text.length() <= 5){
                        if(ClassSet.containsKey(text)){
                            ClassSet.get(text).urls.add(turl);
                        }else{
                            Class cls = new Class();
                            cls.name = text;
                            cls.urls.add(turl);
                            ClassSet.put(text,cls);
                        }
                    }
                    ucount++;
                }
                if(ucount < Threshold){
                    System.out.println("error\t" + curl.name);
                }else{
                    System.out.println(curl.name + "\t" + ucount);
                    success.add(curl);
                }
            }catch (Exception e){
                System.out.println("error\t" + curl.name);
            }
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(BaseUrlPath + "_clean"));
        for(CUrl curl : success){
            bw.write(curl.url + "\t" + curl.name + "\n");
        }
        bw.close();
    }

    private void findClass() throws IOException {
        List<Class> ranks = new ArrayList<Class>();
        for(String key : ClassSet.keySet()){
            ranks.add(ClassSet.get(key));
        }
//        Collections.sort(ranks);
        BufferedWriter bw = new BufferedWriter(new FileWriter(BaseUrlPath + "_classr"));
        for(Class cls : ranks){
            if(cls.urls.size() < 4){
                continue;
            }
            bw.write(cls.name + "\t" + cls.urls.size() + "\n");
        }
        bw.close();
    }

    public static void main(String[] args) throws IOException {
        check ck = new check();
        ck.loadBaseUrl();
        ck.validate();
        ck.findClass();
    }


}
