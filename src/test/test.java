
package test;


//import index.solr.EventIndex;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import db.hbn.model.EventSim;
import db.hbn.model.UrlStatus;




public class test {
	
	
	
	

	public static void updateEvent(List<UrlStatus> scrs){
		try {
			 // 驱动程序名
	           String driver = "com.mysql.jdbc.Driver";

	           // URL指向要访问的数据库名scutcs
	           String url = "jdbc:mysql://222.29.197.238/EventTeller";

	           // MySQL配置时的用户名
	           String user = "dbdm"; 
	  
	           // MySQL配置时的密码
	           String password = "mysql@ET453";

	            // 加载驱动程序
	            Class.forName(driver);

	            // 连续数据库
	            Connection conn = DriverManager.getConnection(url, user, password);

	            if(!conn.isClosed()) 
	             System.out.println("Succeeded connecting to the Database!");
	            
	            
	            
	            String sql = "insert into UrlsStatus(id,status)"
	            			+ "values(?,?)";
	            PreparedStatement ps = conn.prepareStatement(sql);
	            for(UrlStatus ur : scrs){
	            	ps.setInt(1, ur.getId());
	            	ps.setInt(2, ur.getStatus());
	            	ps.executeUpdate();
	            }
	            conn.close();    
		} catch (SQLException ex) {
		    // handle any errors
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String changeName(String old){
		String[] name = old.split("-");
		if(name.length != 3){
			System.out.println("error" + "\t" + old);
			return "";
		}
		String res = "";
		res += name[0] + "-";
		if(name[1].length() == 1){
			res += "0";
		}
		res+= name[1] + "-";
		if(name[2].length() == 1){
			res += "0";
		}
		res += name[2];
		return res;		
	}
	
	
	public static void main(String[] args) throws IOException{
		
		EventSim es = new EventSim();
		es.setFid(1);
		es.setSid(2);
		es.setScore(1.0);
		util.db.Hbn.updateDB(es);
		
		
		
	}
}