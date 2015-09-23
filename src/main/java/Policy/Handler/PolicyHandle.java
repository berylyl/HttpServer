package Policy.Handler;

import org.apache.log4j.Logger;

import utility.Constant;

import BIOWebServer.ConnectionHandler;
import Policy.*;

/**
 * 策略指令字符串操作类，进行策略字符串转化成策略对象并添加到策略队列等操作
 * 
 * @author yinlu
 * 
 */
public class PolicyHandle {

	/**
	 * 根据http request中的string指令，转化成policy进行处理，如添加或删除策略; policyStr格式为："1 max 100"
	 * 
	 * @param value
	 */
	private static Logger logger = Logger.getLogger(PolicyHandle.class);

	public String handlePolicy(String policyStr) {
		Policy p = null;
		String result = null;

		logger.debug("Enter the HttpChannel.handlePolicy method.");

		if (policyStr == null || policyStr.equals("")) {
			result = "2 Invalid arguments:" + policyStr;
			logger.error(result);
		} else {
			// 分割PolicyStr为多个Policy行
			String[] policyRows = policyStr.split("\r\n");

			// 分割每个Policy行为PolicyType、PolicyKey、PolicyValue
			for (String policyRow : policyRows) {
				String[] policyElem = null;
				// 解析Policy字符串
				try {
					policyElem = policyRow.split(" ");

					int policyType = Integer.parseInt(policyElem[0]);
					String policyKey = policyElem[1];
					String policyVal = "";

					// 把数据中除了前两个元素以外的所有元素都做为PolicyValue
					for (int i = 2; i < policyElem.length; i++) {
						policyVal = policyVal + policyElem[i] + " ";
					}
					policyVal = policyVal.trim();

					// 全局策略处理
					if (policyType == 1) {
						// 如果是service相关的，则返回其Value，即对WebServer的操作命令
						if (policyKey.equalsIgnoreCase(PolicyKeys.service)) {
							return policyVal;
							// 如果是remove_policy策略，就从策略队列中移除该策略
						} else if (policyKey
								.equalsIgnoreCase(PolicyKeys.remove_policy)) {

							String[] policyNames = policyVal.split(" ");
							for (String policyName : policyNames) {
								PolicyQueue.removePolicy(PolicyType.Global,
										policyName);
							}

							return "1 OK";
							// 如果是remove_all策略，清空策略队列
						} else if (policyKey
								.equalsIgnoreCase(PolicyKeys.remove_all)) {
							PolicyQueue.clear(PolicyType.Global);
							return "1 OK";
							// 如果是应该添加的策略，则封装Policy
						} else {
							// 如果是connection的conn_close_requests策略，则修改常量reqTimes的值
							if (policyKey
									.equalsIgnoreCase(PolicyKeys.conn_close_requests)) {
								// if (policyVal.equalsIgnoreCase("random")) {
								// Constant.reqTimes = (int) (Math.random() * 30
								// +
								// 1);
								// } else {
								Constant.reqTimes = Integer.parseInt(policyVal);
								// }
							}

							// 如果是connection的conn_close_timeout策略，则修改常量reqTimeout的值
							if (policyKey
									.equalsIgnoreCase(PolicyKeys.conn_close_timeout)) {
								Constant.reqTimeout = Integer
										.parseInt(policyVal);
								ConnectionHandler.connCloseTimeout();
							}

							p = new GlobalPolicy(policyKey, policyVal);

							PolicyQueue.addPolicy(p);
							result = (result == null ? "1 OK" : result
									+ "\r\n1 OK");
						}
					} else {
						result = (result == null ? "2 Invalid arguments:"
								+ policyRow : "2 Invalid arguments:"
								+ policyRow + "\r\n" + result);
						logger.error(result);
					}
				} catch (NumberFormatException e) {
					result = (result == null ? "2 Invalid arguments:"
							+ policyRow : "2 Invalid arguments:" + policyRow
							+ "\r\n" + result);
					logger.error(result, e);
				}
			}
		}
		return result;
	}
}