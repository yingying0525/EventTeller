package util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import db.data.Word;

public class Similarity {

	/**
	 * if content_a is a substring of content_b , the results will be 1.x such as 1.9
	 * else results will be 0.x such as 0.8
	 * get the overlap of two content
	 * @param content_a
	 * @param content_b
	 * @return
	 */
	public static double ContentOverlap(String content_a , String content_b){
		double hits = 0;
		if(content_a == null || content_b == null){
			return -1;
		}
		Map<String,Boolean> check =new HashMap<String,Boolean>();
		Map<String,Boolean> check_b =new HashMap<String,Boolean>();
		List<Word> words = ChineseSplit.SplitStrWithPos(content_a);
		List<Word> words_b = ChineseSplit.SplitStrWithPos(content_b);
		
		for(Word wd : words){
			check.put(wd.getName(),true);
		}
		for(Word wd : words_b){
			check_b.put(wd.getName(),true);
		}
		if(check.size() == 0 || check_b.size() == 0){
			return 0;
		}
		///for content_a small than content_b
		if(words.size() < words_b.size() * 1.2){
			for(Word wd : words){
				if(check_b.containsKey(wd.getName())){
					if(check_b.get(wd.getName())){
						hits++;
						check_b.put(wd.getName(), false);
					}			
				}
			}
			return 1 + hits/check.size();
		}else{
			for(Word wd : words_b){
				if(check.containsKey(wd.getName())){
					if(check.get(wd.getName())){
						hits++;
						check.put(wd.getName(), false);
					}			
				}
			}
			return hits/check_b.size();
		}	
	}
	
	public static double SimilarityOfTF(Map<Word,Double> arg1,Map<Word,Double> arg2){
		Map<String,Double> checks = new HashMap<String,Double>();
		double total_a = 0.001;
		double total_b = 0.001;
		double total_up = 0.0001;
		Iterator<Word> it_a = arg1.keySet().iterator();
		while(it_a.hasNext()){
			Word key = it_a.next();
			total_a += arg1.get(key) * arg1.get(key);
			checks.put(key.getName(),arg1.get(key));
		}
		Iterator<Word> it_b = arg2.keySet().iterator();
		while(it_b.hasNext()){
			Word key = it_b.next();
			if(checks.containsKey(key.getName())){
				total_up += arg2.get(key) * checks.get(key.getName());
			}
			total_b += arg2.get(key) * arg2.get(key);
		}
		return total_up / Math.sqrt(total_a) / Math.sqrt(total_b);
	}
	
	public static double SimilarityWithIDF(Map<Word,Double> arg1,Map<Word,Double> arg2,Map<String,Integer> idf){
		Map<String,Double> checks = new HashMap<String,Double>();
		double total_a = 0.001;
		double total_b = 0.001;
		double total_up = 0.0001;
		Iterator<Word> it_a = arg1.keySet().iterator();
		while(it_a.hasNext()){
			Word key = it_a.next();
			double idf_score = Math.log(idf.size()/ idf.get(key.getName()));
			total_a += arg1.get(key) * arg1.get(key) * idf_score * idf_score;
			checks.put(key.getName(),arg1.get(key));
		}
		Iterator<Word> it_b = arg2.keySet().iterator();
		while(it_b.hasNext()){
			Word key = it_b.next();
			double idf_score = Math.log(idf.size()/ idf.get(key.getName()));
			if(checks.containsKey(key.getName())){
				total_up += arg2.get(key) * checks.get(key.getName()) * Math.pow(idf_score, 2.0);
			}
			total_b += arg2.get(key) * arg2.get(key) * Math.pow(idf_score, 2.0);
		}
		return total_up / Math.sqrt(total_a) / Math.sqrt(total_b);
	}
	
	
}
