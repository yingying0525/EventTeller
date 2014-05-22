package cn.ruc.mblank.cache;

import cn.ruc.mblank.util.ChineseSplit;
import com.alibaba.fastjson.JSON;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;
import org.jsoup.Connection;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @deprecated
 * Created by mblank on 14-3-26.
 */
public class test {

    private String BaseDir = "e:\\other\\";

    private void generateData() throws IOException {
        String base = "data/";
        MyStaticValue.userLibrary = base + "extdic";
        String path = base + "events.sql";
        String outpath = base + "trainWordsWithTime";
        BufferedReader br = new BufferedReader(new FileReader(path));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outpath));
        String line = "";
        int num = 0;
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            if(its.length != 7){
                continue;
            }
            int number = 0;
            try{
                number = Integer.parseInt(its[5]);
            }catch (Exception e){
                continue;
            }
            List<Term> tms = ToAnalysis.parse(its[1] + " " + its[3]);
            bw.write(its[2] + "\t");
            for(Term tm : tms){
                bw.write(tm.getName() + " ");
            }
            bw.write("\n");
            num++;
            if(num % 10000 == 0){
                System.out.println(num);
            }
        }
        br.close();
        bw.close();
        System.out.println("trainWord ok....");
    }

    private void addDay() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(BaseDir + "idDays"));
        String line = "";
        HashMap<String,String> days = new HashMap<String,String>();
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            days.put(its[0],its[1]);
        }
        System.out.println(days.size());
        br = new BufferedReader(new FileReader(BaseDir + "NewsSimHash"));
        BufferedWriter bw = new BufferedWriter(new FileWriter(BaseDir + "NewsSimHashD"));
        while((line = br.readLine()) != null){
            String[] its = line.split(" ");
            if(days.containsKey(its[0])){
                bw.write(its[0] + "\t" + days.get(its[0]) + "\t" + its[1] + "\t" + its[2] + "\n");
            }
        }
        br.close();
        bw.close();
    }

    private void updateWordIndex(){

    }

    class jsonnode{
        public String name;
        public String group;
    }

    class jsonlink{
        public String source;
        public String target;
        public String value;
    }

    class jsontest{
        public List<jsonnode> nodes = new ArrayList<jsonnode>();
        public List<jsonlink> links = new ArrayList<jsonlink>();
    }

    private void testjson(){
        jsontest jt = new jsontest();
        jsonnode jn = new jsonnode();
        jn.name = "a";
        jn.group = "1";
        jt.nodes.add(jn);

        jsonnode jn2 = new jsonnode();
        jn2.name = "b";
        jn2.group = "1";
        jt.nodes.add(jn2);

        jsonlink jl = new jsonlink();
        jl.source = "a";
        jl.target = "b";
        jl.value="1";
        jt.links.add(jl);


//        jsonlink jl2 = new jsonlink();
//        jl2.source = "b";
//        jl2.target = "a";
//        jl2.value="3";
//        jt.links.add(jl2);

        String res = JSON.toJSONString(jt);
        System.out.println(res);
    }

    class pair{
        public String ea;
        public String eb;
        public Double score;
    }

    private void generateEventGraph() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(BaseDir + "EventSims"));
        String line = "";
        HashMap<String,HashMap<String,List<pair>>> idMaps = new HashMap<String, HashMap<String, List<pair>>>();
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            String key = its[3];
            pair pi = new pair();
            pi.ea = its[0];
            pi.eb = its[1];
            pi.score = Double.parseDouble(its[2]);
            if(idMaps.containsKey(key)){
                if(idMaps.get(key).containsKey(its[4])){
                    idMaps.get(key).get(its[4]).add(pi);
                }else{
                    List<pair> ids = new ArrayList<pair>();
                    ids.add(pi);
                    idMaps.get(key).put(its[4],ids);
                }
            }else{
                List<pair> ids = new ArrayList<pair>();
                ids.add(pi);
                HashMap<String,List<pair>> tmps = new HashMap<String, List<pair>>();
                tmps.put(its[4],ids);
                idMaps.put(key,tmps);
            }
        }
        System.out.println("load keys ok...");
//        HashMap<String,String> titleMaps = new HashMap<String, String>();
//        BufferedReader tbr = new BufferedReader(new FileReader(BaseDir + "events.sql"));
//        while((line = tbr.readLine()) != null){
//            String[] its = line.split("\t");
//            titleMaps.put(its[0],its[1]);
//        }
//        tbr.close();


    }

    public static void main(String[] args) throws IOException {
        test t = new test();
//        t.testjson();
        String tess = "3446987!--!李亚鹏硕士毕业&nbsp;盘点商学院离婚明星校友@##@2773700!--!王菲李亚鹏离婚 盘点长江商学院离婚的商界人士@##@0.6277535265426863 2897252!--!乌鲁木齐民政局否认为王菲李亚鹏办理离婚手续@##@2905767!--!新疆四城";
        String[] its = tess.split("@##@");
        System.out.println(its.length);
    }

}
