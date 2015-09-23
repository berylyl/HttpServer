package AIOWebServer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.concurrent.ExecutionException;
import java.lang.InterruptedException;
import org.apache.log4j.Logger;

import Http.Protocol.HttpRequest;
import NIOWebServer.Request;
import Policy.Handler.ResponseHandler;

public class AioReadHandler implements CompletionHandler<Integer, ByteBuffer> {
	private AsynchronousSocketChannel socket;
	private static Logger logger = Logger.getLogger(AioReadHandler.class);

	public AioReadHandler(AsynchronousSocketChannel socket) {
		this.socket = socket;
	}

	public void cancelled(ByteBuffer attachment) {
		logger.warn("read cancelled");
	}

	private CharsetDecoder decoder = Charset.forName("GBK").newDecoder();

	/**
	 * 需要测试一下超过1K的请求，分多次读取时，内容是否完整
	 */
	public void completed(Integer i, ByteBuffer buf) {
		if (i > 0) {
			buf.flip();
			try {
				String requestContent = decoder.decode(buf).toString();
				logger.info("收到" + socket.getRemoteAddress().toString()
						+ "的消息:" + requestContent);
				buf.compact();

				// 合并该链接的请求
				String fullRequest = SocketQueue.combineClientRequest(socket,
						requestContent);
				// 接收到一个完整请求，处理响应后把该链接的请求清空
				if (!fullRequest.isEmpty() && fullRequest.endsWith("\r\n\r\n")) {
					// 根据请求头得到请求对象
					HttpRequest hr = Request.requestLoader(fullRequest);
					byte[] Obj = getResponse(hr);

					// 返回response
					try {
						socket.write(ByteBuffer.wrap(Obj)).get();
					} catch (ExecutionException e) {
						e.printStackTrace();
					} catch (InterruptedException ep) {
						ep.printStackTrace();
					} finally {
						SocketQueue.clearClientRequest(socket);
					}
					ConnectionHandler.closeConnectionByRequestTimes(hr, socket);
					String URL = "http://"
							+ hr.getHttpHeader().getHeaders().get("Host")
							+ hr.getHttpHeader().getURI();
					logger.info("URL=" + URL);
				}
			} catch (CharacterCodingException e) {
				logger.error("readhandler exception", e);
			} catch (IOException e) {
				logger.error("readhandler exception", e);
			}
			socket.read(buf, buf, this);
		}
		// chrome 等待10秒或者关闭tab会进入到这里，ie8不会
		else if (i == -1) {
			try {
				System.out.println("客户端断线:"
						+ socket.getRemoteAddress().toString());
				buf = null;
				SocketQueue.removeClientConnection(socket);
			} catch (IOException e) {
				logger.error("IOException in read completed event", e);
			}
		}
	}

	/**
	 * 已知情况： 1.
	 * 如果有关闭连接的策略，会导致本函数被调用，抛出如下异常：java.nio.channels.ClosedChannelException
	 * 未知情况： 1. chrome的关闭不会进入这里。ie8关闭浏览器，会造成这里收到异常：java.io.IOException:
	 * 指定的网络名不再可用。
	 */
	public void failed(Throwable exc, ByteBuffer buf) {
		// 对方发送rst关闭连接时会进入这里，本条件是匹配到IOException:网络名不再可用的错误，把链接释放掉
		if (exc.getStackTrace()[0].getClassName().equals("sun.nio.ch.Iocp")
				&& exc.getStackTrace()[0].getMethodName().equals(
						"translateErrorToIOException")) {
			try {
				logger
				.warn("connection not closed normally. release this connection!"
						+ socket.getRemoteAddress().toString());
				SocketQueue.closeConnection(socket);
				
			} catch (IOException e) {
				logger.error("IOException:", e);
			}

		}
		//检测到连接是我方主动关闭的，由于异步的原因，连接关闭时依然有可能进入这里，release 连接
		else if(exc.getStackTrace()[0].getClassName().equals("java.nio.channels.ClosedChannelException")){
			try {
				logger
						.warn("connection already closed . release this connection!"
								+ socket.getRemoteAddress().toString());
				SocketQueue.closeConnection(socket);
			} catch (IOException e) {
				logger.error("IOException:", e);
			}
		}

	}

	private byte[] getResponse(HttpRequest re) {

		byte[] Response = null;
		try {
			Response = ResponseHandler.getResponse(re);
		} catch (Exception e) {
			logger.error("AioReaderHandler.getResponse Exception:", e);
		}
		return Response;
	}
}
