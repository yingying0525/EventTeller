package util;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.hadoop.hbase.util.Bytes;


public class SimHash {

	public static int hash(String str) {
			int results = 0;
			int[] weightVector = new int[32];
			///some features 
			String[] words = str.split(",");
			for (String wd : words) {
				if(wd.length() == 0)
					continue;
				String[] wds = wd.split(" ");				
				String word = "";
				int weight = 1;
				if(wds.length == 2){
					word = wds[0];
					weight = Integer.valueOf(wds[1]);
				}
				int wd_hash = word.hashCode();
				byte[] hash = Bytes.toBytes(wd_hash);
				
				/*
				 * get bits of every byte of the hash and add them to the weight
				 * Vector
				 */
				for (int j = 0; j < hash.length; j++) {
					for (int k = 0; k < 8; k++) {
						if ((hash[j] >> (7 - k) & 0x01) == 1)
							weightVector[j * 8 + k] += weight;
						else
							weightVector[j * 8 + k] -= weight;
					}
				}
			}			
			// for bytes result
			byte[] result = new byte[4];
			/*
			 * Convert weightVector to hash number by setting every bit >0 to 1
			 * and all the others to 0
			 */
			for (int i = 0; i < result.length; i++) {
				for (int j = 0; j < 8; j++) {
					if (weightVector[i * 8 + j] > 0) {
						result[i] |= 1 << (7 - j);
					}
				}
			}
			System.out.println(result[0] + "\t" + result[1] + "\t" + result[2] + "\t" + result[3]);
			results = Bytes.toInt(result);
			return results;
	}

	
	/**
	 * @param num_a
	 * @param num_b
	 * @return different bits of two number
	 * @Description:
	 */
	public static int diffBitsOfNums(int num_a , int num_b){
		int result = 0;
		num_a ^= num_b;		
		while(num_a >0)
		{
		   num_a&=(num_a-1);
		   result++;
		}		
		return result;
	}
	
	public static String hash_2(String str) throws UnsupportedEncodingException {

		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");

			/* MD5 has 16 bytes = 128 bit */
			int[] weightVector = new int[128];

			String[] wds = str.split(",");

			for (String wd : wds) {
				if(wd.length() == 0)
					continue;
				String[] words = wd.split(" ");				
				String word = "";
				int weight = 1;
				if(words.length == 2){
					word = words[0];
					weight = Integer.valueOf(words[1]);
				}
				digest.update(word.getBytes("utf-8"));
				byte[] hash = digest.digest();

				/*
				 * get bits of every byte of the hash and add them to the weight
				 * Vector
				 */
				for (int j = 0; j < hash.length; j++) {
					for (int k = 0; k < 8; k++) {
						if ((hash[j] >> (7 - k) & 0x01) == 1)
							weightVector[j * 8 + k] += weight;
						else
							weightVector[j * 8 + k] -= weight;
					}
				}
			}

			// 128 bits = 16 bytes
			byte[] result = new byte[16];
			/*
			 * Convert weightVector to hash number by setting every bit >0 to 1
			 * and all the others to 0
			 */
			for (int i = 0; i < result.length; i++) {
				for (int j = 0; j < 8; j++) {
					if (weightVector[i * 8 + j] > 0) {
						result[i] |= 1 << (7 - j);
					}
				}
			}

			StringBuilder out = new StringBuilder(128);

			for (int i : weightVector) {
				if (i > 0) {
					out.append('1');
				} else {
					out.append('0');

				}
			}

			return out.toString();

		} catch (NoSuchAlgorithmException e) {
			return "";
		}

	}



}