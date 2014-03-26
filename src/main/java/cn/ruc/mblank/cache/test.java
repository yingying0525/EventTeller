package cn.ruc.mblank.cache;

import cn.ruc.mblank.db.hbn.model.Event;
import cn.ruc.mblank.db.hbn.model.EventStatus;
import cn.ruc.mblank.index.solr.EventIndex;
import cn.ruc.mblank.util.db.Hbn;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by mblank on 14-3-26.
 */
public class test {

    public static void main(String[] args){
        EventIndex ei = new EventIndex();
        ei.deleteAll();
        Hbn db = new Hbn();
        int last = 0;
        while(true){
            String sql = "from Event as obj";
            List<Event> events = db.getElementsFromDB(sql,last,100000);
            last += events.size();
            List<EventStatus> ets = new ArrayList<EventStatus>();
            if(events.size() == 0){
                break;
            }
            for(Event et : events){
                EventStatus es  = new EventStatus();
                es.setId(et.getId());
                es.setStatus((short)5);
            }
            ei.update(events);
            db.updateDB(ets);
            System.out.println("one batch ok...." + new Date());
        }

    }
}
