package Administration;

import Policy.PolicyQueue;

/**
 * 当程序Ctrl+C退出时，保存策略到本地文件
 * 
 * @author yinlu
 * 
 */
public class ShutdownHookThread extends Thread {

	@Override
	public void run() {
		PolicyQueue.saveToFile();
	}

}