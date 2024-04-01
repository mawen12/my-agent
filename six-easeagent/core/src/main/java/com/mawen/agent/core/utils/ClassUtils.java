package com.mawen.agent.core.utils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/31
 */
public abstract class ClassUtils {

	public static final String ARRAY_SUFFIX = "[]";

	private static final String INTERNAL_ARRAY_PREFIX = "[";

	private static final String NON_PRIMITIVE_ARRAY_PREFIX = "[L";

	private static final Class<?>[] EMPTY_CLASS_ARRAY = {};

	private static final char PACKAGE_SEPARATOR = '.';

	private static final char PATH_SEPARATOR = '/';

	private static final char NESTED_CLASS_SEPARATOR = '$';

	public static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		}
		catch (Throwable ignored) {
		}
		if (cl == null) {
			cl = ClassUtils.class.getClassLoader();
			if (cl == null) {
				try {
					cl = ClassLoader.getSystemClassLoader();
				}
				catch (Throwable ignored) {
				}
			}
		}
		return cl;
	}
}
