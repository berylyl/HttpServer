package utility;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

/**
 * MD5工具类
 * 
 * @author wb-yinlu
 * 
 */
public class MD5 {
	private static Logger logger = Logger.getLogger(MD5.class);
	/**
	 * 将比特数组转换成相应的MD5字符串
	 * 
	 * @param： input-比特数组
	 * @return MD5字符串
	 */
	public static String getMd5HexString(String input) {
		MessageDigest md;
		StringBuilder ret = null;
		try {
			md = MessageDigest.getInstance("MD5");
			byte b[] = md.digest(input.getBytes());
			ret = new StringBuilder(b.length << 1);
			for (int i = 0; i < b.length; i++) {
				ret.append(Character.forDigit((b[i] >> 4) & 0xf, 16));
				ret.append(Character.forDigit(b[i] & 0xf, 16));
			}
		} catch (NoSuchAlgorithmException e) {
			logger.error("MD5.getMd5HexString NoSuchAlgorithmException", e);
		}
		return ret.toString();
	}

}
