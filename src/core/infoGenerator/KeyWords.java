package core.infoGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
	
	
	public List<tWord> getKeyWords(String content){
		List<tWord> words = new ArrayList<tWord>();
		String[] its = content.split(" ");
		for(String it : its){
			String[] sits = it.split(",");
			if(sits.length != 2){
				continue;
			}
			tWord ntw = new tWord();
			Word nw = new Word();
			nw.setName(sits[0]);
			ntw.name = nw;
			ntw.score = Integer.valueOf(sits[1]);
			words.add(ntw);
		}
		Collections.sort(words,new KeyComparetor());
		return words;
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
