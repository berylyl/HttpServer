package Policy.Handler;

import java.util.Arrays;

import org.apache.log4j.Logger;

import Http.Protocol.HttpRequest;
import Policy.Policy;
import Policy.PolicyException;
import Policy.PolicyKeys;
import Policy.PolicyQueue;
import Policy.PolicyType;

/**
 * HTTP响应总调度类，返回http response
 * 
 * @author yinlu
 * 
 */
public class ResponseHandler {
	private static Logger logger = Logger.getLogger(ResponseBodyHandler.class);

	/**
	 * 根据策略，以比特数组返回响应内容
	 * 
	 * @param request
	 *            -HttpRequest请求
	 * @return 响应内容
	 */
	public static byte[] getResponse(HttpRequest request) {

		// 得到相应头和响应体，返回正确的响应内容；空值时报异常，返回错误页面
		byte[] body;
		try {
			body = ResponseBodyHandler.getResponseBody(request);
		} catch (PolicyException pe) {
			logger.error("ResponseHandler.getResponse PolicyException:", pe);
			return ErrorResponseHandler.getResponse(pe.getMessage());
		}
		byte[] header;
		try {
			header = ResponseHeaderHandler.getResponseHeader(request, body);
		} catch (PolicyException e) {
			logger.error("ResponseHandler.getResponse PolicyException:", e);
			return ErrorResponseHandler.getResponse(e.getMessage());
		}
		byte[] response = new byte[body.length + header.length];

		// 响应码属性，是否返回body体属性
		String rspCode, rspBodyExist;
		// 根据ResponseCode判断Body是否存在的标志，flg小于0时，存在body
		int flg = -1;

		// 从PolicyQueue中找到key="rsp_body_exist"的策略，得到value值
		Policy rspBodyExistPolicy = PolicyQueue.getPolicy(PolicyType.Global,
				PolicyKeys.rsp_body_exist);
		if (rspBodyExistPolicy != null) {
			rspBodyExist = rspBodyExistPolicy.getValue();
		} else {
			rspBodyExist = "auto";
		}

		// 判断是否需要返回body，默认不返还body体
		boolean bodyExists = false;
		// 如果策略中rsp_body_exist值为true，则返回body体
		if (rspBodyExist.equalsIgnoreCase("true")) {
			bodyExists = true;
		} else if (rspBodyExist.equalsIgnoreCase("auto")) {
			// 从PolicyQueue中找到key="rsp_code"的策略，得到value值
			Policy rspCodePolicy = PolicyQueue.getPolicy(PolicyType.Global,
					PolicyKeys.rsp_code);
			if (rspCodePolicy != null) {
				rspCode = rspCodePolicy.getValue();
				// 判断contCode是否为301,302,304,503,504,403中的一个。
				String[] noBodyCode = { "301", "302", "304", "403", "503", "504" };
				flg = Arrays.binarySearch(noBodyCode, rspCode);
			} else {
				flg = -1;
			}

			if (flg < 0) {
				bodyExists = true;
			}
		}

		if (bodyExists) {
			System.arraycopy(header, 0, response, 0, header.length);
			System.arraycopy(body, 0, response, header.length, body.length);
			return response;
		} else {
			return header;
		}
	}
}
