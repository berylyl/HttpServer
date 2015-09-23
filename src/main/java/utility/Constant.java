/**
 * 定义GotIt所需的常量
 */
package utility;

public class Constant {
	
	//依据请求条数关闭连接的阈值
	public static int reqTimes = -1;
	
	//依据时间关闭连接的阈值
	public static int reqTimeout = -1;
	
	//协议
	public static String PROTOCOL = "HTTP/1.1";
	
	//http协议的请求结束标志
	public static String endFlag = "\r\n\r\n";
	
	//请求头设置策略的关键字
	public static String setPolicy = "set-policy";
}
