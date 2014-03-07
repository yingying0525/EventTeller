package core.evolution;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;



import extractor.article.Extractor;



import db.hbn.model.Url;
import db.hbn.model.Word;




class DoubleWords{
	public String words;
	public int count;
	
}



@SuppressWarnings("rawtypes")
class ComparatorWords implements Comparator{

	 public int compare(Object arg0, Object arg1) {
		 DoubleWords eta=(DoubleWords)arg0;
		 DoubleWords etb=(DoubleWords)arg1;		 
		 if(eta.count > etb.count){
			 return -1;
		 }else if(eta.count < etb.count){
			 return 1;
		 }else{
			 return 0;
		 }
	 }
	
} 


class html {
	public Date time;
	public String title;
	public int id;
	public Map<Word,Double> words;
	public String contents;
}



public class SingleLinkageModel {
	

	private static String Path = "D:\\ETT\\";
	private static double Threshold = 0.4;
	public Map<String,Integer> IDF = new HashMap<String,Integer>();
	public Map<Integer,Integer> standard = new HashMap<Integer,Integer>();
	public List<html> htmls = new ArrayList<html>();
	Map<Integer,List<Integer>> Result = new HashMap<Integer,List<Integer>>();
	
	private void loadStandard(){
		List<String> lines = util.FileUtil.readAllToList(Path + "clusters.txt");
		for(String ln : lines){
			String[] its = ln.split("\t");
			standard.put(Integer.valueOf(its[0]), Integer.valueOf(its[1]));
		}
	}
	
	private void loadHtmls(boolean MainPara){
		
		String file = Path + "tianyi";
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			while((line = br.readLine())!= null){
				String[] its = line.split("\t");
				html ht = new html();
				ht.id = Integer.valueOf(its[0]);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				ht.time = sdf.parse(its[1]);
				ht.title = its[2];
				if(MainPara){
					ht.contents = getMainPara(its[3],its[2]);
				}else{
					ht.contents = its[3];
				}
				ht.words = util.ChineseSplit.SplitStrWithPosDoubleTF(ht.contents + " " + ht.title);
				htmls.add(ht);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	private void ldatest(String file){
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			StringBuilder sb = new StringBuilder();
			while((line = br.readLine())!=null){
				sb.append(line);
			}
			
			System.out.println(sb.toString());
			List<String> words = util.ChineseSplit.SplitStr(sb.toString());
			BufferedWriter bw = new BufferedWriter(new FileWriter(file+"_out"));
			for(String wd : words){
				bw.write(wd + " ");
			}
			bw.write("\n");
			br.close();
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private double purityEvaluate(){
		double res = 0.0;
		Iterator<Integer> its = Result.keySet().iterator();
		while(its.hasNext()){
			double max_num = 0.0;
			Map<Integer,Integer> tmp = new HashMap<Integer,Integer>();
			List<Integer> vls = Result.get(its.next());
			for(int vl : vls){
				int cl = standard.get(vl);
				if(tmp.containsKey(cl)){
					tmp.put(cl, 1+tmp.get(cl));
					if(tmp.get(cl) > max_num){
						max_num = tmp.get(cl);
					}
				}else{
					tmp.put(cl, 1);
					if(max_num < 1 ){
						max_num = 1;
					}
				}
			}
			res += max_num;
		}
		return res/standard.size();
	}
	
	private double overlapTwoList(List<Integer> arg1,List<Integer> arg2){
		double result = 0.0;
		Set<Integer> checks = new HashSet<Integer>();
		for(Integer a : arg1){
			checks.add(a);
		}
		for(Integer b : arg2){
			if(checks.contains(b)){
				result++;
			}
		}
		return result;
	}
	
	private double nmiEvaluate(){
		double mi = 0.0;
		double hw = 0.0;
		double hc = 0.0;
		Map<Integer,List<Integer>> standardl = new HashMap<Integer,List<Integer>>();
		//transform standard to standardl
		Iterator<Integer> its = standard.keySet().iterator();
		while(its.hasNext()){
			int key = its.next();
			if(standardl.containsKey(standard.get(key))){
				standardl.get(standard.get(key)).add(key);
			}else{
				List<Integer> ntmp = new ArrayList<Integer>();
				ntmp.add(key);
				standardl.put(standard.get(key), ntmp);
			}
		}
		double N = standard.size();
		Iterator<Integer> ws = Result.keySet().iterator();
		while(ws.hasNext()){
			List<Integer> vls = Result.get(ws.next());
			Iterator<Integer> cs = standardl.keySet().iterator();
			double thc = 0.0;
			while(cs.hasNext()){
				List<Integer> cvls = standardl.get(cs.next());
				double ov = overlapTwoList(vls,cvls);
				if(ov == 0){
					ov = 0.00000001;
				}
				mi += ov/N * (Math.log(N*ov/ vls.size() / cvls.size()));
				thc +=  cvls.size()/N * Math.log(cvls.size()/N);
			}
			hw += vls.size()/N * Math.log(vls.size()/N);
			hc = thc;
		}
		return 2*mi / (-hw - hc);
	}
	
	public double riEvaluate(int beta){
		
		double tp = 0,fp = 0,tn =0,fn = 0;
		//transform result to map
		Map<Integer,Integer> resMap = new HashMap<Integer,Integer>();
		Iterator<Integer> its = Result.keySet().iterator();
		while(its.hasNext()){
			int key = its.next();
			for(int vl : Result.get(key)){
				resMap.put(vl, key);
			}
		}
		for(int i = 0 ; i< standard.size();i++){
			for(int j = i + 1;j<standard.size();j++){
				boolean wb = (resMap.get(i) == resMap.get(j));
				boolean cb = standard.get(i) == standard.get(j);
				if(wb && cb){
					tp ++;
				}else if(!wb && cb){
					fp++;
				}else if(wb && !cb){
					tn++;
				}else{
					fn++;
				}
			}
		}
		if(beta > 0){
			double p = tp/(tp + fp);
			double r = tp / (tp + fn);
			return (beta * beta + 1)*p*r / (beta*beta*p + r);
		}
		double down = tp + fp + tn + fn;
		if(down == 0){
			return 0.00001;
		}else{
			return (tp+fn ) / down;
		}
	}
	
	private void updateIDF(html ht){
		Iterator<Word> it_words = ht.words.keySet().iterator();
		while(it_words.hasNext()){
			Word it_wd = it_words.next();
			if(IDF.containsKey(it_wd.getName())){
				IDF.put(it_wd.getName(), IDF.get(it_wd.getName()) + 1);
			}else{
				IDF.put(it_wd.getName(), 1);
			}
		}
	}
	
	public void showResult(){
		Iterator<Integer> itcls = Result.keySet().iterator();
		while(itcls.hasNext()){
			int key = itcls.next();
			System.out.println("-------------- " + key);
			List<Integer> vls = Result.get(key);
			for(int vl : vls){
				System.out.println(htmls.get(vl).title);
			}
		}
	}

	public void singleLink(boolean idf){
		
		Map<Integer,Integer> lables = new HashMap<Integer,Integer>();
		int maxlb = 0;
		
		for(int i = 0; i < htmls.size(); i++){
			updateIDF(htmls.get(i));
			double max_sim = -1.0;
			int max_id = -1;			
			for(int j = i -1 ; j>=0;j--){
				double sim = 0.0;
				if(idf){
					sim = util.Similarity.SimilarityWithIDF(htmls.get(i).words, htmls.get(j).words,IDF,htmls.size()) ;
				}else{
					sim = util.Similarity.SimilarityOfTF(htmls.get(i).words, htmls.get(j).words);
				}
				if(sim > max_sim ){
					max_sim = sim;
					max_id = j;
				}
			}
			if(max_sim > Threshold){
				int cs = lables.get(max_id);
				lables.put(i, cs);
				Result.get(cs).add(i);
			}else{
				maxlb++;
				lables.put(i, maxlb);
				List<Integer> newls = new ArrayList<Integer>();
				newls.add(i);
				Result.put(maxlb, newls);
			}
//			
////			if(max_sim < 0.3)
////				continue;
////			String var = "var t" + i + "= graph.newNode({label: 't"+i +" "+ htmls.get(i).title + "'});";			
////			String edgs = "graph.newEdge(t" + max_id + ",t" +  i+",{label: '" + max_sim + "'});";
		}
	}
	
	public void averageCluster(boolean idf){
		
		for(int i = 0; i < htmls.size(); i++){
			updateIDF(htmls.get(i));
			double max_sim = -1.0;
			int max_id = -1;
			Iterator<Integer> cls = Result.keySet().iterator();
			while(cls.hasNext()){
				double totals = 0.0;
				double tnum = 0.0001;
				int key = cls.next();
				List<Integer> vls = Result.get(key);
				for(int vl : vls){
					tnum++;
					double sim = 0.0;
					if(idf){
						sim = util.Similarity.SimilarityWithIDF(htmls.get(i).words, htmls.get(vl).words,IDF,htmls.size()) ;
					}else{
						sim = util.Similarity.SimilarityOfTF(htmls.get(i).words, htmls.get(vl).words);
					}
					totals+=sim;
				}
				totals = totals/tnum;
				if(totals > max_sim){
					max_id = key;
					max_sim = totals;
				}
			}
			
			if(max_sim > Threshold){
				Result.get(max_id).add(i);
			}else{
				max_id = Result.size();
				List<Integer> tids = new ArrayList<Integer>();
				tids.add(i);
				Result.put(max_id, tids);
			}			
		}
	}
	
	public void totalCluster(){

		
		List<StringBuffer> contents = new ArrayList<StringBuffer>();
		
		for(int i = 0; i < htmls.size(); i++){
			updateIDF(htmls.get(i));
			double max_sim = -1.0;
			int max_id = -1;
			int num = 0;
			for(StringBuffer ct : contents){
				double sim = util.Similarity.ContentOverlap(ct.toString(), htmls.get(i).contents);
				if(sim > max_sim){
					max_sim = sim;
					max_id = num;
				}
				num++;
			}
			if(max_sim > Threshold){
				contents.set(max_id, contents.get(max_id).append(htmls.get(i).contents));
				Result.get(max_id).add(i);
			}else{
				List<Integer> ntmp = new ArrayList<Integer>();
				ntmp.add(i);
				Result.put(contents.size(), ntmp);
				contents.add(new StringBuffer(htmls.get(i).contents));
			}
			System.out.println(i);
		}
	}

	@SuppressWarnings({ "unused", "deprecation" })
	private void download() throws IOException{
		int num = 0;
		String file = "d:\\ETT\\";
		BufferedReader br = new BufferedReader(new FileReader(file + "url.txt"));
		BufferedWriter bw = new BufferedWriter(new FileWriter(file + "tianyi_raw"));
		String line = "";
		while((line = br.readLine()) != null){
			num++;
			String url = line;
			Url curl = new Url();
			curl.setUrl(url);
			Extractor etor = new Extractor(curl);
			String content = etor.getContent();
			List<String> words = util.ChineseSplit.SplitStr(content);
			if(words.size() == 0)
				continue;
			bw.write(etor.getPublishTime().toLocaleString() + "\t" + etor.getTitle()+"\t" + etor.getContent());
			bw.write("\n");
			System.out.println(url);
		}
		br.close();
		bw.close();
		resort();
	}
	
	private void resort() throws IOException{
		String path = "D:\\ETT\\tianyi_raw";
		String outpath = "D:\\ETT\\tianyi";
		List<String> lines = new ArrayList<String>();
		Map<Integer,Date> dates = new HashMap<Integer,Date>();
		BufferedReader br = new BufferedReader(new FileReader(path));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outpath));
		String line = "";
		
		while((line = br.readLine())!=null){
			String[] its = line.split("\t");
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");        
			Date dt = new Date();   
			// StringToDate   
			try {   
			    dt = format.parse(its[0]);  
			} catch (ParseException e) {   
			    e.printStackTrace();   
			}   
			dates.put(lines.size(), dt);
			lines.add(line);
		}
		for(int i = 0 ; i<lines.size();i++){
			for(int j = i +1;j <lines.size();j++){
				if(dates.get(j).getTime() - dates.get(i).getTime() < 0){
					String tmp = lines.get(i);
					lines.set(i, lines.get(j));
					lines.set(j, tmp);
					Date tmpd = dates.get(i);
					dates.put(i, dates.get(j));
					dates.put(j, tmpd);
				}
			}
			System.out.println(dates.get(i));
		}
		for(int i = 0 ;i < lines.size();i++){
			bw.write(i + "\t" + lines.get(i) + "\n");
		}
		br.close();
		bw.close();
	}
	
	@SuppressWarnings("unused")
	private void FindByDf() throws IOException{
		String path = "D:\\ETT\\tianyi";
		String outpath = "D:\\ETT\\tianyi_df";
		int totalDocNum = 0;
		Map<String,Integer> DF = new HashMap<String,Integer>();
		List<String> lines = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(path));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outpath));
		String line = "";
		while((line = br.readLine())!=null){
			StringBuffer sb = new StringBuffer();
			Set<String> has = new HashSet<String>();
			String[] its = line.split("\t");
			totalDocNum++;
			String[] words = its[3].split(" ");
			for(String wd : words){
				if(!DF.containsKey(wd) || DF.get(wd) < totalDocNum /2){
					sb.append(wd + " ");
				}
			}			
			for(String word : words){
				if(!DF.containsKey(word)){
					DF.put(word, 1);
				}else{
					if(!has.contains(word)){
						DF.put(word, DF.get(word) + 1);
						has.add(word);
					}
				}
			}
			lines.add(its[1] + "\t" + its[2] + "\t" + sb.toString());
			bw.write(its[0] + "\t" + its[1] + "\t" + its[2] + "\t" + sb.toString() + "\n");
		}
		br.close();
		bw.close();
	}

	@SuppressWarnings("unused")
	private void DFDistribution() throws IOException{
		List<Map<Double,Integer>> dfd = new ArrayList<Map<Double,Integer>>();
		List<Map<Double,String>> dfw = new ArrayList<Map<Double,String>>();
		Map<String,Integer> df = new HashMap<String,Integer>();
		String file_in = "D:\\ETT\\tianyi_df";
		String file_out = "D:\\ETT\\tianyi";
		List<String> lines = util.FileUtil.readAllToList(file_in);
		for(String line : lines){
			Map<String,Integer> checks = new HashMap<String,Integer>();
			Map<Double,Integer> tmp = new TreeMap<Double,Integer>();
			Map<Double,String> tmpw = new TreeMap<Double,String>();
			String[] its = line.split("\t");
			String[] words = its[3].split(" ");
			for(int i = 0 ;i < words.length;i++){
				double index = (i + 1) / (double)words.length;
				if(!checks.containsKey(words[i])){
					if(df.containsKey(words[i])){
						tmp.put(index, df.get(words[i]) + 1);
						df.put(words[i], df.get(words[i]) + 1);
					}else{
						tmp.put(index, 1);
						df.put(words[i], 1);
					}
					tmpw.put(index, words[i]);
					checks.put(words[i],tmp.get(index));
				}
			}
			dfd.add(tmp);
			dfw.add(tmpw);
		}
		int num = 0;
		for(Map<Double,Integer> tdf : dfd){
			BufferedWriter bw = new BufferedWriter(new FileWriter(file_out + "_" + num));
			Iterator<Double> its = tdf.keySet().iterator();
			while(its.hasNext()){
				Double key = its.next();
				int val = tdf.get(key);
				bw.write(dfw.get(num).get(key) + "\t" + key + "\t" + val + "\n");
			}
			bw.close();
			num++;
		}
	}
	
	private boolean checkPara(String para){
		if(para.contains(",") || para.contains("，"))
			return true;
		if(para.contains(".") || para.contains("。"))
			return true;
		return false;
	}
	
	private String getMainPara(String text,String title){
		String[] paras = text.split("!##!");
		List<StringBuffer> contents = new ArrayList<StringBuffer>();
		int inpanum = 0;
		StringBuffer content = new StringBuffer();
		for(String para : paras){
			if(para.length() >= 5 && !checkPara(para) && inpanum >= 4){
				contents.add(content);
				content = new StringBuffer();
			}else if(para.length() > 0){
				content.append(para);
				inpanum++;
			}
		}
		//get main content stand for title;
		double max = -1.0;
		for(StringBuffer ct : contents){
			double score = util.Similarity.ContentOverlap(ct.toString(), title);
			if(score > max){
				max = score;
				content = ct;
			}
		}
		if(max > 0.3){
			return content.toString();
		}else{
			return text;
		}
	}
	
	public void TIMethod(){
		
		loadHtmls(false);
		loadStandard();
//		singleLink(true);
//		totalCluster();
		averageCluster(true);
		showResult();
		System.out.println("\n" + "Purity - " + purityEvaluate()); 
		System.out.println("\n" + "NMI - " + nmiEvaluate());
		System.out.println("\n" + "RI - " + riEvaluate(0));
		System.out.println("\n" + "F5 - " + riEvaluate(5));
	}
	
	public static void main(String[] args) throws IOException{
		SingleLinkageModel ts = new SingleLinkageModel();
		ts.TIMethod();		
	}
}