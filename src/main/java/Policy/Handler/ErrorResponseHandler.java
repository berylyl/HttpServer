package Policy.Handler;

import utility.Constant;

/**
 * 请求处理发生错误时，返回此错误页面
 * 
 * @author yinlu
 * 
 */
public class ErrorResponseHandler {

	final static String desc = "<h1>"
			+ "Request information incorrect or the server processing response error!"
			+ "</h1>";

	/**
	 * 以比特数组返回错误信息
	 * 
	 * @return ErrResponse
	 */
	public static byte[] getResponse(String errorInfo) {

		// 构造错误信息Body
		StringBuilder errPageBody = new StringBuilder();
		errPageBody.append(desc + "\r\n");
		errPageBody.append("<h2>" + errorInfo + "</h2>");

		// 构造错误信息Header
		StringBuilder errPageHeader = new StringBuilder();
		errPageHeader.append(Constant.PROTOCOL + " 409 Conflict\r\n");
		errPageHeader.append("Content-Length: " + errPageBody.length()
				+ "\r\n\r\n");

		// 由错误页面的Header与Body组成错误页面
		String errPage = errPageHeader.toString() + errPageBody.toString();
		return errPage.getBytes();
	}
}
