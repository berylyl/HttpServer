package utility;

import org.apache.log4j.Logger;

import Policy.Policy;
import Policy.PolicyException;
import Policy.PolicyKeys;

public class BodySize extends Thread {
	private static Logger logger = Logger.getLogger(BodySize.class);

	/**
	 * ����rsp_body_size���ԣ�����Body�Ĵ�С
	 * 
	 * @param p
	 *            ��rsp_body_size����
	 * @throws PolicyException
	 * @return��Body�Ĵ�С
	 */
	public static int getBodySize(Policy p) throws PolicyException {
		int bodySize = -1;
		try {
			if (p != null) {
				if (p.getKey().equalsIgnoreCase(PolicyKeys.rsp_body_size)) {
					// �õ�Body��С
					String bodySizeStr = p.getValue();
					// �����һ����Χ����ȡ�÷�Χ�������һ������ֵ
					if (bodySizeStr.indexOf("-") > 0) {
						String[] bodyScope = bodySizeStr.split("-");
						Double minBody = Double.parseDouble(bodyScope[0]);
						Double maxBody = Double.parseDouble(bodyScope[1]);
						bodySize = (int) ((Math.random() * (maxBody - minBody + 1D)) + minBody);
					} else {
						bodySize = Integer.parseInt(bodySizeStr);
					}
				}
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid arguments: " + p.getKey() + " "
					+ p.getValue());
			throw new PolicyException("Invalid arguments: " + p.getKey() + " "
					+ p.getValue());
		}
		if (bodySize < 0) {
			throw new PolicyException("Invalid arguments: " + p.getKey() + " "
					+ p.getValue());
		} else {
			return bodySize;
		}
	}
}
