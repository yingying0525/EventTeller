package cn.ruc.mblank.file;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by mblank on 14-4-17.
 */
public class MultiRead implements Runnable{

    private static final String Path = "e://share//events";
    private static BufferedReader br = null;
    private List<String> list = new ArrayList<String>();
    private Map<Integer,Integer> Ids = new HashMap<Integer,Integer>();
    private List<String> Sames = new ArrayList<String>();
    private int Total = 0;

    static{
        try {
            br = new BufferedReader(new FileReader(Path),10);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void run() {
        String line = null;
        int count = 1000;
        while(true) {
            //System.out.println(Thread.currentThread().getName());
            synchronized(br) {
                try {
                    while((line = br.readLine()) != null && --count > 0) {
                        Total++;
                        if(Total % 10000 == 0){
                            System.out.println(Total + "\t" + Sames.size());
                        }
                        String[] its = line.split("@##@");
                        if(its.length < 2){
                            continue;
                        }
                        int hash = its[1].hashCode();
                        if(Ids.containsKey(hash)){
//                            System.out.println(Ids.get(hash) + "\t" + its[0]);
                            Sames.add(Ids.get(hash) + "\t" + its[0]);
                        }else{
                            Ids.put(hash,Integer.parseInt(its[0]));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(1);
                count = 1000;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(line == null)
                break;
        }
    }


    public void display(List <String> list) {
        for(String str:list) {
            System.out.println(str);
        }
        System.out.println(list.size());
    }





    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();
        List<Thread> thds = new ArrayList<Thread>();
        MultiRead mr = new MultiRead();
        int ThreadNum = 2; //38085
        for(int i = 0 ; i < ThreadNum; ++i){
            Thread td = new Thread(mr,"a");
            thds.add(td);
            td.start();
        }

        for(int i = 0 ; i < ThreadNum; ++i){
            thds.get(i).join();
        }
        System.out.println(System.currentTimeMillis() - start);

    }


    }
