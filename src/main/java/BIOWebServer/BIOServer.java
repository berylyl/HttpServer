package BIOWebServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

public class BIOServer extends Thread {
	private int port;
	static ServerSocket ss;
	static Socket so;

	private static Logger logger = Logger.getLogger(BIOServer.class);

	public BIOServer(int port) {
		this.port = port;
	}

	public void run() {

		try {
			ss = new ServerSocket(port);
			while (true) {
				so = ss.accept();// ͣס
				new BIOReaderHandler(so).start();
			}
		} catch (IOException e) {
			logger.error("BIOServer.run IOException:", e);
		}
	}

	public void closeServer() {
		try {
			SocketQueue.closeAllSocket();
			ss.close();
		} catch (IOException e) {
			logger.error("BIOServer.closeServer IOException:", e);
		}
	}
}
