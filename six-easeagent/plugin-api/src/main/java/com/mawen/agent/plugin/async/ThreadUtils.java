package com.mawen.agent.plugin.async;

import java.util.function.Supplier;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class ThreadUtils {

	private ThreadUtils() {

	}

	public static <V> V callWithClassLoader(ClassLoader use, Supplier<V> runnable) {
		var old = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(use);
			return runnable.get();
		} finally {
			Thread.currentThread().setContextClassLoader(old);
		}
	}
}
