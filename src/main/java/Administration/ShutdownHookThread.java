package Administration;

import Policy.PolicyQueue;

/**
 * ������Ctrl+C�˳�ʱ��������Ե������ļ�
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