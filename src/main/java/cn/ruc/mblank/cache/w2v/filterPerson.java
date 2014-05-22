package cn.ruc.mblank.cache.w2v;

import cn.ruc.mblank.core.word2Vec.domain.WordNeuron;
import org.jsoup.Connection;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by mblank on 14-5-14.
 */
public class filterPerson {

    private String BaseDir = "data/";
    private Map<String, WordNeuron> wordMap = new HashMap<String, WordNeuron>();
    private HashSet<String> PersonList = new HashSet<String>();

    private void loadPersonList() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(BaseDir + "personList"));
        String line = "";
        while((line = br.readLine()) != null){
            PersonList.add(line);
        }
        br.close();
        System.out.println("load person list ok..." + "\t" + PersonList.size());
    }

    public void loadVectors() throws IOException {
        long TimeVectorSize = 0;
        long WordVectorSize = 0;
        long words = 0;
        int size = 0;
        try{
            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(BaseDir + "newTrainText_vector.bin")));
            words = dis.readInt();
            size = dis.readInt();
            float vector = 0;
            String key = null;
            double[] totalVector = null;
            for (int i = 0; i < words; i++) {
                double len = 0.0001;
                key = dis.readUTF();

                WordNeuron wn = new WordNeuron(key,0,size);
                //first read totalVector
                totalVector = new double[size];
                for (int j = 0; j < size; j++) {
                    vector = dis.readFloat();
                    len += vector * vector;
                    totalVector[j] = vector;
                }
                len = Math.sqrt(len);
                for (int j = 0; j < size; j++) {
                    totalVector[j] /= len;
                }
                wn.syn0 = totalVector;
                TreeMap<Integer,double[]> dayVectors = new TreeMap<Integer, double[]>();
                //read time vector size
                int timeVectorSize = dis.readInt();
                //TimeVector
                TimeVectorSize += timeVectorSize;
                for(int j = 0 ; j < timeVectorSize; ++j){
                    //read day
                    int day = dis.readInt();
                    double[] oldVectors = new double[size];
                    double oldLen = 0.0001;
                    for(int k = 0 ; k < size; ++k){
                        oldVectors[k] = dis.readFloat();
                        oldLen += oldVectors[k] * oldVectors[k];
                    }
                    oldLen = Math.sqrt(oldLen);
                    for(int k = 0 ; k < size; ++k){
                        oldVectors[k] /= oldLen;
                    }
                    dayVectors.put(day,oldVectors);
                }
                wn.synMap = dayVectors;
                if(!PersonList.contains(key)){
                    continue;
                }
                wordMap.put(key, wn);
            }
        }catch (Exception e){

        }
        System.out.println("wordSize : " + words + "\t timeVectorSize" +  "\t" + TimeVectorSize );
    }

    private void writePersonVector() throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BaseDir + "personVectors"));
        for(String key : wordMap.keySet()){
            oos.writeObject(wordMap.get(key));
        }
        //write null object to mark the end of file
        oos.writeObject(null);
        oos.close();
        System.out.println("");
    }

    public static void main(String[] args) throws IOException {
        filterPerson fp = new filterPerson();
        fp.loadPersonList();
        fp.loadVectors();
        fp.writePersonVector();
        System.out.println(fp.wordMap.size());
    }

}
