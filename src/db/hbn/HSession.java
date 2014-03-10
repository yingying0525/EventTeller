package db.hbn;


import java.io.File;

import util.Const;

import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;

/**
* @PackageName:db
* @ClassName: HSession
* @author: mblank
* @date: 2012-2-21 下午8:14:16
* @Description: get hibernate session 
*/
public class HSession {
	
	public static final String path = Const.HIBERNATE_CFG_PATH;
	public static SessionFactory sFactory = null;
	
	/**
	 * @return
	 * @Description:create a new session
	 */
	public Session createSession(){
		if(sFactory==null){
			File file = new File(path);
			sFactory = new Configuration().configure(file).buildSessionFactory();			
		}
		Session session = sFactory.openSession();
		return session;
	}
	
	/**
	 * @param itc
	 * @return
	 * @Description:just for split-table session
	 * @Mark: we will split IDF table to 1000 small tables,so we use Interceptor.
	 */
	public Session createSession(Interceptor itc){
		if(sFactory == null){
			File file = new File(path);
			sFactory = new Configuration().configure(file).buildSessionFactory();
		}
		Session session = sFactory.openSession(itc);
		return session;
	}
	
	

}
