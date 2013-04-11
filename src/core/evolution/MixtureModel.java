package core.evolution;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class MixtureModel {
	
	///stop condition
	public double Eps = 0.01;
	///for smoothing
	private double Smooth = 1E-20;
	///max iterate number
	public int MaxIteration = 200;	
	//background distribution weight
	public double Lamda = 0.95;
	//how many themes
	public int K = 5;
	//theme weight for every document (number of article) * (k theme)
	public double pi[][];
	//for t-1 pi (used to estimate)
	public double tphi[][];
	//word distribution for themes (number of words) * (k theme) 
	public double phi[][];	
	//all the words and TF of articles
	private List<Map<String,Integer>> Words = new ArrayList<Map<String,Integer>>();
	
	private Map<String,Integer> Background = new LinkedHashMap<String,Integer>();
	
	private List<String> WordsIdMap = new ArrayList<String>();
	
	private Map<String,Integer> IdWordsMap = new HashMap<String,Integer>();
	
	private long TotalWordNum;
	
	//tmp
	private List<String> contents = new ArrayList<String>();
	private List<String> titles = new ArrayList<String>();
	
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
		try {
			br = new BufferedReader(new FileReader(in));
			String line = "";
			while((line = br.readLine()) != null){
				Map<String,Integer> articleWords = new HashMap<String,Integer>();
				String[] its = line.split("\t");
				String[] words = its[3].split(" ");
				titles.add(its[2]);
				contents.add(its[3]);
				for(String wd : words){
					if(wd.length() == 0)
						continue;
					if(Background.containsKey(wd)){
						Background.put(wd, Background.get(wd) + 1);
					}else{
						Background.put(wd, 1);
						IdWordsMap.put(wd, WordsIdMap.size());
						WordsIdMap.add(wd);
					}
					if(articleWords.containsKey(wd)){
						articleWords.put(wd, articleWords.get(wd) + 1);
					}else{
						articleWords.put(wd, 1);
					}
					TotalWordNum++;
				}
				Words.add(articleWords);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//initialize some random values for arguments
	private void initPara(){
		Random rd = new Random(new Date().getTime());
		/// 0 stands for background
		pi = new double[Words.size()][K + 1];
		tphi = new double[Background.size() + 1][K + 1];
		phi = new double[Background.size() + 1][K + 1];
		///init of pi
		for(int i = 0 ;i < Words.size();i++){
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
		for(int i = 0 ;i < Background.size();i++){
			double total_phi = 0;
			for(int j = 0 ;j<= K ;j++){
				if(j == 0){
					phi[i][j] = Background.get(WordsIdMap.get(i)) / (double)TotalWordNum;
				}else{
					phi[i][j] = rd.nextDouble();
				}
				total_phi+= phi[i][j];
			}
			for(int j = 0 ;j<= K;j++){
				phi[i][j] = phi[i][j] / total_phi;
			}
		}		
	}
	
	private void init(){
		initWords("D:\\ETT\\tianyi");
		initPara();
	}
	
	/**
	 * update phi and pi
	 */
	private void updatePara(){
		
		////update the p(z_dw = j) and p(z_dw = B)
		List<Map<String,List<Double>>> p_dw_t = new ArrayList<Map<String,List<Double>>>();
		for(int i = 0 ;i < Words.size(); i++){
			Map<String,List<Double>> dw_t = new HashMap<String,List<Double>>();
			Map<String,Integer> wds = Words.get(i);
			Iterator<String> it_wds = wds.keySet().iterator();
			while(it_wds.hasNext()){
				String wd = it_wds.next();
				List<Double> ts = new ArrayList<Double>();
				double t_t = 0.0;
				double smooth = 0.0;
				double[] tmp_ts =new double[K+1]; 
				for(int t = 0 ; t<= K ; t++){
					//for background 
					if(t == 0){ 
						double up = Lamda * Background.get(wd) / TotalWordNum;
						double tmp_down = 0.0;
						for(int tt = 1 ; tt<= K ;tt ++){
							tmp_down += pi[i][tt] * phi[IdWordsMap.get(wd)][tt];
						}
						double down = up + (1-Lamda) * tmp_down;
						ts.add(up / down);
					}else{
						double up = pi[i][t] * phi[IdWordsMap.get(wd)][t];
						if(up == 0){
							up = Smooth;
							smooth= Smooth * K;
						}else{
							t_t += up;
						}
						tmp_ts[t] = up;
					}
				}
				for(int t = 1 ;t<= K ;t++){
					ts.add(tmp_ts[t]/ (t_t + smooth));
				}
				dw_t.put(wd, ts);
			}
			p_dw_t.add(dw_t);
		}
		
		///update pi
		for(int i = 0 ; i< Words.size() ; i++){
			Map<String,Integer> wds = Words.get(i);
			double[] tmp_pis = new double[K + 1];
			double total_pis = 0.0;
			for(int t = 1 ; t <= K ;t ++){
				double t_j = 0.0;
				Iterator<String> it_wds = wds.keySet().iterator();
				while(it_wds.hasNext()){
					String word = it_wds.next();
					int count = wds.get(word);
					t_j += count * p_dw_t.get(i).get(word).get(t);
				}
				tmp_pis[t] = t_j;
				total_pis += t_j;
			}
			for(int t = 1 ;t <= K ;t++){
				pi[i][t] = tmp_pis[t] / total_pis;
			}
		}
		
		///update phi
		double[] tmp_pws = new double[K + 1];
		for(int i = 1 ;i<= K ;i++){
			for(int a = 0 ; a < Words.size(); a++){
				Map<String,Integer> wds = Words.get(a);
				Iterator<String> it_wds = wds.keySet().iterator();
				while(it_wds.hasNext()){
					String word = it_wds.next();
					tmp_pws[i] += wds.get(word) * 
							(1 - p_dw_t.get(a).get(word).get(0)) * 
							p_dw_t.get(a).get(word).get(i);
				}
			}
		}
		for(int i = 0 ; i< WordsIdMap.size(); i++){
			String word = WordsIdMap.get(i);
			for(int t = 1 ; t<= K ;t++){
				double tmp_s = 0.0;
				for(int j = 0 ; j< Words.size();j++){
					if(Words.get(j).containsKey(word)){
						tmp_s += Words.get(j).get(word) * (1- p_dw_t.get(j).get(word).get(0)) *p_dw_t.get(j).get(word).get(t);
					}
				}
				phi[i][t] = tmp_s / tmp_pws[t];
			}
		}		
	}
	
	/**
	 * tpa stands for t-1 iteration phi and tpb stands for t iteration phi 
	 * @param tpa
	 * @param tpb
	 * @return
	 */
	private double estimator(double tpa[][] , double tpb[][]){
		double result = 0.0;
		for(int i = 0 ; i<Background.size();i++){
			for(int j = 1; j<= K ;j++){
				result += Math.abs(tpa[i][j] - tpb[i][j]);
			}
		}
		return result;
	}
	
	/**
	 * get top n words from the K distributions
	 * @param N
	 */
	private void printTopWords(int N){
		//sort
		double[][] keys = new double[K + 1][ N ];
		int[][] vals = new int[K+1][N];
		for(int t = 0 ; t<= K;t++){
			for(int j = 0 ; j < N ;j++){
				double max = -1;
				int index = 0;
				for(int i = 0 ; i< WordsIdMap.size(); i++){
					if(tphi[i][t] > max && j != 0 &&	tphi[i][t] < keys[t][j - 1]){
						max = tphi[i][t];
						index = i;
					}else if(j == 0){
						if(tphi[i][t] > max){
							max = tphi[i][t];
							index = i;
						}
					}
				}
				keys[t][j] = max;
				vals[t][j] = index;
			}
		}
		for(int t = 0 ; t<= K ;t++){
			for(int i = 0 ; i< N ;i ++){
				System.out.print(WordsIdMap.get(vals[t][i]) + "\t" + keys[t][i]+"\t");
			}
			System.out.println();
		}
	}
	
	/**
	 * get k theme distribution (phi[][])
	 */
	private void getKThemDist(){
		double diff = Double.MAX_VALUE;
		init();
		System.out.println("init ok ... ");
		for(int i = 0 ; i< 2000 && diff >= Eps;i++){
			util.Util.ArrayCopy(tphi, phi, Background.size(), K + 1);
			updatePara();	
			diff = estimator(tphi,phi);
			System.out.println("iteration... " + i + "\t" + diff);
		}
		printTopWords(20);
	}
	
	/**
	 * get articles' theme from pi
	 * @return
	 */
	private int[] getArticleTheme(){
		int result[] = new int[Words.size()];
		for(int i = 0 ;i<Words.size();i++){
			double max = -1;
			for(int j = 1 ; j <= K ;j++){
				if(pi[i][j] > max){
					max = pi[i][j];
					result[i] = j;
				}
			}
		}	
		return result;
	}
	
	
	public static void main(String[] args){
	
		MixtureModel mm = new MixtureModel();
		mm.getKThemDist();
		int athemes[] = mm.getArticleTheme();
		for(int i = 0 ;i < mm.Words.size();i++){
			if(athemes[i] > 0){
				System.out.println(athemes[i] + "\t" + mm.titles.get(i));
			}
		}
	}

}
