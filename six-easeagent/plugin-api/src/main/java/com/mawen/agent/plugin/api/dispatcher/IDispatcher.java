package com.mawen.agent.plugin.api.dispatcher;

import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.interceptor.MethodInfo;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface IDispatcher {

	void enter(int chainIndex, MethodInfo info);

	Object exit(int chainIndex, MethodInfo info,
			Context context, Object result, Throwable e);
}
