package cn.ruc.mblank.core.word2Vec;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;

import cn.ruc.mblank.core.word2Vec.domain.WordEntry;
import cn.ruc.mblank.core.word2Vec.domain.WordNeuron;

public class Word2Vec {

    public static void main(String[] args) throws IOException {
        String inputPath = args[0];
//        String inputPath = "d:\\ETT\\tianyiwords_vector.bin";
        Word2Vec vec = new Word2Vec();
        vec.loadJavaModel(inputPath);

        while(true){
            String str = "李天一";
            System.out.println("please input entity:");
            Scanner sc = new Scanner(System.in);
            str = sc.next();
            System.out.println(str);
            if(str == null){
                break;
            }
            long start = System.currentTimeMillis();
            System.out.println(vec.distance(str));
            System.out.println(System.currentTimeMillis() - start);
        }
        //        System.out.println(vec2.analogy("毛泽东", "毛泽东思想", "邓小平"));
    }

    private Map<String, WordNeuron> wordMap = new HashMap<String, WordNeuron>();

    private int words;
    private int size;
    private int topNSize = 40;

    /**
     * 加载模型
     * 
     * @param path
     *            模型的路径
     * @throws java.io.IOException
     */
    public void loadGoogleModel(String path) throws IOException {
        DataInputStream dis = null;
        BufferedInputStream bis = null;
        double len = 0;
        float vector = 0;
        try {
            bis = new BufferedInputStream(new FileInputStream(path));
            dis = new DataInputStream(bis);
            // //读取词数
            words = Integer.parseInt(readString(dis));
            // //大小
            size = Integer.parseInt(readString(dis));
            String word;
            double[] vectors = null;
            for (int i = 0; i < words; i++) {
                word = readString(dis);
                vectors = new double[size];
                len = 0;
                for (int j = 0; j < size; j++) {
                    vector = readFloat(dis);
                    len += vector * vector;
                    vectors[j] = (float) vector;
                }
                len = Math.sqrt(len);

                for (int j = 0; j < size; j++) {
                    vectors[j] /= len;
                }
                WordNeuron wn = new WordNeuron(word,0,size);
                wn.syn0 = vectors;
                wordMap.put(word, wn);
                dis.read();
            }
        } finally {
            bis.close();
            dis.close();
        }
    }

    /**
     * 加载模型
     * 
     * @param path
     *            模型的路径
     * @throws java.io.IOException
     */
    public void loadJavaModel(String path) throws IOException {
        long TimeVectorSize = 0;
        long WordVectorSize = 0;
        try{
            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(path)));
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
                if(timeVectorSize != 0){
                    System.out.println(timeVectorSize + "\t" +TimeVectorSize + "\t" + key);
                }
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
                wordMap.put(key, wn);
            }
        }catch (Exception e){

        }
        System.out.println("wordSize : " + words + "\t timeVectorSize" + TimeVectorSize );
    }

    private static final int MAX_SIZE = 50;


    /**
     * 近义词
     * @return 
     */
    public TreeSet<WordEntry> analogy(String word0, String word1, String word2) {
        double[] wv0 = getWordVector(word0);
        double[] wv1 = getWordVector(word1);
        double[] wv2 = getWordVector(word2);

        if (wv1 == null || wv2 == null || wv0 == null) {
            return null;
        }
        double[] wordVector = new double[size];
        for (int i = 0; i < size; i++) {
            wordVector[i] = wv1[i] - wv0[i] + wv2[i];
        }
        double[] tempVector;
        String name;
        List<WordEntry> wordEntrys = new ArrayList<WordEntry>(topNSize);
        for (Entry<String, WordNeuron> entry : wordMap.entrySet()) {
            name = entry.getKey();
            if (name.equals(word0) || name.equals(word1) || name.equals(word2)) {
                continue;
            }
            float dist = 0;
            tempVector = entry.getValue().syn0;
            for (int i = 0; i < wordVector.length; i++) {
                dist += wordVector[i] * tempVector[i];
            }
            insertTopN(name, dist, wordEntrys);
        }
        return new TreeSet<WordEntry>(wordEntrys);
    }

    private void insertTopN(String name, float score, List<WordEntry> wordsEntrys) {
        if (wordsEntrys.size() < topNSize) {
            wordsEntrys.add(new WordEntry(name, score));
            return;
        }
        float min = Float.MAX_VALUE;
        int minOffe = 0;
        for (int i = 0; i < topNSize; i++) {
            WordEntry wordEntry = wordsEntrys.get(i);
            if (min > wordEntry.score) {
                min = wordEntry.score;
                minOffe = i;
            }
        }

        if (score > min) {
            wordsEntrys.set(minOffe, new WordEntry(name, score));
        }

    }


    public Set<WordEntry> distance(String queryWord) {

        double[] center = wordMap.get(queryWord).syn0;
        if (center == null) {
            return Collections.emptySet();
        }

        int resultSize = wordMap.size() < topNSize ? wordMap.size() : topNSize;
        TreeSet<WordEntry> result = new TreeSet<WordEntry>();

        double min = Float.MIN_VALUE;
        for (Entry<String, WordNeuron> entry : wordMap.entrySet()) {
            double[] vector = entry.getValue().syn0;
            float dist = 0;
            for (int i = 0; i < vector.length; i++) {
                dist += center[i] * vector[i];
            }

            if (dist > min) {
                result.add(new WordEntry(entry.getKey(), dist));
                if (resultSize < result.size()) {
                    result.pollLast();
                }
                min = result.last().score;
            }
        }
        result.pollFirst();

        return result;
    }

    /**
     * 得到词向量
     * 
     * @param word
     * @return
     */
    public double[] getWordVector(String word) {
        return wordMap.get(word).syn0;
    }

    public static float readFloat(InputStream is) throws IOException {
        byte[] bytes = new byte[4];
        is.read(bytes);
        return getFloat(bytes);
    }

    /**
     * 读取一个float
     * 
     * @param b
     * @return
     */
    public static float getFloat(byte[] b) {
        int accum = 0;
        accum = accum | (b[0] & 0xff) << 0;
        accum = accum | (b[1] & 0xff) << 8;
        accum = accum | (b[2] & 0xff) << 16;
        accum = accum | (b[3] & 0xff) << 24;
        return Float.intBitsToFloat(accum);
    }

    /**
     *
     * @param dis
     * @return
     * @throws IOException
     */
    private static String readString(DataInputStream dis) throws IOException {
        byte[] bytes = new byte[MAX_SIZE];
        byte b = dis.readByte();
        int i = -1;
        StringBuilder sb = new StringBuilder();
        while (b != 32 && b != 10) {
            i++;
            bytes[i] = b;
            b = dis.readByte();
            if (i == 49) {
                sb.append(new String(bytes));
                i = -1;
                bytes = new byte[MAX_SIZE];
            }
        }
        sb.append(new String(bytes, 0, i + 1));
        return sb.toString();
    }

    public int getTopNSize() {
        return topNSize;
    }

    public void setTopNSize(int topNSize) {
        this.topNSize = topNSize;
    }

    public Map<String, WordNeuron> getWordMap() {
        return wordMap;
    }

    public int getWords() {
        return words;
    }

    public int getSize() {
        return size;
    }

}
