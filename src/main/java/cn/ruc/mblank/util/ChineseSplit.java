package cn.ruc.mblank.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;

import cn.ruc.mblank.core.infoGenerator.model.Word;






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
	
	
	public static boolean checkPerson(Word wd){
		if(wd.getNature() != null && wd.getNature().indexOf("nr") >= 0){
			return true;
		}
		return false;
	}
	
	public static boolean checkPosition(Word wd){
		if(wd.getNature() != null && wd.getNature().indexOf("ns") >= 0){
			return true;
		}
		return false;
	}
	
	public static boolean checkVerb(Word wd){
		if(wd.getNature() != null && wd.getNature().indexOf("v") == 0){
			return true;
		}
		return false;
	}
	
	/**
	 * @param str
	 * @return
	 * @Description:filter some useless words
	 */
	private static boolean checkNature(String str){
		boolean result = true;
		if(str == null || str.equals("null")){
			return false;
		}
		List<String> check = new ArrayList<String>();
		check.add("w");//punctuation
		check.add("p");//prepositional
		check.add("c");//conjunction
		check.add("u");//conjunction
		check.add("r");//pronoun
		check.add("y");
		check.add("z");
		check.add("u");
		check.add("r");
		check.add("o");
		check.add("e");
		check.add("d");
		check.add("j");
		for(String ch : check){
			if(str.indexOf(ch) == 0){
				result = false;
				break;
			}
		}
		return result;
	}
    
    /**
     * @param text
     * @return
     * @Description:split chinese string using ansj_seg using nlp paser
     * https://github.com/ansjsun/ansj_seg/wiki
     */
    public static List<String> SplitStr(String text){
    	List<String> result = new ArrayList<String>();
    	List<Term> terms = NlpAnalysis.parse(text);
		for(Term term : terms){
			String nature = term.getNatrue().natureStr;
			if(!checkNature(nature) ||term.getName().length() < 2 )
				continue;
			String tmp = term.getName().trim();
			if(tmp.length() == 0){
				continue;
			}
			result.add(tmp);
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
    	List<Term> terms = NlpAnalysis.parse(text);
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

