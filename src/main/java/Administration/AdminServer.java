package Administration;

import org.apache.log4j.Logger;

import utility.ReadProperties;

import AIOWebServer.AioTcpServer;
import AIOWebServer.ConnectionHandler;
import Policy.PolicyQueue;

/**
 * Web Server�Ŀ����࣬����Web Server��������ֹͣ��������������
 * 
 * @author yinlu
 * 
 */
public class AdminServer {
	private static Logger logger = Logger.getLogger(AdminServer.class);
	private static AioTcpServer webServer = null;

	private AdminServer() {
		Runtime.getRuntime().addShutdownHook(new ShutdownHookThread());
	}

	/**
	 * Web Server����������
	 */
	@SuppressWarnings("deprecation")
	public void restart() {
		stop();
		start();
	}

	/**
	 * Web Server����
	 */
	public void start() {
		int webPort = Integer.valueOf(ReadProperties
				.getValue("WebServerPort"));
		try{
			webServer = new AioTcpServer(webPort);
			
			webServer.start();
			
			//������ʱ������Գ�ʱ���Խ��д���
			ConnectionHandler.setConnCloseTimer();
			
			logger.info("Started Web Server.");
		}catch(Exception e){
			logger.error("Web server started failed",e);
		}
		
	}

	/**
	 * Web Serverֹͣ
	 */
	@SuppressWarnings("deprecation")
	public void stop() {
		webServer.closeServer();
		
		//�رճ�ʱ��ʱ��
		ConnectionHandler.closeTimer();
		
		webServer.stop();

		logger.info("Stop Web Server.");
	}

	/**
	 * �������
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		AdminServer as = new AdminServer();
		//�ӱ��������ļ���ȡ������Ϣ
		PolicyQueue.loadFromFile();
		
		int controlPort = Integer.valueOf(ReadProperties
		.getValue("ControlServerPort"));
		
		//�������ƶ�web server
		new NIOControlServer(controlPort, as).start();
		
		//���������web server
		as.start();

	}
}
