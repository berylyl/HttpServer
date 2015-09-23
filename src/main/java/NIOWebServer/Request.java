package NIOWebServer;

import org.apache.log4j.Logger;

import utility.Constant;

import Administration.NIOControlServer;
import Http.Protocol.HttpHeader;
import Http.Protocol.HttpMethod;
import Http.Protocol.HttpProtocol;
import Http.Protocol.HttpRequest;
import Http.Protocol.HttpMethod.RequestType;
import Http.Protocol.HttpProtocol.HttpVersion;

/**
 * 对客户端请求的综合处理
 * 
 * @author yinlu
 * @version 1.0
 */

public class Request {
	private static Logger logger = Logger.getLogger(Request.class);

	public static HttpRequest requestLoader(String req) {

		logger.debug("Enter the Request.requestLoader method.");

		HttpHeader header = new HttpHeader();
		HttpRequest httpRequest = new HttpRequest();
		if (req == null || req.equals("")) {
			logger.error("Invalid arguments:" + req);
		} else {
			// 按行获得请求头
			String[] headerItems = req.split("\r\n");

			if (headerItems.length > 1) {
				// 解析请求类型(get)，请求地址(/index.jsp),协议版本(HTTP/1.1)
				String[] MUP = headerItems[0].split(" ");
				if (MUP.length == 3) {
					// 添加method对象
					RequestType[] types = HttpMethod.RequestType.values();
					for (RequestType type : types) {
						if (MUP[0].equalsIgnoreCase(type.toString())) {
							HttpMethod method = HttpMethod.valueof(type
									.toString());
							header.setMethod(method);
							break;
						}
					}

					// 添加url对象
					if (MUP[1].contains("/")) {
						header.setURI(MUP[1]);
					}

					// 添加protocol对象
					HttpVersion[] versions = HttpProtocol.HttpVersion.values();
					for (HttpVersion version : versions) {
						if (MUP[2].equalsIgnoreCase(version.getValue())) {
							HttpProtocol protocol = HttpProtocol
									.valueof(version.getValue());
							header.setProtocol(protocol);
							break;
						}
					}

					// 从第二行开始，添加：分隔的键值对
					for (int i = 1; i < headerItems.length; i++) {
						if (headerItems[i].contains(": ")) {
							int sidx = headerItems[i].indexOf(": ");
							int eidx = headerItems[i].lastIndexOf(": ");
							if (sidx > 0 & eidx < headerItems[i].length() - 2) {
								String key = headerItems[i].substring(0, sidx);
								String value = headerItems[i]
										.substring(sidx + 2);
								//处理请求头中带有的策略信息
								if (key.toLowerCase().contains(Constant.setPolicy)) {
									NIOControlServer.serverHandle(value);
								} else {
									header.addHeader(key, value);
								}
							}
						}
					}

					// 将header添加到HttpRuquest里
					httpRequest.setHttpHeader(header);
				} else {
					logger.error("Invalid arguments:" + req);
				}

			} else {
				logger.error("Invalid arguments:" + req);
			}
		}
		return httpRequest;
	}
}
