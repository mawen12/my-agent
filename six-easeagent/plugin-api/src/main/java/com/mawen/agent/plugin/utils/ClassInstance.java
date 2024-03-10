package com.mawen.agent.plugin.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public abstract class ClassInstance<T> {
	private final Class<?> type;

	public ClassInstance() {
		var superClass = getClass().getGenericSuperclass();
		if (superClass instanceof Class<?>) {
			throw new IllegalArgumentException("Internal error: MetricInstance constructed without actual type information");
		}
		Type t = ((ParameterizedType) superClass).getActualTypeArguments()[0];
		if (!(t instanceof Class)) {
			throw new IllegalArgumentException("Internal error: MetricInstance constructed without actual type information");
		}
		type = (Class<?>) t;
	}

	public boolean isInstance(Object o) {
		return type.isInstance(o);
	}

	public T to(Object o) {
		return (T) o;
	}
}
