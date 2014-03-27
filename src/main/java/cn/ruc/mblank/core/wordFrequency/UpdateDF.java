package cn.ruc.mblank.core.wordFrequency;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import cn.ruc.mblank.db.hbn.HSession;
import cn.ruc.mblank.db.hbn.model.Event;
import cn.ruc.mblank.db.hbn.model.EventStatus;
import cn.ruc.mblank.util.Const;
import cn.ruc.mblank.config.JsonConfigModel;
import cn.ruc.mblank.util.db.Hbn;
import org.hibernate.Session;

public class UpdateDF {
	
	private Map<String,Integer> TDFMap;
	private Map<Integer,Integer> DDNMap;
	private Map<String,TreeMap<Integer,Integer>> DFMap;

    private List<EventStatus> EStatus;
    private Session session;
	
	private int BatchSize = 1000;
	
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
        session = HSession.getSession();
	}
	
	private void getInstances(){
		String hql = "from EventStatus as obj where obj.status = " + Const.TaskId.CreateNewEvent.ordinal() ;
        EStatus = Hbn.getElementsFromDB(hql,0,BatchSize,session);
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
	
	private void runTask(){
        int num = 0;
        getInstances();
        for(EventStatus es : EStatus){
			Event et = Hbn.getElementFromDB(session,Event.class,es.getId());
			if(et == null){
				//some error
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
			es.setStatus((short) Const.TaskId.UpdateDFSuccess.ordinal());
		}
		//write three map to disk
		writeDF2Disk();
		//update db
		Hbn.updateDB(session);
        session.clear();
	}
	
	public static void main(String[] args){
        UpdateDF ud = new UpdateDF();
        while(true){
			ud.runTask();
			if(ud.EStatus.size() == 0){
                System.out.println("no event to process.. will sleep for " +  Const.UpdateDFSleepTime / 1000 / 60 + " minutes");
                try {
                    Thread.sleep(Const.UpdateDFSleepTime);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }else{
                System.out.println("one batch ok...." + new Date() + "\t" + ud.EStatus.get(ud.EStatus.size() - 1).getId());
            }

		}
	}
	
	
}
