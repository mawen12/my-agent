package com.mawen.agent.core.plugin;

import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.api.InitializeContext;
import com.mawen.agent.plugin.api.dispatcher.IDispatcher;
import com.mawen.agent.plugin.bridge.Agent;
import com.mawen.agent.plugin.interceptor.MethodInfo;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public class BridgeDispatcher implements IDispatcher {

	@Override
	public void enter(int chainIndex, MethodInfo info) {
		InitializeContext context = Agent.initializeContextSupplier.getContext();
		if (context.isNoop()) {
			return;
		}
		Dispatcher.enter(chainIndex, info, context);
	}

	@Override
	public Object exit(int chainIndex, MethodInfo info, Context context, Object result, Throwable e) {
		if (context.isNoop() || !(context instanceof InitializeContext)) {
			return result;
		}

		info.throwable(e);
		info.retValue(result);

		InitializeContext iContext = (InitializeContext) context;

		Dispatcher.exit(chainIndex, info, iContext);
		if (info.isChanged()) {
			result = info.getRetValue();
		}
		return result;
	}
}
