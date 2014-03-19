package util;

//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;


public class Util {
	
	
	
	
	
	
	
	
	
//	//words string in db convert to map
//	//format is "a,5 b,10" 
//	public static  Map<String,Integer> getDdfMap(String words){
//		Map<String,Integer> results = new HashMap<String,Integer>();
//		String[] wds = words.split(" ");
//		for(String wd : wds){
//			if(wd.length() == 0)
//				continue;
//			String[] score = wd.split(",");
//			results.put(score[0], Integer.valueOf(score[1]));
//		}			
//		return results;
//	}
//	
//	///write map to str format
//	public static String DdfMapToStr(Map<String,Integer> words){
//		StringBuilder result = new StringBuilder();
//		Iterator<String> its = words.keySet().iterator();
//		while(its.hasNext()){
//			String word = its.next();
//			result.append(word + "," + words.get(word) + " ");
//		}
//		return result.toString();
//	}

	public static void ArrayCopy(double a[][] , double b[][], int N, int M){
		for(int i = 0 ;i<N;i++){
			for(int j = 0 ;j<M;j++){
				a[i][j] = b[i][j];
			}
		}
	}

	

	


}
