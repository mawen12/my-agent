package com.mawen.agent.mock.context;

import java.util.Iterator;
import java.util.ServiceLoader;

import com.mawen.agent.mock.config.MockConfig;
import com.mawen.agent.mock.utils.MockProvider;
import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.api.metric.MetricProvider;
import com.mawen.agent.plugin.api.trace.TracingProvider;
import com.mawen.agent.plugin.bridge.Agent;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class MockContextManager {

	private static final ContextManager CONTEXT_MANAGER_MOCK = ContextManager.build(MockConfig.getConfigs());

	static {
		ServiceLoader<MockProvider> loader = ServiceLoader.load(MockProvider.class);
		Iterator<MockProvider> iterator = loader.iterator();
		while (iterator.hasNext()) {
			MockProvider mockProvider = iterator.next();
			Object o = mockProvider.get();
			if (o == null) {
				continue;
			}
			if (o instanceof TracingProvider) {
				CONTEXT_MANAGER_MOCK.setTracing((TracingProvider)o);
			}
			else if (o instanceof MetricProvider) {
				CONTEXT_MANAGER_MOCK.setMetric((MetricProvider) o);
			}
		}
	}

	public static ContextManager getContextManagerMock() {
		return CONTEXT_MANAGER_MOCK;
	}

	public static Context getContext() {
		return Agent.getContext();
	}
}
