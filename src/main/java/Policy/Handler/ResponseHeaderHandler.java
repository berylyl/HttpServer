package Policy.Handler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import utility.Constant;
import utility.DateFormat;
import utility.MD5;
import Http.Protocol.HttpRequest;
import Policy.Policy;
import Policy.PolicyException;
import Policy.PolicyKeys;
import Policy.PolicyQueue;
import Policy.PolicyType;

/**
 * 找到header相关所有策略，综合处理
 * 
 * @author yinlu
 * 
 */
public class ResponseHeaderHandler {
	private static Logger logger = Logger
			.getLogger(ResponseHeaderHandler.class);

	/**
	 * 根据所有header相关策略得到响应头
	 * 
	 * @param request
	 *            ：请求
	 * @param body
	 *            ：body字节数组
	 * @return header字节数组
	 * @throws PolicyException
	 *             策略参数异常
	 */
	public static byte[] getResponseHeader(HttpRequest request, byte[] body)
			throws PolicyException {
		byte[] header = null;
		Policy rspPolicy = PolicyQueue.getPolicy(PolicyType.Global,
				PolicyKeys.rsp_header_source);
		if (rspPolicy != null) {
			if (rspPolicy.getValue() != null
					&& !rspPolicy.getValue().equalsIgnoreCase("")) {
				header = getResponseHeaderFromFile(rspPolicy.getValue());
			} else {
				logger.error("Invalid arguments: The key to rsp_header_source policy value is null.");
				throw new PolicyException(
						"Invalid arguments: The key to rsp_header_source policy value is null.");
			}
		} else {
			header = getResponseHeaderFromPolicy(request, body);
		}

		return header;
	}

	/**
	 * 从文件中读取响应头内容的方法
	 * 
	 * @param filePath
	 *            ：要读取的文件路径
	 * @throws PolicyException
	 * @return：Header字节数组
	 */
	private static byte[] getResponseHeaderFromFile(String filePath)
			throws PolicyException {
		StringBuilder headerStr = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String resHeader = br.readLine();
			while (resHeader != null && !resHeader.trim().equals("")) {
				headerStr.append(resHeader + "\r\n");
				resHeader = br.readLine();
			}
			headerStr.append("\r\n");
			br.close();
		} catch (FileNotFoundException e) {
			logger.error("Response header source file Not Found: ", e);
			throw new PolicyException("Response header source file Not Found.");
		} catch (IOException e) {
			logger.error("Response header source file IOException: ", e);
			throw new PolicyException(
					"Response header source file IOException: "
							+ e.getMessage());
		}
		return headerStr.toString().getBytes();
	}

	/**
	 * 根据header相关所有策略拼出符合策略的完整header
	 * 
	 * @param request
	 *            ：请求
	 * @param body
	 *            ：body字节数组
	 * @return header字节数组
	 * @throws PolicyException
	 *             策略参数异常
	 */
	private static byte[] getResponseHeaderFromPolicy(HttpRequest request,
			byte[] body) throws PolicyException {
		StringBuilder headerStr = new StringBuilder();

		headerStr.append(Constant.PROTOCOL + " ");

		// 从PolicyQueue中找到key="rsp_code"的策略
		Policy rspPolicy = PolicyQueue.getPolicy(PolicyType.Global,
				PolicyKeys.rsp_code);
		headerStr.append(getHttpCode(request, rspPolicy));

		// 从PolicyQueue中找到key="content_length"的策略
		rspPolicy = PolicyQueue.getPolicy(PolicyType.Global,
				PolicyKeys.content_length);
		Policy contentCodePolicy = PolicyQueue.getPolicy(PolicyType.Global,
				PolicyKeys.rsp_content_code);
		if ((rspPolicy != null && contentCodePolicy == null)
				|| (rspPolicy != null && contentCodePolicy != null && !contentCodePolicy
						.getValue().toLowerCase().contains("chunked"))) {
			if (rspPolicy.getValue().equalsIgnoreCase("auto")) {
				if (body != null) {
					int bodySize = body.length;
					headerStr.append(rspPolicy.getKey() + ": " + bodySize
							+ "\r\n");
				} else {
					headerStr.append(rspPolicy.getKey() + ": " + 0 + "\r\n");
				}
			} else if (rspPolicy.getValue().equalsIgnoreCase("no")) {

			} else {
				headerStr.append(rspPolicy.getKey() + ": "
						+ rspPolicy.getValue() + "\r\n");
			}
		}

		// 从PolicyQueue中找到key="md5"的策略
		rspPolicy = PolicyQueue.getPolicy(PolicyType.Global, PolicyKeys.md5);
		if (rspPolicy != null) {
			String MD5Value = "";
			if (rspPolicy.getValue().equalsIgnoreCase("auto")) {
				MD5Value = MD5.getMd5HexString(new String(body));
				headerStr.append(rspPolicy.getKey().toUpperCase() + ": "
						+ MD5Value + "\r\n");
			} else if (rspPolicy.getValue().equalsIgnoreCase("no")) {

			} else {
				headerStr.append(rspPolicy.getKey().toUpperCase() + ": "
						+ rspPolicy.getValue() + "\r\n");
			}
		}

		// 从PolicyQueue中找到key="add_rsp_header_key"的策略
		List<Policy> rspPolicys = PolicyQueue.getAll();
		String headerKey = "";
		for (Policy p : rspPolicys) {
			if (p.getType().equals(PolicyType.Global)
					&& p.getKey().toUpperCase()
							.contains(PolicyKeys.add_rsp_header)) {
				if (p.getKey().contains("[")) {
					int endIndex = p.getKey().indexOf("[");
					headerKey = p.getKey().substring(15, endIndex);
				} else {
					headerKey = p.getKey().substring(15);
				}
				headerStr.append(headerKey + ": " + p.getValue() + "\r\n");
			}
		}

		// 从PolicyQueue中找到key="Date"的策略
		rspPolicy = PolicyQueue.getPolicy(PolicyType.Global, PolicyKeys.Date);
		if (rspPolicy != null) {
			if (rspPolicy.getValue().equalsIgnoreCase("now")) {
				headerStr.append(rspPolicy.getKey() + ": "
						+ DateFormat.toGMTString(new Date()) + "\r\n");
			} else {
				headerStr.append(rspPolicy.getKey() + ": "
						+ rspPolicy.getValue() + "\r\n");
			}
		}

		// 从PolicyQueue中找到key="Last_Modified"的策略
		rspPolicy = PolicyQueue.getPolicy(PolicyType.Global,
				PolicyKeys.Last_Modified);
		if (rspPolicy != null) {
			if (rspPolicy.getValue().equalsIgnoreCase("now")) {
				headerStr.append(rspPolicy.getKey() + ": "
						+ DateFormat.toGMTString(new Date()) + "\r\n");
			} else {
				headerStr.append(rspPolicy.getKey() + ": "
						+ rspPolicy.getValue() + "\r\n");
			}
		}

		// 从PolicyQueue中找到key="cache_control"的策略
		rspPolicy = PolicyQueue.getPolicy(PolicyType.Global,
				PolicyKeys.cache_control);
		if (rspPolicy != null) {
			headerStr.append(rspPolicy.getKey() + ": " + rspPolicy.getValue()
					+ "\r\n");
		}

		// 从PolicyQueue中找到key="rsp_content_code"的策略
		rspPolicy = PolicyQueue.getPolicy(PolicyType.Global,
				PolicyKeys.rsp_content_code);
		if (rspPolicy != null) {
			if (rspPolicy.getValue().equalsIgnoreCase("gzip")) {
				headerStr.append("Content-Encoding: " + rspPolicy.getValue()
						+ "\r\n");
			} else if (rspPolicy.getValue().equalsIgnoreCase("chunked")) {
				headerStr.append("Transfer-Encoding: " + rspPolicy.getValue()
						+ "\r\n");
			} else if (rspPolicy.getValue().toLowerCase().contains("gzip")
					&& rspPolicy.getValue().toLowerCase().contains("chunked")) {
				headerStr
						.append("Content-Encoding: gzip\r\nTransfer-Encoding: chunked\r\n");
			}
		}

		// 从PolicyQueue中找到key="Expires"的策略
		rspPolicy = PolicyQueue
				.getPolicy(PolicyType.Global, PolicyKeys.Expires);
		if (rspPolicy != null) {
			try {
				long timeDif = Long.parseLong(rspPolicy.getValue());
				Date date = new Date();
				long longDate = date.getTime() + timeDif;
				headerStr.append(rspPolicy.getKey() + ": "
						+ DateFormat.toGMTString(new Date(longDate)) + "\r\n");
			} catch (NumberFormatException e) {
				headerStr.append(rspPolicy.getKey() + ": "
						+ rspPolicy.getValue() + "\r\n");
			}
		}

		headerStr.append("\r\n");
		return headerStr.toString().getBytes();
	}

	/**
	 * 拼出HTTP响应的code与Describe
	 * 
	 * @param request
	 *            ：请求
	 * @param rspPolicy
	 *            ：与code相关的策略
	 * @return ：code与Describe拼成的字符串
	 * @throws PolicyException
	 *             策略参数异常
	 */
	private static String getHttpCode(HttpRequest request, Policy rspPolicy)
			throws PolicyException {
		String codeDescribe = "";
		if (rspPolicy != null) {
			if (rspPolicy.getValue().equals("200")) {
				// 从ResponseHeaderHandler获取header的字节数组，如果请求是range请求，依然需要返回206
				Hashtable<String, String> reqHeader = request.getHttpHeader()
						.getHeaders();
				Iterator<String> it = reqHeader.keySet().iterator();
				boolean fag = true;
				while (it.hasNext()) {
					String key = it.next();
					if (key.equalsIgnoreCase("Range")) {
						codeDescribe = "206 Partial Content\r\n";
						fag = false;
						break;
					}
				}

				if (fag) {
					codeDescribe = "200 OK\r\n";
				}
			} else if (rspPolicy.getValue().equals("206")) {
				codeDescribe = "206 Partial Content\r\n";
			} else if (rspPolicy.getValue().equals("301")) {
				codeDescribe = "301 Moved Permanently\r\n";
			} else if (rspPolicy.getValue().equals("302")) {
				codeDescribe = "302 Moved Temporarily\r\n";
			} else if (rspPolicy.getValue().equals("304")) {
				codeDescribe = "304 Not Modified\r\n";
			} else if (rspPolicy.getValue().equals("403")) {
				codeDescribe = "403 permission denied\r\n";
			} else if (rspPolicy.getValue().equals("404")) {
				codeDescribe = "404 Not Found\r\n";
			} else if (rspPolicy.getValue().equals("503")) {
				codeDescribe = "503 Service Unavailable\r\n";
			} else if (rspPolicy.getValue().equals("504")) {
				codeDescribe = "504 Gateway Timeout\r\n";
			} else {
				logger.error("Invalid arguments: " + rspPolicy.getKey() + " "
						+ rspPolicy.getValue());
				throw new PolicyException("Invalid arguments: "
						+ rspPolicy.getKey() + " " + rspPolicy.getValue());
			}
		} else {
			logger.error("Not found Policy: rsp_code");
			throw new PolicyException("Not found Policy: rsp_code");
		}

		return codeDescribe;
	}

}
