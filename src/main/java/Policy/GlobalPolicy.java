package Policy;

/**
 * ȫ�ֲ�����
 * 
 * @author yinlu
 * 
 */
public class GlobalPolicy extends PolicyItem {
	private PolicyType type = PolicyType.Global;

	public GlobalPolicy(String key, String value) {
		super(key, value);
	}

	public PolicyType getType() {
		return type;
	}

}
