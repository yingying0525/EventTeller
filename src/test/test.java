package test;




import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


import news.crawler.Article.Extractor;

import org.hibernate.Query;
import org.hibernate.Session;

import util.Const;
import db.HSession;
import db.data.Topic;
import db.data.Event;
import db.data.Word;




//


class DoubleWords{
	public String words;
	public int count;
	
}



@SuppressWarnings("rawtypes")
class ComparatorWords implements Comparator{

	 public int compare(Object arg0, Object arg1) {
		 DoubleWords eta=(DoubleWords)arg0;
		 DoubleWords etb=(DoubleWords)arg1;		 
		 if(eta.count > etb.count){
			 return -1;
		 }else if(eta.count < etb.count){
			 return 1;
		 }else{
			 return 0;
		 }
	 }
	
} 


class html {
	public Date time;
	public String title;
	public int id;
	public Map<Word,Double> words;
}



public class test {
	
//1274342
	
	@SuppressWarnings("unchecked")
	public static List<Topic> getTopicFromDB(int id){
		Session session = new HSession().createSession();
		List<Topic> results = new ArrayList<Topic>();
		String hql = "from Topic as obj where obj.id = " + id; 
		Query query = session.createQuery(hql);
		results = (List<Topic>)query.list();
		session.close();
		return results;
	}
	
	
	@SuppressWarnings("unchecked")
	public static Event getEventById(String id){
		Session session = new HSession().createSession();
		List<Event> results = new ArrayList<Event>();
		String hql = "from event as obj where obj.id="+ id;
		Query query = session.createQuery(hql).setMaxResults(Const.MysqlToIndexMaxItemNum);
		results = (List<Event>)query.list();
		Event result = new Event();
		if(results.size()>0){
			result = results.get(0);
		}
		return result;
	}

	
	@SuppressWarnings("unused")
	private void ldatest(String file){
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			StringBuilder sb = new StringBuilder();
			while((line = br.readLine())!=null){
				sb.append(line);
			}
			
			System.out.println(sb.toString());
			List<String> words = util.ChineseSplit.SplitStr(sb.toString());
			BufferedWriter bw = new BufferedWriter(new FileWriter(file+"_out"));
			for(String wd : words){
				bw.write(wd + " ");
			}
			bw.write("\n");
			br.close();
			bw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@SuppressWarnings({ "unused"})
	private static void TIMethod(){
		Map<String,Integer> IDF = new HashMap<String,Integer>();
		int total_word = 0;
		List<html> htmls = new ArrayList<html>();
		String file = "D:\\ETT\\tianyi_df";
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			while((line = br.readLine())!= null){
				String[] its = line.split("\t");
				html ht = new html();
				ht.id = Integer.valueOf(its[0]);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				ht.time = sdf.parse(its[1]);
				ht.title = its[2];
				ht.words = util.ChineseSplit.SplitStrWithPosDoubleTF(its[3]);
				Iterator<Word> it_words = ht.words.keySet().iterator();
				while(it_words.hasNext()){
					Word it_wd = it_words.next();
					if(IDF.containsKey(it_wd.getName())){
						IDF.put(it_wd.getName(), IDF.get(it_wd.getName()) + 1);
					}else{
						IDF.put(it_wd.getName(), 1);
					}
				}
				htmls.add(ht);
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		for(int i = htmls.size() -1; i>=0; i--){
			double max_sim = -1.0;
			int max_id = -1;
			for(int j = i -1 ; j>=0;j--){
				double sim = util.Similarity.SimilarityWithIDF(htmls.get(i).words, htmls.get(j).words,IDF) ;
				if(sim > max_sim){
					max_sim = sim;
					max_id = j;
				}
			}
			if(max_sim < 0.15)
				continue;
			String var = "var t" + i + "= graph.newNode({label: 't" + i + "'});";
			String edgs = "graph.newEdge(t" + max_id + ",t" +  i+",{label: '" + max_sim + "'});";
			System.out.println(edgs);
		}
		
	}
	
	
	
	@SuppressWarnings({ "unused", "deprecation" })
	private static void download() throws IOException{
		int num = 0;
		String file = "d:\\ETT\\";
		BufferedReader br = new BufferedReader(new FileReader(file + "url.txt"));
		BufferedWriter bw = new BufferedWriter(new FileWriter(file + "test\\tianyi"));
		String line = "";
		while((line = br.readLine()) != null){
			num++;
			String url = line;
			Extractor etor = new Extractor(url);
			String content = etor.getContent();
			
//			List<String> twords = util.ChineseSplit.SplitStr(etor.getTitle());
//			String[] its = content.split("!##!");
//			StringBuilder sb = new StringBuilder();
//			for(String it : its){
//				boolean check = false;
//				for(String twd : twords){
//					if(it.contains(twd)){
//						check = true;
//					}
//				}
//				if(check){
//					sb.append(it + " ");
//				}
//			}
//			List<String> words = util.ChineseSplit.SplitStr(sb.toString());
			List<String> words = util.ChineseSplit.SplitStr(content);
			if(words.size() == 0)
				continue;
			bw.write(num + "\t" + etor.getPublishTime().toLocaleString() + "\t" + etor.getTitle()+"\t");
			for(String wd : words){
				bw.write(wd + " ");
			}
			bw.write("\n");
			System.out.println(url);
		}
		br.close();
		bw.close();
	}
	
	
	@SuppressWarnings("unused")
	private void resort() throws IOException{
		String path = "D:\\ETT\\tianyi";
		String outpath = "D:\\ETT\\tianyi_sort";
		List<String> lines = new ArrayList<String>();
		Map<Integer,Date> dates = new HashMap<Integer,Date>();
		BufferedReader br = new BufferedReader(new FileReader(path));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outpath));
		String line = "";
		
		while((line = br.readLine())!=null){
			String[] its = line.split("\t");
			
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");        
			Date dt = new Date();   
			// String转Date   
			try {   
			    dt = format.parse(its[0]);  // Thu Jan 18 00:00:00 CST 2007   
			} catch (ParseException e) {   
			    e.printStackTrace();   
			}   
			dates.put(lines.size(), dt);
			lines.add(line);
		}
		for(int i = 0 ; i<lines.size();i++){
			for(int j = i +1;j <lines.size();j++){
				if(dates.get(j).getTime() - dates.get(i).getTime() < 0){
					String tmp = lines.get(i);
					lines.set(i, lines.get(j));
					lines.set(j, tmp);
					Date tmpd = dates.get(i);
					dates.put(i, dates.get(j));
					dates.put(j, tmpd);
				}
			}
			System.out.println(dates.get(i));
		}
		for(int i = 0 ;i < lines.size();i++){
			bw.write(i + "\t" + lines.get(i) + "\n");
		}
		br.close();
		bw.close();
	}
	
	@SuppressWarnings("unused")
	private void FindByDf() throws IOException{
		String path = "D:\\ETT\\tianyi";
		String outpath = "D:\\ETT\\tianyi_df";
		int totalDocNum = 0;
		Map<String,Integer> DF = new HashMap<String,Integer>();
		List<String> lines = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(path));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outpath));
		String line = "";
		while((line = br.readLine())!=null){
			StringBuffer sb = new StringBuffer();
			Set<String> has = new HashSet<String>();
			String[] its = line.split("\t");
			totalDocNum++;
			String[] words = its[3].split(" ");
			for(String wd : words){
				if(!DF.containsKey(wd) || DF.get(wd) < totalDocNum /2){
					sb.append(wd + " ");
				}
			}			
			for(String word : words){
				if(!DF.containsKey(word)){
					DF.put(word, 1);
				}else{
					if(!has.contains(word)){
						DF.put(word, DF.get(word) + 1);
						has.add(word);
					}
				}
			}
			lines.add(its[1] + "\t" + its[2] + "\t" + sb.toString());
			bw.write(its[0] + "\t" + its[1] + "\t" + its[2] + "\t" + sb.toString() + "\n");
		}
		br.close();
		bw.close();
	}

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException{


		test ts = new test();
		ts.TIMethod();
		
		
	}
}