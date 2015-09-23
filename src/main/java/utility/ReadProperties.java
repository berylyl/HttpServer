package utility;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

public class ReadProperties {
	private static ResourceBundle resourceBundle = ResourceBundle
			.getBundle("config/Config");
	private static Logger logger = Logger.getLogger(ReadProperties.class);

	public static String getValue(String key) {
		if (key == null || key.equals("") || key.equals("null")) {
			return "";
		}
		String result = "";
		try {
			result = resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			logger.error("Not exist configuration items:" + key);
		}
		return result;
	}
}
