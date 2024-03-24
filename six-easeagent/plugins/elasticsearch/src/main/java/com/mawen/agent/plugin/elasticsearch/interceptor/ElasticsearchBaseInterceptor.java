package com.mawen.agent.plugin.elasticsearch.interceptor;

import com.mawen.agent.plugin.api.config.AutoRefreshPluginConfigImpl;
import com.mawen.agent.plugin.api.config.AutoRefreshPluginConfigRegistry;
import com.mawen.agent.plugin.api.config.ConfigConst;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.interceptor.Interceptor;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public abstract class ElasticsearchBaseInterceptor implements Interceptor {

	protected IPluginConfig config;

	@Override
	public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
		this.config = AutoRefreshPluginConfigRegistry.getOrCreate(ConfigConst.OBSERVABILITY, ConfigConst.Namespace.ELASTICSEARCH, this.getType());
	}
}
