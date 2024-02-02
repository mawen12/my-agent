package com.mawen.agent.util;

import java.util.Objects;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/2
 */
public class ClassAndMethodMetricKey {

	private final String className;
	private final String methodName;
	private final String metricName;

	public ClassAndMethodMetricKey(String className, String methodName, String metricName) {
		this.className = className;
		this.methodName = methodName;
		this.metricName = metricName;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getMetricName() {
		return metricName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClassAndMethodMetricKey that = (ClassAndMethodMetricKey) o;
		return Objects.equals(className, that.className) && Objects.equals(methodName, that.methodName) && Objects.equals(metricName, that.metricName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(className, methodName, metricName);
	}

	@Override
	public String toString() {
		return className + "." + methodName + "." + metricName;
	}
}
