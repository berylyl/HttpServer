package utility;

import org.apache.log4j.Logger;

import Policy.Policy;
import Policy.PolicyException;
import Policy.PolicyKeys;

public class BodySize extends Thread {
	private static Logger logger = Logger.getLogger(BodySize.class);

	/**
	 * 传入rsp_body_size策略，返回Body的大小
	 * 
	 * @param p
	 *            ：rsp_body_size策略
	 * @throws PolicyException
	 * @return：Body的大小
	 */
	public static int getBodySize(Policy p) throws PolicyException {
		int bodySize = -1;
		try {
			if (p != null) {
				if (p.getKey().equalsIgnoreCase(PolicyKeys.rsp_body_size)) {
					// 得到Body大小
					String bodySizeStr = p.getValue();
					// 如果是一个范围，就取该范围中随机的一个整型值
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
