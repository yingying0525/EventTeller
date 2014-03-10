package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


class Words{
	String name;
	List<Integer> days = new ArrayList<Integer>();
	List<Integer> counts = new ArrayList<Integer>();
}

class Day{
	public long id;
	public int wordCount;
	public int DocCount;
	public HashMap<String,Double> words = new HashMap<String,Double>();
}


public class GetWordBurst {

	private static String FolderPath = "E:\\share\\DDF\\";
	private List<Day> Days;
	
	public GetWordBurst(){
		try {
			Days = loadFiles();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Date str2Date(String str) throws ParseException{
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");//小写的mm表示的是分钟    
		java.util.Date date=sdf.parse(str); 
		return date;
	}
	
	private List<Day> loadFiles() throws IOException, ParseException{
		List<Day> days = new LinkedList<Day>();
		File[] files = new File(FolderPath).listFiles();
		for(File file : files){
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			Day day = new Day();
			line = br.readLine();
//			System.out.println(file.getName());
			Date dt = str2Date(file.getName());
			day.id = dt.getTime() / 1000 / 60 / 60 /24;
			String[] its = line.split("\t");
			day.wordCount = Integer.parseInt(its[0]);
			day.DocCount = Integer.parseInt(its[1]);
			while((line = br.readLine()) != null){
				String[] wds = line.split("\t");
				day.words.put(wds[0], Integer.parseInt(wds[2]) / (double)day.DocCount);
			}
			String key = "声音";
			if(day.words.containsKey(key)){
				System.out.println(day.id + "\t" + day.words.get(key));
			}else{
				System.out.println(day.id + "\t" + "0.001");
			}
//			days.add(day);
			br.close();
		}		
		return days;
	}
	
	public void getWordDF(String word){
		Words wd = new Words();
		wd.name = word;
		for(Day d : Days){
			System.out.print(d.id);
			if(d.words.containsKey(wd.name)){
				System.out.println(d.words.get(wd.name));
			}else{
				System.out.println(0.001);
			}
		}
	}
	
	
	@SuppressWarnings("unused")
	public static void main(String[] args){
		GetWordBurst gwb = new GetWordBurst();
//		gwb.getWordDF("李天一");
	}
	
	
	
}
