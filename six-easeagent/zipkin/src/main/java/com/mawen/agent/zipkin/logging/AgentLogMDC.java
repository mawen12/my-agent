package com.mawen.agent.zipkin.logging;

import java.lang.reflect.Method;

import com.mawen.agent.plugin.utils.common.WeakConcurrentMap;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/20
 */
public class AgentLogMDC {

	static WeakConcurrentMap<ClassLoader, AgentLogMDC> appMdcMap = new WeakConcurrentMap();

	private final Class<?> clazz;
	private final Method method4Get;
	private final Method method4Put;
	private final Method method4Remove;

	public static AgentLogMDC create(ClassLoader classLoader) {
		AgentLogMDC mdc = appMdcMap.getIfPresent(classLoader);
		if (mdc != null) {
			return mdc;
		}
		Class<?> aClass = LogUtils.checkLog4JMDC(classLoader);
		if (aClass == null) {
			aClass = LogUtils.checkLogBackMDC(classLoader);
		}
		if (aClass != null) {
			mdc = new AgentLogMDC(aClass);
			appMdcMap.putIfProbablyAbsent(classLoader, mdc);
			return mdc;
		}
		return null;
	}

	public AgentLogMDC(Class<?> clazz) {
		this.clazz = clazz;
		this.method4Get = LogUtils.findMethod(clazz, "get", String.class);
		this.method4Put = LogUtils.findMethod(clazz, "put", String.class, String.class);
		this.method4Remove = LogUtils.findMethod(clazz, "remove", String.class);
	}

	public String get(String key) {
		return (String) LogUtils.invokeMethod(method4Get, null, key);
	}

	public void put(String key, String value) {
		LogUtils.invokeMethod(method4Put, null, key, value);
	}

	public void remove(String key) {
		LogUtils.invokeMethod(method4Remove, null, key);
	}
}
