package Policy.Handler;

/**
 * ����Ĭ��body���ò���
 * 
 * @author yinlu
 * 
 */
public class DefaultResponseBodyHandler {

	/**
	 * ��uri���춨����md5��������
	 * 
	 * @param URI
	 *            ����Դ��ַ,
	 * @param bodySize
	 *            ��body�ĳ���
	 * @return��md5��������
	 */
	public static byte[] getResponseBody(String cell, int bodySize) {
		StringBuilder content = new StringBuilder();
		while (content.length() < bodySize) {
			content.append(cell);
		}
		return content.substring(0, bodySize).getBytes();
	}
}
