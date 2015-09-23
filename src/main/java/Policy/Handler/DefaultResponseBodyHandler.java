package Policy.Handler;

/**
 * 处理默认body配置策略
 * 
 * @author yinlu
 * 
 */
public class DefaultResponseBodyHandler {

	/**
	 * 以uri构造定长的md5比特数组
	 * 
	 * @param URI
	 *            ：资源地址,
	 * @param bodySize
	 *            ：body的长度
	 * @return：md5比特数组
	 */
	public static byte[] getResponseBody(String cell, int bodySize) {
		StringBuilder content = new StringBuilder();
		while (content.length() < bodySize) {
			content.append(cell);
		}
		return content.substring(0, bodySize).getBytes();
	}
}
