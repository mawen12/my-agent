package com.mawen.agent.core.plugin.interceptor;

import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.AgentPlugin;
import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.api.InitializeContext;
import com.mawen.agent.plugin.api.config.AutoRefreshPluginConfigImpl;
import com.mawen.agent.plugin.api.config.AutoRefreshPluginConfigRegistry;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.bridge.NoOpIPluginConfig;
import com.mawen.agent.plugin.interceptor.Interceptor;
import com.mawen.agent.plugin.interceptor.MethodInfo;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public class InterceptorPluginDecorator implements Interceptor {

	private static final Logger logger = LoggerFactory.getLogger(InterceptorPluginDecorator.class);

	private final Interceptor interceptor;
	private final AgentPlugin plugin;
	private final IPluginConfig config;

	public InterceptorPluginDecorator(Interceptor interceptor, AgentPlugin plugin) {
		this.interceptor = interceptor;
		this.plugin = plugin;
		this.config = AutoRefreshPluginConfigRegistry.getOrCreate(plugin.getDomain(), plugin.getNamespace(), interceptor.getType());
	}

	public IPluginConfig getConfig() {
		if (config instanceof AutoRefreshPluginConfigImpl) {
			AutoRefreshPluginConfigImpl autoRefreshPluginConfig = (AutoRefreshPluginConfigImpl) config;
			return autoRefreshPluginConfig.getConfig();
		}
		return config;
	}

	@Override
	public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
		this.interceptor.init(config, className, methodName, methodDescriptor);
	}

	@Override
	public void init(IPluginConfig config, int uniqueIndex) {
		this.interceptor.init(config, uniqueIndex);
	}

	@Override
	public void before(MethodInfo methodInfo, Context context) {
		IPluginConfig cfg = getConfig();
		InitializeContext innerContext = (InitializeContext) context;
		innerContext.pushConfig(config);
		if (cfg == null || cfg.enabled() || cfg instanceof NoOpIPluginConfig) {
			innerContext.pushRetBound();
			this.interceptor.before(methodInfo, context);
		} else {
			logger.debugIfEnabled("plugin.{}.{}.{} is not enabled", config.domain(), config.namespace(), config.id());
		}
	}

	@Override
	public void after(MethodInfo methodInfo, Context context) {
		IPluginConfig cfg = getConfig();
		InitializeContext innerContext = (InitializeContext) context;
		try {
			if (cfg == null || cfg.enabled() || cfg instanceof NoOpIPluginConfig) {
				try {
					this.interceptor.after(methodInfo, context);
				}
				finally {
					innerContext.popToBound();
					innerContext.popRetBound();
				}
			}
		}
		finally {
			innerContext.popConfig();
		}
	}

	@Override
	public String getType() {
		return this.interceptor.getType();
	}

	@Override
	public int order() {
		int pluginOrder = this.plugin.order();
		int interceptorOrder = this.interceptor.order();
		return (interceptorOrder << 8) + pluginOrder;
	}
}
