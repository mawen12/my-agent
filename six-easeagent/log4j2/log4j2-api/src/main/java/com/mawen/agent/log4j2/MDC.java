package com.mawen.agent.log4j2;

import com.mawen.agent.log4j2.api.Mdc;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public class MDC {
	private static final Mdc MDC_I = LoggerFactory.FACTORY.mdc();

	public static void put(String key, String value) {
		MDC_I.put(key,value);
	}

	public static void remove(String key) {
		MDC_I.remove(key);
	}

	public static String get(String key) {
		return MDC_I.get(key);
	}
}
