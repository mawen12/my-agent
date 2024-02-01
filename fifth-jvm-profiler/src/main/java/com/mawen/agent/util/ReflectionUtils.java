package com.mawen.agent.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/1
 */
public class ReflectionUtils {
	private static final AgentLogger logger = AgentLogger.getLogger(ReflectionUtils.class.getName());

	public static <T> Constructor<T> getConstructor(String implementationClass, Class<T> interfaceClass) {
		Class<?> clazz = null;

		try {
			clazz = Class.forName(implementationClass);
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException(String.format("Failed to get class for %s", implementationClass), e);
		}

		if (!interfaceClass.isAssignableFrom(clazz)) {
			throw new RuntimeException(String.format("Invalid class %s, please make sure it is an implementation of %s", clazz, interfaceClass.getName()));
		}

		try {
			Class<T> concreteClass = (Class<T>) clazz;
			Constructor<T> constructor = concreteClass.getConstructor();
			return constructor;
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(String.format("Failed to get constructor for %s", clazz.getName()), e);
		}
	}

	public static <T> T createInstance(String implementationClass, Class<T> interfaceClass) {
		try {
			Constructor<T> constructor = getConstructor(implementationClass, interfaceClass);
			T result = constructor.newInstance();
			logger.info(String.format("Created %s instance (%s) for interface %s", implementationClass, result, interfaceClass));
			return result;
		}
		catch (Throwable e) {
			throw new RuntimeException(String.format("Failed to create %s instance for interface %s",implementationClass, interfaceClass));
		}
	}

	public static Object executeStaticMethods(String className, String methods) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		String[] methodArray = methods.split("\\.");
		Class<?> clazz = Class.forName(className);
		Object clazzObject = null;
		Object result = null;
		for (String entry : methodArray) {
			Method method = clazz.getMethod(entry);
			if (method == null) {
				return null;
			}
			result = method.invoke(clazzObject);
			if (result == null) {
				return null;
			}

			clazz = result.getClass();
			clazzObject = result;
		}
		return result;
	}
}
