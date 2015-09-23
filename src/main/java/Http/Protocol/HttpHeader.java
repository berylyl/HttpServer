package Http.Protocol;

import java.util.Hashtable;

/**
 * HTTP����ͷ�ķ�װ��
 * 
 * @author yinlu
 * 
 */
public class HttpHeader {
	private HttpMethod method = new HttpMethod();
	private HttpProtocol protocol = new HttpProtocol();
	private String uri = "";
	private Hashtable<String, String> keyvalues = new Hashtable<String, String>();

	/**
	 * �������ͷ��Connection:keep-alive,key=Connection value=keep-alive,�������ո��ð��
	 * 
	 * @param key
	 * @param value
	 */
	public void addHeader(String key, String value) {
		keyvalues.put(key, value);
	}

	/**
	 * �������ͷ��value=Connection:keep-alive
	 * 
	 * @param value
	 */
	public void addHeader(String value) {

	}

	public Hashtable<String, String> getHeaders() {
		return this.keyvalues;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public HttpMethod getMethod() {
		return this.method;
	}

	public void setURI(String uri) {
		this.uri = uri;
	}

	public String getURI() {
		return this.uri;
	}

	public HttpProtocol getProtocol() {
		return this.protocol;
	}

	public void setProtocol(HttpProtocol p) {
		this.protocol = p;
	}
}
