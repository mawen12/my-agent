package com.mawen.agent.config;

import java.util.Locale;

import com.mawen.agent.plugin.utils.SystemEnv;

/**
 * Get config from system properties or environment variables.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/26
 */
public final class ConfigPropertiesUtils {

	private ConfigPropertiesUtils() {}

	public static boolean getBoolean(String propertyName, boolean defaultValue) {
		var strValue = getString(propertyName);
		return strValue == null ? defaultValue : Boolean.parseBoolean(strValue);
	}

	public static int getInt(String propertyName, int defaultValue) {
		var strValue = getString(propertyName);
		if (strValue == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(strValue);
		}
		catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static String getString(String propertyName) {
		var value = System.getProperty(propertyName);
		if (value != null) {
			return value;
		}
		return SystemEnv.get(toEnvVarName(propertyName));
	}

	public static String toEnvVarName(String propertyName) {
		return propertyName.toUpperCase(Locale.ROOT).replace('-', '_').replace('.', '_');
	}
}
