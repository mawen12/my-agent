package com.mawen.agent.report.plugin;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mawen.agent.plugin.report.Call;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/29
 */
public class NoOpCall<V> implements Call<V> {

	private static final Map<Class<?>, NoOpCall> INSTANCE_MAP = new ConcurrentHashMap<>();

	public static <T> NoOpCall<T> getInstance(Class<?> clazz) {
		NoOpCall<T> b = INSTANCE_MAP.get(clazz);
		if (b != null) {
			return b;
		}
		b = new NoOpCall<>();
		INSTANCE_MAP.put(clazz, b);
		return b;
	}

	@Override
	public V execute() throws IOException {
		return null;
	}
}
