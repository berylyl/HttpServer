package BIOWebServer;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Socket�����࣬�洢���е�Socket�Լ��Զ�����Socket����ӡ��Ƴ�����ѯ�Ȳ���
 * 
 * @author yinlu
 * 
 */

public class SocketQueue {
	private static Hashtable<Socket, Integer> socketTimes = new Hashtable<Socket, Integer>();
	private static ArrayList<Socket> socketTimeout = new ArrayList<Socket>();

	private static Logger logger = Logger.getLogger(SocketQueue.class);

	/**
	 * ����������Ӧ�ü�����Socket
	 * 
	 * @param socket
	 *            ��Ҫ��ӵ�Socket
	 * @param times
	 *            ����Socket�ܵ��������
	 */
	public static void addTimesSocket(Socket socket, Integer times) {
		socketTimes.put(socket, times);
	}

	/**
	 * ����������Ӧ�ü�ʱ��Socket
	 * 
	 * @param socket
	 *            ��Ҫ��ӵ�Socket
	 * @param times
	 *            ����Socket�ܵ��������
	 */
	public static void addTimeoutSocket(Socket socket) {
		socketTimeout.add(socket);
	}

	/**
	 * �Ӷ������Ƴ��ѹرյ�Socket
	 * 
	 * @param socket
	 *            ��Ҫ�Ƴ���Socket
	 */
	public static void removeSocket(Socket socket) {
		socketTimes.remove(socket);
		socketTimeout.remove(socket);
	}

	/**
	 * ��ȡ���ڼ���������Socket
	 * 
	 * @return�����ڼ���������Socket�ļ���
	 */
	public static Hashtable<Socket, Integer> getTimesSocket() {
		return socketTimes;
	}

	/**
	 * ��ȡȫ��δ�رյ�Socket
	 * 
	 * @return��ȫ��δ�رյ�Socket�ļ���
	 */
	public static HashSet<Socket> getAllSocket() {
		Set<Socket> all = new HashSet<Socket>();
		all.addAll(socketTimeout);
		all.addAll(socketTimes.keySet());
		return (HashSet<Socket>) all;
	}

	/**
	 * �ر�ȫ��Socket
	 */
	public static void closeAllSocket() {
		Iterator<Socket> it = getAllSocket().iterator();
		while (it.hasNext()) {
			Socket so = it.next();
			try {
				so.close();
			} catch (IOException e) {
				logger.error("SocketQueue.closeAllSocket IOException:", e);
			}
		}
		socketTimeout.clear();
		socketTimes.clear();
	}
}
