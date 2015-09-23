package NIOWebServer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
public class ChannelInfo {
	
	private static Hashtable<SocketChannel,ChannelInfo>  ChannelInfoTable = new Hashtable<SocketChannel,ChannelInfo>();
	private ArrayList<ByteBuffer> bufferList = new ArrayList<ByteBuffer>();
	private static Hashtable<SocketChannel,ArrayList<ByteBuffer>>  BufferListTable = new Hashtable<SocketChannel,ArrayList<ByteBuffer>>();

	//��clientд��֮ǰ����client��ȡ��\r\n����Ŀ���ж�client�����ʱ������
	private int clientRequestReturnCount = 0;
	
	//�Ѿ������client�����ۼ���Ŀ
	private int clientRequestTotalCount = 0;
	
	//�������һ���������Ӧʱ��
	private Date lastResponseTime = null;
	
	
	
	

	public int getClientRequestReturnCount() {
		return clientRequestReturnCount;
	}

	public void setClientRequestReturnCount(int clientRequestReturnCount) {
		this.clientRequestReturnCount = clientRequestReturnCount;
	}

	public int getClientRequestTotalCount() {
		return clientRequestTotalCount;
	}

	public void setClientRequestTotalCount(int clientRequestTotalCount) {
		this.clientRequestTotalCount = clientRequestTotalCount;
	}

	public Date getLastResponseTime() {
		return lastResponseTime;
	}

	public void setLastResponseTime(Date lastResponseTime) {
		this.lastResponseTime = lastResponseTime;
	}

	/**
	 * ���һ��client������SocketChannel����Ϊ������һ���µ�ChannelInfo����
	 * @param sockChannel
	 */
	public static void addChannel(SocketChannel sockChannel){
		ChannelInfo info = new ChannelInfo();
		ChannelInfoTable.put(sockChannel, info);
	}
	
	/**
	 * ����SocketChannel���󣬻�ȡ��֮��Ӧ��ChannelInfo����
	 * @param channel
	 * @return
	 */
	public static ChannelInfo getChannelInfo(SocketChannel channel){
		return ChannelInfoTable.get(channel);
	}

	public ArrayList<ByteBuffer> getBufferList() {
		return bufferList;
	}

	

}
