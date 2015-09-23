package Administration;

import org.apache.log4j.Logger;

import utility.ReadProperties;

import AIOWebServer.AioTcpServer;
import AIOWebServer.ConnectionHandler;
import Policy.PolicyQueue;

/**
 * Web Server的控制类，进行Web Server的启动、停止与重新启动操作
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
	 * Web Server的重新启动
	 */
	@SuppressWarnings("deprecation")
	public void restart() {
		stop();
		start();
	}

	/**
	 * Web Server启动
	 */
	public void start() {
		int webPort = Integer.valueOf(ReadProperties
				.getValue("WebServerPort"));
		try{
			webServer = new AioTcpServer(webPort);
			
			webServer.start();
			
			//启动定时器，针对超时策略进行处理
			ConnectionHandler.setConnCloseTimer();
			
			logger.info("Started Web Server.");
		}catch(Exception e){
			logger.error("Web server started failed",e);
		}
		
	}

	/**
	 * Web Server停止
	 */
	@SuppressWarnings("deprecation")
	public void stop() {
		webServer.closeServer();
		
		//关闭超时定时器
		ConnectionHandler.closeTimer();
		
		webServer.stop();

		logger.info("Stop Web Server.");
	}

	/**
	 * 启动入口
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		AdminServer as = new AdminServer();
		//从本地配置文件读取策略信息
		PolicyQueue.loadFromFile();
		
		int controlPort = Integer.valueOf(ReadProperties
		.getValue("ControlServerPort"));
		
		//启动控制端web server
		new NIOControlServer(controlPort, as).start();
		
		//启动服务端web server
		as.start();

	}
}
