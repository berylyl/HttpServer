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

	//对client写入之前，从client读取的\r\n的数目，判断client请求何时结束了
	private int clientRequestReturnCount = 0;
	
	//已经处理的client请求累计数目
	private int clientRequestTotalCount = 0;
	
	//处理最后一个请求的响应时间
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
	 * 添加一个client交互的SocketChannel，并为它创建一个新的ChannelInfo对象
	 * @param sockChannel
	 */
	public static void addChannel(SocketChannel sockChannel){
		ChannelInfo info = new ChannelInfo();
		ChannelInfoTable.put(sockChannel, info);
	}
	
	/**
	 * 根据SocketChannel对象，获取与之对应的ChannelInfo对象
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
