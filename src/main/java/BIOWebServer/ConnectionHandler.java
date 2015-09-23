package BIOWebServer;

import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import utility.Constant;
import Http.Protocol.HttpRequest;
import Policy.Policy;
import Policy.PolicyKeys;
import Policy.PolicyQueue;
import Policy.PolicyType;

/**
 * 去策略池里找到connection相关的策略，找到合并后的综合处理方法
 * 
 * @author yinlu
 * 
 */
public class ConnectionHandler {
	private static Timer timer = new Timer();

	private static Logger logger = Logger.getLogger(ConnectionHandler.class);

	/**
	 * KeepAlive策略处理
	 * 
	 * @param hr
	 *            ：HTTP请求头
	 * @param so
	 *            ：Socket连接对象
	 */
	public static void keepAlive(HttpRequest hr, Socket so) {
		Policy KAPolicy = PolicyQueue.getPolicy(PolicyType.Global,
				PolicyKeys.keep_alive);
		String ka = hr.getHttpHeader().getHeaders().get("Connection");

		// 如果请求头中带有的Connection项是Close，则每次都关闭连接，否则，按Connection策略处理连接
		if (ka != null && ka.equalsIgnoreCase("close")) {
			try {
				so.close();
				SocketQueue.removeSocket(so);
			} catch (IOException e) {
				logger.error("ConnectionHandler.keepAlive IOException:", e);
			}
		} else {
			// 按Keep_Alive策略处理连接
			if (KAPolicy != null) {
				String KeepAlive = KAPolicy.getValue();
				if (KeepAlive.equalsIgnoreCase("off")) {
					try {
						so.close();
						SocketQueue.removeSocket(so);
					} catch (IOException e) {
						logger.error(
								"ConnectionHandler.keepAlive IOException:", e);
					}
				} else if (KeepAlive.equalsIgnoreCase("on")) {
					// 按请求数处理连接
					connCloseReq(so);
				}
			} else {// 如果没有指定的keepAlive处理方式
				connCloseReq(so);
			}
		}
	}

	/**
	 * 按请求次数处理连接
	 * 
	 * @param so
	 *            ：计数的连接
	 */
	public static void connCloseReq(Socket so) {
		Integer times = SocketQueue.getTimesSocket().get(so);
		// 如果需要按请求次数关闭连接，则校验次数，否则检查是否按时间关闭连接
		if (Constant.reqTimes != -1) {
			try {
				// 如果连接池中有该连接，则到达允许次数关闭该连接，否则，向连接池中添加该连接
				if (times != null) {
					// 如果到达允许次数则关闭该连接，否则，将计数器加1
					if (times < Constant.reqTimes - 1) {
						SocketQueue.addTimesSocket(so, times + 1);
					} else {
						so.close();
						SocketQueue.removeSocket(so);
					}
				} else {
					SocketQueue.addTimesSocket(so, 1);
				}
			} catch (IOException e) {
				logger.error("ConnectionHandler.connCloseReq IOException:", e);
			}
		} else {
			SocketQueue.addTimeoutSocket(so);
		}
	}

	/**
	 * 通过改变策略conn-close-timeout改变超时的处理方法
	 * 
	 * public static void connCloseTimeout(String value) {
	 * if(PolicyQueue.getPolicy(PolicyType.Global,
	 * PolicyKeys.conn_close_timeout)!=null){ timer.cancel(); timer = new
	 * Timer(); }
	 * 
	 * if (value.equalsIgnoreCase("random")) { timer.schedule(new TimerTask() {
	 * public void run() { Constant.reqTimeout = (int) (Math .random() * 30 +
	 * 1); } }, 0, 10 * 1000); } else { try { Constant.reqTimeout = Integer
	 * .parseInt(value); } catch (NumberFormatException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } }
	 * 
	 * connCloseTimeout(); }
	 */

	/**
	 * 启动conn-close-timeout超时的处理方法
	 */
	public static void connCloseTimeout() {
		// 如果已经启动了定时器，则先将其取消，再启动新计时器
		if (PolicyQueue.getPolicy(PolicyType.Global,
				PolicyKeys.conn_close_timeout) != null) {
			timer.cancel();
			timer = new Timer();
		}
		// 如果到达超时时间，则关闭所有连接
		if (Constant.reqTimeout > 0) {
			timer.schedule(new TimerTask() {
				public void run() {
					SocketQueue.closeAllSocket();
				}
			}, Constant.reqTimeout * 1000, Constant.reqTimeout * 1000);
		}
	}

	/**
	 * 取消时间器方法
	 */
	public static void closeTimer() {
		timer.cancel();
	}
}