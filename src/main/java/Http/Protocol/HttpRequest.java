package Http.Protocol;

/**
 * HTTP����ķ�װ��
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
