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
 * ȥ���Գ����ҵ�connection��صĲ��ԣ��ҵ��ϲ�����ۺϴ�����
 * 
 * @author yinlu
 * 
 */
public class ConnectionHandler {
	private static Timer timer = new Timer();

	private static Logger logger = Logger.getLogger(ConnectionHandler.class);

	public static void closeConnectionByRequestTimes(HttpRequest hr, AsynchronousSocketChannel socket){
		String ka = hr.getHttpHeader().getHeaders().get("Connection");
		// �������ͷ�д��е�Connection����Close����ÿ�ζ��ر����ӣ����򣬰�Connection���Դ�������
		if (ka != null && ka.equalsIgnoreCase("close")) {
			SocketQueue.closeConnection(socket);
			
		} else {
			Policy KAPolicy = PolicyQueue.getPolicy(PolicyType.Global,
					PolicyKeys.keep_alive);
			// ��Keep_Alive���Դ�������
			if (KAPolicy != null) {
				String KeepAlive = KAPolicy.getValue();
				if (KeepAlive.equalsIgnoreCase("off")) {
					SocketQueue.closeAllConnections();
				} else if (KeepAlive.equalsIgnoreCase("on")) {
					//�ر���������������ﵽ���ޣ������������������
					if (Constant.reqTimes>0 && (SocketQueue.increaseClientRequestTimes(socket)) >= Constant.reqTimes) {
						SocketQueue.closeConnection(socket);
					}
				}
			} 
		}
	}

	/**
	 * ͨ���ı����conn-close-timeout�ı䳬ʱ�Ĵ�����
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
	 * ����conn-close-timeout��ʱ�Ĵ�����
	 */
	public static void setConnCloseTimer() {
		// ����Ѿ������˶�ʱ�������Ƚ���ȡ�����������¼�ʱ��
		if (PolicyQueue.getPolicy(PolicyType.Global,
				PolicyKeys.conn_close_timeout) != null) {
			timer.cancel();
			timer = new Timer();
		}
		// ������ﳬʱʱ�䣬��ر���������
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
	 * ȡ��ʱ��������
	 */
	public static void closeTimer() {
		timer.cancel();
	}
}