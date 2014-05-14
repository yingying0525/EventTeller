package cn.ruc.mblank.core.word2Vec.domain;

import java.util.*;

public class WordNeuron extends Neuron {
    public String name;
    public double[] syn0 = null; //input->hidden
    public List<Neuron> neurons = null;//路径神经元
    public int[] codeArr = null;
    /**
     * for word in different time
     */
    public Map<Integer,double[]> synMap = null;
    public int LastDay = -1;

    public List<Neuron> makeNeurons() {
        if (neurons != null) {
            return neurons;
        }
        Neuron neuron = this;
        neurons = new LinkedList<Neuron>();
        while ((neuron = neuron.parent) != null) {
            neurons.add(neuron);
        }
        Collections.reverse(neurons);
        codeArr = new int[neurons.size()];

        for (int i = 1; i < neurons.size(); i++) {
            codeArr[i - 1] = neurons.get(i).code;
        }
        codeArr[codeArr.length - 1] = this.code;
        return neurons;
    }

    public WordNeuron(String name, int freq, int layerSize) {
        this.name = name;
        this.freq = freq;
        this.syn0 = new double[layerSize];
        Random random = new Random();
        for (int i = 0; i < syn0.length; i++) {
            syn0[i] = (random.nextDouble() - 0.5) / layerSize;
        }
        //init synMap
        synMap = new TreeMap<Integer, double[]>();
    }

}