package com.mawen.agent.core.plugin.interceptor;

import java.util.function.Supplier;

import com.mawen.agent.plugin.AgentPlugin;
import com.mawen.agent.plugin.interceptor.Interceptor;
import com.mawen.agent.plugin.interceptor.InterceptorProvider;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public record ProviderPluginDecorator(AgentPlugin plugin,  InterceptorProvider provider) implements InterceptorProvider {

	@Override
	public Supplier<Interceptor> getInterceptorProvider() {
		return () -> {
			var origin = ProviderPluginDecorator.this.provider.getInterceptorProvider();
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
