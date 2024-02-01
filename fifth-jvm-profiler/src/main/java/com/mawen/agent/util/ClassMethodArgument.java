package com.mawen.agent.util;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/1
 */
public class ClassMethodArgument {
	private final String className;
	private final String methodName;
	private final int argumentIndex;

	public ClassMethodArgument(String className, String methodName, int argumentIndex) {
		if (className == null) {
			throw new NullPointerException("className");
		}

		if (methodName == null) {
			throw new NullPointerException("methodName");
		}

		if (argumentIndex < 0) {
			throw new IllegalArgumentException("argumentIndex (must equal or greater than 0: 0 means not collecting argument value, 1 means collecting first argument value)");
		}

		this.className = className;
		this.methodName = methodName;
		this.argumentIndex = argumentIndex;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public int getArgumentIndex() {
		return argumentIndex;
	}

	@Override
	public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ClassMethodArgument that)) return false;

		return argumentIndex == that.argumentIndex && className.equals(that.className) && methodName.equals(that.methodName);
	}

	@Override
	public int hashCode() {
		int result = className.hashCode();
		result = 31 * result + methodName.hashCode();
		result = 31 * result + argumentIndex;
		return result;
	}

	@Override
	public String toString() {
		return "{" +
				"className='" + className + '\'' +
				", methodName='" + methodName + '\'' +
				", argumentIndex=" + argumentIndex +
				'}';
	}
}
