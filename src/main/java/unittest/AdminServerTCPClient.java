package unittest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class AdminServerTCPClient {

	/* ��������С */
	private static int BLOCK = 4096;
	/* �������ݻ����� */
	private static ByteBuffer sendbuffer = ByteBuffer.allocate(BLOCK);
	/* �������ݻ����� */
	private static ByteBuffer receivebuffer = ByteBuffer.allocate(BLOCK);
	/* �������˵�ַ */
	private final static InetSocketAddress SERVER_ADDRESS = new InetSocketAddress(
			6666);

	public AdminServerTCPClient(String name) {
		sendbuffer.clear();
	}

	public static void xx(){
		// TODO Auto-generated method stub
		// ��socketͨ��
		try {
			SocketChannel socketChannel = SocketChannel.open();
			// ����Ϊ��������ʽ
			socketChannel.configureBlocking(false);
			// ��ѡ����
			Selector selector = Selector.open();
			// ע�����ӷ����socket����
			socketChannel.register(selector, SelectionKey.OP_CONNECT);
			// ����
			socketChannel.connect(SERVER_ADDRESS);

			Set<SelectionKey> selectionKeys;
			Iterator<SelectionKey> iterator;
			SelectionKey selectionKey;
			SocketChannel channel = null;
			String receiveText;
			String sendText;
			int count = 0;
			// ˵��ע���connect�����Ѿ�������
			while (selector.select() > 0) {
				// ��ע���¼�����ʱ�������أ����򷽷�һֱ����
				// ���ش�ѡ��������ѡ�������
				selectionKeys = selector.selectedKeys();
				// System.out.println(selectionKeys.size());
				iterator = selectionKeys.iterator();
				while (iterator.hasNext()) {
					selectionKey = iterator.next();
					if (selectionKey.isConnectable()) {
						System.out.println("channel connect");
						channel = (SocketChannel) selectionKey.channel();
						// �жϴ�ͨ�����Ƿ����ڽ������Ӳ�����
						// ����׽���ͨ�������ӹ��̡�
						// if (channel.isConnectionPending()) {
						channel.finishConnect();
						System.out.println("�������!");
						// sendbuffer.clear();

						// sendbuffer.put("1 min 1".getBytes());
						// sendbuffer.put("1 max 100".getBytes());
						// sendbuffer.put("1 size 666".getBytes());

						// sendbuffer.flip();
						// channel.write(sendbuffer);
						// }else{
						// break;
						// }
						// selectionKey.cancel();
						// channel.register(selector,
						// SelectionKey.OP_READ|SelectionKey.OP_WRITE);
						channel.register(selector, SelectionKey.OP_WRITE);
					} else if (selectionKey.isReadable()) {
						channel = (SocketChannel) selectionKey.channel();
						// ������������Ա��´ζ�ȡ
						receivebuffer.clear();
						// ��ȡ�����������������ݵ���������
						count = channel.read(receivebuffer);
						if (count > 0) {
							receiveText = new String(receivebuffer.array(), 0,
									count);
							channel.provider();
							System.out.println("�ͻ��˽��ܷ�����������--:" + receiveText);
							System.out.println("��������--:" + channel.provider());
							// selectionKey.cancel();
							// channel.close();
							// channel.register(selector,
							// SelectionKey.OP_WRITE);
						}

					} else if (selectionKey.isWritable()) {
						channel = (SocketChannel) selectionKey.channel();
						sendbuffer.clear();

						
						 //���������ı���Ϊ�����ַ��� 
						InputStreamReader isr = new InputStreamReader(System.in);
						BufferedReader br = new BufferedReader(isr);
						sendText = br.readLine();
						 

						
//						 //ֱ������
//						sendText = "1 a 1\r\n1 b 2\r\n1 c 3";
//						 
						
						//�ļ���ȡ
//						String str=""; 
//						FileReader fr=new FileReader(
//						"C:\\Users\\wb-zhoujiankai\\Desktop\\Policy.txt");
//						char[] chars=new char[1024];
//						int b=0;
//						while((b=fr.read(chars))!=-1){
//						str+=String.valueOf(chars); }
//						fr.close();
//						sendText = str;
//						System.out.println(str);
						 
						
						sendbuffer.put(sendText.getBytes());
						// sendbuffer.put("Write,Write by Client".getBytes());
						sendbuffer.flip();
						channel.write(sendbuffer);

						channel.register(selector, SelectionKey.OP_READ);
						// selectionKey.cancel();

						// selector.close();
						// channel.close();
					} else {
						System.out.println("-----------");
					}
				}
				// System.out.println(selector.select());
				// selectionKeys.clear();

			}
		} catch (ClosedChannelException e) {
			System.out.println("�ͻ���1�쳣" + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("�ͻ���2�쳣" + e.getMessage());
			e.printStackTrace();
		} catch (ClosedSelectorException e) {
			e.printStackTrace();
			System.out.println("�ͻ��˹ر�");
		}
	}

	public static void main(String[] args) {
		AdminServerTCPClient.xx();
	}
}
