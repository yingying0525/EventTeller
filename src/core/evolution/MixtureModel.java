package core.evolution;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class MixtureModel {
	
	///stop condition
	public double Eps = 0.01;
	///max iterate number
	public int MaxIteration = 1000;	
	//background distribution weight
	public double Lamda = 0.95;
	//how many themes
	public int K = 5;
	//theme weight for every document (number of article) * (k theme)
	public double pi[][];
	//word distribution for themes (k theme) * (number of words)
	public double phi[][];	
	//all the words and TF of articles
	private List<Map<String,Integer>> Words = new ArrayList<Map<String,Integer>>();
	
	private Map<String,Integer> Background = new HashMap<String,Integer>();
	
	private int ArticleNum;
	
	private long TotalWordNum;
	
	
	
	
	public MixtureModel(){
		
	}
	
	
	///constructor
	public MixtureModel(int k, double lamda){
		this.K = k;
		this.Lamda = lamda;
	}
	
	///constructor
	public MixtureModel(int k, double lamda,int maxIteration,double eps){
		this.K = k;
		this.Lamda = lamda;
		this.MaxIteration = maxIteration;
		this.Eps = eps;
	}
	
	
	///initialize the words map from a file
	private void initWords(String in){
		BufferedReader br;
		int num = 0;
		try {
			br = new BufferedReader(new FileReader(in));
			String line = "";
			while((line = br.readLine()) != null){
				Map<String,Integer> articleWords = new HashMap<String,Integer>();
				String[] its = line.split("\t");
				String[] words = its[3].split(" ");
				for(String wd : words){
					if(wd.length() == 0)
						continue;
					if(Background.containsKey(wd)){
						Background.put(wd, Background.get(wd) + 1);
					}else{
						Background.put(wd, 1);
					}
					if(articleWords.containsKey(wd)){
						articleWords.put(wd, articleWords.get(wd) + 1);
					}else{
						articleWords.put(wd, 1);
					}
					TotalWordNum++;
				}
				num++;
				Words.add(articleWords);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ArticleNum = num;
	}
	
	
	//initialize some random value to arguments
	private void initPara(){
		Random rd = new Random(new Date().getTime());
		/// 0 stands for background
		pi = new double[ArticleNum][K + 1];
		phi = new double[K + 1][Words.size()];
		///init of pi
		for(int i = 0 ;i < ArticleNum;i++){
			double total_pi = 0;
			for(int j = 0 ;j<=K;j++){
				pi[i][j] = rd.nextDouble();
				total_pi+= pi[i][j];
			}
			for(int j = 0 ;j<=K;j++){
				pi[i][j] = pi[i][j] / total_pi;
			}
		}
		///init of phi
		for(int i = 0 ;i <=K;i++){
			double total_phi = 0;
			for(int j = 0 ;j<Words.size();j++){
				phi[i][j] = rd.nextDouble();
				total_phi+= phi[i][j];
			}
			for(int j = 0 ;j<Words.size();j++){
				phi[i][j] = phi[i][j] / total_phi;
			}
		}		
	}
	
	
	private void init(){
		initWords("D:\\v-quzhao\\test\\tianyi");
		initPara();
		pi = new double[ArticleNum][K];
		phi = new double[K][Words.size()];
	}
	
	
	
	private void updatePara(){
		
		
		
		
	}
	
	
	public static void main(String[] args){
		MixtureModel mm = new MixtureModel();
		mm.init();
		mm.updatePara();
		
	}

}
