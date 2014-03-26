package cn.ruc.mblank;

import cn.ruc.mblank.db.hbn.model.Event;
import cn.ruc.mblank.util.db.Hbn;

/**
 * Created by mblank on 14-3-25.
 */
public class dbTest {

    public static void main(String[] args){
        Hbn db = new Hbn();
        String hql = "select max(id) from Event";
        int maxid = db.getMaxFromDB(Event.class,"id");
        System.out.println(maxid);
    }
}
