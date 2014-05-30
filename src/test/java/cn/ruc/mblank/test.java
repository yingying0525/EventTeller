package cn.ruc.mblank;

import cn.ruc.mblank.db.hbn.HSession;
import cn.ruc.mblank.db.hbn.model.Url;
import cn.ruc.mblank.db.hbn.model.UrlStatus;
import cn.ruc.mblank.mq.Sender;
import cn.ruc.mblank.util.Const;
import cn.ruc.mblank.util.db.Hbn;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mblank on 14-5-15.
 */
public class test {

    public static void main(String[] args){
        Sender<Url> sender = new Sender<Url>(Const.MQUrl);
        List<Url> urls = new ArrayList<Url>();
        Url url = new Url();
        url.setId(1);
        url.setUrl("dkdkdk");
        urls.add(url);
        sender.send("UrlQueue",urls);

    }
}
