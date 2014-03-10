package test;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

class JsonTestObj{
	public int count;
	public String name;
	public double score;
	public Date now;
	
	private String sex;
	
	
	public JsonTestObj(){
		count = 1;
		name = "a";
		score = 1.1;
		now = new Date();
		setSex("m");
	}


	public String getSex() {
		return sex;
	}


	public void setSex(String sex) {
		this.sex = sex;
	}
};

public class FastJsonTest {
	

	
	
	public static void main(String[] args){
		JsonTestObj jto = new JsonTestObj();
		String text = JSON.toJSONString(jto,SerializerFeature.WriteDateUseDateFormat);
		System.out.println(text);
		JsonTestObj jto2 = JSON.parseObject(text,JsonTestObj.class);
		System.out.println(jto2.count + "\t" + jto2.name);
//		JsonTestObj jto2 = JSON.T
	}
	

}
