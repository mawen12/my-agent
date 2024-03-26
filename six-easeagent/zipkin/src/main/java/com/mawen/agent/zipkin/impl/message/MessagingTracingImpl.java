package com.mawen.agent.zipkin.impl.message;

import java.util.function.Function;

import brave.Tracing;
import brave.propagation.TraceContext;
import com.mawen.agent.plugin.api.trace.Extractor;
import com.mawen.agent.plugin.api.trace.Injector;
import com.mawen.agent.plugin.api.trace.Message;
import com.mawen.agent.plugin.api.trace.MessagingRequest;
import com.mawen.agent.plugin.api.trace.MessagingTracing;
import com.mawen.agent.plugin.api.trace.Span;
import com.mawen.agent.plugin.bridge.trace.EmptyMessagingTracing;
import com.mawen.agent.plugin.bridge.trace.NoOpSpan;
import com.mawen.agent.plugin.utils.NoNull;
import com.mawen.agent.zipkin.impl.MessageImpl;
import com.mawen.agent.zipkin.impl.RemoteGetterImpl;
import com.mawen.agent.zipkin.impl.RemoteSetterImpl;
import com.mawen.agent.zipkin.impl.SpanImpl;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/20
 */
public class MessagingTracingImpl<R extends MessagingRequest> implements MessagingTracing<R> {

	private final brave.messaging.MessagingTracing messagingTracing;
	private final Extractor<R> producerExtractor;
	private final Extractor<R> consumerExtractor;
	private final Injector<R> producerInjector;
	private final Injector<R> consumerInjector;

	private final Function<R, Boolean> consumerSampler;
	private final Function<R, Boolean> producerSampler;

	private final TraceContext.Injector<R> zipkinProducerInjector;
	private final TraceContext.Injector<R> zipkinConsumerInjector;
	private final TraceContext.Extractor<R> zipkinProducerExtractor;
	private final TraceContext.Extractor<R> zipkinConsumerExtractor;

	private MessagingTracingImpl(brave.messaging.MessagingTracing messagingTracing) {
		this.messagingTracing = messagingTracing;
		this.zipkinProducerExtractor = messagingTracing.propagation().extractor(new RemoteGetterImpl<>(brave.Span.Kind.PRODUCER));
		this.zipkinConsumerExtractor = messagingTracing.propagation().extractor(new RemoteGetterImpl<>(brave.Span.Kind.CONSUMER));
		this.zipkinProducerInjector = messagingTracing.propagation().injector(new RemoteSetterImpl<>(brave.Span.Kind.PRODUCER));
		this.zipkinConsumerInjector = messagingTracing.propagation().injector(new RemoteSetterImpl<>(brave.Span.Kind.CONSUMER));

		this.producerExtractor = new ExtractorImpl(this.zipkinProducerExtractor);
		this.consumerExtractor = new ExtractorImpl(this.zipkinConsumerExtractor);
		this.producerInjector = new InjectorImpl(this.zipkinProducerInjector);
		this.consumerInjector = new InjectorImpl(this.zipkinConsumerInjector);

		this.consumerSampler = new SamplerFunction(ZipkinConsumerRequest::new, messagingTracing.consumerSampler());
		this.producerSampler = new SamplerFunction(ZipkinProducerRequest::new, messagingTracing.producerSampler());
	}

	public static MessagingTracing<MessagingRequest> build(brave.Tracing tracing) {
		if (tracing == null) {
			return EmptyMessagingTracing.INSTANCE;
		}
		brave.messaging.MessagingTracing messagingTracing = brave.messaging.MessagingTracing.newBuilder(tracing).build();
		return new MessagingTracingImpl<>(messagingTracing);
	}

	@Override
	public Extractor<R> producerExtractor() {
		return producerExtractor;
	}

	@Override
	public Extractor<R> consumerExtractor() {
		return consumerExtractor;
	}

	@Override
	public Injector<R> producerInjector() {
		return producerInjector;
	}

	@Override
	public Injector<R> consumerInjector() {
		return consumerInjector;
	}

	@Override
	public Function<R, Boolean> producerSampler() {
		return producerSampler;
	}

	@Override
	public Function<R, Boolean> consumerSampler() {
		return consumerSampler;
	}

	@Override
	public Span producerSpan(R request) {
		Tracing tracing = messagingTracing.tracing();
		brave.Span span = SpanImpl.nextBraveSpan(tracing, this.zipkinProducerExtractor, request);
		if (span.isNoop()) {
			return NoOpSpan.INSTANCE;
		}

		setMessageInfo(span, request);
		Span eSpan = SpanImpl.build(messagingTracing.tracing(), span, request.cacheScope(), zipkinProducerInjector);
		producerInjector.inject(eSpan, request);
		return NoNull.of(eSpan, NoOpSpan.INSTANCE);
	}

	@Override
	public Span consumerSpan(R request) {
		Tracing tracing = messagingTracing.tracing();
		brave.Span span = SpanImpl.nextBraveSpan(tracing, this.zipkinConsumerExtractor, request);
		if (span.isNoop()) {
			return NoOpSpan.INSTANCE;
		}

		setMessageInfo(span, request);
		Span eSpan = SpanImpl.build(messagingTracing.tracing(), span, request.cacheScope(), this.zipkinConsumerInjector);
		return NoNull.of(eSpan, NoOpSpan.INSTANCE);
	}

	private void setMessageInfo(brave.Span span, MessagingRequest request) {
		if (request.operation() != null) {
			span.tag("messaging.operation", request.operation());
		}
		if (request.channelKind() != null) {
			span.tag("messaging.channel_kind", request.channelKind());
		}
		if (request.channelName() != null) {
			span.tag("messaging.channel_name", request.channelName());
		}
	}

	public class ExtractorImpl implements Extractor<R> {
		private final TraceContext.Extractor<R> extractor;

		public ExtractorImpl(TraceContext.Extractor<R> extractor) {
			this.extractor = extractor;
		}

		@Override
		public Message extract(R request) {
			return new MessageImpl(extractor.extract(request));
		}
	}

	public class InjectorImpl implements Injector<R> {
		private final TraceContext.Injector<R> injector;

		public InjectorImpl(TraceContext.Injector<R> injector) {
			this.injector = injector;
		}

		@Override
		public void inject(Span span, R request) {
			Object span0 = span.unwrap();
			if (span0 instanceof brave.Span) {
				brave.Span braveSpan = (brave.Span) span0;
				this.injector.inject(braveSpan.context(), request);
			}
		}

		public TraceContext.Injector<R> getInjector() {
			return injector;
		}
	}

	public class SamplerFunction implements Function<R, Boolean> {
		private final Function<R, brave.messaging.MessagingRequest> builder;
		private final brave.sampler.SamplerFunction<brave.messaging.MessagingRequest> function;

		public SamplerFunction(Function<R, brave.messaging.MessagingRequest> builder, brave.sampler.SamplerFunction<brave.messaging.MessagingRequest> function) {
			this.builder = builder;
			this.function = function;
		}

		@Override
		public Boolean apply(R request) {
			return function.trySample(builder.apply(request));
		}
	}
}
