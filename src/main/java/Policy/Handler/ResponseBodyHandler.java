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
 * 根据具体策略，返回相应的ResponseBody
 * 
 * @author：yinlu
 */
public class ResponseBodyHandler {

	private static Logger logger = Logger.getLogger(ResponseBodyHandler.class);

	/**
	 * 根据请求的request对象，查找策略队列，生成需要的body，以字符串数组的形式返回
	 * 
	 * @param request
	 *            ：HttpRequest请求对象
	 * @return body字节流
	 * @throws PolicyException
	 */
	public static byte[] getResponseBody(HttpRequest request)
			throws PolicyException {

		byte[] bodyContent = null;
		int bodySize; // Body的大小

		// 获取Body填充单元
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

			// 从策略中得到body_size
			Policy rspBodySizePolicy = PolicyQueue.getPolicy(PolicyType.Global,
					PolicyKeys.rsp_body_size);
			// 根据bodysize的策略值，计算出实际返回值
			if (rspBodySizePolicy != null) {
				bodySize = BodySize.getBodySize(rspBodySizePolicy);
			} else {
				logger.error("Not found Policy: rsp_body_size");
				throw new PolicyException("Not found Policy: rsp_body_size");
			}

			// 从策略中得到content_code
			Policy rspContentCodePolicy = PolicyQueue.getPolicy(PolicyType.Global,
					PolicyKeys.rsp_content_code);
			// 如果策略为空。则以默认md5方式返回响应
			if (rspContentCodePolicy != null) {
				// 如果策略不为空，则按照策略返回default，gzip，chunked类型的body体
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
	 * 根据请求的request对象，查找策略队列，生成需要的body，以字符数组的形式返回
	 * 
	 * @param request
	 *            ：HttpRequest请求对象
	 * @return 填充ResponseBody的单元内容
	 */
	private static String getResponseBodyFromRequestURI(HttpRequest request) {
		String bodyCell = "";
		// 从客户端的请求中得到的uri
		String requestURI = request.getHttpHeader().getURI();
		if (requestURI != null) {
			// 正确取到uri地址，截取有？或#出现之前的字符串;如为空，就使用默认值""
			String URI = requestURI.split("[?#]")[0];
			bodyCell = MD5.getMd5HexString(URI);
		}
		return bodyCell;
	}

	/**
	 * 从策略配置的文件中读取文件内容
	 * 
	 * @param request
	 *            ：HttpRequest请求对象
	 * @return 填充ResponseBody的单元内容
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
	 *            ：ResponseBody的来源URL
	 * @return 填充ResponseBody的单元内容
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
