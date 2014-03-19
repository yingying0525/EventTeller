package util;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {
	
	public static int getDayGMT8(Date dt){
		int res = 0;
		if(dt == null){
			return res;
		}
		res = (int)((dt.getTime() / 1000 / 60 / 60 + 8 ) / 24);
		return res;
	}
	
	public static String getDateStr(Date date){
		if(date == null){
			date = new Date();
		}
		String result = "";
		java.text.DateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd");
	    result = format.format(date);
		return result;
	}
	
	public static String extractTimeFromText(String text){
		Pattern pattern = Pattern.compile("\\d{2,4}.\\d{1,2}.\\d{1,2}.?\\s*\\d{1,2}:\\d{1,2}:?\\d{0,2}");
		Matcher matcher = pattern.matcher(text);
		if(matcher.find()){
			return matcher.group().trim();
		}
		return "";
	}

}
