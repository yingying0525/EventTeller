package util.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import db.hbn.HSession;
import db.hbn.model.Article;

public class Hbn {
	
	
	/**
	 * @param hql
	 * @param maxNum 
	 * @return
	 * @Description: get elements from db, if maxNum <= 0 will not set the maxNum per returned
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> getElementsFromDB(String hql,int maxNum) {
		List<T> result = new ArrayList<T>();
		Session session = new HSession().createSession();
		Query query = session.createQuery(hql);
		if(maxNum > 0){
			query.setMaxResults(maxNum);
		}		
		result = query.list();
		session.close();
		return result;
	}
	
	/**
	 * @param hql
	 * @return
	 * @Description: get element from db
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T getElementFromDB(String hql) {
		Session session = new HSession().createSession();
		Query query = session.createQuery(hql);	
		List res = query.list();
		session.close();
		if(res.size() == 1){
			return (T)res.get(0);
		}
		return null;
	}
	
	/**
	 * @param id
	 * @return
	 * @Description:from id to get article
	 */
	@SuppressWarnings("unchecked")
	public static Article getArticleById(String id){
		Session session = new HSession().createSession();
		List<Article> results = new ArrayList<Article>();
		String hql = "from Article as obj where obj.id=" + id;
		Query query = session.createQuery(hql);
		results = (List<Article>)query.list();
		Article result = new Article();
		if(results.size()>0){
			result = results.get(0);
		}
		return result;
	}
	
	public static <T> void updateDB(Collection<T> scrs) {
		Session session = new HSession().createSession();
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
			session.flush();	
			session.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static <T> void updateDB(T t) {
		Session session = new HSession().createSession();
		Transaction tx = session.beginTransaction();		
		try{
			session.merge(t);				
		}catch(Exception e){
			System.out.println("update error" );
			e.printStackTrace();
		}
		try{
			tx.commit();
			session.flush();	
			session.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public static void updateHQLs(List<String> hqls){
		Session session = new HSession().createSession();
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
		session.flush();	
		session.close();
	}
	
	@SuppressWarnings("unchecked")
	public static int getMaxIdFromDB(String hql){
		 Session session = new HSession().createSession();
         int results = 0;
         if(session!=null){
             Query query = session.createQuery(hql);
             List<Integer> ls_results = query.list();
             if(ls_results != null && ls_results.size()>0){
            	 if(ls_results.get(0) == null){
            		 results = -1;
            	 }else{
            		 results = ls_results.get(0);
            	 }
             }else{
            	 results = -1;
             }
         }
         session.close();
         return results;
	}

}
