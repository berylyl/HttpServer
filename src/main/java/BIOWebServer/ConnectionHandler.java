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
 * ȥ���Գ����ҵ�connection��صĲ��ԣ��ҵ��ϲ�����ۺϴ�����
 * 
 * @author yinlu
 * 
 */
public class ConnectionHandler {
	private static Timer timer = new Timer();

	private static Logger logger = Logger.getLogger(ConnectionHandler.class);

	/**
	 * KeepAlive���Դ���
	 * 
	 * @param hr
	 *            ��HTTP����ͷ
	 * @param so
	 *            ��Socket���Ӷ���
	 */
	public static void keepAlive(HttpRequest hr, Socket so) {
		Policy KAPolicy = PolicyQueue.getPolicy(PolicyType.Global,
				PolicyKeys.keep_alive);
		String ka = hr.getHttpHeader().getHeaders().get("Connection");

		// �������ͷ�д��е�Connection����Close����ÿ�ζ��ر����ӣ����򣬰�Connection���Դ�������
		if (ka != null && ka.equalsIgnoreCase("close")) {
			try {
				so.close();
				SocketQueue.removeSocket(so);
			} catch (IOException e) {
				logger.error("ConnectionHandler.keepAlive IOException:", e);
			}
		} else {
			// ��Keep_Alive���Դ�������
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
					// ����������������
					connCloseReq(so);
				}
			} else {// ���û��ָ����keepAlive����ʽ
				connCloseReq(so);
			}
		}
	}

	/**
	 * �����������������
	 * 
	 * @param so
	 *            ������������
	 */
	public static void connCloseReq(Socket so) {
		Integer times = SocketQueue.getTimesSocket().get(so);
		// �����Ҫ����������ر����ӣ���У��������������Ƿ�ʱ��ر�����
		if (Constant.reqTimes != -1) {
			try {
				// ������ӳ����и����ӣ��򵽴���������رո����ӣ����������ӳ�����Ӹ�����
				if (times != null) {
					// ����������������رո����ӣ����򣬽���������1
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
	public static void connCloseTimeout() {
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
					SocketQueue.closeAllSocket();
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