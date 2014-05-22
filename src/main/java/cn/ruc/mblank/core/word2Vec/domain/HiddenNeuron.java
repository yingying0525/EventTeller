package cn.ruc.mblank.core.word2Vec.domain;

import java.io.Serializable;

public class HiddenNeuron extends Neuron implements Serializable{
    
    public double[] syn1 ; //hidden->out
    
    public HiddenNeuron(int layerSize){
        syn1 = new double[layerSize] ;
    }
    
}
