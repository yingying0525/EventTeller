package cn.ruc.mblank.crawler;

import cn.ruc.mblank.config.JsonConfigModel;
import cn.ruc.mblank.db.hbn.model.Url;
import cn.ruc.mblank.db.hbn.model.WebSite;
import cn.ruc.mblank.util.BloomFilter;
import cn.ruc.mblank.util.Const;
import cn.ruc.mblank.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 1 read web sites from config file
 * 2 parse all the sites, get urls and their titles
 * 3 if title in the web type list,the url will be a second level url
 * 4 update url which are filtered by some rules to db with type 0
 * 5 crawled second level url and parse urls and update the urls with their types
 * 6 after every XX times will update the second level url
 * Created by mblank on 2014/6/13.
 */
public class UrlCrawler {

    private static String Bloom_File_Path;
    private String SaveFolderPath;
    private BloomFilter bloomfilter;
    private Map<String,Integer> UrlTopicMaps =  new HashMap<String,Integer>();
    private List<WebSite> WebSites = new ArrayList<WebSite>();


    public UrlCrawler(){
        Log.getLogger().info("Start TitleCrawler!");
        //read Bloom filter file path from json config file
        JsonConfigModel jcm = JsonConfigModel.getConfig();
        Bloom_File_Path = jcm.UrlsBloomFilterFilePath;
        SaveFolderPath = jcm.HtmlSavePath;
        bloomfilter =  InitBloomFilter();
        loadTopicMap();
        loadWebSites();
        System.out.println("bloom filter init ok...");
    }


    private BloomFilter InitBloomFilter(){
        BloomFilter bloomfilter = new BloomFilter();
        try {
            BufferedReader br = new BufferedReader(new FileReader(Bloom_File_Path));
            String line = "";
            while((line = br.readLine())!=null){
                bloomfilter.add(line.toString().toLowerCase());
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return bloomfilter;
    }

    /**
     * type 2^1 for main
     * type 2^2 for guonei
     * type 2^3 for guoji
     * type ....
     */
    private void loadTopicMap(){
        try {
            BufferedReader br = new BufferedReader(new FileReader(Const.Web_Type_Path));
            String line = "";
            int num = 1;
            while((line = br.readLine()) != null){
                String[] its = line.split("\t");
                num++;
                for(String it : its){
                    UrlTopicMaps.put(it,num);
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * load web sites from config file
     */
    private void loadWebSites(){
        try {
            BufferedReader br = new BufferedReader(new FileReader(Const.WEB_SITES_PATH));
            String line = "";
            while((line = br.readLine()) != null){
                String[] its = line.split("\t");
                WebSite ws = new WebSite();
                ws.name = its[1];
                ws.url = its[0];
                WebSites.add(ws);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Url> get

    private void runTask(){
        int k = 0;
        while(k ++ < 70){
            for(WebSite ws : WebSites){
                try{
                    Document doc = Jsoup.connect(ws.url).userAgent(Const.CrawlerUserAgent).timeout(5000).get();
                    Elements as = doc.getElementsByTag("a");
                    for(Element els)
                }catch (Exception e){

                }
            }
        }
    }




}
