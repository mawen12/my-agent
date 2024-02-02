package com.mawen.agent.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/2
 */
public class ClassAndMethodLongMetricBuffer {

	private volatile ConcurrentHashMap<ClassAndMethodMetricKey, Histogram> metrics = new ConcurrentHashMap<>();

	public void appendValue(String className, String methodName, String metricName, long value) {
		ClassAndMethodMetricKey methodMetricKey = new ClassAndMethodMetricKey(className, methodName, metricName);
		Histogram histogram = metrics.computeIfAbsent(methodMetricKey, key -> new Histogram());
		histogram.appendValue(value);
	}

	public Map<ClassAndMethodMetricKey, Histogram> reset() {
		ConcurrentHashMap<ClassAndMethodMetricKey, Histogram> oldCopy = metrics;
		metrics = new ConcurrentHashMap<>();
		return oldCopy;
	}
}
