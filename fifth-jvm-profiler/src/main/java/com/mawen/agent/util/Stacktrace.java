package com.mawen.agent.util;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/3
 */
public class Stacktrace {
	private String threadName;
	private String threadState;
	private ClassAndMethod[] stack = new ClassAndMethod[0];

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public String getThreadState() {
		return threadState;
	}

	public void setThreadState(String threadState) {
		this.threadState = threadState;
	}

	public ClassAndMethod[] getStack() {
		return stack;
	}

	public void setStack(ClassAndMethod[] stack) {
		if (stack == null) {
			this.stack = new ClassAndMethod[0];
		} else {
			this.stack = stack;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Stacktrace that)) return false;

		if (!Objects.equals(threadName, that.threadName)) return false;
		if (!Objects.equals(threadState, that.threadState)) return false;
		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		return Arrays.equals(stack, that.stack);
	}

	@Override
	public int hashCode() {
		int result = threadName != null ? threadName.hashCode() : 0;
		result = 31 * result + (threadState != null ? threadState.hashCode() : 0);
		result = 31 * result + Arrays.hashCode(stack);
		return result;
	}
}
