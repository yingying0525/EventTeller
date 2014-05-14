package cn.ruc.mblank.cache.w2v;

import java.io.*;

/**
 * Created by mblank on 2014/5/13.
 */
public class test {

    private String BaseDir = "data/";

    private void getSmallFile() throws IOException {
        String path = BaseDir + "trainWordsWithTime";
        int num = 0;
        BufferedReader br = new BufferedReader(new FileReader(path));
        BufferedWriter bw = new BufferedWriter(new FileWriter(path + "_small"));
        String line;
        while((line = br.readLine()) != null){
            num++;
            if(num == 100000){
                break;
            }
            bw.write(line + "\n");
        }
        br.close();
        bw.close();
    }

    public static void main(String[] args) throws IOException {
        test ts = new test();
        ts.getSmallFile();
    }
}
