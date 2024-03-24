package com.mawen.agent.plugin.bridge.trace;

import java.util.function.Function;

import com.mawen.agent.plugin.api.trace.Extractor;
import com.mawen.agent.plugin.api.trace.Injector;
import com.mawen.agent.plugin.api.trace.MessagingRequest;
import com.mawen.agent.plugin.api.trace.MessagingTracing;
import com.mawen.agent.plugin.api.trace.Span;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/24
 */
public enum EmptyMessagingTracing implements MessagingTracing<MessagingRequest> {
	INSTANCE;

	private static final Function<MessagingRequest, Boolean> NOOP_SAMPLER = r -> null;

	@Override
	public Extractor<MessagingRequest> producerExtractor() {
		return EmptyExtractor.INSTANCE;
	}

	@Override
	public Extractor<MessagingRequest> consumerExtractor() {
		return EmptyExtractor.INSTANCE;
	}

	@Override
	public Injector<MessagingRequest> producerInjector() {
		return EmptyInjector.INSTANCE;
	}

	@Override
	public Injector<MessagingRequest> consumerInjector() {
		return EmptyInjector.INSTANCE;
	}

	@Override
	public Function<MessagingRequest, Boolean> producerSampler() {
		return NOOP_SAMPLER;
	}

	@Override
	public Function<MessagingRequest, Boolean> consumerSampler() {
		return NOOP_SAMPLER;
	}

	@Override
	public Span producerSpan(MessagingRequest request) {
		return NoOpSpan.INSTANCE;
	}

	@Override
	public Span consumerSpan(MessagingRequest request) {
		return NoOpSpan.INSTANCE;
	}
}
