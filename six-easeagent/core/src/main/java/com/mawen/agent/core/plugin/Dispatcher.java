package com.mawen.agent.core.plugin;

import com.google.auto.service.AutoService;
import com.mawen.agent.core.utils.AgentArray;
import com.mawen.agent.core.utils.ContextUtils;
import com.mawen.agent.plugin.AppendBootstrapLoader;
import com.mawen.agent.plugin.api.InitializeContext;
import com.mawen.agent.plugin.interceptor.AgentInterceptorChain;
import com.mawen.agent.plugin.interceptor.MethodInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AutoService(AppendBootstrapLoader.class)
public final class Dispatcher implements AppendBootstrapLoader {

	static AgentArray<AgentInterceptorChain> chains = new AgentArray<>();

	public static AgentInterceptorChain register(int index, AgentInterceptorChain chain) {
		return chains.putIfAbsent(index, chain);
	}

	public static void enter(int index, MethodInfo info, InitializeContext ctx) {
		var chain = chains.getUncheck(index);
		var pos = 0;
		ContextUtils.setBeginTime(ctx);
		chain.doBefore(info, pos, ctx);
	}

	public static Object exit(int index, MethodInfo info, InitializeContext ctx) {
		var chain = chains.getUncheck(index);
		var pos = chain.size() - 1;
		ContextUtils.setEndTime(ctx);
		return chain.doAfter(info, pos, ctx);
	}

	public static AgentInterceptorChain getChain(int index) {
		return chains.get(index);
	}

	public static boolean updateChain(int index, AgentInterceptorChain chain) {
		return chains.replace(index, chain) != null;
	}
}
