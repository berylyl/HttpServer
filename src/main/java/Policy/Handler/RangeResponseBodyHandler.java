package Policy.Handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.axis.transport.http.ChunkedOutputStream;
import org.apache.log4j.Logger;

/**
 * 如果body的处理策略里有chunked相关策略，综合处理
 * 
 * @author yinlu
 * 
 */
public class RangeResponseBodyHandler {
	private static Logger logger = Logger
			.getLogger(RangeResponseBodyHandler.class);
	private static byte[] responseBody;

	/**
	 * 根据已构造的的md5body值，返回chunk类型的body
	 * 
	 * @param： md5Coontent：需要分片的内容
	 * @return 分片后的body
	 */
	public static byte[] getResponseBody(byte[] md5Content) {

		ByteArrayOutputStream content = new ByteArrayOutputStream();
		ChunkedOutputStream cout = new ChunkedOutputStream(content);
		// 多少字节分一片
		int num = 11;
		int len = md5Content.length % num;
		byte[] piece = new byte[num];
		byte[] last = new byte[len];
		try {
			// 整片的循环分片写入
			for (int i = 0; i < md5Content.length - len; i = i + num) {
				System.arraycopy(md5Content, i, piece, 0, num);
				cout.write(piece);
			}
			// 不够一整片的单独写入
			System.arraycopy(md5Content, md5Content.length - len, last, 0, len);
			cout.write(last);
			// 长度为0的chunk;close调用了eos;eos,构造0chunk的方法
			cout.close();
			// tailer

		} catch (IOException e) {
			logger.error(
					"ChunkedResponseBodyHandler.getResponseBody Exception:", e);
		}
		System.out.println(content);
		return content.toByteArray();
	}
	public static void main(String[] args) {
		System.out.println("abcdefghjiklfjiaowjefiwofjwoaijfioj".getBytes().length);
		getResponseBody("abcdefghjiklfjiaowjefiwofjwoaijfioj".getBytes());
		System.out.println("======");
		//System.out.println(getResponseBody("aaaaaaaaaaaa".getBytes()));
	    
	}
}
