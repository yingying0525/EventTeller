package cn.ruc.mblank.word;

import cn.ruc.mblank.core.word2Vec.domain.Neuron;
import cn.ruc.mblank.core.word2Vec.domain.WordNeuron;
import cn.ruc.mblank.util.Haffman;
import love.cq.util.MapCount;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mblank on 14-4-17.
 */
public class haffman {

    public final String oldPath = "e://share//events_new_words";

    public final String basePath = "e://share//events_base";

    public final String addPath = "e://share//events_add";

    private final int BaseNum = 500 * 10000;


    private Map<String, Neuron> wordMap = new HashMap<String, Neuron>();

    private int layerSize = 100;



    private void getBaseWords() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(oldPath));
        BufferedWriter bw = new BufferedWriter(new FileWriter(addPath));
        String line = "";
        int num = 0;
        while((line = br.readLine()) != null){
            String[] its = line.split("\t");
            if(its.length != 2){
                continue;
            }
            String[] words = its[1].split(" ");
            num += words.length;
            System.out.println(words.length);
            for(String wd : words){
                bw.write(wd + " ");
            }
            bw.write("\n");
            if(num >= BaseNum){
                break;
            }
            if(num % 10000 == 0){
                System.out.println(num);
            }
        }
        bw.close();
        br.close();
    }



    public void buildHaffman(File file) throws IOException {
        long start = System.currentTimeMillis();
        MapCount<String> mc = new MapCount<String>();
        try{
            int line = 0;
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String temp = null;
            while ((temp = br.readLine()) != null) {
                ++line;
                String[] its = temp.split(" ");
                for (String string : its) {
                    mc.add(string);
                }
                if(line % 10000 == 0){
                    System.out.println(line);
                }
            }
        }catch (Exception e){

        }
        long startadd = System.currentTimeMillis();
        System.out.println("read ok...start to build haffman tree" + (startadd - start));

        ///start to read add file
        String line = "";

//        BufferedReader addbr = new BufferedReader(new FileReader(addPath));
//        while((line = addbr.readLine()) != null){
//            String[] its = line.split(" ");
//            for (String string : its) {
//                mc.add(string);
//            }
//        }
//        addbr.close();
        long endadd = System.currentTimeMillis();
        System.out.println("read add ok...start to build haffman tree" + (endadd - start));



        for (Map.Entry<String, Integer> element : mc.get().entrySet()) {
            wordMap.put(element.getKey(), new WordNeuron(element.getKey(), element.getValue(),layerSize));
        }
        new Haffman(layerSize).make(wordMap.values());
        //查找每个神经元
        for (Neuron neuron : wordMap.values()) {
            ((WordNeuron)neuron).makeNeurons() ;
        }
        System.out.println("time spent" + "\t" + (System.currentTimeMillis() - endadd));

    }


    public static void main(String[] args) throws IOException {
        haffman hm = new haffman();
//        File file = new File(hm.basePath);
//        hm.buildHaffman(file);
        hm.getBaseWords();
    }



}
