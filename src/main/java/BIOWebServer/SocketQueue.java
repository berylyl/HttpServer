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
 * Socket队列类，存储所有的Socket以及对队列中Socket的添加、移除及查询等操作
 * 
 * @author yinlu
 * 
 */

public class SocketQueue {
	private static Hashtable<Socket, Integer> socketTimes = new Hashtable<Socket, Integer>();
	private static ArrayList<Socket> socketTimeout = new ArrayList<Socket>();

	private static Logger logger = Logger.getLogger(SocketQueue.class);

	/**
	 * 向队列中添加应该计数的Socket
	 * 
	 * @param socket
	 *            ：要添加的Socket
	 * @param times
	 *            ：该Socket总的请求次数
	 */
	public static void addTimesSocket(Socket socket, Integer times) {
		socketTimes.put(socket, times);
	}

	/**
	 * 向队列中添加应该计时的Socket
	 * 
	 * @param socket
	 *            ：要添加的Socket
	 * @param times
	 *            ：该Socket总的请求次数
	 */
	public static void addTimeoutSocket(Socket socket) {
		socketTimeout.add(socket);
	}

	/**
	 * 从队列中移除已关闭的Socket
	 * 
	 * @param socket
	 *            ：要移除的Socket
	 */
	public static void removeSocket(Socket socket) {
		socketTimes.remove(socket);
		socketTimeout.remove(socket);
	}

	/**
	 * 获取正在计数的所有Socket
	 * 
	 * @return：正在计数的所有Socket的集合
	 */
	public static Hashtable<Socket, Integer> getTimesSocket() {
		return socketTimes;
	}

	/**
	 * 获取全部未关闭的Socket
	 * 
	 * @return：全部未关闭的Socket的集合
	 */
	public static HashSet<Socket> getAllSocket() {
		Set<Socket> all = new HashSet<Socket>();
		all.addAll(socketTimeout);
		all.addAll(socketTimes.keySet());
		return (HashSet<Socket>) all;
	}

	/**
	 * 关闭全部Socket
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
