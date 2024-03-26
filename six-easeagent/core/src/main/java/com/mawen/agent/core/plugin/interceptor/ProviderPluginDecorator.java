package com.mawen.agent.core.plugin.interceptor;

import java.util.function.Supplier;

import com.mawen.agent.plugin.AgentPlugin;
import com.mawen.agent.plugin.interceptor.Interceptor;
import com.mawen.agent.plugin.interceptor.InterceptorProvider;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public class ProviderPluginDecorator implements InterceptorProvider {
	private final AgentPlugin plugin;
	private final InterceptorProvider provider;

	public ProviderPluginDecorator(AgentPlugin plugin, InterceptorProvider provider) {
		this.plugin = plugin;
		this.provider = provider;
	}

	public AgentPlugin plugin() {
		return plugin;
	}

	public InterceptorProvider provider() {
		return provider;
	}

	@Override
	public Supplier<Interceptor> getInterceptorProvider() {
		return () -> {
			Supplier<Interceptor> origin = ProviderPluginDecorator.this.provider.getInterceptorProvider();
			return new InterceptorPluginDecorator(origin.get(), this.plugin);
		};
	}

	@Override
	public String getAdviceTo() {
		return this.provider.getAdviceTo();
	}

	@Override
	public String getPluginClassName() {
		return this.provider.getPluginClassName();
	}
}
