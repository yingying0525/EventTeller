package util;

import java.util.List;

public class StringUtil {
	
	

	/**
	 * @param texts
	 * @return
	 * @Description:convert list to string
	 */
	public static String ListToStr(List<String> texts){
		String result = "";
		for(String text:texts ){
			result+=text + " ";
		}		
		return result;
	}
	
	/**
	 * @param texts
	 * @return
	 * @Description:convert list to string
	 */
	public static String ListToStrForm(List<String> texts){
		String result = "";
		for(String text:texts ){
			result += "<p>    " + text + "</p>\n";
		}		
		return result;
	}
	
	/**
	 * @param texts
	 * @return
	 * @Description:convert list to string
	 */
	public static String ListToStr(List<String> texts,String split){
		String result = "";
		int num = 0;
		for(String text:texts ){
			num++;
			if(num == 1){
				result = text;
			}else{
				result += split +  text ;	
			}		
		}		
		return result;
	}
	
	/**
	 * @param texts
	 * @return
	 * @Description:convert list to string
	 */
	public static String ListToStr(List<String> texts,String split,int n){
		String result = "";
		int num = 0;
		for(String text:texts ){
			num++;
			if(num == 1){
				result = text;
			}else{
				result += split +  text ;	
			}		
			if(num > n)
				break;
		}		
		return result;
	}

}
