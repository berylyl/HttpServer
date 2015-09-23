package AIOWebServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

public class AioTcpServer extends Thread {
	private AsynchronousChannelGroup asyncChannelGroup;// aio的核心之一通道组.由它负责处理事件,完成之后通知相应的handler
	private AsynchronousServerSocketChannel listener;// 端口侦听器
	private static Logger logger = Logger.getLogger(AioTcpServer.class);

	public AioTcpServer(int port) throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(20);
		asyncChannelGroup = AsynchronousChannelGroup.withThreadPool(executor);
		listener = AsynchronousServerSocketChannel.open(asyncChannelGroup)
				.bind(new InetSocketAddress(port));
        
	}

	public void run() {
		try {
			listener.accept(listener, new AioAcceptHandler());
			while (true) {
				Thread.sleep(60000);
			}

		} catch (Exception e) {
			logger.error("listener.accept exception",e);
		} 
	}

	/**
	 * close all client connections; close server listener
	 */
	public void closeServer() {
		try {
			SocketQueue.closeAllConnections();
			listener.close();
		} catch (Exception e) {
			logger.error("close server exception", e);
		}
	}

	public static void main(String... args) throws Exception {
		AioTcpServer server = new AioTcpServer(9008);
		new Thread(server).start();
	}
}