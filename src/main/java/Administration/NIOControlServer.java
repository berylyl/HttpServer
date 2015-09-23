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
 * ���ղ���ָ��ķ��������ࣻ
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
	 * ���̴߳�����ղ��ԣ��Ӽ�����ʼ��ѭ������ÿһ�����ӵ�����
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
					// Accept�¼�
					if (selectionKey.isAcceptable()) {
						ssc = (ServerSocketChannel) selectionKey.channel();

						sc = ssc.accept();
						sc.configureBlocking(false);
						sc.register(selector, SelectionKey.OP_READ);

						logger.debug(this.getName() + "is connected.");
						// Read�¼�
					} else if (selectionKey.isReadable()) {
						logger.debug(this.getName() + "is readable.");

						sc = (SocketChannel) selectionKey.channel();
						receivebuffer.clear();

						String receiveText = "";// �ͻ��˴������ַ���

						// ��ȡ�ͻ��˴�����������
						if (sc.isConnected()) {
							int count;
							while ((count = sc.read(receivebuffer)) > 0) {
								receiveText = receiveText.concat(new String(
										receivebuffer.array(), 0, count));
								receivebuffer.clear();
							}
						}

						// �����յ����ַ�������serverHandle�����������صĽ����Ϊ����������Ӧ����
						sendText = serverHandle(receiveText);

						sc.register(selector, SelectionKey.OP_WRITE);

						// Write�¼�
					} else if (selectionKey.isWritable()) {
						logger.debug(this.getName() + "is readable.");

						sendbuffer.clear();
						sc = (SocketChannel) selectionKey.channel();

						// ���ؿͻ���OK��ʾ�Ѿ����յ���ȷ����
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
	 * �������Policy�ַ����������Ҫ�������رջ�������WebServer�ģ���ִ�в����������װ��Policy����ӵ�Policy������
	 * 
	 * @param policyStr
	 *            �������ַ���
	 * @param as
	 *            ��AdminServer��̽�������
	 * @return���Բ����ַ����Ĵ�����
	 */
	public static String serverHandle(String policyStr) {
		logger.debug("Enter the NIOTCP.serverHandle method.");
		// ��policyStr����HttpChannel.handlePolicy����AddPolicy: 1 service stop
		String handle = new PolicyHandle().handlePolicy(policyStr);

		if (!handle.trim().contains(" ")) {
			handle = ServiceHandler.servicePolicy(as, handle);
		}
		return handle;
	}
}