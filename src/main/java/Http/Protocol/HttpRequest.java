package Http.Protocol;

/**
 * HTTP请求的封装类
 * 
 * @author yinlu
 * 
 */
public class HttpRequest {
	private HttpHeader header = new HttpHeader();

	public void setHttpHeader(HttpHeader header) {
		this.header = header;
	}

	public HttpHeader getHttpHeader() {
		return this.header;
	}
}
