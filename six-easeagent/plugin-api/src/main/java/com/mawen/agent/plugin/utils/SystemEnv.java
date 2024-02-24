package com.mawen.agent.plugin.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class SystemEnv {
	private static final Map<String, String> ENVIRONMENTS = new ConcurrentHashMap<>();

	public static String get(String name) {
		String value = ENVIRONMENTS.get(name);
		if (value != null) {
			return value;
		}
		String result = System.getenv(name);
		if (result == null) {
			return null;
		}
		synchronized (ENVIRONMENTS) {
			value = ENVIRONMENTS.get(name);
			if (value != null) {
				return value;
			}
			value = result;
			ENVIRONMENTS.put(name, value);
			return value;
		}
	}

	public static void set(String name, String value) {
		ENVIRONMENTS.put(name, value);
	}
}
