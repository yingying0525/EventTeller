package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ansj.domain.Term;
import org.ansj.recognition.NatureRecognition;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;

import db.hbn.model.Word;





/**
* @PackageName:news.extractor
* @ClassName: ChineseSplit
* @author: mblank
* @date: 2012-3-6 下午8:55:38
* @Description: split chinese words
* @Marks: using ikanaylzer.jar
* @chages: add a new pos tool (ansj_seg.jar https://github.com/ansjsun/ansj_seg),this tool can give out word nature.
*/
public class ChineseSplit {
	
	
	
	private static boolean checkNature(String str){
		boolean result = true;
		if(str == null || str.equals("null")){
			result = false;
			return result;
		}
		Map<String,Boolean> check = new HashMap<String,Boolean>();
		check.put("w", true);//punctuation
		check.put("p", true);//prepositional
		check.put("c", true);//conjunction
		check.put("un", true);//conjunction
		check.put("r", true);//pronoun
		check.put("y", true);
		check.put("z", true);
		check.put("u", true);
		check.put("r", true);
		check.put("o", true);
		check.put("e", true);
		check.put("d", true);
		if(check.containsKey(str)|| str.indexOf("j") >= 0){
			result = false;
		}
		//for gc
		check = null;
		return result;
	}
    
    /**
     * @param text
     * @return
     * @Description:split chinese string using ansj_seg 
     * https://github.com/ansjsun/ansj_seg/wiki
     */
    public static List<String> SplitStr(String text){
    	List<String> result = new ArrayList<String>();
    	List<Term> terms = ToAnalysis.parse(text);
    	new NatureRecognition(terms).recognition();
		for(Term term : terms){
			String nature = term.getNatrue().natureStr;
			if(!checkNature(nature) ||term.getName().length() < 2 )
				continue;
			result.add(term.getName());
		}
    	return result;
    }
    
    public static List<String> SplitStrNlp(String text){
    	List<String> result = new ArrayList<String>();
    	List<Term> terms = NlpAnalysis.parse(text);
    	new NatureRecognition(terms).recognition();
		for(Term term : terms){
			String nature = term.getNatrue().natureStr;
			if(!checkNature(nature) ||term.getName().length() < 2 )
				continue;
			result.add(term.getName());
		}
    	return result;
    }
    
    /**
     * @param text
     * @return
     * @Description:split chinese string using ansj_seg with pos
     * https://github.com/ansjsun/ansj_seg/wiki
     */
    public static List<Word> SplitStrWithPos(String text){
    	List<Word> result = new ArrayList<Word>();
    	List<Term> terms = null;
    	try{
    		terms = ToAnalysis.parse(text);
    	}catch(Exception e){
    		return result;
    	}    	
    	new NatureRecognition(terms).recognition() ;
    	for(Term tm : terms){
    		String nature = tm.getNatrue().natureStr;
			if(tm.getName().length() < 2 || !checkNature(nature))
				continue;
    		Word wp = new Word();
    		wp.setName( tm.getName());
    		wp.setNature( nature);
    		result.add(wp);
    	}
    	return result;
    }
    
    public static Map<Word,Integer> SplitStrWithPosTF(String text){
    	Map<Word,Integer> results = new HashMap<Word,Integer>();
    	Map<String,Word> maps = new HashMap<String,Word>();
    	Map<String,Integer> counts = new HashMap<String,Integer>();
    	List<Word> tmps = SplitStrWithPos(text);
    	Integer total = 0;
    	for(Word tmp : tmps){
    		if(counts.containsKey(tmp.getName())){
    			counts.put(tmp.getName(), counts.get(tmp.getName())+1);
    		}else{
    			counts.put(tmp.getName(), 1);
    		}
    		maps.put(tmp.getName(), tmp);
    		total++;
    	}
    	//for gc 
    	Iterator<String> it_words = counts.keySet().iterator();
    	while(it_words.hasNext()){
    		String key = it_words.next();
    		results.put(maps.get(key), counts.get(key));
    	}
    	counts = null;
    	maps = null;
    	return results;    	
    }
    
    public static Map<String,Integer> SplitStrWithPosTFS(String text){
    	Map<String,Integer> results = new HashMap<String,Integer>();
    	List<Word> tmps = SplitStrWithPos(text);
    	for(Word tmp : tmps){
    		if(results.containsKey(tmp.getName())){
    			results.put(tmp.getName(), results.get(tmp.getName())+1);
    		}else{
    			results.put(tmp.getName(), 1);
    		}
    	}
    	return results;    	
    }
    
    public static Map<Word,Double> SplitStrWithPosDoubleTF(String text){
    	Map<Word,Double> results = new HashMap<Word,Double>();
    	Map<String,Word> maps = new HashMap<String,Word>();
    	Map<String,Integer> counts = new HashMap<String,Integer>();
    	List<Word> tmps = SplitStrWithPos(text);
    	double total = 0.001;
    	for(Word tmp : tmps){
    		if(tmp.getName().length() == 0)
    			continue;
    		if(counts.containsKey(tmp.getName())){
    			counts.put(tmp.getName(), counts.get(tmp.getName())+1);
    		}else{
    			counts.put(tmp.getName(), 1);
    		}
    		maps.put(tmp.getName(), tmp);
    		total++;
    	}
    	//for gc 
    	Iterator<String> it_words = counts.keySet().iterator();
    	while(it_words.hasNext()){
    		String key = it_words.next();
    		results.put(maps.get(key), counts.get(key)/total);
    	}
    	counts = null;
    	maps = null;
    	return results;    	
    }
    
}

