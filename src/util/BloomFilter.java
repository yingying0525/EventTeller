package util;

import java.util.BitSet;  


/**
* @PackageName:news.crwaler.urlBloom
* @ClassName: BloomFilter
* @author: mblank
* @date: Jul 1, 2012 9:41:18 PM
* @Description: TODO
* @Marks: TODO
*/
public class BloomFilter {  
  
    public  int DEFAULT_SIZE ; 
    public  int[] seeds  ; 
    public  BitSet bits ; 
    public  SimpleHash[] func ; 
    
    public BloomFilter(){
    	DEFAULT_SIZE = 2 << 28;
    	seeds  = new int[]{3,5,7, 11, 13, 31, 37, 61};
    	bits = new BitSet(DEFAULT_SIZE);
    	func  = new SimpleHash[seeds.length];
        for (int i = 0; i < seeds.length; i++) {  
            func[i] = new SimpleHash(DEFAULT_SIZE, seeds[i]);  
        } 
    }
  
    public  void addValue(String value)  
    {  
    	
        for(SimpleHash f : func)//将字符串value哈希为8个或多个整数，然后在这些整数的bit上变为1  
            bits.set(f.hash(value),true);  
    }  
      
    public  void add(String value)  
    {  
        if(value != null) addValue(value);  
    }  
      
    public  boolean contains(String value)  
    {  
        if(value == null) return false;  
        boolean ret = true;  
        for(SimpleHash f : func)//这里其实没必要全部跑完，只要一次ret==false那么就不包含这个字符串  
            ret = ret && bits.get(f.hash(value));  
        return ret;  
    }  
      

}  
  
class SimpleHash {
  
    private int cap;  
    private int seed;  
  
    public  SimpleHash(int cap, int seed) {  
        this.cap = cap;  
        this.seed = seed;  
    }  
  
    public int hash(String value) {//字符串哈希，选取好的哈希函数很重要  
        int result = 0;  
        int len = value.length();  
        for (int i = 0; i < len; i++) {  
            result = seed * result + value.charAt(i);  
        }  
        return (cap - 1) & result;  
    }  
    

}
