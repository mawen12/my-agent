package com.mawen.agent.mock.utils;

import java.util.Map;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public class MockSystemEnv {

	private static final Map<String, String> SETTER = getEnvironments();

	@SuppressWarnings("all")
	private static Map<String, String> getEnvironments() {
		try {
			var systemEnv = Thread.currentThread().getContextClassLoader().loadClass("com.mawen.agent.plugin.utils.SystemEnv");
			var environments = systemEnv.getDeclaredField("ENVIRONMENTS");
			environments.setAccessible(true);
			return (Map<String, String>) environments.get(null);
		}
		catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void set(String name, String value) {
		if (SETTER != null) {
			SETTER.put(name, value);
		}
	}

	public static void remove(String name) {
		if (SETTER != null) {
			SETTER.remove(name);
		}
	}
}
