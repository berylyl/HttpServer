package Policy.Handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;

/**
 * 如果body的处理策略里有gzip相关策略，将Body内容进行压缩并保证压缩后大小满足策略中对Body大小的要求
 * 
 * @author yinlu
 * 
 */
public class GzipResponseBodyHandler {
	private static Logger logger = Logger
			.getLogger(GzipResponseBodyHandler.class);

	/**
	 * 根据请求URI的MD5值，生成符合策略指定大小的GZIP压缩body，以字节数组的形式返回
	 * 
	 * @param uri
	 *            ：请求的URI
	 * @param bodySize
	 *            ：压缩后的Body大小
	 * @return：
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

		// Body内容压缩完后的数组
		byte[] contentByteArray = contentByteStream.toByteArray();
		// 返回固定大小的压缩后的数据
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