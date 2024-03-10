package com.mawen.agent.plugin.api.config;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * AutoRefreshConfig Supplier
 *
 * @param <T> the type of Config by this Supplier
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public abstract class AutoRefreshConfigSupplier<T extends AutoRefreshPluginConfig> {
	private final Type type;

	public AutoRefreshConfigSupplier() {
		var superClass = getClass().getGenericSuperclass();
		if (superClass instanceof Class<?>) { // sanity check, should never happen
			throw new IllegalArgumentException("Internal error: TypeReference constructed without actual type information");
		}
		type = ((ParameterizedType)superClass).getActualTypeArguments()[0];
	}

	/**
	 * the type of AutoRefreshConfig
	 *
	 * @return {@link Type}
	 */
	public Type getType() {
		return type;
	}

	/**
	 * new a AutoRefreshConfig
	 *
	 * @return AutoRefreshConfig
	 */
	public abstract T newInstance();
}
