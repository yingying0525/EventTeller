package core.duplicateRemoval;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import db.hbn.model.Event;
import util.IOReader;

public class SameRemove {
	
	
	public static String SimHashPath = "E:\\share\\simhash";
	public static String EventIdPath = "E:\\share\\eventID";
	public static String EventDiskPath = "E:\\share\\events";
	public static int SimHashNum = 8;
	
	private HashMap<Long,ArrayList<HashMap<Integer,List<Integer>>>> SimHashIndex = new HashMap<Long,ArrayList<HashMap<Integer,List<Integer>>>>();
	private HashMap<Integer,Long> IdSimHash;
	private HashMap<Integer,List<Integer>> Events = new HashMap<Integer,List<Integer>>();
	private HashMap<Integer,Date> IdDates = new HashMap<Integer,Date>();
	
	
	
	
	public SameRemove(){
		IdSimHash = new HashMap<Integer,Long>();
	}
	
	private int findSameInIndex(long hash, int oid,Date dt){
		int MinDistance = 65;
		int SameId = -1;
		byte[] bts = util.Util.long2Byte(hash);
		for(int t = 0 ; t < 7; t++){
			long day = dt.getTime() / 1000 / 60 / 60 / 24 - t;
			ArrayList<HashMap<Integer,List<Integer>>> simhashs = SimHashIndex.get(day);
			if(simhashs == null){
				continue;
			}else{
				for(int i = 0 ; i < SimHashNum; i++ ){
					if(simhashs.get(i).containsKey((int)bts[i])){
						//cal the disstance
						List<Integer> ids = simhashs.get(i).get((int)bts[i]);
						for(int id : ids){
							int bits = util.Util.diffBitsOfNums(hash,IdSimHash.get(id));
							if(bits < MinDistance && id != oid){
								MinDistance = bits;
								SameId = id;
							}
						}
					}
				}
			}
		}
		if(MinDistance <= util.Const.MinSimHashBitNumber){
			//find the same article
			return SameId;
		}else{
			//not find
			return -1;
		}
	}
	
	private void add2SimHashIndex(int id, long hash,Date dt){
		byte[] bts = util.Util.long2Byte(hash);
		long day = dt.getTime() / 1000 / 60 / 60 / 24;
		ArrayList<HashMap<Integer,List<Integer>>> simhashs = new ArrayList<HashMap<Integer,List<Integer>>>();
		if(SimHashIndex.containsKey(day)){
			simhashs = SimHashIndex.get(day);
		}else{
			for(int i = 0 ; i < SimHashNum; i++){
				HashMap<Integer,List<Integer>> simhash = new HashMap<Integer,List<Integer>>();
				simhashs.add(simhash);
			}
			SimHashIndex.put(day, simhashs);
		}
		for(int i = 0 ; i < SimHashNum; i++ ){
			List<Integer> tmps = new ArrayList<Integer>();
			if(simhashs.get(i).containsKey((int)bts[i])){
				tmps = simhashs.get(i).get((int)bts[i]);
			}
			tmps.add(id);
			simhashs.get(i).put((int)bts[i], tmps);
		}
	}
	
	private void writeEvent2Disk(HashMap<Integer,List<Integer>> scrs) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(EventIdPath));
		for(int id : scrs.keySet()){
			bw.write(id + "\t");
			for(int same : scrs.get(id)){
				bw.write(same + " ");
			}
			bw.write("\n");
		}
		bw.close();		
	}
	
	@SuppressWarnings("deprecation")
	public void dealwithSimHash() throws IOException{
		IOReader reader = new IOReader(SimHashPath);
		String line = "";
		int lnum = 0;
		Date start = new Date();
		while((line = reader.readLine()) != null){
			String[] its = line.split("\t");
			if(its.length < 3){
				System.out.println(line);
				continue;
			}
			String[] dts = its[2].split("!@@@@!");
			if(dts.length < 2){
				continue;
			}
			int id = Integer.parseInt(its[0]);
			long hash = Long.parseLong(its[1]);
			Date dt = new Date();
			try{
				dt = new Date(dts[1]);
			}catch(Exception e){
				System.out.println(dts[1]);
				continue;
			}
			int sameId = findSameInIndex(hash,id,dt);
			if(sameId != -1){
				Events.get(sameId).add(id);
			}else{
				add2SimHashIndex(id,hash,dt);
				IdSimHash.put(id, hash);
				List<Integer> sames = new ArrayList<Integer>();
				sames.add(id);
				Events.put(id, sames);
				IdDates.put(id, dt);
			}
			lnum++;
			if(lnum % 10000 == 0){
				Date end = new Date();
				System.out.println(lnum + "\t" + (end.getTime() - start.getTime())/1000.0);
				start = end;
			}
		}
		System.out.println(lnum + " start to write to disk");
		writeEvent2Disk(Events);
	}
	
	
	private void writeEvent2Disk(List<Event> ets) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(EventDiskPath,true));
		for(Event et : ets){
			bw.write(et.toString() + "\n");
		}
		bw.close();
	}
	
	@SuppressWarnings("deprecation")
	public void updateEventDB() throws IOException, FileNotFoundException{
		IOReader reader = new IOReader(EventIdPath);
		String line = "";
		List<Event> updates = new ArrayList<Event>();
		HashMap<Integer,Integer> eventSize = new HashMap<Integer,Integer>();
		HashMap<Integer,Event> events = new HashMap<Integer,Event>();
		HashMap<Integer,Integer> Events = new HashMap<Integer,Integer>();
		while((line = reader.readLine()) != null){
			String[] its = line.split("\t");
			String[] ids = its[1].split(" ");
			for(String id : ids){
				Events.put(Integer.parseInt(id), Integer.parseInt(its[0]));
			}
			eventSize.put(Integer.parseInt(its[0]), ids.length);
		}
		reader.close();
		//load ids ok.
		System.out.println("load ok...");
		IOReader raw = new IOReader(SimHashPath);
		int lnum = 0;
		while((line = raw.readLine()) != null){
			lnum++;
			String[] its = line.split("\t");
			String[] infos = its[2].split("!@@@@!");
			if(infos.length < 3){
				System.out.println(infos.length);
				continue;
			}
			int id = Integer.parseInt(its[0]);
			if(Events.containsKey(id)){
				int eventid = Events.get(id);
				if(events.containsKey(eventid)){
					Event oet = events.get(eventid);
					if(infos.length > 3  && infos[3].length() > 10){
						oet.setImgs(oet.getImgs() + "!##!" + infos[3]);
					}
					oet.setNumber(oet.getNumber() + 1);
					Date dt = new Date(infos[1]);
					if(dt.getTime() < oet.getPubTime().getTime()){
						oet.setPubTime(dt);
					}
				}else{
					Event et = new Event();
					et.setId(eventid);
					et.setTitle(infos[0]);
					et.setPubTime(new Date(infos[1]));
					et.setContent(infos[2]);
					if(infos.length < 4){
						et.setImgs("");
					}else{
						et.setImgs(infos[3]);
					}
					et.setNumber(1);
					et.setTaskStatus(1);
					et.setSubTopic(0);
					events.put(eventid, et);
				}
				if(eventSize.get(eventid) == events.get(eventid).getNumber()){
					updates.add(events.get(eventid));
					events.remove(eventid);
				}
				if(updates.size() == 1000 ){
					System.out.println("start to update batch...");
//					util.Util.updateDB(updates);
//					updateEvent(updates);
					writeEvent2Disk(updates);
					updates.clear();
				}
			}
			if(lnum % 10000 == 0){
				System.out.println(lnum);
			}			
		}
		System.out.println("start to update left");
//		util.Util.updateDB(updates);
//		updateEvent(updates);
		writeEvent2Disk(updates);
	}
	
	
	
	
	public static void main(String[] args) throws IOException{
		SameRemove sr = new SameRemove();
		sr.updateEventDB();
	}
	
	public void updateEvent(List<Event> ets){
		try {
			 // 驱动程序名
	           String driver = "com.mysql.jdbc.Driver";

	           // URL指向要访问的数据库名scutcs
	           String url = "jdbc:mysql://10.77.50.245/EventTeller";

	           // MySQL配置时的用户名
	           String user = "root"; 
	  
	           // MySQL配置时的密码
	           String password = "111111";

	            // 加载驱动程序
	            Class.forName(driver);

	            // 连续数据库
	            Connection conn = DriverManager.getConnection(url, user, password);

	            if(!conn.isClosed()) 
	             System.out.println("Succeeded connecting to the Database!");
	            
	            
	            
	            String sql = "insert into Event(id,title,pubtime,content,source,imgs,number,taskstatus,subtopic)"
	            			+ "values(?,?,?,?,?,?,?,?,?)";
	            PreparedStatement ps = conn.prepareStatement(sql);
	            for(Event et : ets){
	            	ps.setInt(1, et.getId());
	            	ps.setString(2, et.getTitle());
	            	java.sql.Date dt = new java.sql.Date( et.getPubTime().getTime());
	            	ps.setDate(3, dt);
	            	ps.setString(4, et.getContent());
	            	ps.setString(5, "");
	            	ps.setString(6, et.getImgs());
	            	ps.setInt(7, et.getNumber());
	            	ps.setInt(8, 0);
	            	ps.setInt(9, 0);
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

}
