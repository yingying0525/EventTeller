package cn.ruc.mblank.cache.crawler;

import cn.ruc.mblank.config.JsonConfigModel;
import cn.ruc.mblank.cache.crawler.url.MUrl;
import cn.ruc.mblank.db.hbn.model.WebSite;
import cn.ruc.mblank.util.BloomFilter;
import cn.ruc.mblank.util.Const;
import cn.ruc.mblank.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;

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
    private static String UrlSavePath;
    private String SaveFolderPath;
    private BloomFilter bloomfilter;
    private Map<String,Integer> UrlTopicMaps =  new HashMap<String,Integer>();
    private List<WebSite> WebSites = new ArrayList<WebSite>();
    private Set<MUrl> MainUrls = new HashSet<MUrl>();
    private Set<MUrl> SecondUrls = new HashSet<MUrl>();


    public UrlCrawler(){
        Log.getLogger().info("Start TitleCrawler!");
        //read Bloom filter file path from json config file
        JsonConfigModel jcm = JsonConfigModel.getConfig();
        Bloom_File_Path = jcm.UrlsBloomFilterFilePath;
        SaveFolderPath = jcm.HtmlSavePath;
        UrlSavePath = "./Urls/";
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

    private void getSecondLevelUrls() {
        MainUrls = new HashSet<MUrl>();
        SecondUrls = new HashSet<MUrl>();
        for (WebSite ws : WebSites) {
            try {
                Document doc = Jsoup.connect(ws.url).userAgent(Const.CrawlerUserAgent).timeout(5000).get();
                Elements as = doc.getElementsByTag("a");
                for (Element a : as) {
                    //if a.href contains htm and the length of a.title is bigger than 10,then it is a news
                    //if a.title is contained in WebType,then it is a second level url
                    String title = a.text();
                    String href = a.absUrl("href");
                    if (href == null || title == null || href.indexOf("http") < 0) {
                        continue;
                    }
                    if (href.contains("htm") && title.length() > 10) {
                        MUrl url = new MUrl();
                        url.crawlTime = new Date();
                        url.title = title;
                        url.url = href;
                        url.level = 1;
                        url.subTopic = 2;
                        url.webSite = ws.name;
                        MainUrls.add(url);
                    }
                    if(UrlTopicMaps.containsKey(title)){
                        MUrl url = new MUrl();
                        url.title = title;
                        url.url = href;
                        url.level = 2;
                        url.webSite = ws.name;
                        url.subTopic = 1 << UrlTopicMaps.get(title);
                        SecondUrls.add(url);
                    }
                }
            } catch (Exception e) {
                System.out.println("can't parse " + ws.url);
            }
        }
    }

    private void getUrlsFromSecondLevel(){
        for(MUrl url : SecondUrls){
            try{
                int num = 0;
                Document doc = Jsoup.connect(url.url).userAgent(Const.CrawlerUserAgent).timeout(5000).get();
                Elements as = doc.getElementsByTag("a");
                for(Element a : as){
                    String title = a.text();
                    String href = a.absUrl("href");
                    if (href == null || title == null) {
                        continue;
                    }
                    if (href.contains("htm") && title.length() > 10) {
                        MUrl surl = new MUrl();
                        surl.title = title;
                        surl.url = href;
                        surl.level = 2;
                        surl.crawlTime = new Date();
                        surl.subTopic = url.subTopic;
                        surl.webSite = url.webSite;
                        if(!MainUrls.contains(surl)){
                            MainUrls.add(surl);
                            num++;
                        }
                    }
                }
                System.out.println("parse " + num + " url from " + url.url);
            }catch (Exception e){
                System.out.println("can't parse " + url.url);
            }
        }
    }

    public void writerToBloomFile(Set<MUrl> urls){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(Bloom_File_Path),true));
            for(MUrl url : urls){
                bw.write(url.url+"\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeUrls2Disk() throws IOException {
        String datePath =  new Long(new Date().getTime()).toString();
        String urlPath = UrlSavePath  + datePath;
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(urlPath));
        Set<MUrl> res = new HashSet<MUrl>();
        int id  = 0;
        for(MUrl url : MainUrls){
            if(!bloomfilter.contains(url.url.toLowerCase())){
                //add to bloom filter
                bloomfilter.add(url.url.toLowerCase());
                url.id = id++;
                res.add(url);
                oos.writeObject(url);
            }
        }
        oos.writeObject(null);
        oos.close();
        System.out.println("get " + res.size() + " new urls");
        writerToBloomFile(res);
        //start a new thread to download htmls

    }

    private void downloadHtmls(){

    }

    private void runTask() throws IOException {
        getSecondLevelUrls();
        getUrlsFromSecondLevel();
        writeUrls2Disk();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        UrlCrawler uc = new UrlCrawler();
        while(true){
            uc.runTask();
            System.out.println("start to sleep for 10 minutes");
            Thread.sleep(10 * 60 * 1000);
        }
    }




}
