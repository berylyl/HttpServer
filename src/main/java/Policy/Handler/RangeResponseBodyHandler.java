package Policy.Handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.axis.transport.http.ChunkedOutputStream;
import org.apache.log4j.Logger;

/**
 * ���body�Ĵ����������chunked��ز��ԣ��ۺϴ���
 * 
 * @author yinlu
 * 
 */
public class RangeResponseBodyHandler {
	private static Logger logger = Logger
			.getLogger(RangeResponseBodyHandler.class);
	private static byte[] responseBody;

	/**
	 * �����ѹ���ĵ�md5bodyֵ������chunk���͵�body
	 * 
	 * @param�� md5Coontent����Ҫ��Ƭ������
	 * @return ��Ƭ���body
	 */
	public static byte[] getResponseBody(byte[] md5Content) {

		ByteArrayOutputStream content = new ByteArrayOutputStream();
		ChunkedOutputStream cout = new ChunkedOutputStream(content);
		// �����ֽڷ�һƬ
		int num = 11;
		int len = md5Content.length % num;
		byte[] piece = new byte[num];
		byte[] last = new byte[len];
		try {
			// ��Ƭ��ѭ����Ƭд��
			for (int i = 0; i < md5Content.length - len; i = i + num) {
				System.arraycopy(md5Content, i, piece, 0, num);
				cout.write(piece);
			}
			// ����һ��Ƭ�ĵ���д��
			System.arraycopy(md5Content, md5Content.length - len, last, 0, len);
			cout.write(last);
			// ����Ϊ0��chunk;close������eos;eos,����0chunk�ķ���
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
