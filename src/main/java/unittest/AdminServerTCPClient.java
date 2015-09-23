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

	/* 缓冲区大小 */
	private static int BLOCK = 4096;
	/* 接受数据缓冲区 */
	private static ByteBuffer sendbuffer = ByteBuffer.allocate(BLOCK);
	/* 发送数据缓冲区 */
	private static ByteBuffer receivebuffer = ByteBuffer.allocate(BLOCK);
	/* 服务器端地址 */
	private final static InetSocketAddress SERVER_ADDRESS = new InetSocketAddress(
			6666);

	public AdminServerTCPClient(String name) {
		sendbuffer.clear();
	}

	public static void xx(){
		// TODO Auto-generated method stub
		// 打开socket通道
		try {
			SocketChannel socketChannel = SocketChannel.open();
			// 设置为非阻塞方式
			socketChannel.configureBlocking(false);
			// 打开选择器
			Selector selector = Selector.open();
			// 注册连接服务端socket动作
			socketChannel.register(selector, SelectionKey.OP_CONNECT);
			// 连接
			socketChannel.connect(SERVER_ADDRESS);

			Set<SelectionKey> selectionKeys;
			Iterator<SelectionKey> iterator;
			SelectionKey selectionKey;
			SocketChannel channel = null;
			String receiveText;
			String sendText;
			int count = 0;
			// 说明注册的connect对象已经连接上
			while (selector.select() > 0) {
				// 当注册事件到达时方法返回；否则方法一直阻塞
				// 返回此选择器的已选择键集。
				selectionKeys = selector.selectedKeys();
				// System.out.println(selectionKeys.size());
				iterator = selectionKeys.iterator();
				while (iterator.hasNext()) {
					selectionKey = iterator.next();
					if (selectionKey.isConnectable()) {
						System.out.println("channel connect");
						channel = (SocketChannel) selectionKey.channel();
						// 判断此通道上是否正在进行连接操作。
						// 完成套接字通道的连接过程。
						// if (channel.isConnectionPending()) {
						channel.finishConnect();
						System.out.println("完成连接!");
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
						// 将缓冲区清空以备下次读取
						receivebuffer.clear();
						// 读取服务器发送来的数据到缓冲区中
						count = channel.read(receivebuffer);
						if (count > 0) {
							receiveText = new String(receivebuffer.array(), 0,
									count);
							channel.provider();
							System.out.println("客户端接受服务器端数据--:" + receiveText);
							System.out.println("服务器是--:" + channel.provider());
							// selectionKey.cancel();
							// channel.close();
							// channel.register(selector,
							// SelectionKey.OP_WRITE);
						}

					} else if (selectionKey.isWritable()) {
						channel = (SocketChannel) selectionKey.channel();
						sendbuffer.clear();

						
						 //键盘输入文本做为策略字符串 
						InputStreamReader isr = new InputStreamReader(System.in);
						BufferedReader br = new BufferedReader(isr);
						sendText = br.readLine();
						 

						
//						 //直接输入
//						sendText = "1 a 1\r\n1 b 2\r\n1 c 3";
//						 
						
						//文件读取
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
			System.out.println("客户端1异常" + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("客户端2异常" + e.getMessage());
			e.printStackTrace();
		} catch (ClosedSelectorException e) {
			e.printStackTrace();
			System.out.println("客户端关闭");
		}
	}

	public static void main(String[] args) {
		AdminServerTCPClient.xx();
	}
}
