package util.hash;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;




class HashWord{
	long hash;
	int weight;
}

public class SimHash {

	
	private static long calSimHash(Map<String,Integer> words){
		long res = 0;
		long[] bits = new long[util.Const.SimHashBitNumber];
		List<HashWord> hws = new ArrayList<HashWord>();
		for(String word : words.keySet()){
			HashWord hw = new HashWord();
			hw.hash = MurmurHash.hash64(word);
			hw.weight = words.get(word);
			hws.add(hw);
		}
		for(HashWord hw : hws){
			long tmp = hw.hash;
			for(int i = 0 ; i < util.Const.SimHashBitNumber; i++){
				long bit = tmp & 1;
				bits[i] += (bit == 1 ? 1 : -1) * hw.weight;
				tmp = tmp >> 1;
			}
		}
		for(int i = 0 ; i < util.Const.SimHashBitNumber; i++){
			if(bits[i] > 0){
				res += 1;
			}
			res = res << 1;
		}
		return res;
	}
	
	public static long getSimHash(String text){
		Map<String, Integer> words = util.ChineseSplit.SplitStrWithPosTFS(text);
		return calSimHash(words);
	}
	
	public static int getSimHashDiffBits(String texta, String textb){
		long simHashA = getSimHash(texta);
		long simHashB = getSimHash(textb);
		return util.Coding.diffBitsOfNums(simHashA,simHashB);
	}
	


}