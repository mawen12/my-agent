package com.mawen.agent.plugin.interceptor;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public class MethodInfo {

	private Object invoker;

	private String type;

	private String method;

	private Object[] args;

	private Throwable throwable;

	private Object retValue;

	private boolean changed;

	public Object getInvoker() {
		return invoker;
	}

	public String getType() {
		return type;
	}

	public String getMethod() {
		return method;
	}

	public Object[] getArgs() {
		return args;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public Object getRetValue() {
		return retValue;
	}

	public boolean isChanged() {
		return changed;
	}

	public boolean isSuccess() {
		return this.throwable == null;
	}

	public int argSize() {
		return this.args == null ? 0 : this.args.length;
	}

	public void setInvoker(Object invoker) {
		this.invoker = invoker;
		this.changed = true;
	}

	public void setType(String type) {
		this.type = type;
		this.changed = true;
	}

	public void setMethod(String method) {
		this.method = method;
		this.changed = true;
	}

	public void changeArg(int index, Object arg) {
		this.args[index] = arg;
		this.changed = true;
	}

	public void setArgs(Object[] args) {
		this.args = args;
		this.changed = true;
	}

	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
		this.changed = true;
	}

	public void setRetValue(Object retValue) {
		this.retValue = retValue;
		this.changed = true;
	}

	public void markChanged() {
		this.changed = true;
	}

	public void throwable(Throwable throwable) {
		this.throwable = throwable;
	}

	public void retValue(Object retValue) {
		this.retValue = retValue;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		MethodInfo that = (MethodInfo) o;
		return Objects.equals(invoker, that.invoker)
				&& Objects.equals(type, that.type)
				&& Objects.equals(method, that.method)
				&& Arrays.equals(args, that.args)
				&& Objects.equals(throwable, that.throwable)
				&& Objects.equals(retValue, that.retValue);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(invoker, type, method, throwable, retValue);
		result = 31 * result + Arrays.hashCode(args);
		return result;
	}

	@Override
	public String toString() {
		return "MethodInfo{" +
				"invoker=" + invoker +
				", type='" + type + '\'' +
				", method='" + method + '\'' +
				", args=" + Arrays.deepToString(args) +
				", throwable=" + throwable +
				", retValue=" + retValue +
				'}';
	}
}
