package cn.ruc.mblank.core.infoGenerator.topic;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.ruc.mblank.core.infoGenerator.model.Word;
import cn.ruc.mblank.db.hbn.model.Event;

public class KeyWords {
	
	private List<Event> Events;
	private List<Word> TWords = new ArrayList<Word>();
//	private List<Word> CWords = new ArrayList<Word>();
	
	public KeyWords(List<Event> ets){
		this.Events = ets;
		getTWords();
	}
	
	private void getTWords(){
		HashMap<String,Word> wordMap = new HashMap<String,Word>();
        double TTF = 1.0;
		for(Event et : Events){
			Set<Word> has = new HashSet<Word>();
			if(et.getTitle() != null){
				List<Word> wds = cn.ruc.mblank.util.ChineseSplit.SplitStrWithPos(et.getTitle());
                TTF += wds.size();
				for(Word wd : wds){
					if(wordMap.containsKey(wd.getName())){
						Word nwd = wordMap.get(wd.getName());
						if(!has.contains(nwd)){
							has.add(nwd);
							nwd.setDf(nwd.getDf() + 1);
						}
						nwd.setTf(nwd.getTf() + 1);
					}else{
						wd.setTf(1);
						wd.setDf(1);
						wordMap.put(wd.getName(), wd);
						has.add(wd);
					}
				}
			}
		}
		for(Word wd : wordMap.values()){
            wd.setScore(wd.getTf() / TTF);
			TWords.add(wd);
		}
	}
	
	class WordComparator implements Comparator<Word>{

		@Override
		public int compare(Word wd1, Word wd2) {
			// TODO Auto-generated method stub
			if(((Word)wd1).getScore() > ((Word)wd2).getScore()){
				return -1;
			}else if(((Word)wd1).getScore() < ((Word)wd2).getScore()){
				return 1;
			}
			return 0;
		}
		
	}
	
	public List<String> getKeyWords(int N){
		List<String> res = new ArrayList<String>();
		int num = 0;
		Collections.sort(TWords, new WordComparator());
		for(Word wd : TWords){
			if(num++ < N){
                DecimalFormat df = new DecimalFormat("#0.000");
				res.add(wd.getName() + ":" + df.format(wd.getScore()));
			}else{
				break;
		}
		}
		return res;		
	}
	


}
