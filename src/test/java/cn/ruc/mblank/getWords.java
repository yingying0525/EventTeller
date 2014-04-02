package cn.ruc.mblank;

import cn.ruc.mblank.util.ChineseSplit;
import cn.ruc.mblank.util.Const;

import java.io.*;
import java.util.List;

/**
 * Created by mblank on 14-4-2.
 */
public class getWords {

    public static  void main(String[] args) throws IOException {
        String path = "d:\\ETT\\events.sql";
        String outPath = "d:\\ETT\\events_words";

        BufferedWriter bw = new BufferedWriter(new FileWriter(outPath));
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line = "";
        int num = 0;
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            if(its.length < 4){
                continue;
            }
            List<String> twords = ChineseSplit.SplitStr(its[1] + " " + its[3]);
            if(twords.size() > 0){
                bw.write(its[2] + "\t");
            }
            for(String wd : twords){
                bw.write(wd + " ");
            }
            bw.write("\n");
            num++;
            if(num % 10000 == 0){
                System.out.println(num);
            }
        }
        br.close();
    }
}
