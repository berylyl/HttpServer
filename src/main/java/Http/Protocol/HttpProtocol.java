package Http.Protocol;

/**
 * HTTP请求协议封装类
 * 
 * @author yinlu
 * 
 */
public class HttpProtocol {

	String version = "";

	public HttpProtocol() {
	}

	public void setVersion(String value) {
		this.version = value;
	}

	public String getVersion() {
		return this.version;
	}

	public static HttpProtocol valueof(String value) {
		HttpProtocol p = new HttpProtocol();
		p.setVersion(value);
		return p;
	}

	public enum HttpVersion {
		Http0(1.0), Http1(1.1);

		private double ver;

		private HttpVersion(double ver) {
			this.ver = ver;
		}

		public String getValue() {
			return "HTTP/".concat(String.valueOf(this.ver));
		}
	}
}
