package Policy;

/**
 * �������࣬�������Ե�Key��Value
 * 
 * @author wb-zhoujiankai
 * 
 */
public class PolicyItem implements Policy {
	private String key = "";
	private String value = "";

	public PolicyItem() {
		super();
	}

	public PolicyItem(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}

	// @Override
	public PolicyType getType() {
		return null;
	}

}
