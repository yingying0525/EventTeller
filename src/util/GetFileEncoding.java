package util;

import java.io.IOException;

import org.mozilla.universalchardet.UniversalDetector;

public class GetFileEncoding {
	
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

}
