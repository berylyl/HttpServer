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
 * HTTP��Ӧ�ܵ����࣬����http response
 * 
 * @author yinlu
 * 
 */
public class ResponseHandler {
	private static Logger logger = Logger.getLogger(ResponseBodyHandler.class);

	/**
	 * ���ݲ��ԣ��Ա������鷵����Ӧ����
	 * 
	 * @param request
	 *            -HttpRequest����
	 * @return ��Ӧ����
	 */
	public static byte[] getResponse(HttpRequest request) {

		// �õ���Ӧͷ����Ӧ�壬������ȷ����Ӧ���ݣ���ֵʱ���쳣�����ش���ҳ��
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

		// ��Ӧ�����ԣ��Ƿ񷵻�body������
		String rspCode, rspBodyExist;
		// ����ResponseCode�ж�Body�Ƿ���ڵı�־��flgС��0ʱ������body
		int flg = -1;

		// ��PolicyQueue���ҵ�key="rsp_body_exist"�Ĳ��ԣ��õ�valueֵ
		Policy rspBodyExistPolicy = PolicyQueue.getPolicy(PolicyType.Global,
				PolicyKeys.rsp_body_exist);
		if (rspBodyExistPolicy != null) {
			rspBodyExist = rspBodyExistPolicy.getValue();
		} else {
			rspBodyExist = "auto";
		}

		// �ж��Ƿ���Ҫ����body��Ĭ�ϲ�����body��
		boolean bodyExists = false;
		// ���������rsp_body_existֵΪtrue���򷵻�body��
		if (rspBodyExist.equalsIgnoreCase("true")) {
			bodyExists = true;
		} else if (rspBodyExist.equalsIgnoreCase("auto")) {
			// ��PolicyQueue���ҵ�key="rsp_code"�Ĳ��ԣ��õ�valueֵ
			Policy rspCodePolicy = PolicyQueue.getPolicy(PolicyType.Global,
					PolicyKeys.rsp_code);
			if (rspCodePolicy != null) {
				rspCode = rspCodePolicy.getValue();
				// �ж�contCode�Ƿ�Ϊ301,302,304,503,504,403�е�һ����
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
