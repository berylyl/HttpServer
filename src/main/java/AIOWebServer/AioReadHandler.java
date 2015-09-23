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
	 * ��Ҫ����һ�³���1K�����󣬷ֶ�ζ�ȡʱ�������Ƿ�����
	 */
	public void completed(Integer i, ByteBuffer buf) {
		if (i > 0) {
			buf.flip();
			try {
				String requestContent = decoder.decode(buf).toString();
				logger.info("�յ�" + socket.getRemoteAddress().toString()
						+ "����Ϣ:" + requestContent);
				buf.compact();

				// �ϲ������ӵ�����
				String fullRequest = SocketQueue.combineClientRequest(socket,
						requestContent);
				// ���յ�һ���������󣬴�����Ӧ��Ѹ����ӵ��������
				if (!fullRequest.isEmpty() && fullRequest.endsWith("\r\n\r\n")) {
					// ��������ͷ�õ��������
					HttpRequest hr = Request.requestLoader(fullRequest);
					byte[] Obj = getResponse(hr);

					// ����response
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
		// chrome �ȴ�10����߹ر�tab����뵽���ie8����
		else if (i == -1) {
			try {
				System.out.println("�ͻ��˶���:"
						+ socket.getRemoteAddress().toString());
				buf = null;
				SocketQueue.removeClientConnection(socket);
			} catch (IOException e) {
				logger.error("IOException in read completed event", e);
			}
		}
	}

	/**
	 * ��֪����� 1.
	 * ����йر����ӵĲ��ԣ��ᵼ�±����������ã��׳������쳣��java.nio.channels.ClosedChannelException
	 * δ֪����� 1. chrome�Ĺرղ���������ie8�ر������������������յ��쳣��java.io.IOException:
	 * ָ�������������ٿ��á�
	 */
	public void failed(Throwable exc, ByteBuffer buf) {
		// �Է�����rst�ر�����ʱ����������������ƥ�䵽IOException:���������ٿ��õĴ��󣬰������ͷŵ�
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
		//��⵽�������ҷ������رյģ������첽��ԭ�����ӹر�ʱ��Ȼ�п��ܽ������release ����
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
