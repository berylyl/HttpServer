package AIOWebServer;

import java.io.IOException;  
import java.nio.ByteBuffer;  
import java.nio.channels.AsynchronousServerSocketChannel;  
import java.nio.channels.AsynchronousSocketChannel;  
import java.nio.channels.CompletionHandler;  
import java.util.concurrent.ExecutionException;  
import java.util.concurrent.Future;  

import org.apache.log4j.Logger;
  
public class AioAcceptHandler implements CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {  
	private static Logger logger = Logger.getLogger(AioAcceptHandler.class);
	
	public void cancelled(AsynchronousServerSocketChannel attachment) {  
		logger.warn("connection cancelled by peer");  
    }  
  
    public void completed(AsynchronousSocketChannel socket, AsynchronousServerSocketChannel attachment) {  
        try {  
        	SocketQueue.addClientConnection(socket);
        	//�˷����е�ݹ����˼.Ŀ���Ǽ��������˿�,��channelGroup����ִ��.  
            attachment.accept(attachment, this);
            logger.info("�пͻ�������:" + socket.getRemoteAddress().toString()); 
            startRead(socket);
        } catch (IOException e) {  
        	logger.error("accept connection failed at completed event",e);  
        }  
    }  
  
    public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) { 
    	logger.warn("connection failed exception!");
    }  
  
    public void startRead(AsynchronousSocketChannel socket) {  
        ByteBuffer clientBuffer = ByteBuffer.allocate(1024);  
        try {
        socket.read(clientBuffer, clientBuffer, new AioReadHandler(socket));  
        } catch (Exception e) {  
        	logger.error("read error",e);   
        }  
    }  
} 