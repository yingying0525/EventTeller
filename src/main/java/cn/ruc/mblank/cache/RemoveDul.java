package cn.ruc.mblank.cache;

import cn.ruc.mblank.core.infoGenerator.model.Word;
import cn.ruc.mblank.core.word2Vec.domain.WordNeuron;
import cn.ruc.mblank.db.hbn.model.Event;
import cn.ruc.mblank.index.solr.WordIndex;
import cn.ruc.mblank.index.solr.model.WebWord;
import cn.ruc.mblank.util.ChineseSplit;
import cn.ruc.mblank.util.TimeUtil;
import org.ansj.domain.Term;
import org.ansj.recognition.NatureRecognition;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;
import org.jsoup.Connection;

import java.io.*;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by mblank on 14-4-24.
 */
public class RemoveDul {

//    private String BaseDir = "h:\\";
    private String BaseDir = "/tmp/";
    private String EventPath = "";
    private HashMap<Integer,String> TitleChecks = new HashMap<Integer,String>();
    private HashMap<String,String> SameEventMap = new HashMap<String,String>();
    private HashMap<String,Integer> EventCount = new HashMap<String,Integer>();
    private HashMap<Integer,Event> memEvent = new HashMap<Integer,Event>();

    public RemoveDul(){
        MyStaticValue.userLibrary = "data/extdic";
    }


    private int hashCode(List<Term> scrs){
        int res = 0;
        for(Term tm : scrs){
            res += res * 251 + tm.getName().hashCode() * 31;
        }
        return res;
    }

    private void remove() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(EventPath));
        BufferedWriter bw = new BufferedWriter(new FileWriter(EventPath + "_clean"));
        String line = "";
        int num  = 0;
        int failCount = 0;
        int failContent = 0;
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            if(its.length != 7){
                failCount++;
                continue;
            }
//            if(its[3].length() < 20){
//                failContent++;
//                System.out.println(its[3]);
//                continue;
//            }
//            bw.write(line + "\n");
//
            int number = 0;
            try{
                number = Integer.parseInt(its[5]);
            }catch (Exception e){
                continue;
            }
            MyStaticValue.userLibrary = "h:\\other\\extdic.txt";
            List<Term> terms = ToAnalysis.parse(its[1]);
            int hash = hashCode(terms);
            if(TitleChecks.containsKey(hash)){
                String orgid = TitleChecks.get(hash);
                SameEventMap.put(its[0],orgid);
                EventCount.put(orgid,EventCount.get(orgid) + number);
            }else{
                TitleChecks.put(hash,its[0]);
                EventCount.put(its[0],number);
            }
            num++;
            if(num % 10000 == 0){
                System.out.println(num + "\t" + SameEventMap.size() + "\t" + EventCount.size());
            }
        }
        writeSame2Disk();
        System.out.println("write ok....");
    }

    private void writeSame2Disk() throws IOException {
        String SameEventPath = "H:\\other\\sameEventIds";
        String EventCountPath = "h:\\other\\eventCount";
        //SameEvent
        BufferedWriter bw = new BufferedWriter(new FileWriter(SameEventPath));
        for(String id : SameEventMap.keySet()){
            bw.write(id + "\t" + SameEventMap.get(id) + "\n");
        }
        bw.close();
        //EventCount
        BufferedWriter ecbw = new BufferedWriter(new FileWriter(EventCountPath));
        for(String id : EventCount.keySet()){
            ecbw.write(id + "\t" + EventCount.get(id) + "\n");
        }
        ecbw.close();
    }

    private void loadMemEvent() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(EventPath));
        String line = "";
        int num = 0;
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            if(its.length != 7){
                continue;
            }
            int id = 0;
            int subTopic = 2;
            int number = 0;
            Date date = new Date();
            try{
                id = Integer.parseInt(its[0]);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                date = sdf.parse(its[2]);
                number = Integer.parseInt(its[5]);
                subTopic = Integer.parseInt(its[6]);
            }catch (Exception e){
                continue;
            }
            Event et = new Event();
            et.setId(id);
            et.setTitle(its[1]);
            et.setPubTime(date);
            et.setContent(its[3]);
            et.setImgs(its[4]);
            et.setNumber(number);
            et.setTopic(subTopic);
            memEvent.put(id,et);
            num++;
            if(num % 10000 == 0){
                System.out.println(num);
            }
        }
        br.close();
        System.out.println("load events ok...");
    }


    private void generateNewEvent() throws IOException {
        HashMap<Integer,Integer> SameMap = new HashMap<Integer, Integer>();
        String SameEventPath = "/tmp/sameEventIds";
        //load
        BufferedReader sbr = new BufferedReader(new FileReader(SameEventPath));
        String line = "";
        while((line = sbr.readLine()) != null){
            String[] its = line.split("\t");
            try{
                int id = Integer.parseInt(its[0]);
                int count = Integer.parseInt(its[1]);
                SameMap.put(id,count);
            }catch (Exception e){
                continue;
            }
        }
        sbr.close();
        System.out.println("load map ok...");
        //generate new
        for(int id : SameMap.keySet()){
            int val = SameMap.get(id);
            if(memEvent.containsKey(val)){
                Event valet = memEvent.get(val);
                //update content
                if(valet.getContent().length() < 20 && memEvent.containsKey(id) && memEvent.get(id).getContent().length() > 20){
                    valet.setContent(memEvent.get(id).getContent());
                    if(memEvent.get(id).getImgs() != null && memEvent.get(id).getImgs().length() > 5){
                        valet.setImgs(valet.getImgs() + "!##!" + memEvent.get(id).getImgs());
                    }
                }
                //update number
                valet.setNumber(valet.getNumber() + 1);
            }
        }
        System.out.println("combine event ok....");
        //write to disk
        BufferedWriter bw = new BufferedWriter(new FileWriter("/tmp/new_events.sql"));
        for(int id : memEvent.keySet()){
            Event et = memEvent.get(id);
            if(SameMap.containsKey(id) || et.getContent().length() < 20){
                continue;
            }
//            int day = TimeUtil.getDayGMT8(et.getPubTime());
            //write and split by "\t"
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            bw.write(et.getId() + "\t");
            bw.write(et.getTitle() + "\t");
            bw.write( sdf.format(et.getPubTime()) + "\t");
            bw.write(et.getContent() + "\t");
            bw.write(et.getImgs() + "\t");
            bw.write(et.getNumber() + "\t");
            bw.write(et.getTopic() + "\n");
//            bw.write(day + "\n");
        }
        bw.close();
        System.out.println("write ok....");
    }


    private void getRelatedWord() throws IOException {
        MyStaticValue.userLibrary = BaseDir + "extdic";
        BufferedReader br = new BufferedReader(new FileReader(EventPath));
        String line = "";
        //word,nature,time,word,count
        HashMap<String,Map<String,TreeMap<Integer,HashMap<String,Integer>>>> related = new HashMap<String, Map<String, TreeMap<Integer, HashMap<String, Integer>>>>();
        BufferedWriter bw = new BufferedWriter(new FileWriter(BaseDir + "wordPair"));
        int num = 0;
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            if(its.length != 7){
                continue;
            }
            int number = 0;
            int day = 0;
            try{
                number = Integer.parseInt(its[5]);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                day = TimeUtil.getDayGMT8(sdf.parse(its[2]));
            }catch (Exception e){
                continue;
            }
            List<Word> words = ChineseSplit.SplitStrWithPos(its[1]);
            for(Word wd : words){
                if(wd.getNature().indexOf("n") == 0){
                    for(Word tmp : words){
                        if(tmp.equals(wd)){
                            continue;
                        }
                        if(tmp.getNature().indexOf("n")==0 || tmp.getNature().indexOf("v") == 0){
                            bw.write(wd.getName() + "\t" + tmp.getName() + "\t" + tmp.getNature() + "\t" + day + "\t" + number + "\t" + its[0] + "\n");
                        }
                    }
                }
            }
            num++;
            if(num % 10000 == 0){
                System.out.println(num);
            }
        }
        br.close();
        bw.close();
    }


    private void combinePair() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(BaseDir + "wordPair"));
        BufferedWriter bw = new BufferedWriter(new FileWriter(BaseDir + "relatedWords"));
        String line = "";
        //word,day,type,word,id,number
        HashMap<String,TreeMap<Integer,HashMap<String,HashMap<String,HashMap<Integer,Integer>>>>> Relatedwords = new HashMap<String, TreeMap<Integer, HashMap<String, HashMap<String, HashMap<Integer, Integer>>>>>();
        int num = 0;
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            int id = Integer.parseInt(its[5]);
            int number = Integer.parseInt(its[4]);
            int day = Integer.parseInt(its[3]);
            TreeMap<Integer,HashMap<String,HashMap<String,HashMap<Integer,Integer>>>> days = new TreeMap<Integer, HashMap<String, HashMap<String, HashMap<Integer, Integer>>>>();
            HashMap<String,HashMap<String,HashMap<Integer,Integer>>> natures = new HashMap<String, HashMap<String, HashMap<Integer, Integer>>>();
            HashMap<String,HashMap<Integer,Integer>> related = new HashMap<String, HashMap<Integer, Integer>>();
            HashMap<Integer,Integer> ids = new HashMap<Integer, Integer>();
            if(Relatedwords.containsKey(its[0])){
                days = Relatedwords.get(its[0]);
            }
            if(days.containsKey(day)){
                natures = days.get(day);
            }
            if(natures.containsKey(its[2])){
                related = natures.get(its[2]);
            }
            if(related.containsKey(its[1])){
                ids = related.get(its[1]);
            }
            ids.put(id,number);
            related.put(its[1],ids);
            natures.put(its[2],related);
            days.put(day,natures);
            Relatedwords.put(its[0],days);
            num++;
            if(num % 10000 == 0){
                System.out.println(num / 10000);
            }
        }
        System.out.println("start to write to disk");
        for(String word : Relatedwords.keySet()){
            bw.write(word + "\t");
            for(int day : Relatedwords.get(word).keySet()){
                bw.write(day + " ");
                for(String type : Relatedwords.get(word).get(day).keySet()){
                    bw.write(type + ",");
                    for(String wb : Relatedwords.get(word).get(day).get(type).keySet()){
                        bw.write(wb + ";");
                        for(int id : Relatedwords.get(word).get(day).get(type).get(wb).keySet()){
                            bw.write(id + "|" + Relatedwords.get(word).get(day).get(type).get(wb).get(id) + ";");
                        }
                        bw.write(",");
                    }
                    bw.write(" ");
                }
                bw.write("\t");
            }
            bw.write("\n");
        }
        br.close();
        bw.close();
    }

    private void filterEventsOfPerson() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(EventPath));
        String line = "";
        BufferedWriter bw = new BufferedWriter(new FileWriter(this.BaseDir + "pids"));
        int num=0;
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            if(its.length != 7){
                continue;
            }
            MyStaticValue.userLibrary = this.BaseDir + "extdic";
            List<Term> terms = NlpAnalysis.parse(its[1]);
            HashSet<String> checks = new HashSet<String>();
            for(Term tm : terms){
                if(tm.getNatrue().natureStr.indexOf("nr") == 0 && !checks.contains(tm.getName())){
                    bw.write(tm.getName() + "\t" + its[0] + "\t" + its[2] + "\n");
                    checks.add(tm.getName());
                }
            }
            if(num++ % 10000 == 0){
                System.out.println(num);
            }
        }
        bw.close();
        br.close();
    }

    private void getPersonIds() throws IOException, ParseException {
        BufferedReader br = new BufferedReader(new FileReader(this.BaseDir + "pids"));
        String line = "";
        HashMap<String,List<String>> maps = new HashMap<String, List<String>>();
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            int day = TimeUtil.getDayGMT8(sdf.parse(its[2]));
            if(maps.containsKey(its[0])){
                maps.get(its[0]).add(its[1] + "\t" + day);
            }else{
                List<String> tmps = new ArrayList<String>();
                tmps.add(its[1] + "\t" + day);
                maps.put(its[0],tmps);
            }
        }
        System.out.println("start to write to disk");
        BufferedWriter bw = new BufferedWriter(new FileWriter(this.BaseDir + "names"));
        int num = 0;
        int fail = 0;
        for(String name : maps.keySet()){
            num++;
            if(maps.get(name).size() < 10){
                fail++;
                continue;
            }
            bw.write(name + ";");
            for(String val : maps.get(name)){
                bw.write(val + ";");
            }
            bw.write("\n");
        }
        System.out.println(fail + "\t" + num);
        bw.close();
    }


    private void addEmptyDay(){

    }

    //id title time content imgs number topic;
    private void getWordTimeLine() throws IOException, ParseException {
        BufferedReader br = new BufferedReader(new FileReader(EventPath));
        String line = "";
        int num = 0;
        HashMap<String,TreeMap<Integer,Integer>> words = new HashMap<String, TreeMap<Integer, Integer>>();
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            int number = Integer.parseInt(its[5]);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            int day = TimeUtil.getDayGMT8(sdf.parse(its[2]));
            HashSet<String> checks = new HashSet<String>();
            List<Term> terms = ToAnalysis.parse(its[1]);
            new NatureRecognition(terms).recognition();
            for(Term tm : terms){
                if(tm.getNatrue().natureStr.indexOf("nr") == 0 && !checks.contains(tm.getName()) && tm.getName().length() > 1){
                    if(words.containsKey(tm.getName())){
                        if(words.get(tm.getName()).containsKey(day)){
                            words.get(tm.getName()).put(day,words.get(tm.getName()).get(day) + number);
                        }else{
                            words.get(tm.getName()).put(day,number);
                        }
                    }else{
                        TreeMap<Integer,Integer> days = new TreeMap<Integer, Integer>();
                        days.put(day,number);
                        words.put(tm.getName(),days);
                    }
                }
            }
            num++;
            if(num % 10000 == 0){
                System.out.println(num);
            }
        }
        br.close();
        System.out.println("start to write to disk");
        BufferedWriter bw = new BufferedWriter(new FileWriter(BaseDir + "word_timeline"));
        for(String word : words.keySet()){
            if(words.get(word).size() < 5){
                continue;
            }
            bw.write(word + "\t");
            //add empty day
            int curDay = -1;
            for(int day : words.get(word).keySet()){
                if(day < 0){
                    continue;
                }
                if(curDay < 0){
                    curDay = day;
                }
                while(curDay < day){
                    bw.write(curDay + ","+"0" + " ");
                    curDay++;
                }
                bw.write(day + "," + words.get(word).get(day) + " ");
            }
            bw.write("\n");
        }
        bw.close();
    }


    private void parseTitle() throws IOException, ParseException {
        BufferedReader br = new BufferedReader(new FileReader(BaseDir + "personList"));
        BufferedWriter bw = new BufferedWriter(new FileWriter(BaseDir + "wordPair"));
        String line = "";
        HashSet<String> names = new HashSet<String>();
        while((line = br.readLine()) != null){
            names.add(line);
        }
        br = new BufferedReader(new FileReader(EventPath));
        int num = 0;
        //word , word type day number id
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            int number = Integer.parseInt(its[5]);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            int day = TimeUtil.getDayGMT8(sdf.parse(its[2]));
            List<Term> terms = ToAnalysis.parse(its[1]);
            new NatureRecognition(terms).recognition();
            HashSet<String> checks = new HashSet<String>();
            for(Term tm : terms){
                if(!names.contains(tm.getName()) || checks.contains(tm.getName())){
                    continue;
                }
                HashSet<String> schecks = new HashSet<String>();
                for(Term stm : terms){
                    if(stm.getName().equals(tm.getName()) || stm.getName().length() < 2 || schecks.contains(stm.getName())){
                        continue;
                    }
                    String nature = stm.getNatrue().natureStr;
                    if(nature.indexOf("nr") < 0 && nature.indexOf("ns") < 0 && nature.indexOf("v") < 0){
                        continue;
                    }
                    schecks.add(stm.getName());
                    bw.write(tm.getName() + "\t" + stm.getName() + "\t" + stm.getNatrue().natureStr + "\t" + day + "\t" + number + "\t" + its[0] + "\n");
                }
            }
            num++;
            if(num % 10000 == 0){
                System.out.println(num);
            }
        }
        System.out.println("write ok...");
        bw.close();
        br.close();
    }



    private void updateWordIndex() throws IOException {
        BufferedReader tbr = new BufferedReader(new FileReader(BaseDir + "word_timeline"));
        String line = "";
        HashMap<String,WebWord> words = new HashMap<String, WebWord>();
        while((line = tbr.readLine()) != null){
            String[] its = line.split("\t");
            WebWord word = new WebWord();
            word.setName(its[0]);
            word.setWordTimeNumber(its[1]);
            words.put(its[0],word);
        }
        System.out.println("load word timeline ok...");
        tbr = new BufferedReader(new FileReader(BaseDir + "relatedWords"));
        while((line = tbr.readLine()) != null){
            String[] its = line.split("\t");
            StringBuffer tmp = new StringBuffer();
            for(int i = 1; i < its.length; ++i){
                tmp.append(its[i] + "\t");
            }
            if(words.containsKey(its[0])){
                words.get(its[0]).setRelatedWords(tmp.toString());
            }
        }
        System.out.println("load related word ok...");
        WordIndex wi = new WordIndex();
        wi.deleteAll();
        List<WebWord> updates = new ArrayList<WebWord>();
        for(String key : words.keySet()){
            updates.add(words.get(key));
        }
        wi.update(updates);
        System.out.println("update ok...");
    }

    private void filterPersonVector() throws IOException {
        HashSet<String> persons = new HashSet<String>();
        List<WordNeuron> res = new ArrayList<WordNeuron>();
        BufferedReader br = new BufferedReader(new FileReader(BaseDir + "personList"));
        BufferedWriter bw = new BufferedWriter(new FileWriter(BaseDir + "pvectorsnum"));
        String line = "";
        while((line = br.readLine()) != null){
            persons.add(line);
        }
        br.close();
        System.out.println("load person ok..");
        //wordSize,vectorSize
        //
        int num = 0;
        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(BaseDir + "trainWordsWithTime_vector.bin")));
        int wordSize = dis.readInt();
        int vectorSize = dis.readInt();
        int vnum = 0;
        System.out.println(wordSize + "\t" + vectorSize);
        for(int i =0; i<wordSize; ++i){
            String name = "";
            float vector = 0;
            double[] totalVector = new double[vectorSize];
            double len = 0.0001;
            name = dis.readUTF();
            WordNeuron wn = new WordNeuron(name,0,vectorSize);
            for (int j = 0; j < vectorSize; j++) {
                vector = dis.readFloat();
                len += vector * vector;
                totalVector[j] = vector;
            }
            len = Math.sqrt(len);
            for (int j = 0; j < vectorSize; j++) {
                totalVector[j] /= len;
            }
            wn.syn0 = totalVector;
            int timeVectorSize = dis.readInt();
            TreeMap<Integer,double[]> dayVectors = new TreeMap<Integer, double[]>();
            for(int j = 0 ; j < timeVectorSize; ++j){
                //read day
                int day = dis.readInt();
                double[] dvector = new double[vectorSize];
                for(int k = 0 ; k < vectorSize; ++k){
                    dvector[k] = dis.readFloat();
                }
                dayVectors.put(day,dvector);
            }
            wn.synMap = dayVectors;
            if(persons.contains(name)){
                num++;
                vnum += timeVectorSize;
//                System.out.println(name + "\t" + num + "\t" + timeVectorSize);
                bw.write(name + "\t" + timeVectorSize + "\n");
                res.add(wn);
            }
            if(i >= 2372500){
                break;
            }
            if(i % 10000 ==0){
                System.out.println(i);
            }
        }
        dis.close();
        bw.close();
        BufferedWriter nbw = new BufferedWriter(new FileWriter(BaseDir + "pvectors"));
        for(WordNeuron wn : res){
            nbw.write(wn.name + "\t");
            for(double v : wn.syn0){
                nbw.write(v + " ");
            }
            nbw.write("\t" + wn.synMap.size() + "\n");
            for(int day : wn.synMap.keySet()){
                nbw.write(day + "\t");
                for(double tv : wn.synMap.get(day)){
                    nbw.write(tv + " ");
                }
                nbw.write("\n");
            }
        }
        nbw.close();
        System.out.println(num + "\t" + persons.size() + "\t" + vnum);
    }

    private void getTopicWordIndex() throws IOException, ParseException {
        HashMap<String,HashMap<String,TreeMap<Integer,HashSet<Integer>>>> MemIndex = new HashMap<String, HashMap<String, TreeMap<Integer, HashSet<Integer>>>>();
        BufferedReader br = new BufferedReader(new FileReader(BaseDir + "pevents"));
        String line = "";
        int num = 0;
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            int id = Integer.parseInt(its[0]);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            int day = TimeUtil.getDayGMT8(sdf.parse(its[2]));
            List<Term> terms = ToAnalysis.parse(its[1]);
            new NatureRecognition(terms).recognition();
            HashSet<String> nchecks = new HashSet<String>();
            for(Term tm : terms){
                if(nchecks.contains(tm.getName()) || tm.getName().length() < 2 || tm.getNatrue().natureStr.indexOf("nr") < 0){
                    continue;
                }
                nchecks.add(tm.getName());
                HashSet<String> vchecks  = new HashSet<String>();
                for(Term vtm : terms){
                    if(vchecks.contains(vtm.getName()) || vtm.getName().length() < 2 || (vtm.getNatrue().natureStr.indexOf("n") < 0
                        && vtm.getNatrue().natureStr.indexOf("v") < 0) || vtm.getName().equals(tm.getName())){
                        continue;
                    }
                    vchecks.add(vtm.getName());
                    //add to memory index
                    if(MemIndex.containsKey(tm.getName())){
                        if(MemIndex.get(tm.getName()).containsKey(vtm.getName())){
                            if(MemIndex.get(tm.getName()).get(vtm.getName()).containsKey(day)){
                                MemIndex.get(tm.getName()).get(vtm.getName()).get(day).add(id);
                            }else{
                                HashSet<Integer> ids = new HashSet<Integer>();
                                ids.add(id);
                                MemIndex.get(tm.getName()).get(vtm.getName()).put(day,ids);
                            }
                        }else{
                            TreeMap<Integer,HashSet<Integer>> days = new TreeMap<Integer, HashSet<Integer>>();
                            HashSet<Integer> ids = new HashSet<Integer>();
                            ids.add(id);
                            days.put(day,ids);
                            MemIndex.get(tm.getName()).put(vtm.getName(),days);
                        }
                    }else{
                        HashMap<String,TreeMap<Integer,HashSet<Integer>>> words = new HashMap<String, TreeMap<Integer, HashSet<Integer>>>();
                        TreeMap<Integer,HashSet<Integer>> days = new TreeMap<Integer, HashSet<Integer>>();
                        HashSet<Integer> ids = new HashSet<Integer>();
                        ids.add(id);
                        days.put(day,ids);
                        words.put(vtm.getName(),days);
                        MemIndex.put(tm.getName(),words);
                    }
                }
            }
            num++;
            if(num % 10000 == 0){
                System.out.println(num);
            }
        }
        br.close();
        //write to disk
        //format
        //word,pairnum
        //w1,daynum
        //d1,ids
        //d2,ids
        System.out.println("start to write to disk");
        BufferedWriter bw = new BufferedWriter(new FileWriter(BaseDir + "MemTopicIndex"));
        for(String word : MemIndex.keySet()){
            bw.write(word + "\t" + MemIndex.get(word).size() + "\n");
            for(String wb : MemIndex.get(word).keySet()){
                bw.write(wb + "\t" + MemIndex.get(word).get(wb).size() + "\n");
                for(int day : MemIndex.get(word).get(wb).keySet()){
                    bw.write(day + "\t");
                    for(int id : MemIndex.get(word).get(wb).get(day)){
                        bw.write(id + " ");
                    }
                    bw.write("\n");
                }
            }
        }
        System.out.println("write ok...");
    }

    class TopicPair{
        public int number;
        public HashSet<String> ids = new HashSet<String>();
    }

    private void getTopicInfo() throws IOException {
        HashMap<String,HashMap<String,TopicPair>> res = new HashMap<String, HashMap<String, TopicPair>>();
        BufferedReader br = new BufferedReader(new FileReader(BaseDir + "topics"));
        String line = "";
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            if(res.containsKey(its[2])){
                if(res.get(its[2]).containsKey(its[3])){
                    res.get(its[2]).get(its[3]).number += Integer.parseInt(its[1]);
                    res.get(its[2]).get(its[3]).ids.add(its[0] + "|" + its[1]);
                }else{
                    TopicPair tp = new TopicPair();
                    tp.number = Integer.parseInt(its[1]);
                    HashSet<String> ids = new HashSet<String>();
                    ids.add(its[0] + "|" + its[1]);
                    tp.ids = ids;
                    res.get(its[2]).put(its[3],tp);
                }
            }else{
                HashMap<String,TopicPair> objs = new HashMap<String, TopicPair>();
                TopicPair tp = new TopicPair();
                tp.number = Integer.parseInt(its[1]);
                HashSet<String> ids = new HashSet<String>();
                ids.add(its[0] + "|" + its[1]);
                tp.ids = ids;
                objs.put(its[3],tp);
                res.put(its[2],objs);
            }
        }
        br.close();
        System.out.println("get topic info ok...");
        BufferedWriter bw = new BufferedWriter(new FileWriter(BaseDir + "topicInfos"));
        for(String word : res.keySet()){
            bw.write(word + "\t");
            for(String pair : res.get(word).keySet()){
                bw.write(pair + "," + res.get(word).get(pair).number + ",");
                for(String id : res.get(word).get(pair).ids){
                    bw.write(id + " ");
                }
                bw.write(";");
            }
            bw.write("\n");
        }
        bw.close();
        System.out.println("write to disk ok...");
    }


    private void getWord() throws IOException, ParseException {
        getWordTimeLine();
        parseTitle();
        combinePair();
        updateWordIndex();
    }

    public static void main(String[] args) throws IOException, ParseException {
        RemoveDul rd = new RemoveDul();
        rd.BaseDir = "d:\\ETT\\";
        rd.EventPath = rd.BaseDir + "events.sql";
        rd.getTopicInfo();
    }

}
