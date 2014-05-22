package cn.ruc.mblank.util.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.ruc.mblank.db.hbn.HSession;
import com.sun.accessibility.internal.resources.accessibility;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;


public class Hbn {


    public <T> List<T> getElementsFromDB(String hql,int start ,int maxNum) {
        List<T> result = new ArrayList<T>();
        Query query =  HSession.getSession().createQuery(hql);
        if(maxNum > 0){
            query.setMaxResults(maxNum);
        }
        if(start >= 0){
            query.setFirstResult(start);
        }
        result = query.list();
        HSession.closeSession();
        return result;
    }

    public static <T> T getElementFromDB(Session session,java.lang.Class obj,int id){
        return (T)session.get(obj,id);
    }

    public static <T> List<T> getElementsFromDB(String hql,int start ,int maxNum,Session session) {
        List<T> result = new ArrayList<T>();
        Query query =  session.createQuery(hql);
        if(maxNum > 0){
            query.setMaxResults(maxNum);
        }
        if(start >= 0){
            query.setFirstResult(start);
        }
        result = query.list();
        return result;
    }

    public static <T> List<T> getElementsFromDBC(Session session,java.lang.Class aClass,short start ,short end,int maxNum) {
        List<T> result = new ArrayList<T>();
        Criteria criteria =  session.createCriteria(aClass);
        criteria.add(Restrictions.between("status",start,end));
        criteria.setMaxResults(maxNum);
        result = criteria.list();
        return result;
    }

    public static void updateDB(Session session){
        session.beginTransaction().commit();
    }

    public static <T> T getElement(String sql,Session session){
        Query query =  session.createQuery(sql);
        List res = query.list();
        if(res.size() >= 1){
            return (T)res.get(0);
        }
        return null;
    }

    public <T> T getElementFromDB(String hql) {
        Query query =  HSession.getSession().createQuery(hql);
        List res = query.list();
        HSession.closeSession();
        if(res.size() == 1){
            return (T)res.get(0);
        }
        return null;
    }

    public <T> void updateDB(Collection<T> scrs) {
        Session session = HSession.getSession();
        Transaction tx = session.beginTransaction();
        for(T t : scrs){
            try{
                session.merge(t);
            }catch(Exception e){
                System.out.println("update error" );
                e.printStackTrace();
            }
        }
        try{
            tx.commit();
        }catch(Exception e){
            e.printStackTrace();
        }
        HSession.closeSession();
    }

    public <T> void updateDB(T t) {
        Session session = HSession.getSession();
        Transaction tx = session.beginTransaction();
        try{
            session.merge(t);
        }catch(Exception e){
            System.out.println("update error" );
            e.printStackTrace();
        }
        try{
            tx.commit();
        }catch(Exception e){
            e.printStackTrace();
        }
        HSession.closeSession();
    }


    public void updateHQLs(List<String> hqls){
        Session session = HSession.getSession();
        Transaction tx = session.beginTransaction();
        for(String hql : hqls){
            try{
                Query query = session.createQuery(hql);
                query.executeUpdate();
            }catch(Exception e){
                System.out.println("update hql error" + "\n" + e.getMessage());
            }
        }
        tx.commit();
        HSession.closeSession();
    }

    public int getMaxFromDB(java.lang.Class aClass,String col){
        Criteria criteria = HSession.getSession().createCriteria(aClass).setProjection(Projections.max(col));
        Integer max = (Integer)criteria.uniqueResult();
        if(max == null){
            //no item in db..
            max = 1;
        }
        HSession.closeSession();
        return max;
    }

    public static int getMaxFromDB(Session session,java.lang.Class aClass,String col){
        Criteria criteria = session.createCriteria(aClass).setProjection(Projections.max(col));
        Integer max = (Integer)criteria.uniqueResult();
        if(max == null){
            //no item in db..
            max = 1;
        }
        return max;
    }



}
