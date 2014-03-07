package util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import db.hbn.model.Article;
import db.hbn.model.Word;

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
	
	public static double SimilarityWithIDF(Map<Word,Double> arg1,Map<Word,Double> arg2,Map<String,Integer> idf , long totalDocNum){
		Map<String,Double> checks = new HashMap<String,Double>();
		double total_a = 0.001;
		double total_b = 0.001;
		double total_up = 0.0001;
		Iterator<Word> it_a = arg1.keySet().iterator();
		while(it_a.hasNext()){
			Word key = it_a.next();
			double idf_score = Math.log(totalDocNum / idf.get(key.getName()));
			total_a += arg1.get(key) * arg1.get(key) * idf_score * idf_score;
			checks.put(key.getName(),arg1.get(key));
		}
		Iterator<Word> it_b = arg2.keySet().iterator();
		while(it_b.hasNext()){
			Word key = it_b.next();
			if(key.getName() == null || idf.get(key.getName()) == null){
				continue;
			}
			double idf_score = Math.log(totalDocNum / idf.get(key.getName()));
			if(checks.containsKey(key.getName())){
				total_up += arg2.get(key) * checks.get(key.getName()) * Math.pow(idf_score, 2.0);
			}
			total_b += arg2.get(key) * arg2.get(key) * Math.pow(idf_score, 2.0);
		}
		return total_up / Math.sqrt(total_a) / Math.sqrt(total_b);
	}
	
	private static Map<String,Double> improveWordsWeight(Article at){
		Map<String,Double> results = new HashMap<String,Double>();
		Map<Word,Integer> cwdsa = util.ChineseSplit.SplitStrWithPosTF(at.getContent());
		Map<Word,Integer> twdsa = util.ChineseSplit.SplitStrWithPosTF(at.getTitle());
		int totalWordCount = 1;
		//improve the weight of title words and who (name entity) , where (location)
		Iterator<Word> wds = twdsa.keySet().iterator();
		while(wds.hasNext()){
			Word wd = wds.next();
			if(results.containsKey(wd.getName())){
				results.put(wd.getName(), results.get(wd.getName()) + twdsa.get(wd) * util.Const.TitleWordsWeight);
			}else{
				results.put(wd.getName(),1.0* twdsa.get(wd) * util.Const.TitleWordsWeight);
			}
			totalWordCount += twdsa.get(wd) * util.Const.TitleWordsWeight;
		}
		wds = cwdsa.keySet().iterator();
		while(wds.hasNext()){
			Word wd = wds.next();
			int weight = cwdsa.get(wd);
			if(wd.getNature().equals("nr") || wd.getNature().equals("nt")||wd.getNature().equals("nz")){
				weight *= util.Const.NameEntityWeight;
			}else if(wd.getNature().equals("ns")){
				weight *= util.Const.LocationWeight;
			}
			if(results.containsKey(wd.getName())){
				results.put(wd.getName(), results.get(wd.getName()) + weight);
			}else{
				results.put(wd.getName(), 1.0 * weight);
			}
			totalWordCount += weight;
		}
		Iterator<String> ims = results.keySet().iterator();
		while(ims.hasNext()){
			String key = ims.next();
			results.put(key,results.get(key)/totalWordCount);
		}
		return results;
	}
	
	
	public static double similarityOfEvent(Article ata,Article atb,Map<String,Integer> idf , long totalDocNum){
		Map<String,Double> checks = new HashMap<String,Double>();
		double total_a = 0.001;
		double total_b = 0.001;
		double total_up = 0.0001;
		Map<String,Double> arg1 = improveWordsWeight(ata);
		Map<String,Double> arg2 = improveWordsWeight(atb);
		Iterator<String> it_a = arg1.keySet().iterator();
		while(it_a.hasNext()){
			String key = it_a.next();
			if(key == null || idf.get(key) == null){
				continue;
			}
			double idf_score = Math.log(totalDocNum / idf.get(key));
			total_a += arg1.get(key) * arg1.get(key) * idf_score * idf_score;
			checks.put(key,arg1.get(key));
		}
		Iterator<String> it_b = arg2.keySet().iterator();
		while(it_b.hasNext()){
			String key = it_b.next();
			if(key == null || idf.get(key) == null){
				continue;
			}
			double idf_score = Math.log(totalDocNum / idf.get(key));
			if(checks.containsKey(key)){
				total_up += arg2.get(key) * checks.get(key) * Math.pow(idf_score, 2.0);
			}
			total_b += arg2.get(key) * arg2.get(key) * Math.pow(idf_score, 2.0);
		}
		return total_up / Math.sqrt(total_a) / Math.sqrt(total_b);
	}
	
}
