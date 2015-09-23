package AIOWebServer;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

public class SocketQueue {
	
	public static class ClientConnection{
		public String clientRequest = "";
		public Integer requestTimes = 0;
	}
	
	private static Hashtable<AsynchronousSocketChannel,ClientConnection> allClientSocket = new Hashtable<AsynchronousSocketChannel,ClientConnection>();
	private static Logger logger = Logger.getLogger(SocketQueue.class);
	
	public static int increaseClientRequestTimes(AsynchronousSocketChannel socket){
		ClientConnection oldValue = allClientSocket.get(socket);
		if(oldValue != null){
			return oldValue.requestTimes = oldValue.requestTimes+1;
		}else
			return 0;
	}
	
	public static int getClientRequestTimes(AsynchronousSocketChannel socket){
		ClientConnection oldValue = allClientSocket.get(socket);
		if(oldValue != null){
			return oldValue.requestTimes;
		}else
			return 0;
	}
	
	
	/**
	 * 每接收一个client连接，便把连接加入到数组里
	 * @param socket
	 */
	public static void addClientConnection(AsynchronousSocketChannel socket){
		allClientSocket.put(socket,new ClientConnection());
	}
	
	/**
	 * 每次与client的连接关闭，便从数组中移除
	 * @param socket
	 */
	public static void removeClientConnection(AsynchronousSocketChannel socket){
		allClientSocket.remove(socket);
	}
	
	/**
	 * close socket，remove socket from array
	 * @param socket
	 */
	public static void closeConnection(AsynchronousSocketChannel socket){
		try{
			logger.info("close client socket:"+socket.getRemoteAddress().toString());
			socket.close();
		}catch(IOException e){
			logger.error("socket close error",e);
		}
		removeClientConnection(socket);
	}
	/**
	 * 合并该连接对应的请求内容
	 * @param socket
	 * @param value
	 * @return
	 */
	public static String combineClientRequest(AsynchronousSocketChannel socket, String value){
		ClientConnection oldValue = allClientSocket.get(socket);
		if(oldValue != null){
			return oldValue.clientRequest = oldValue.clientRequest+value;
		}else
			return "";
	}
	
	/**
	 * 清空连接对应的请求内容
	 * @param socket
	 */
	public static void clearClientRequest(AsynchronousSocketChannel socket){
		ClientConnection value = allClientSocket.get(socket);
		if(value != null){
			value.clientRequest = "";
		}
	}

	/**
	 * 显示所有active的连接
	 */
	public static void showAllConnections(){
		Enumeration<AsynchronousSocketChannel> it = allClientSocket.keys();
		while (it.hasMoreElements()) {
			AsynchronousSocketChannel so = it.nextElement();
			try {
			logger.info(so.getRemoteAddress().toString());
			} catch (IOException e) {
				logger.error("SocketQueue.showAllConnections IOException:", e);
			}
		}
	}
	/**
	 * 关闭全部Socket
	 */
	public static void closeAllConnections() {
		Enumeration<AsynchronousSocketChannel> it = allClientSocket.keys();
		while (it.hasMoreElements()) {
			AsynchronousSocketChannel so = it.nextElement();
			
			try {
				logger.info("close client socket:"+so.getRemoteAddress().toString());
				so.close();
			} catch (IOException e) {
				logger.error("SocketQueue.closeAllSocket IOException:", e);
			}
		}
		allClientSocket.clear();
	}
}
