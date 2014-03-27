package cn.ruc.mblank.core.infoGenerator.topic;

import cn.ruc.mblank.db.hbn.model.Event;
import cn.ruc.mblank.util.TimeUtil;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by mblank on 14-3-27.
 */
public class TimeNumber {

    private List<Event> Events;
    private Map<Integer,Integer> NumMaps;

    public TimeNumber(List<Event> scrs){
        this.Events = scrs;
        NumMaps = new TreeMap<Integer, Integer>();
    }

    private String map2Str() {
        StringBuffer res = new StringBuffer();
        for(int date : NumMaps.keySet()){
            res.append(date + ":" + NumMaps.get(date) + ",");
        }
        return res.toString();
    }

    public String getTimeNumber(){
        for(Event et : Events){
            if(et.getPubtime() != null){
                int date = TimeUtil.getDayGMT8(et.getPubtime());
                if(NumMaps.containsKey(date)){
                    NumMaps.put(date,NumMaps.get(date) + 1);
                }else{
                    NumMaps.put(date,1);
                }
            }
        }
        return map2Str();
    }



}
