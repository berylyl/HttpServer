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
 * ���Զ����࣬���Բ��Զ����еĲ��Զ����ṩ����ɾ���顢���г־û������Լ��صȷ���
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
	 * ����Զ�������Ӳ��Զ���p
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
	 * ���ĳһ���͵�ȫ������
	 * 
	 * @param pt
	 *            ��Ҫ��յĲ�������
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
	 * �Ӳ��Զ������Ƴ�ָ�����Զ���p
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
	 * ��������Policy�����List����
	 */
	public static List<Policy> getAll() {
		logger.debug("Enter the PolicyQueue.List<Policy> method.");

		List<Policy> policyList = new ArrayList<Policy>();
		policyList.addAll(globalPolicyTable.values());
		policyList.addAll(directoryPolicyTable.values());

		return policyList;
	}

	/**
	 * ����ָ�����͡�ָ��keyֵ��Policy����
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
	 * �����Զ��г־û���fileName��Ӧ�Ĳ����ļ�
	 * 
	 * @param fileName
	 */
	public static void saveToFile(String fileName) {
		logger.debug("Enter the PolicyQueue.saveToFile(String) method.");

		String policyStr = "Global\r\n";

		// ȡ������Global����
		Iterator<String> it = globalPolicyTable.keySet().iterator();
		while (it.hasNext()) {
			String k = it.next();
			Policy p = globalPolicyTable.get(k);
			String key = p.getKey();
			String value = p.getValue();

			String policyRow = "\t" + key + " " + value + "\r\n";
			policyStr += policyRow;
		}

		// ȡ������directory����
		it = directoryPolicyTable.keySet().iterator();
		while (it.hasNext()) {
			String k = it.next();
			Policy p = directoryPolicyTable.get(k);
			String key = p.getKey();
			String value = p.getValue();

			String policyRow = "\t" + key + " " + value + "\r\n";
			policyStr += policyRow;
		}

		// �����в���д���ļ�
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
	 * �����Զ��г־û���Ĭ��·���Ĳ����ļ�
	 */
	public static void saveToFile() {
		logger.debug("Enter the PolicyQueue.saveToFile method.");

		saveToFile(policyFilePath);
	}

	/**
	 * ��fileNameָ���Ĳ����ļ����ص����Զ���
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

				// �ж�Ҫ�����Ķ�������
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

				// ������ַ���
				if (type.equals(PolicyType.Global)) {
					policyRow = "1 " + policyRow.trim();
				} else if (type.equals(PolicyType.Directory)) {
					policyRow = "2 " + policyRow.trim();
				} else {
					policyRow = "";
				}

				// ���ַ�����װ��Policy����ӵ�Policy������
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
	 * ��Ĭ��·���Ĳ����ļ����ص����Զ���
	 */
	public static void loadFromFile() {
		logger.debug("Enter the PolicyQueue.loadFromFile method.");
		loadFromFile(policyFilePath);
	}

}