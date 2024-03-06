package com.mawen.agent.core.plugin.interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.mawen.agent.plugin.interceptor.Interceptor;
import com.mawen.agent.plugin.interceptor.InterceptorProvider;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public class ProviderChain {

	private final List<InterceptorProvider> providers;

	ProviderChain(List<InterceptorProvider> providers) {
		this.providers = providers;
	}

	public List<Supplier<Interceptor>> getSupplierChain() {
		return this.providers.stream()
				.map(InterceptorProvider::getInterceptorProvider)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private List<InterceptorProvider> providers = new ArrayList<>();

		Builder (){}

		public Builder providers(List<InterceptorProvider> providers) {
			this.providers = providers;
			return this;
		}

		public Builder addProvider(InterceptorProvider provider) {
			this.providers.add(provider);
			return this;
		}

		public ProviderChain build() {
			return new ProviderChain(providers);
		}

		@Override
		public String toString() {
			return "ProviderChain.Builder(providers=" + this.providers + ")";
		}
	}
}
