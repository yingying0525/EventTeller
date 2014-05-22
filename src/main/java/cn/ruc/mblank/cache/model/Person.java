package cn.ruc.mblank.cache.model;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by mblank on 14-5-16.
 */
public class Person {

    public String name;
    public List<WordSims> relations = new ArrayList<WordSims>();

    @Override
    public int hashCode(){
        return this.name.hashCode();
    }
}
