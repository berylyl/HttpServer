package AIOWebServer;

import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.nio.channels.AsynchronousSocketChannel;  
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

	public static void closeConnectionByRequestTimes(HttpRequest hr, AsynchronousSocketChannel socket){
		String ka = hr.getHttpHeader().getHeaders().get("Connection");
		// 如果请求头中带有的Connection项是Close，则每次都关闭连接，否则，按Connection策略处理连接
		if (ka != null && ka.equalsIgnoreCase("close")) {
			SocketQueue.closeConnection(socket);
			
		} else {
			Policy KAPolicy = PolicyQueue.getPolicy(PolicyType.Global,
					PolicyKeys.keep_alive);
			// 按Keep_Alive策略处理连接
			if (KAPolicy != null) {
				String KeepAlive = KAPolicy.getValue();
				if (KeepAlive.equalsIgnoreCase("off")) {
					SocketQueue.closeAllConnections();
				} else if (KeepAlive.equalsIgnoreCase("on")) {
					//关闭连接如果请求数达到上限，否则继续递增请求数
					if (Constant.reqTimes>0 && (SocketQueue.increaseClientRequestTimes(socket)) >= Constant.reqTimes) {
						SocketQueue.closeConnection(socket);
					}
				}
			} 
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
	public static void setConnCloseTimer() {
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
					//logger.info("close all client sockets because close timer reached");
					SocketQueue.closeAllConnections();
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