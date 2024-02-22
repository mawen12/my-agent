package com.mawen.agent.plugin.api;

/**
 * A Context remains in the session it was bounded to until business finish.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public interface Context {

	boolean isNoop();

	Tracing currentTracing();

	<V> V get(Object key);

	<V> V remove(Object key);

	<V> V put(Object key, V value);

	IPluginConfig getConfig();

	int enter(Object key);

	default boolean entry(Object key, int times) {
		return enter(key) == times;
	}

	int exit(Object key);

	default boolean exit(Object key, int times) {
		return exit(key) == times;
	}

	AsyncContext exportAsync();
}
