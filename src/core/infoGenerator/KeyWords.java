package core.infoGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import db.data.Word;



class KeyComparetor implements Comparator<tWord>{

	@Override
	public int compare(tWord wa, tWord wb) {
		if(wa.score > wb.score){
			return 	-1;
		}else if(wa.score == wb.score){
			return 0;
		}else{
			return 1;
		}
	}
}

class tWord{
	Word name;
	double score;
}

public class KeyWords {

	private List<tWord> words ;
	
	public KeyWords(String content){
		words = getKeyWords(content);
	}
	
	
	private List<tWord> getKeyWords(String content){
		List<tWord> results = new ArrayList<tWord>();
		Map<Word,Integer> words = util.ChineseSplit.SplitStrWithPosTF(content);
		Iterator<Word> its = words.keySet().iterator();
		while(its.hasNext()){
			Word key = its.next();
			tWord tw = new tWord();
			tw.name = key;
			tw.score = words.get(key);
			results.add(tw);
		}
		Collections.sort(results,new KeyComparetor());
		return results;		
	}
	
	public static String getTopWords(String content,String nstr,int N){
		List<tWord> words = new ArrayList<tWord>();
		StringBuffer result = new StringBuffer();
		String[] its = content.split(" ");
		Map<String,Integer> nwords = util.ChineseSplit.SplitStrWithPosTFS(nstr);
		for(String it : its){
			String[] sits = it.split(",");
			if(sits.length != 2){
				continue;
			}
			if(nwords.containsKey(sits[0])){
				nwords.put(sits[0], Integer.valueOf(sits[1]) + nwords.get(sits[0]));
			}else{
				nwords.put(sits[0], Integer.valueOf(sits[1]));
			}
		}
		Iterator<String> iwords = nwords.keySet().iterator();
		while(iwords.hasNext()){
			String key = iwords.next();
			tWord ntw = new tWord();
			Word nw = new Word();
			nw.setName(key);
			ntw.name = nw;
			ntw.score = nwords.get(key);
			words.add(ntw);
		}
		Collections.sort(words,new KeyComparetor());
		int num = 0;
		for(tWord tmp : words){
			if(num >= N){
				break;
			}
			result.append(tmp.name.getName() + " ");
			num++;
		}
		return result.toString().trim();
	}
	
	/**
	 * type = 0 all 
	 * type = 1 only person
	 * type = 2 only location
	 * @param N
	 * @param type
	 * @return
	 */
	public String getTopNwords(int N, int type){
		if(words == null)
			return "";
		StringBuffer result = new StringBuffer();
		for(int i = 0 ; i <N && i <words.size();i++){
			if(type == 0){
				result.append(words.get(i).name.getName() + " ");
			}else if(type == 1){
				if(words.get(i).name.getNature().equals("nr") || words.get(i).name.getNature().equals("nt")){
					result.append(words.get(i).name.getName());
				}				
			}else if(type == 2){
				if(words.get(i).name.getNature().equals("ns")){
					result.append(words.get(i).name.getName());
				}	
			}				
		}
		return result.toString().trim();
	}
}
