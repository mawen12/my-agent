package com.mawen.agent.mock.log4j2;

import java.net.URL;

import com.mawen.agent.log4j2.ClassLoaderUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public class AllUrlsSupplier implements UrlSupplier{
	public static final String USE_ENV = "AGENT_SLF4J2-USE-CURRENT";
	private static volatile boolean enabled = false;

	public static void setEnabled(boolean enabled) {
		AllUrlsSupplier.enabled = enabled;
	}

	@Override
	public URL[] get() {
		if (!enabled()) {
			return new URL[0];
		}
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		return ClassLoaderUtils.getAllURLs(classLoader);
	}

	private boolean enabled() {
		if (enabled) {
			return true;
		}
		String enabledStr = System.getProperty(USE_ENV);
		if (enabledStr == null) {
			return false;
		}
		try {
			return Boolean.parseBoolean(enabledStr);
		}
		catch (Exception e) {
			return false;
		}
	}
}
