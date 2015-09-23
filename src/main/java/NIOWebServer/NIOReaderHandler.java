package NIOWebServer;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import Http.Protocol.HttpRequest;
import Policy.Handler.ResponseHandler;

public class NIOReaderHandler extends Thread {
	private SocketChannel sockchennel = null;
	private SelectionKey selectedKey = null;
	private ByteBuffer sendbuffer = ByteBuffer.allocate(1024);
	public NIOReaderHandler(SelectionKey sk) {
		this.selectedKey = sk;
	}

	public void run() {
		readAndWrite();
	}

	private void readAndWrite() {
		// 读取客户端传过来的数据
		try {
			sockchennel = (SocketChannel) this.selectedKey.channel();
			ChannelInfo cInfo = ChannelInfo.getChannelInfo(this.sockchennel);
			ArrayList<ByteBuffer> bufferList = cInfo.getBufferList();
			int count = 0;
			if (sockchennel.isConnected()) {
				ByteBuffer receivebuffer = ByteBuffer.allocate(100);
				count = sockchennel.read(receivebuffer);
				if (count > 0) {
					// 追加到receive
					// buffer数组里，等找到两个连续的\r\n就结束，并把后面的数据丢弃掉(post数据流暂不支持)，然后处理写数据
					bufferList.add(receivebuffer);

					// 判断是否找到两个连续的\r\n，如果是则把后续的数据流丢弃掉，然后写响应
					String tmp = new String(receivebuffer.array(), 0, count);
					if (tmp.contains("\r\n\r\n")) {
						cInfo.setClientRequestReturnCount(2);
					} else if (tmp.endsWith("\r\n")) {
						cInfo.setClientRequestReturnCount(cInfo
								.getClientRequestReturnCount() + 1);
					} else {
						cInfo.setClientRequestReturnCount(cInfo
								.getClientRequestReturnCount() <= 0 ? 0 : cInfo
								.getClientRequestReturnCount() - 1);
					}

					// 找到\r\n\r\n
					if (2 == cInfo.getClientRequestReturnCount()) {
						// 将接受到的请求传服务器以获得相应的响应
						// System.out.println();
						byte[] response = getResponse(bufferList);
						sendbuffer.clear();
						sendbuffer.put(response);
						System.out.println(sockchennel.hashCode()+" :sockchennel");
						System.out.println(this.getName()+" :threadName");
						sendbuffer.flip();
						sockchennel.write(sendbuffer);

						// 写完之后清空读buffer list
						for (ByteBuffer bf : bufferList) {
							bf.clear();
						}
						bufferList.clear();
						cInfo.setClientRequestReturnCount(0);
						
					}

				}
				// -1是表明客户端关闭了连接,服务器端也关闭连接
				else if (count < 0) {
					System.out.println("read empty " + count);
					sockchennel.close();
					selectedKey.cancel();
				}
			}
			// 这行代码目前还从未走到过
		} catch (java.io.IOException e) {
			// handle io exception
		}
	}

	/**
	 * 根据请求与策略获取Response的入口
	 * @param bufferList：请求头
	 * @return：Response
	 */
	private byte[] getResponse(ArrayList<ByteBuffer> bufferList) {

		StringBuilder requestStr = new StringBuilder();
		
		//拿到请求的全部内容
		for (ByteBuffer bb : bufferList) {
			requestStr.append(new String(bb.array()));
		}
		//根据请求头得到请求对象
		HttpRequest re = Request.requestLoader(requestStr.toString());
		byte[] Response = null;
		try {
			Response = ResponseHandler.getResponse(re);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return Response;
	}
}
