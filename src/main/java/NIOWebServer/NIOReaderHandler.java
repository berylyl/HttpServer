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
		// ��ȡ�ͻ��˴�����������
		try {
			sockchennel = (SocketChannel) this.selectedKey.channel();
			ChannelInfo cInfo = ChannelInfo.getChannelInfo(this.sockchennel);
			ArrayList<ByteBuffer> bufferList = cInfo.getBufferList();
			int count = 0;
			if (sockchennel.isConnected()) {
				ByteBuffer receivebuffer = ByteBuffer.allocate(100);
				count = sockchennel.read(receivebuffer);
				if (count > 0) {
					// ׷�ӵ�receive
					// buffer��������ҵ�����������\r\n�ͽ��������Ѻ�������ݶ�����(post�������ݲ�֧��)��Ȼ����д����
					bufferList.add(receivebuffer);

					// �ж��Ƿ��ҵ�����������\r\n���������Ѻ�������������������Ȼ��д��Ӧ
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

					// �ҵ�\r\n\r\n
					if (2 == cInfo.getClientRequestReturnCount()) {
						// �����ܵ������󴫷������Ի����Ӧ����Ӧ
						// System.out.println();
						byte[] response = getResponse(bufferList);
						sendbuffer.clear();
						sendbuffer.put(response);
						System.out.println(sockchennel.hashCode()+" :sockchennel");
						System.out.println(this.getName()+" :threadName");
						sendbuffer.flip();
						sockchennel.write(sendbuffer);

						// д��֮����ն�buffer list
						for (ByteBuffer bf : bufferList) {
							bf.clear();
						}
						bufferList.clear();
						cInfo.setClientRequestReturnCount(0);
						
					}

				}
				// -1�Ǳ����ͻ��˹ر�������,��������Ҳ�ر�����
				else if (count < 0) {
					System.out.println("read empty " + count);
					sockchennel.close();
					selectedKey.cancel();
				}
			}
			// ���д���Ŀǰ����δ�ߵ���
		} catch (java.io.IOException e) {
			// handle io exception
		}
	}

	/**
	 * ������������Ի�ȡResponse�����
	 * @param bufferList������ͷ
	 * @return��Response
	 */
	private byte[] getResponse(ArrayList<ByteBuffer> bufferList) {

		StringBuilder requestStr = new StringBuilder();
		
		//�õ������ȫ������
		for (ByteBuffer bb : bufferList) {
			requestStr.append(new String(bb.array()));
		}
		//��������ͷ�õ��������
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
