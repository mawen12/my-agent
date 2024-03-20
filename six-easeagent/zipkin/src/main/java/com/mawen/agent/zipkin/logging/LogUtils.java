package com.mawen.agent.zipkin.logging;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/20
 */
public class LogUtils {

	private static final Map<Class<?>, Method[]> declaredMethodsCache = new ConcurrentHashMap<>(256);
	private static final Method[] EMPTY_METHOD_ARRAY = new Method[0];
	private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];
	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
	private static final String LOG4J_MDC_CLASS_NAME = "org.apache.logging.log4j.ThreadContext";
	private static final String LOGBACK_MDC_CLASS_NAME = "org.slf4j.MDC";
	private static final String LOG4J_CHECK_CLASS_NAME = "org.apache.logging.log4j.core.Appender";
	private static final String LOGBACK_CHECK_CLASS_NAME = "ch.qos.logback.core.Appender";

	private static Boolean log4jLoaded;
	private static Boolean logbackLoaded;
	private static Class<?> log4jMdcClass;
	private static Class<?> logbackMdcClass;

	private LogUtils() {
	}

	public static Class<?> checkLog4JMDC(ClassLoader classLoader) {
		if (log4jLoaded != null) {
			return log4jMdcClass;
		}

		if (loadClass(classLoader, LOG4J_CHECK_CLASS_NAME) != null) {
			log4jMdcClass = loadClass(classLoader, LOG4J_MDC_CLASS_NAME);
		}
		log4jLoaded = true;
		return log4jMdcClass;
	}

	public static Class<?> checkLogBackMDC(ClassLoader classLoader) {
		if (logbackLoaded != null) {
			return logbackMdcClass;
		}

		if (loadClass(classLoader, LOGBACK_CHECK_CLASS_NAME) != null) {
			log4jMdcClass = loadClass(classLoader, LOGBACK_MDC_CLASS_NAME);
		}
		logbackLoaded = true;
		return logbackMdcClass;
	}

	public static Class<?> loadClass(ClassLoader classLoader, String className) {
		try {
			return classLoader.loadClass(className);
		}
		catch (ClassNotFoundException e) {
			return null;
		}
	}

	public static Method findMethod(Class<?> clazz, String name) {
		return findMethod(clazz, name, EMPTY_CLASS_ARRAY);
	}

	public static Method findMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
		Class<?> searchType = clazz;
		while (searchType != null) {
			Method[] methods = (searchType.isInterface() ? searchType.getMethods()
					: getDeclaredMethods(searchType, false));
			for (Method method : methods) {
				if (name.equals(method.getName()) && (parameterTypes == null || hasSameParams(method, parameterTypes))) {
					return method;
				}
			}
			searchType = searchType.getSuperclass();
		}
		return null;
	}


	private static Method[] getDeclaredMethods(Class<?> clazz, boolean defensive) {
		Method[] result = declaredMethodsCache.get(clazz);
		if (result == null) {
			Method[] declaredMethods = clazz.getDeclaredMethods();
			List<Method> defaultMethods = findConcreteMethodsOnInterfaces(clazz);
			if (defaultMethods != null) {
				result = new Method[declaredMethods.length + defaultMethods.size()];
				System.arraycopy(declaredMethods, 0, result, 0, declaredMethods.length);
				int index = declaredMethods.length;
				for (Method defaultMethod : defaultMethods) {
					result[index++] = defaultMethod;
				}
			}
			else {
				result = declaredMethods;
			}
		}
		return (result.length == 0 || !defensive) ? result : result.clone();
	}

	private static List<Method> findConcreteMethodsOnInterfaces(Class<?> clazz) {
		List<Method> result = null;
		for (Class<?> ifc : clazz.getInterfaces()) {
			for (Method ifcMethod : ifc.getMethods()) {
				if (!Modifier.isAbstract(ifcMethod.getModifiers())) {
					if (result == null) {
						result = new ArrayList<>();
					}
					result.add(ifcMethod);
				}
			}
		}
		return result;
	}

	private static boolean hasSameParams(Method method, Class<?>[] parameterTypes) {
		return (parameterTypes.length == method.getParameterCount()) &&
				Arrays.equals(parameterTypes, method.getParameterTypes());
	}

	public static Object invokeMethod(Method method, Object target) {
		return invokeMethod(method, target, EMPTY_OBJECT_ARRAY);
	}

	public static Object invokeMethod(Method method, Object target, Object... args) {
		try {
			return method.invoke(target, args);
		}
		catch (Exception e) {
			handleReflectionException(e);
		}
		throw new IllegalStateException("Should never get here");
	}

	public static void handleReflectionException(Exception e) {
		if (e instanceof NoSuchMethodException) {
			throw new IllegalStateException("Method not found: " + e.getMessage());
		}
		if (e instanceof IllegalAccessException) {
			throw new IllegalStateException("Could not access method or field: " + e.getMessage());
		}
		if (e instanceof InvocationTargetException ex) {
			handleInvocationTargetException(ex);
		}
		if (e instanceof RuntimeException ex) {
			throw ex;
		}
		throw new UndeclaredThrowableException(e);
	}

	public static void handleInvocationTargetException(InvocationTargetException ex) {
		rethrowRuntimeException(ex.getTargetException());
	}

	public static void rethrowRuntimeException(Throwable throwable) {
		if (throwable instanceof RuntimeException ex) {
			throw ex;
		}
		if (throwable instanceof Error ex) {
			throw ex;
		}
		throw new UndeclaredThrowableException(throwable);
	}
}
