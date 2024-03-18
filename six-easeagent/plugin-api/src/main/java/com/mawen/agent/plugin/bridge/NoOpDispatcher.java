package com.mawen.agent.plugin.bridge;

import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.api.dispatcher.IDispatcher;
import com.mawen.agent.plugin.interceptor.MethodInfo;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public enum NoOpDispatcher implements IDispatcher {
	INSTANCE;

	@Override
	public void enter(int chainIndex, MethodInfo info) {
		// NOP
	}

	@Override
	public Object exit(int chainIndex, MethodInfo info, Context context, Object result, Throwable e) {
		return result;
	}
}
