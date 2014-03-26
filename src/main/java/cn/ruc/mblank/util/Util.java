package cn.ruc.mblank.util;

//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;


public class Util {
	

	public static void ArrayCopy(double a[][] , double b[][], int N, int M){
		for(int i = 0 ;i<N;i++){
			for(int j = 0 ;j<M;j++){
				a[i][j] = b[i][j];
			}
		}
	}

	

	


}
