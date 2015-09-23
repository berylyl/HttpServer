package Administration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import Policy.Handler.PolicyHandle;
import Policy.Handler.ServiceHandler;

/**
 * 接收策略指令的服务器主类；
 * 
 * @author yinlu
 * 
 */

public class NIOControlServer extends Thread {

	private static Selector selector;
	private static ServerSocketChannel ssc = null;

	private int BLOCK = 1024;
	private ByteBuffer sendbuffer = ByteBuffer.allocate(BLOCK);
	private ByteBuffer receivebuffer = ByteBuffer.allocate(BLOCK);

	private SocketChannel sc = null;
	private int port;
	private static AdminServer as;

	private static Logger logger = Logger.getLogger(NIOControlServer.class);

	@SuppressWarnings("static-access")
	public NIOControlServer(int port, AdminServer as) {
		this.port = port;
		this.as = as;
	}

	/**
	 * 多线程处理接收策略，从监听开始到循环处理每一个连接的请求
	 */
	public void run() {

		logger.debug("Enter the NIOTCP.run method." + this.getName());

		String sendText = "";

		try {
			ssc = ServerSocketChannel.open();
			ssc.configureBlocking(false);
			ssc.socket().bind(new InetSocketAddress(port));
			selector = Selector.open();
			ssc.register(selector, SelectionKey.OP_ACCEPT);

			while (true) {
				selector.select();

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

						logger.debug(this.getName() + "is connected.");
						// Read事件
					} else if (selectionKey.isReadable()) {
						logger.debug(this.getName() + "is readable.");

						sc = (SocketChannel) selectionKey.channel();
						receivebuffer.clear();

						String receiveText = "";// 客户端传来的字符串

						// 读取客户端传过来的数据
						if (sc.isConnected()) {
							int count;
							while ((count = sc.read(receivebuffer)) > 0) {
								receiveText = receiveText.concat(new String(
										receivebuffer.array(), 0, count));
								receivebuffer.clear();
							}
						}

						// 将接收到的字符串交给serverHandle处理，并将返回的结果做为服务器端响应内容
						sendText = serverHandle(receiveText);

						sc.register(selector, SelectionKey.OP_WRITE);

						// Write事件
					} else if (selectionKey.isWritable()) {
						logger.debug(this.getName() + "is readable.");

						sendbuffer.clear();
						sc = (SocketChannel) selectionKey.channel();

						// 返回客户端OK表示已经接收到正确数据
						sendbuffer.put(sendText.getBytes());
						sendbuffer.flip();
						sc.write(sendbuffer);
						sc.close();
					}
				}
			}
		} catch (NumberFormatException e) {
			logger.error("NIOTCP.run NumberFormatException:", e);
		} catch (ClosedChannelException e) {
			logger.error("NIOTCP.run ClosedChannelException:", e);
		} catch (IOException e) {
			logger.error("NIOTCP.run IOException:", e);
		}

		logger.debug("Out of the method NIOTCP.run." + this.getName());
	}

	/**
	 * 处理传入的Policy字符串，如果是要启动、关闭或者重启WebServer的，则执行操作，否则封装成Policy并添加到Policy队列中
	 * 
	 * @param policyStr
	 *            ：策略字符串
	 * @param as
	 *            ：AdminServer即探测服务器
	 * @return：对策略字符串的处理结果
	 */
	public static String serverHandle(String policyStr) {
		logger.debug("Enter the NIOTCP.serverHandle method.");
		// 将policyStr交给HttpChannel.handlePolicy处理AddPolicy: 1 service stop
		String handle = new PolicyHandle().handlePolicy(policyStr);

		if (!handle.trim().contains(" ")) {
			handle = ServiceHandler.servicePolicy(as, handle);
		}
		return handle;
	}
}