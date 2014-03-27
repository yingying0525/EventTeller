package cn.ruc.mblank;

import cn.ruc.mblank.db.hbn.HSession;
import cn.ruc.mblank.db.hbn.model.Event;
import cn.ruc.mblank.db.hbn.model.EventStatus;
import cn.ruc.mblank.db.hbn.model.Topic;
import cn.ruc.mblank.db.hbn.model.UrlStatus;
import cn.ruc.mblank.index.solr.EventIndex;
import cn.ruc.mblank.util.db.Hbn;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.stat.SessionStatistics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by mblank on 14-3-25.
 */
public class dbTest implements  Runnable{

    private List<UrlStatus> Uss;
    private static Session session;


    public dbTest(){

    }

    public void load(){
        String sql = "from UrlStatus as obj where obj.status = 0";
        Session session = HSession.getSession();
        Uss = Hbn.getElementsFromDB(sql,0,10,session);
        System.out.println(session.isConnected());
    }

    public static void main(String[] args) throws InterruptedException {

        dbTest test = new dbTest();
        test.load();
        Thread thread = new Thread(test);
        thread.run();
        thread.join();

//        System.out.println(test.session.isConnected());
//        test.session.beginTransaction().commit();
//        HSession.closeSession();


    }

    @Override
    public void run() {

        Uss.get(0).setStatus((short)15);

    }
}
