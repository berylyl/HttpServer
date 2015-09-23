package Http.Protocol;

/**
 * HTTP请求方法封装类
 * 
 * @author yinlu
 * 
 */
public class HttpMethod {
	String method = "";

	public HttpMethod() {
	}

	public void setMethod(String value) {
		this.method = value;
	}

	public String getMethod() {
		return this.method;
	}

	public static HttpMethod valueof(String value) {
		HttpMethod p = new HttpMethod();
		p.setMethod(value);
		return p;
	}

	public static enum RequestType {
		GET, POST, PURGE, DELETE, HEAD;
	}
}
