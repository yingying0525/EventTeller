package cn.ruc.mblank.file;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by mblank on 14-4-4.
 */
public class MultiMap implements Runnable{

    ConcurrentHashMap<Integer,Integer> maps = new ConcurrentHashMap<Integer, Integer>();
    private int TotalThreadNumber = 4;

    @Override
    public void run() {
        String path = "d:\\ETT\\tywords";
        int threadNum = Integer.parseInt(Thread.currentThread().getName());
        System.out.println(threadNum);
        File file = new File(path);
        try {
            readTest(threadNum,file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readTest(int N,File file) throws IOException {



    }

    public static void main(String[] args) throws InterruptedException {
        MultiMap mm = new MultiMap();
        List<Thread> threads = new ArrayList<Thread>();
        for(Integer i = 0 ; i < mm.TotalThreadNumber; i++){
            Thread thread = new Thread(mm);
            thread.setName(i.toString());
            thread.start();
            threads.add(thread);
        }
        for(Thread th : threads){
            th.join();
        }
        System.out.println(mm.maps.size());
    }
}
