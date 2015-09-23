package Policy.Handler;

import org.apache.log4j.Logger;

import utility.Constant;

import BIOWebServer.ConnectionHandler;
import Policy.*;

/**
 * ����ָ���ַ��������࣬���в����ַ���ת���ɲ��Զ�����ӵ����Զ��еȲ���
 * 
 * @author yinlu
 * 
 */
public class PolicyHandle {

	/**
	 * ����http request�е�stringָ�ת����policy���д�������ӻ�ɾ������; policyStr��ʽΪ��"1 max 100"
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
			// �ָ�PolicyStrΪ���Policy��
			String[] policyRows = policyStr.split("\r\n");

			// �ָ�ÿ��Policy��ΪPolicyType��PolicyKey��PolicyValue
			for (String policyRow : policyRows) {
				String[] policyElem = null;
				// ����Policy�ַ���
				try {
					policyElem = policyRow.split(" ");

					int policyType = Integer.parseInt(policyElem[0]);
					String policyKey = policyElem[1];
					String policyVal = "";

					// �������г���ǰ����Ԫ�����������Ԫ�ض���ΪPolicyValue
					for (int i = 2; i < policyElem.length; i++) {
						policyVal = policyVal + policyElem[i] + " ";
					}
					policyVal = policyVal.trim();

					// ȫ�ֲ��Դ���
					if (policyType == 1) {
						// �����service��صģ��򷵻���Value������WebServer�Ĳ�������
						if (policyKey.equalsIgnoreCase(PolicyKeys.service)) {
							return policyVal;
							// �����remove_policy���ԣ��ʹӲ��Զ������Ƴ��ò���
						} else if (policyKey
								.equalsIgnoreCase(PolicyKeys.remove_policy)) {

							String[] policyNames = policyVal.split(" ");
							for (String policyName : policyNames) {
								PolicyQueue.removePolicy(PolicyType.Global,
										policyName);
							}

							return "1 OK";
							// �����remove_all���ԣ���ղ��Զ���
						} else if (policyKey
								.equalsIgnoreCase(PolicyKeys.remove_all)) {
							PolicyQueue.clear(PolicyType.Global);
							return "1 OK";
							// �����Ӧ����ӵĲ��ԣ����װPolicy
						} else {
							// �����connection��conn_close_requests���ԣ����޸ĳ���reqTimes��ֵ
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

							// �����connection��conn_close_timeout���ԣ����޸ĳ���reqTimeout��ֵ
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