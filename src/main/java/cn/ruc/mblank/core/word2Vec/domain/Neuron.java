package cn.ruc.mblank.core.word2Vec.domain;

import java.io.Serializable;

public abstract class Neuron implements Comparable<Neuron> ,Serializable{
    public int freq;
    public Neuron parent;
    public int code;
    
    @Override
    public int compareTo(Neuron o) {
        // TODO Auto-generated method stub
        if (this.freq > o.freq) {
            return 1;
        } else {
            return -1;
        }
    }

}
