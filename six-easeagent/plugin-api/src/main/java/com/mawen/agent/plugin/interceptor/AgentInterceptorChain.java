package com.mawen.agent.plugin.interceptor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.mawen.agent.plugin.Ordered;
import com.mawen.agent.plugin.api.InitializeContext;
import com.mawen.agent.plugin.api.logging.Logger;
import com.mawen.agent.plugin.bridge.Agent;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class AgentInterceptorChain {
	private static Logger logger = Agent.loggerFactory.getLogger(AgentInterceptorChain.class);

	private ArrayList<Interceptor> interceptors;

	public AgentInterceptorChain(List<Interceptor> interceptors) {
		this.interceptors = new ArrayList<>(interceptors);
	}

	public AgentInterceptorChain(ArrayList<Interceptor> interceptors) {
		this.interceptors = interceptors;
	}

	public void doBefore(MethodInfo methodInfo, int pos, InitializeContext context) {
		if (pos == interceptors.size()) {
			return;
		}
		Interceptor interceptor = interceptors.get(pos);
		try {
			interceptor.before(methodInfo, context);
		}
		catch (Throwable e) {
			// set error message to context;
			logger.debug("Interceptor before execute exception:" + e.getMessage());
		}
		this.doBefore(methodInfo,pos + 1,context);
	}

	public Object doAfter(MethodInfo methodInfo, int pos, InitializeContext context) {
		if (pos < 0) {
			return methodInfo.getRetValue();
		}
		Interceptor interceptor = interceptors.get(pos);
		try {
			interceptor.after(methodInfo,context);
		}
		catch (Throwable e) {
			// set error message to context;
			logger.debug("Interceptor exit execute exception:" + e.getMessage());
		}
		return this.doAfter(methodInfo, pos - 1, context);
	}

	public void merge(AgentInterceptorChain other) {
		if (other == null) {
			return;
		}
		interceptors.addAll(other.interceptors);
		this.interceptors = interceptors.stream()
				.sorted(Comparator.comparing(Ordered::order))
				.collect(Collectors.toCollection(ArrayList::new));
	}

	public int size() {
		return this.interceptors.size();
	}
}
