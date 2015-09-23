package Policy.Handler;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import utility.BodySize;
import utility.MD5;
import Http.Protocol.HttpRequest;
import Policy.Policy;
import Policy.PolicyException;
import Policy.PolicyKeys;
import Policy.PolicyQueue;
import Policy.PolicyType;

/**
 * ���ݾ�����ԣ�������Ӧ��ResponseBody
 * 
 * @author��yinlu
 */
public class ResponseBodyHandler {

	private static Logger logger = Logger.getLogger(ResponseBodyHandler.class);

	/**
	 * ���������request���󣬲��Ҳ��Զ��У�������Ҫ��body�����ַ����������ʽ����
	 * 
	 * @param request
	 *            ��HttpRequest�������
	 * @return body�ֽ���
	 * @throws PolicyException
	 */
	public static byte[] getResponseBody(HttpRequest request)
			throws PolicyException {

		byte[] bodyContent = null;
		int bodySize; // Body�Ĵ�С

		// ��ȡBody��䵥Ԫ
		Policy rspContentSourcePolicy = PolicyQueue.getPolicy(
				PolicyType.Global, PolicyKeys.rsp_content_source);
		if (rspContentSourcePolicy != null) {
			if (rspContentSourcePolicy.getValue() != null
					&& !rspContentSourcePolicy.getValue().equals("")) {

				/*
				 * if (rspContentSourcePolicy.getValue().toLowerCase()
				 * .contains("http://")) { bodyCell =
				 * getResponseBodyFromURL(rspContentSourcePolicy .getValue()); }
				 * else {
				 */
				return getResponseBodyFromFile(rspContentSourcePolicy
						.getValue());
				// }
			} else {
				logger.error("Invalid arguments: The key to rsp_content_source policy value is null.");
				throw new PolicyException(
						"Invalid arguments: The key to rsp_content_source policy value is null.");
			}
		} else {
			String bodyCell = getResponseBodyFromRequestURI(request);

			// �Ӳ����еõ�body_size
			Policy rspBodySizePolicy = PolicyQueue.getPolicy(PolicyType.Global,
					PolicyKeys.rsp_body_size);
			// ����bodysize�Ĳ���ֵ�������ʵ�ʷ���ֵ
			if (rspBodySizePolicy != null) {
				bodySize = BodySize.getBodySize(rspBodySizePolicy);
			} else {
				logger.error("Not found Policy: rsp_body_size");
				throw new PolicyException("Not found Policy: rsp_body_size");
			}

			// �Ӳ����еõ�content_code
			Policy rspContentCodePolicy = PolicyQueue.getPolicy(PolicyType.Global,
					PolicyKeys.rsp_content_code);
			// �������Ϊ�ա�����Ĭ��md5��ʽ������Ӧ
			if (rspContentCodePolicy != null) {
				// ������Բ�Ϊ�գ����ղ��Է���default��gzip��chunked���͵�body��
				String rspContentCode = rspContentCodePolicy.getValue()
						.toLowerCase();
				if (rspContentCode.equalsIgnoreCase("gzip")) {
					bodyContent = GzipResponseBodyHandler.getResponseBody(bodyCell,
							bodySize);
				} else if (rspContentCode.equalsIgnoreCase("chunked")) {
					byte[] body = DefaultResponseBodyHandler.getResponseBody(
							bodyCell, bodySize);
					bodyContent = ChunkedResponseBodyHandler.getResponseBody(body);
				} else if (rspContentCode.contains("gzip")
						&& rspContentCode.contains("chunked")) {
					byte[] gzipBody = GzipResponseBodyHandler.getResponseBody(
							bodyCell, bodySize);
					bodyContent = ChunkedResponseBodyHandler
							.getResponseBody(gzipBody);
				} else {
					bodyContent = DefaultResponseBodyHandler.getResponseBody(
							bodyCell, bodySize);
				}
			} else {
				bodyContent = DefaultResponseBodyHandler.getResponseBody(bodyCell,
						bodySize);
			}

		}

		return bodyContent;
	}

	/**
	 * ���������request���󣬲��Ҳ��Զ��У�������Ҫ��body�����ַ��������ʽ����
	 * 
	 * @param request
	 *            ��HttpRequest�������
	 * @return ���ResponseBody�ĵ�Ԫ����
	 */
	private static String getResponseBodyFromRequestURI(HttpRequest request) {
		String bodyCell = "";
		// �ӿͻ��˵������еõ���uri
		String requestURI = request.getHttpHeader().getURI();
		if (requestURI != null) {
			// ��ȷȡ��uri��ַ����ȡ�У���#����֮ǰ���ַ���;��Ϊ�գ���ʹ��Ĭ��ֵ""
			String URI = requestURI.split("[?#]")[0];
			bodyCell = MD5.getMd5HexString(URI);
		}
		return bodyCell;
	}

	/**
	 * �Ӳ������õ��ļ��ж�ȡ�ļ�����
	 * 
	 * @param request
	 *            ��HttpRequest�������
	 * @return ���ResponseBody�ĵ�Ԫ����
	 * @throws PolicyException
	 */
	private static byte[] getResponseBodyFromFile(String filePath)
			throws PolicyException {
		byte[] bodyCell;
		try {
			FileInputStream fis = new FileInputStream(filePath);
			bodyCell = new byte[fis.available()];
			fis.read(bodyCell);
			fis.close();
		} catch (FileNotFoundException e) {
			logger.error("Response body source file Not Found: ", e);
			throw new PolicyException("Response body source file Not Found.");
		} catch (IOException e) {
			logger.error("Response body source file IOException: ", e);
			throw new PolicyException("Response body source file IOException: "
					+ e.getMessage());
		}

		return bodyCell;
	}

	/**
	 * 
	 * @param Address
	 *            ��ResponseBody����ԴURL
	 * @return ���ResponseBody�ĵ�Ԫ����
	 * @throws PolicyException
	 */
	@SuppressWarnings("unused")
	private static String getResponseBodyFromURL(String Address)
			throws PolicyException {
		StringBuffer bodyCell = new StringBuffer();
		try {
			byte[] contentByte = new byte[1024];
			URL url = new URL(Address);
			DataInputStream br = new DataInputStream(url.openStream());
			int readNum = br.read(contentByte);
			while (readNum > 0) {
				bodyCell.append(new String(contentByte, 0, readNum));
				readNum = br.read(contentByte);
			}

			br.close();
		} catch (MalformedURLException e) {
			logger.error("Response body source URL Malformed: ", e);
			throw new PolicyException("Response body source URL Malformed.");
		} catch (IOException e) {
			logger.error("Response body source URL IOException: ", e);
			throw new PolicyException("Response body source URL IOException: "
					+ e.getMessage());
		}
		return bodyCell.toString();
	}

}
