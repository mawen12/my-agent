package com.mawen.agent.log4j2.api;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public record Mdc(BiFunction<String, String, Void> putFunction, Function<String, Void> removeFunction, Function<String, String> getFunction) {

	public void put(String key, String value) {
		putFunction.apply(key, value);
	}

	public void remove(String key) {
		removeFunction.apply(key);
	}

	public String get(String key) {
		return getFunction.apply(key);
	}
}
