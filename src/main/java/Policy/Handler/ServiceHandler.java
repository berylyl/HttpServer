package Policy.Handler;

import org.apache.log4j.Logger;

import Administration.AdminServer;

public class ServiceHandler {
	
	private static Logger logger = Logger.getLogger(ServiceHandler.class);
	
	public static String servicePolicy(AdminServer as, String order){
		if (order.equalsIgnoreCase("start")) {
			as.start();
		} else if (order.equalsIgnoreCase("stop")) {
			as.stop();
		} else if (order.equalsIgnoreCase("restart")) {
			as.restart();
		} else {
			logger.error("2 Invalid arguments:" + order);
			return "2 Invalid arguments:" + order;
		}
		return "1 OK";
	}
}
