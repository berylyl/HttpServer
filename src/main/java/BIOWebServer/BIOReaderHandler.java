package BIOWebServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.Logger;

import Http.Protocol.HttpRequest;
import NIOWebServer.Request;
import Policy.Handler.ResponseHandler;

public class BIOReaderHandler extends Thread {
	private Socket so;
	private DataInputStream dis;
	private DataOutputStream dos;

	private static Logger logger = Logger.getLogger(BIOReaderHandler.class);

	public BIOReaderHandler(Socket so) {
		this.so = so;
	}

	public void run() {

		try {
			dis = new DataInputStream(so.getInputStream());
			dos = new DataOutputStream(so.getOutputStream());
			while (true) {
				byte[] b = new byte[1024];
				int i = -1;
				try {
					i = dis.read(b);
				} catch (SocketException e) {
					logger.warn(e.getMessage());
					break;
				}

				String temp = "";
				while (true) {
					temp = temp + new String(b);
					if(i < 0){
						break;
					}else if(temp.contains("\r\n\r\n")){
						temp = temp.substring(0, temp.indexOf("\r\n\r\n"));
						break;
					}
					i = dis.read(b);
				}
				if (!temp.equals("")) {
					// 根据请求头得到请求对象
					HttpRequest hr = Request.requestLoader(temp);
					byte[] Obj = getResponse(hr);
					if (!so.isClosed()) {
						dos.write(Obj);// 返回组成的结果
					}
					ConnectionHandler.keepAlive(hr, so);

					String URL = "http://"
							+ hr.getHttpHeader().getHeaders().get("Host")
							+ hr.getHttpHeader().getURI();
					String response = new String(Obj);
					String responseCode = response.substring(
							response.indexOf(" ") + 1,
							response.indexOf(" ") + 4);
					logger.info("URL=" + URL + ",ResponseCode=" + responseCode);
				}
			}

		} catch (Exception e) {
			logger.error("BIOReaderHandler.run Exception:", e);
		}
	}

	private byte[] getResponse(HttpRequest re) {

		byte[] Response = null;
		try {
			Response = ResponseHandler.getResponse(re);
		} catch (Exception e) {
			logger.error("BIOReaderHandler.getResponse Exception:", e);
		}

		return Response;
	}
}
