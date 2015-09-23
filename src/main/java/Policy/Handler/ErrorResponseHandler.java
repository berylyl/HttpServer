package Policy.Handler;

import utility.Constant;

/**
 * ������������ʱ�����ش˴���ҳ��
 * 
 * @author yinlu
 * 
 */
public class ErrorResponseHandler {

	final static String desc = "<h1>"
			+ "Request information incorrect or the server processing response error!"
			+ "</h1>";

	/**
	 * �Ա������鷵�ش�����Ϣ
	 * 
	 * @return ErrResponse
	 */
	public static byte[] getResponse(String errorInfo) {

		// ���������ϢBody
		StringBuilder errPageBody = new StringBuilder();
		errPageBody.append(desc + "\r\n");
		errPageBody.append("<h2>" + errorInfo + "</h2>");

		// ���������ϢHeader
		StringBuilder errPageHeader = new StringBuilder();
		errPageHeader.append(Constant.PROTOCOL + " 409 Conflict\r\n");
		errPageHeader.append("Content-Length: " + errPageBody.length()
				+ "\r\n\r\n");

		// �ɴ���ҳ���Header��Body��ɴ���ҳ��
		String errPage = errPageHeader.toString() + errPageBody.toString();
		return errPage.getBytes();
	}
}
