package com.mawen.agent.config;

import java.util.function.BiFunction;

import com.mawen.agent.plugin.api.config.Config;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public class AutoRefreshConfigItem<T> {

	private volatile T value;

	public AutoRefreshConfigItem(Config config, String name, BiFunction<Config, String, T> func) {
		ConfigUtils.bindProp(name, config, func, v -> this.value = v);
	}

	public T getValue() {
		return value;
	}
}
