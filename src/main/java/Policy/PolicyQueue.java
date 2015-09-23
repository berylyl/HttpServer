package Policy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import utility.ReadProperties;

import Policy.Handler.PolicyHandle;

/**
 * 策略队列类，并对策略队列中的策略对象提供增、删、查、队列持久化、策略加载等方法
 * 
 * @author yinlu
 * 
 */
public class PolicyQueue {

	private static Hashtable<String, Policy> globalPolicyTable = new Hashtable<String, Policy>();
	private static Hashtable<String, Policy> directoryPolicyTable = new Hashtable<String, Policy>();

	private static File policyFile = null;
	private static String policyFilePath = ReadProperties
			.getValue("PolicyFilePath");

	private static Logger logger = Logger.getLogger(PolicyQueue.class);

	/**
	 * 向策略队列中添加策略对象p
	 * 
	 * @param p
	 */
	public static void addPolicy(Policy p) {
		logger.debug("Enter the PolicyQueue.addPolicy method.");

		if (p.getType().equals(PolicyType.Global)) {
			globalPolicyTable.put(p.getKey().toUpperCase(), p);
			logger.info("Add a global Policy: key=" + p.getKey()
					+ ",value=" + p.getValue());
		} else {
			directoryPolicyTable.put(p.getKey().toUpperCase(), p);
			logger.info("Add a directory Policy: key=" + p.getKey()
					+ ",value=" + p.getValue());
		}
	}

	/**
	 * 清空某一类型的全部策略
	 * 
	 * @param pt
	 *            ：要清空的策略类型
	 */
	public static void clear(PolicyType pt) {
		if (pt.equals(PolicyType.Global)) {
			globalPolicyTable.clear();
			logger.info("The global Policy is emptied.");
		} else if (pt.equals(PolicyType.Global)) {
			directoryPolicyTable.clear();
			logger.info("The global Policy is emptied.");
		}
	}

	/**
	 * 从策略队列中移除指定策略对象p
	 * 
	 * @param p
	 */
	public static void removePolicy(PolicyType pt, String PolicyKey) {
		logger.debug("Enter the PolicyQueue.removePolicy method.");

		if (pt.equals(PolicyType.Global)) {
			globalPolicyTable.remove(PolicyKey.toUpperCase());
			logger.info("Remove a global Policy: " + PolicyKey.toUpperCase());
		} else if (pt.equals(PolicyType.Directory)) {
			directoryPolicyTable.remove(PolicyKey.toUpperCase());
			logger.info("Remove a directory Policy: " + PolicyKey);
		} else {
			logger.error("Not found Policy: " + PolicyKey);
		}
	}

	/**
	 * 返回所有Policy对象的List集合
	 */
	public static List<Policy> getAll() {
		logger.debug("Enter the PolicyQueue.List<Policy> method.");

		List<Policy> policyList = new ArrayList<Policy>();
		policyList.addAll(globalPolicyTable.values());
		policyList.addAll(directoryPolicyTable.values());

		return policyList;
	}

	/**
	 * 返回指定类型、指定key值的Policy对象
	 * 
	 * @param type
	 * @param key
	 * @return
	 */
	public static Policy getPolicy(PolicyType type, String key) {
		logger.debug("Enter the PolicyQueue.getPolicy method.");

		Policy policy = null;
		if (type.equals(PolicyType.Global)) {
			policy = globalPolicyTable.get(key);
		} else {
			policy = directoryPolicyTable.get(key);
		}

		return policy;
	}

	/**
	 * 将策略队列持久化到fileName对应的策略文件
	 * 
	 * @param fileName
	 */
	public static void saveToFile(String fileName) {
		logger.debug("Enter the PolicyQueue.saveToFile(String) method.");

		String policyStr = "Global\r\n";

		// 取出所有Global策略
		Iterator<String> it = globalPolicyTable.keySet().iterator();
		while (it.hasNext()) {
			String k = it.next();
			Policy p = globalPolicyTable.get(k);
			String key = p.getKey();
			String value = p.getValue();

			String policyRow = "\t" + key + " " + value + "\r\n";
			policyStr += policyRow;
		}

		// 取出所有directory策略
		it = directoryPolicyTable.keySet().iterator();
		while (it.hasNext()) {
			String k = it.next();
			Policy p = directoryPolicyTable.get(k);
			String key = p.getKey();
			String value = p.getValue();

			String policyRow = "\t" + key + " " + value + "\r\n";
			policyStr += policyRow;
		}

		// 将所有策略写入文件
		try {
			policyFile = new File(fileName);
			if (!policyFile.exists()) {
				policyFile.createNewFile();
			}

			BufferedWriter output = new BufferedWriter(new FileWriter(
					policyFile));
			output.write(policyStr);

			output.close();
		} catch (IOException e) {
			logger.error("PolicyQueue.saveToFile(String) IOException:", e);
		}

		logger.info("Save to the file successfully. The file path is "
				+ fileName);
	}

	/**
	 * 将策略队列持久化到默认路径的策略文件
	 */
	public static void saveToFile() {
		logger.debug("Enter the PolicyQueue.saveToFile method.");

		saveToFile(policyFilePath);
	}

	/**
	 * 将fileName指定的策略文件加载到策略队列
	 * 
	 * @param fileName
	 */
	public static void loadFromFile(String fileName) {
		logger.debug("Enter the addPolicy PolicyQueue.loadFromFile(String).");

		PolicyType type = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String policyRow = br.readLine();
			while (policyRow != null && !policyRow.trim().equals("")) {

				// 判断要创建的对象类型
				if (policyRow.equals("Global")) {
					type = PolicyType.Global;
					policyRow = br.readLine();
					continue;
				}
				// else if (policyRow.contains("/")) {
				// type = PolicyType.Directory;
				// policyRow = br.readLine();
				// continue;
				// }

				// 造策略字符串
				if (type.equals(PolicyType.Global)) {
					policyRow = "1 " + policyRow.trim();
				} else if (type.equals(PolicyType.Directory)) {
					policyRow = "2 " + policyRow.trim();
				} else {
					policyRow = "";
				}

				// 将字符串封装成Policy并添加到Policy队列中
				new PolicyHandle().handlePolicy(policyRow);
				policyRow = br.readLine();
			}

			br.close();
		} catch (FileNotFoundException e) {
			logger.error(
					"PolicyQueue.loadFromFile(String) FileNotFoundException:",
					e);
		} catch (IOException e) {
			logger.error("PolicyQueue.loadFromFile(String) IOException:", e);
		}

		logger.info("Load from the file successfully. The file path is "
				+ fileName);
	}

	/**
	 * 将默认路径的策略文件加载到策略队列
	 */
	public static void loadFromFile() {
		logger.debug("Enter the PolicyQueue.loadFromFile method.");
		loadFromFile(policyFilePath);
	}

}