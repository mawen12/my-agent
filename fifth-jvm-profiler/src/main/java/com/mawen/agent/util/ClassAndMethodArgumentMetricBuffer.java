package com.mawen.agent.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/3
 */
public class ClassAndMethodArgumentMetricBuffer {
	private volatile ConcurrentHashMap<ClassAndMethodMetricKey, AtomicLong> metrics = new ConcurrentHashMap<>();

	public void appendValue(String className, String methodName, String argument) {
		ClassAndMethodMetricKey methodMetricKey = new ClassAndMethodMetricKey(className, methodName, argument);
		AtomicLong counter = metrics.computeIfAbsent(methodMetricKey, k -> new AtomicLong(0L));
		counter.incrementAndGet();
	}

	public Map<ClassAndMethodMetricKey, AtomicLong> reset() {
		ConcurrentHashMap<ClassAndMethodMetricKey, AtomicLong> oldCopy = metrics;
		metrics = new ConcurrentHashMap<>();
		return oldCopy;
	}

}
