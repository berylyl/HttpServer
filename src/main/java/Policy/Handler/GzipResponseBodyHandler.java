package Policy.Handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;

/**
 * ���body�Ĵ����������gzip��ز��ԣ���Body���ݽ���ѹ������֤ѹ�����С��������ж�Body��С��Ҫ��
 * 
 * @author yinlu
 * 
 */
public class GzipResponseBodyHandler {
	private static Logger logger = Logger
			.getLogger(GzipResponseBodyHandler.class);

	/**
	 * ��������URI��MD5ֵ�����ɷ��ϲ���ָ����С��GZIPѹ��body�����ֽ��������ʽ����
	 * 
	 * @param uri
	 *            �������URI
	 * @param bodySize
	 *            ��ѹ�����Body��С
	 * @return��
	 */
	public static byte[] getResponseBody(String cell, int bodySize) {

		ByteArrayOutputStream contentByteStream = null;
		try {

			contentByteStream = new ByteArrayOutputStream();
			GZIPOutputStream GZIPStream = new GZIPOutputStream(
					contentByteStream);

			byte[] buf = cell.getBytes();
			GZIPStream.write(buf, 0, buf.length);

			GZIPStream.finish();
			GZIPStream.close();

		} catch (IOException e) {
			logger.error(
					"GzipResponseBodyHandler.getResponseBody IOException: ", e);
		}

		// Body����ѹ����������
		byte[] contentByteArray = contentByteStream.toByteArray();
		// ���ع̶���С��ѹ���������
		byte[] bodyArray = new byte[bodySize];
		int num = bodySize / contentByteArray.length;
		for (int i = 0; i < num; i++) {
			System.arraycopy(contentByteArray, 0, bodyArray, i
					* contentByteArray.length, contentByteArray.length);
		}
		System.arraycopy(contentByteArray, 0, bodyArray,
				contentByteArray.length * num, bodySize
						% contentByteArray.length);
		return bodyArray;
	}
}