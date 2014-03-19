package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.mozilla.universalchardet.UniversalDetector;

public class FileUtil {
	
	
	public static StringBuffer readAll(String path){
		StringBuffer result = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line = "";
			while((line = br.readLine())!= null){
				result.append(line + "\n");
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static List<String> readAllToList(String path){
		List<String> result = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line = "";
			while((line = br.readLine())!= null){
				result.add(line);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * @param path
	 * @return
	 * @Description: will return the file encoding, if can't detect, will return "";
	 */
	public static String getEncoding(String path){
		String encoding = "";
		try{
			byte[] buf = new byte[4096];
	        String fileName = path;
	        java.io.FileInputStream fis = new java.io.FileInputStream(fileName);
	        UniversalDetector detector = new UniversalDetector(null);
	        int nread;
	        while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
	          detector.handleData(buf, 0, nread);
	        }
	        detector.dataEnd();
	        encoding = detector.getDetectedCharset();
	        fis.close();
	        if (encoding == null){
	        	return "";
	        }
		}catch(IOException ioe){
			return "";
		}
		return encoding;
	}
	
	public static void tranformFileEncoding(String inFilePath, String originalEncoding,String outFolder, String outFilePath,String NewEncoding){
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inFilePath),originalEncoding));
			File file = new File(outFolder);
			if(!file.exists()){
				file.mkdir();
			}
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFilePath),NewEncoding));
			String line = br.readLine();
			while((line = br.readLine()) != null){
				bw.write(line + "\n");
			}
			br.close();
			bw.close();	
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
