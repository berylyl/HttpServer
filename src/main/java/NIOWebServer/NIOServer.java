package NIOWebServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

public class NIOServer extends Thread {

	/**
	 * 从监听开始到循环处理每一个连接的请求
	 */

	private static Selector selector;
	private static ServerSocketChannel ssc = null;

	private SocketChannel sc = null;
	private int port;
	int count = 0;

	private static Logger logger = Logger.getLogger(NIOServer.class);
	// 线程池相关处理
	ExecutorService pool = null;

	public NIOServer() {
	}

	public NIOServer(int port) {
		this.port = port;
	}

	public void run() {
		pool = Executors.newFixedThreadPool(2);

		try {
			ssc = ServerSocketChannel.open();
			ssc.configureBlocking(false);
			ssc.socket().bind(new InetSocketAddress(port));
			selector = Selector.open();
			ssc.register(selector, SelectionKey.OP_ACCEPT);
		} catch (Exception e) {
			// 程序退出处理
			e.printStackTrace();
			return;
		}
		System.out.println("Server is started=======");

		while (true) {
			try {
				int n = selector.selectNow();
				if (n == 0) {
					continue;
				}
				Set<SelectionKey> selectionKeys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = selectionKeys.iterator();
				while (iterator.hasNext()) {
					SelectionKey selectionKey = iterator.next();
					iterator.remove();
					// Accept事件
					if (selectionKey.isAcceptable()) {
						ssc = (ServerSocketChannel) selectionKey.channel();

						sc = ssc.accept();
						sc.configureBlocking(false);
						sc.register(selector, SelectionKey.OP_READ);
						ssc.register(selector, SelectionKey.OP_ACCEPT);

						// 添加SocketChannel对象到全局队列里
						ChannelInfo.addChannel(sc);
						// Read事件
					} else if (selectionKey.isReadable()) {
						pool.execute(new NIOReaderHandler(selectionKey));

						// Write事件
					} else if (selectionKey.isWritable()) {

					}
				}
			} catch (Exception e) {
				System.out.println("444");
				e.printStackTrace();
			}
		}
	}
	

	public void closeSelector(){
		try {
			selector.close();
		} catch (IOException e) {
			logger.error("NIOServer.closeSelector IOException:",e);
		}
	}
	
	public void closeServerSocketChannel(){
		try {
			ssc.close();
		} catch (IOException e) {
			logger.error("NIOServer.closeServerSocketChannel IOException:",e);
		}
	}
}
