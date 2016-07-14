package cn.ruc.mblank.cache;

import cn.ruc.mblank.db.hbn.model.Article;
import cn.ruc.mblank.db.hbn.model.Url;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mblank on 14-5-28.
 * Use New algo. to extract original Html for better News contents.
 */
public class Extractor implements Runnable{

    private String BaseDir = "/mnt/diskc/ETHtmlFiles/";
    private String SaveDir = "/mnt/diskd/NewExtracted/";
    private String UrlDir = "/tmp/urls";
    private File[] Folders;
    private HashMap<Integer,Url> UrlMaps = new HashMap<Integer, Url>();
    private int TotalThreadNum = 28;

    private void loadUrlMap() throws IOException, ParseException {
        BufferedReader br = new BufferedReader(new FileReader(UrlDir));
        String line = "";
        while((line = br.readLine()) != null){
            Url url = new Url();
            String[] its = line.split("\t");
            if(its.length != 4){
                continue;
            }
            url.setId(Integer.parseInt(its[0]));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            url.setCrawltime(sdf.parse(its[1]));
            url.setUrl(its[2]);
            url.setWebsite(its[3]);
            UrlMaps.put(url.getId(),url);
        }
        br.close();
        System.out.println("load url ok.....");
    }

    private void getFolders(){
        File root = new File(BaseDir);
        Folders = root.listFiles();
    }

    private boolean checkValid(Article at){
        if(at == null || at.getTitle() == null || at.getTitle().length() < 5){
            return false;
        }
        if(at.getContent() == null || at.getContent().length() < 20){
            return false;
        }
        return true;
    }

    private void extract(File folder) throws IOException {
            int fail = 0;
            int num = 0;
            File[] files = folder.listFiles();
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SaveDir + folder.getName()));
            for(File file : files){
                num++;
                if(num % 1000 == 0){
                    System.out.println(num);
                }
                int id = 0;
                try{
                    id = Integer.parseInt(file.getName());
                }catch (Exception e){
                    continue;
                }
                if(!UrlMaps.containsKey(id)){
                    fail++;
                    continue;
                }
                cn.ruc.mblank.extractor.article.Extractor etor = new cn.ruc.mblank.extractor.article.Extractor(UrlMaps.get(id),file);
                Article at = etor.getArticle();
                if(!checkValid(at)){
                    fail++;
                    continue;
                }
                oos.writeObject(at);
            }
            oos.close();
            System.out.println(folder.getName() + "\t" + (files.length - fail) + "\t" + fail);
    }

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        Extractor etor = new Extractor();
        etor.loadUrlMap();
        etor.getFolders();
        List<Thread> thds = new ArrayList<Thread>();
        for(int i = 0 ; i < etor.TotalThreadNum; ++i){
            Thread th = new Thread(etor);
            th.setName("" + i);
            th.start();
            thds.add(th);
        }
        for(Thread th : thds){
            th.join();
        }
        System.out.println("all fininshed ..");
    }


    @Override
    public void run() {
        //for one thread
        int curThread = Integer.parseInt(Thread.currentThread().getName());
        int batch = Folders.length / TotalThreadNum;
        int last = 0;
        if(curThread == TotalThreadNum - 1){
            last = Folders.length - batch * TotalThreadNum;
        }
        int start = batch * curThread;
        int end = start + batch + last;
        for(int i = start; i < end; ++i ){
            try {
                extract(Folders[i]);
                System.out.println(curThread + "\t" + i + "\t" + end);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
