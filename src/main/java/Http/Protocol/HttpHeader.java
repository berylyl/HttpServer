package Http.Protocol;

import java.util.Hashtable;

/**
 * HTTP请求头的封装类
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
	 * 添加请求头，Connection:keep-alive,key=Connection value=keep-alive,不包含空格和冒号
	 * 
	 * @param key
	 * @param value
	 */
	public void addHeader(String key, String value) {
		keyvalues.put(key, value);
	}

	/**
	 * 添加请求头：value=Connection:keep-alive
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
