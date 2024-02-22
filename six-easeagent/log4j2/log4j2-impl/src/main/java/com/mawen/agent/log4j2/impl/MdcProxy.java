package com.mawen.agent.log4j2.impl;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.slf4j.MDC;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public class MdcProxy {
	public static final MdcPut PUT_INSTANCE = new MdcPut();
	public static final MdcRemove REMOVE_INSTANCE = new MdcRemove();
	public static final MdcGet GET_INSTANCE = new MdcGet();

	private static class MdcPut implements BiFunction<String, String, Void> {
		@Override
		public Void apply(String key, String value) {
			MDC.put(key, value);
			return null;
		}
	}

	private static class MdcRemove implements Function<String, Void> {
		@Override
		public Void apply(String key) {
			MDC.remove(key);
			return null;
		}
	}

	private static class MdcGet implements Function<String, String> {
		@Override
		public String apply(String key) {
			return MDC.get(key);
		}
	}

}
