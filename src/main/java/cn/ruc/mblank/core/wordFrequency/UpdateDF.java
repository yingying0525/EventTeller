package cn.ruc.mblank.core.wordFrequency;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import cn.ruc.mblank.db.hbn.model.Event;
import cn.ruc.mblank.db.hbn.model.EventStatus;
import cn.ruc.mblank.util.Const;
import cn.ruc.mblank.config.JsonConfigModel;
import cn.ruc.mblank.util.db.Hbn;

public class UpdateDF {
	
	private Map<String,Integer> TDFMap;
	private Map<Integer,Integer> DDNMap;
	private Map<String,TreeMap<Integer,Integer>> DFMap;
	
	private List<EventStatus> EStatus;
	
	private int BatchSize = 3000;
	
	private String LocalTDFPath;
	private String LocalDFPath;
	private String LocalDDNPath;
	
	
	
	private void loadDF(){
		//should load two file, one for word-df
		//another for day-df
		TDFMap = new HashMap<String,Integer>();
		DFMap = new HashMap<String,TreeMap<Integer,Integer>>();
		DDNMap = new TreeMap<Integer,Integer>();
		//load tdf file
		String line;
		try {
			BufferedReader br = new BufferedReader(new FileReader(LocalTDFPath));
			while((line = br.readLine()) != null){
				String[] its = line.split("\t");
				if(its.length != 2 || its[1].length() == 0){
					System.out.println(line + "\t" + line.length());
					continue;
				}
				TDFMap.put(its[0], Integer.parseInt(its[1]));
			}
			br.close();
		} catch (IOException e) {
			System.out.println("can't load local TDF file.");
		}
		//load df file
		try{
			BufferedReader dbr = new BufferedReader(new FileReader(LocalDFPath));
			while((line = dbr.readLine()) != null){
				String[] its = line.split("\t");
				if(its.length < 2){
					continue;
				}
				TreeMap<Integer, Integer> dayNumbers = new TreeMap<Integer,Integer>();
				for(int i = 1; i < its.length; i++){
					String[] sub = its[i].split(" ");
					if(sub.length != 2){
						continue;
					}
					dayNumbers.put(Integer.parseInt(new String(sub[0])), Integer.parseInt(new String(sub[1])));
				}
				DFMap.put(its[0], dayNumbers);
			}
			dbr.close();
		}catch(Exception e){
			System.out.println("can't find local DF file");
		}
		//load ddn file
		try {
			BufferedReader nbr = new BufferedReader(new FileReader(LocalDDNPath));
			while((line = nbr.readLine()) != null){
				String[] its = line.split("\t");
				DDNMap.put(Integer.parseInt(its[0]), Integer.parseInt(its[1]));
			}
			nbr.close();
		} catch (IOException e) {
			System.out.println("can't load local DDN file.");
		}
	}

	public UpdateDF(){
		JsonConfigModel jcm = JsonConfigModel.getConfig();
		LocalTDFPath = jcm.LocalTDFPath;
		LocalDDNPath = jcm.LocalDDNPath;
		LocalDFPath = jcm.LocalDFPath;
		loadDF();
		EStatus = new ArrayList<EventStatus>();
	}
	
	private void getInstances(){
		String hql = "from EventStatus as obj where obj.status = " + Const.TaskId.CreateNewEvent.ordinal() ;
        Hbn db = new Hbn();
		EStatus = db.getElementsFromDB(hql,-1,BatchSize);
	}
	
	private void writeDF2Disk(){
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(LocalDDNPath));
			for(int day : DDNMap.keySet()){
				bw.write(day + "\t" + DDNMap.get(day) + "\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(LocalTDFPath));
			for(String word : TDFMap.keySet()){
				bw.write(word + "\t" + TDFMap.get(word) + "\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(LocalDFPath));
			for(String word : DFMap.keySet()){
				bw.write(word + "\t");
				TreeMap<Integer,Integer> days = DFMap.get(word);
				for(int day : days.keySet()){
					bw.write(day + " " + days.get(day) + "\t");
				}
				bw.write("\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private int runTask(){
		int num = 0;
		getInstances();
		num = EStatus.size();
		if(num == 0){
			return 0;
		}
		for(EventStatus es : EStatus){
			String sql = "from Event as obj where obj.id = " + es.getId();
            Hbn db = new Hbn();
			Event et = db.getElementFromDB(sql);
			if(et == null){
				//some error
				System.out.println(es.getId());
				es.setStatus((short)Const.TaskId.UpdateDFFailed.ordinal());
				continue;
			}
			int day = cn.ruc.mblank.util.TimeUtil.getDayGMT8(et.getPubtime());
			List<String> words = cn.ruc.mblank.util.ChineseSplit.SplitStr(et.getTitle() + et.getContent());
			//update three map in memory
			Set<String> has = new HashSet<String>();
			//for Day Document Number file, one document only add one number..
			if(DDNMap.containsKey(day)){
				DDNMap.put(day, DDNMap.get(day) + 1);
			}else{
				DDNMap.put(day, 1);
			}
			for(String word : words){
				if(has.contains(word)){
					continue;
				}
				has.add(word);
				if(TDFMap.containsKey(word)){
					TDFMap.put(word, TDFMap.get(word) + 1);
				}else{
					TDFMap.put(word, 1);
				}
				if(DFMap.containsKey(word)){
					if(DFMap.get(word).containsKey(day)){
						DFMap.get(word).put(day, DFMap.get(word).get(day) + 1);
					}else{
						DFMap.get(word).put(day,1);
					}
				}else{
					TreeMap<Integer, Integer> days = new TreeMap<Integer,Integer>();
					days.put(day, 1);
					DFMap.put(word, days);
				}
			}
			es.setStatus((short)Const.TaskId.UpdateDFSuccess.ordinal());
		}
		//write three map to disk
		writeDF2Disk();
		//update db
        Hbn db = new Hbn();
		db.updateDB(EStatus);
		return num;
	}
	
	public static void main(String[] args){
		while(true){
			UpdateDF ud = new UpdateDF();
			int num = ud.runTask();
			if(num == 0){
				System.out.println("no event to process.. will sleep for " +  Const.UpdateDFSleepTime / 1000 / 60 + " minutes");
				try {
					Thread.sleep(Const.UpdateDFSleepTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
	}
	
	
}
