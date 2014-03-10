package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;


public class TransformFileEncoding extends Thread{
	
	public static String NewEncoding = "UTF8";
	private int id;
	private String[] args;
	private static int TotalThreadNum = 8;
	
	public TransformFileEncoding(int id,String[] args){
		this.id = id;
		this.args = args;
	}
	
	
	
	private static void tranform(String inFilePath, String originalEncoding,String outFolder, String outFilePath){
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
	
	public void run(){
		String folderPath = args[0];
		String outFolderPath = args[1];
		int folderNum = 0;
		int fail = 0;
		String[] files = new File(folderPath).list();
		int start = id * (files.length / TotalThreadNum);
		int end = Math.min((id + 1) * (files.length /TotalThreadNum) - 1, files.length - 1);
		for(String file : files){
			int FileNum = 0;
			if(file.contains("2012") || folderNum < start || folderNum > end){
				folderNum++;
				continue;
			}
			File nfile = new File(folderPath + File.separator + file);
			String[] htmls = nfile.list();
			for(String html : htmls){
				String path = folderPath + File.separator + file + File.separator + html;
				String encoding = util.GetFileEncoding.getEncoding(path);
				if(encoding == ""){
					fail++;
					continue;
				}
				tranform(path,encoding,outFolderPath + File.separator + file, outFolderPath + File.separator + file + File.separator + html);
				FileNum++;
				if(FileNum % 500 == 0){
					System.out.println("thread " + id + "\t " + FileNum / (double)htmls.length + "\t fail: " + fail + "\t" + folderNum / (double)files.length / TotalThreadNum);
				}
			}
			folderNum++;
		}
	}
	
	
	public static void main(String[] args) throws IOException, InterruptedException{
		
		for(int i = 0 ; i< TotalThreadNum; i++){
			Thread td = new TransformFileEncoding(i,args);
			td.start();
		}		
	}

}
