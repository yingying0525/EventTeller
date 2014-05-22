package cn.ruc.mblank.event;

import cn.ruc.mblank.core.infoGenerator.model.Word;
import cn.ruc.mblank.db.hbn.HSession;
import cn.ruc.mblank.db.hbn.model.WordTime;
import cn.ruc.mblank.util.ChineseSplit;
import cn.ruc.mblank.util.TimeUtil;
import cn.ruc.mblank.util.db.Hbn;
import org.hibernate.Session;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by mblank on 14-4-4.
 */
public class getPerson {

    private final String inputPath = "H:\\other\\events.sql";
    private final String outputPath = "H:\\other\\persons";

    private HashMap<Integer,Integer> TitleChecks = new HashMap<Integer,Integer>();
    private HashMap<Integer,Integer> SameEvent = new HashMap<Integer, Integer>();
    private List<String> FailEvent = new ArrayList<String>();

    private HashMap<Word,TreeMap<Integer,Integer>> WordDays = new HashMap<Word, TreeMap<Integer, Integer>>();


    private Set<String> getPerson(List<Word> wds){
        Set<String> res = new HashSet<String>();
        for(Word wd : wds){
            if(wd.getNature().contains("nr")){
                res.add(wd.getName());
            }
        }
        return res;
    }

    private Set<String> getVerbs(List<Word> wds){
        Set<String> res = new HashSet<String>();
        for(Word wd : wds){
            if(wd.getNature().indexOf("v") == 0){
                res.add(wd.getName());
            }
        }
        return res;
    }


    public void runTask() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(inputPath));
        String line = "";
        int fail = 0;
        int success = 0;
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            if(its.length != 7){
                ++fail;
                continue;
            }
            ++success;
            if(success % 10000 == 0){
                System.out.println(success + "\t" + line);
            }
            int hash = its[1].hashCode();
            int id = 0;
            try{
                id = Integer.parseInt(its[0]);
            }catch (Exception e){
                e.printStackTrace();
                continue;
            }
            if(TitleChecks.containsKey(hash)){
                SameEvent.put(id,TitleChecks.get(hash));
            }else{
                TitleChecks.put(hash,id);
            }
            if(its[3].length() < 20){
                FailEvent.add(its[0]);
            }
        }
        System.out.println(success + "\t" + fail + "\t" + SameEvent.size() + "\t" + FailEvent.size());
        writeSame2Disk();
        writeFail2Disk();
        br.close();
    }

    private void writeSame2Disk() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("h:\\other\\sames"));
        for(int id : SameEvent.keySet()){
            bw.write(id + "\t" + SameEvent.get(id) + "\n");
        }
        bw.close();
    }

    private void writeFail2Disk() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("h:\\other\\fails"));
        for(String id : FailEvent){
            bw.write(id + "\n");
        }
        bw.close();
    }

    private void getWords() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(inputPath));
        String line = "";
        int fail = 0;
        int success = 0;
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            if(its.length != 7){
                continue;
            }
            ++success;
            if(success % 10000 == 0){
                System.out.println(success);
            }
            int hash = its[1].hashCode();
            int id = 0;
            int day = 0;
            try{
                id = Integer.parseInt(its[0]);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                day = TimeUtil.getDayGMT8(sdf.parse(its[2]));
            }catch (Exception e){
                e.printStackTrace();
                continue;
            }
            List<Word> words = ChineseSplit.SplitStrWithPos(its[1]);
            for(Word wd : words){
                if(wd.getNature().indexOf("v") == 0 || wd.getNature().indexOf("n") == 0){
                    if(WordDays.containsKey(wd)){
                        if(WordDays.get(wd).containsKey(day)){
                            WordDays.get(wd).put(day,WordDays.get(wd).get(day) + 1);
                        }else{
                            WordDays.get(wd).put(day,1);
                        }
                    }else{
                        TreeMap<Integer,Integer> days = new TreeMap<Integer, Integer>();
                        days.put(day,1);
                        WordDays.put(wd,days);
                    }
                }
            }
        }
        System.out.println(success + "\t" + fail + "\t" + SameEvent.size() + "\t" + FailEvent.size());
        writeWord2Disk();
        br.close();
    }

    private void writeWord2Disk() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("h:\\other\\wordTime"));
        for(Word wd : WordDays.keySet()){
            bw.write(wd.getName() + "\t" + wd.getNature() + "\t");
            TreeMap<Integer,Integer> days = WordDays.get(wd);
            for(int day : days.keySet()){
                bw.write(day + "," + days.get(day) + " ");
            }
            bw.write("\n");
        }
        bw.close();
    }


    private void updateWordDB() throws IOException {
        Session session = HSession.getSession();
        String wordPath = "h:\\other\\wordTime";
        Set<String> checks = new HashSet<String>();
        BufferedReader br = new BufferedReader(new FileReader(wordPath));
        String line = "";
        int num = 0;
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            if(its.length != 3){
                System.out.println("dddd" + its.length);
                continue;
            }
            if(checks.contains(its[0])){
                continue;
            }
            checks.add(its[0]);
            WordTime wt = new WordTime();
            wt.setName(its[0]);
            wt.setType(its[1]);
            wt.setTimeLine(its[2]);
            num++;
            if(num % 10000 == 0){
                System.out.println(num);
            }
            session.saveOrUpdate(wt);
        }
        br.close();
        System.out.println("start to update");
        Hbn.updateDB(session);
        session.close();
        System.out.println("update ok..");
    }






    public static void main(String[] args) throws IOException {
        getPerson gp = new getPerson();
        gp.updateWordDB();
    }

}
