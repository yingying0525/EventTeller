package cn.ruc.mblank.util;

public class Coding {
	
	public static byte[] long2Byte(long x) { 
		byte[] res = new byte[8];
		res[ 0] = (byte) (x >> 56); 
		res[ 1] = (byte) (x >> 48); 
		res[ 2] = (byte) (x >> 40); 
		res[ 3] = (byte) (x >> 32); 
		res[ 4] = (byte) (x >> 24); 
		res[ 5] = (byte) (x >> 16); 
		res[ 6] = (byte) (x >> 8); 
		res[ 7] = (byte) (x >> 0); 
		return res;
	} 
	
	/**
	 * @param num_a
	 * @param num_b
	 * @return different bits of two number
	 * @Description:
	 */
	public static int diffBitsOfNums(long num_a , long num_b){
		int result = 0;
		for(int i = 0 ; i < Const.SimHashBitNumber; i++){
			if((num_a & 1) != (num_b & 1)){
				result++;
			}
			num_a = num_a >> 1;
			num_b = num_b >> 1;
		}
		return result;
	}

}
