package Policy;

/**
 * �������͵�ö��
 * 
 * @author wb-zhoujiankai
 * 
 */
public enum PolicyType {

	Global(1), Directory(2);
	private PolicyType(int va) {
		this.value = va;
	}

	private int value;

	public int getValue() {
		return this.value;
	}

}
