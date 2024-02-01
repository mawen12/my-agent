package com.mawen.agent.util;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/1
 */
public class ClassAndMethod {
	private final String className;
	private final String methodName;

	public ClassAndMethod(String className, String methodName) {
		if (className == null) {
			throw new NullPointerException("className");
		}

		if (methodName == null) {
			throw new NullPointerException(methodName);
		}

		this.className = className;
		this.methodName = methodName;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	@Override
	public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ClassAndMethod that)) return false;

		return className.equals(that.className) && methodName.equals(that.methodName);
	}

	@Override
	public int hashCode() {
		int result = className.hashCode();
		result = 31 * result + methodName.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return className + "." + methodName;
	}
}
