package com.mawen.agent.core.plugin.matcher;

import java.util.Comparator;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.mawen.agent.core.plugin.interceptor.InterceptorPluginDecorator;
import com.mawen.agent.core.plugin.interceptor.ProviderChain;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.Ordered;
import com.mawen.agent.plugin.interceptor.AgentInterceptorChain;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
@Data
@AllArgsConstructor
public class MethodTransformation {

	private static final Logger logger = LoggerFactory.getLogger(MethodTransformation.class);

	private int index;
	private ElementMatcher.Junction<? super MethodDescription> matcher;
	private ProviderChain.Builder providerBuilder;

	public AgentInterceptorChain getAgentInterceptorChain(final int uniqueIndex,
			final String type,
			final String method,
			final String methodDescription) {
		var suppliers = this.providerBuilder.build()
				.getSupplierChain();

		var interceptors = suppliers.stream()
				.map(Supplier::get)
				.sorted(Comparator.comparing(Ordered::order))
				.collect(Collectors.toList());

		interceptors.forEach(i -> {
			if (i instanceof InterceptorPluginDecorator interceptor) {
				try {
					interceptor.init(interceptor.getConfig(), type, method, methodDescription);
					interceptor.init(interceptor.getConfig(), uniqueIndex);
				}
				catch (Exception e) {
					logger.error("Interceptor init fail: {}:{} {}", type, method, interceptor.getClass().getSimpleName());
				}
			}
		});

		return new AgentInterceptorChain(interceptors);
	}
}
