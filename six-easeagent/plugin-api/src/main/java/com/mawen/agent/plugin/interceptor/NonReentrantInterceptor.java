package com.mawen.agent.plugin.interceptor;

import com.mawen.agent.plugin.api.Context;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public interface NonReentrantInterceptor extends Interceptor{

	@Override
	default void before(MethodInfo methodInfo, Context context) {
		if (!context.enter(getEnterKey(methodInfo, context), 1)) {
			return;
		}
		doBefore(methodInfo, context);
	}

	@Override
	default void after(MethodInfo methodInfo, Context context) {
		if (!context.exit(getEnterKey(methodInfo, context), 1)) {
			return;
		}
		doAfter(methodInfo, context);
	}

	default Object getEnterKey(MethodInfo methodInfo, Context context) {
		return this.getClass();
	}

	default void doBefore(MethodInfo methodInfo, Context context) {

	}

	default void doAfter(MethodInfo methodInfo, Context context) {

	}
}
