package cn.ruc.mblank;

import cn.ruc.mblank.core.infoGenerator.event.MainSentence;
import cn.ruc.mblank.core.infoGenerator.model.Sentence;
import cn.ruc.mblank.db.hbn.HSession;
import cn.ruc.mblank.db.hbn.model.Event;
import cn.ruc.mblank.util.db.Hbn;
import org.hibernate.Session;

import java.util.List;

/**
 * Created by mblank on 14-3-31.
 */
public class SentenceTest {


    public static  void main(String[] args){
        int id = 4783592;
        Session session = HSession.getSession();
        Event et = Hbn.getElementFromDB(session, Event.class, id);
        MainSentence ms = new MainSentence(et);
        List<Sentence> sens = ms.getMainSens(3);
        for(Sentence sen : sens){
            System.out.println(sen.getCurIndex() + "\t" + sen.getCurScore() + "\t" + sen.getCurOriginal());
        }
    }
}
