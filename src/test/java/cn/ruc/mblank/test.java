package cn.ruc.mblank;

import cn.ruc.mblank.db.hbn.HSession;
import cn.ruc.mblank.db.hbn.model.UrlStatus;
import cn.ruc.mblank.util.db.Hbn;
import org.hibernate.Session;

import java.util.List;

/**
 * Created by mblank on 14-5-15.
 */
public class test {

    public static void main(String[] args){
        Session session = HSession.getSession();
        String sql = "from UrlStatus as obj where obj.status = 0 or obj.status = -1";
        List<UrlStatus> instances = Hbn.getElementsFromDB(sql,0,1000,session);
        int maxId = Hbn.getMaxFromDB(session,UrlStatus.class,"id");
        System.out.println(instances.size() + "\t" + maxId);
        HSession.closeSession();

    }
}
