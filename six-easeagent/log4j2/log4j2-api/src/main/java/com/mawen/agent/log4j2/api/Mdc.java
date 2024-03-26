package com.mawen.agent.log4j2.api;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public class Mdc {
	private  final BiFunction<String, String, Void> putFunction;
	private final Function<String, Void> removeFunction;
	private final Function<String, String> getFunction;

	public Mdc(BiFunction<String, String, Void> putFunction, Function<String, Void> removeFunction, Function<String, String> getFunction) {
		this.putFunction = putFunction;
		this.removeFunction = removeFunction;
		this.getFunction = getFunction;
	}

	public BiFunction<String, String, Void> putFunction() {
		return putFunction;
	}

	public Function<String, Void> removeFunction() {
		return removeFunction;
	}

	public Function<String, String> getFunction() {
		return getFunction;
	}

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
