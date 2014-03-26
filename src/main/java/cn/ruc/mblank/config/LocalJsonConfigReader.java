package cn.ruc.mblank.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class LocalJsonConfigReader {
	
	public static String readJsonFile(String filePath){
		StringBuffer res = new StringBuffer();
		try{
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line;
			while((line = br.readLine()) != null){
				res.append(line);
			}
			br.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		return res.toString();
	}

}
