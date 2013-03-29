package test;




import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


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
	
	
	@SuppressWarnings({ "unused", "deprecation" })
	private static void testl(){
		Map<String,Integer> IDF = new HashMap<String,Integer>();
		int total_word = 0;
		List<html> htmls = new ArrayList<html>();
		String file = "D:\\ETT\\test\\tianyi";
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
		
		for(html hl : htmls){
			double max_sim = -1.0;
			int max_id = 0;
			String max_title = "";
			for(html hs : htmls){
				if(hs.id == hl.id )
					continue;
				double time_cha =  (hl.time.getTime() - hs.time.getTime())/1000/60 /60 /24;
//				if( time_cha <= 1 || time_cha > 2)
//					continue;
				if(time_cha <= 0)
					continue;
				
//				System.out.println(time_cha);
				
				
				double sim = util.Similarity.SimilarityWithIDF(hl.words, hs.words,IDF) ;
				if(sim > max_sim){
					max_sim = sim;
					max_id = hs.id;
					max_title = hs.title;
				}
			}
			if(max_sim < 0.2)
				continue;
			String var = "var t" + String.valueOf(hl.id) + "= graph.newNode({label: 't" + hl.time.toLocaleString() + "'});";
			String edgs = "graph.newEdge(t" + max_id + ",t" +  hl.id+",{label: '" + max_sim + "'});";
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
	

	public static void main(String[] args) throws IOException{


		testl();
//		download();
		
		
		
		
		
		
		

		
		
		
	}
}