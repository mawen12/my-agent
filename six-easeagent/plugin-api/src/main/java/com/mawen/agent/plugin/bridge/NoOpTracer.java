package com.mawen.agent.plugin.bridge;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.mawen.agent.plugin.api.context.RequestContext;
import com.mawen.agent.plugin.api.trace.Extractor;
import com.mawen.agent.plugin.api.trace.ITracing;
import com.mawen.agent.plugin.api.trace.Injector;
import com.mawen.agent.plugin.api.trace.Message;
import com.mawen.agent.plugin.api.trace.MessagingRequest;
import com.mawen.agent.plugin.api.trace.MessagingTracing;
import com.mawen.agent.plugin.api.trace.Request;
import com.mawen.agent.plugin.api.trace.Scope;
import com.mawen.agent.plugin.api.trace.Span;
import com.mawen.agent.plugin.api.trace.SpanContext;
import com.mawen.agent.plugin.utils.NoNull;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class NoOpTracer {
	public static final ITracing NO_OP_TRACING = NoopTracing.INSTANCE;
	public static final Span NO_OP_SPAN = NoopSpan.INSTANCE;
	public static final SpanContext NO_OP_SPAN_CONTEXT = EmptySpanContext.INSTANCE;
	public static final Scope NO_OP_SCOPE = NoopScope.INSTANCE;
	public static final EmptyExtractor NO_OP_EXTRACTOR = EmptyExtractor.INSTANCE;
	public static final EmptyMessagingTracing NO_OP_MESSAGING_TRACING = EmptyMessagingTracing.INSTANCE;

	public static Span noNullSpan(Span span) {
		return NoNull.of(span, NO_OP_SPAN);
	}

	public static Extractor noNullExtractor(Extractor extractor) {
		return NoNull.of(extractor, NO_OP_EXTRACTOR);
	}

	public enum NoopSpan implements Span {
		INSTANCE;

		@Override
		public boolean isNoop() {
			return true;
		}

		@Override
		public Span name(String name) {
			return this;
		}

		@Override
		public Span tag(String key, String value) {
			return this;
		}

		@Override
		public Span annotate(String value) {
			return this;
		}

		@Override
		public Span start() {
			return this;
		}

		@Override
		public Span start(long timestamp) {
			return this;
		}

		@Override
		public Span kind(@Nullable Kind kind) {
			return this;
		}

		@Override
		public Span annotate(long timestamp, String value) {
			return this;
		}

		@Override
		public Span error(Throwable throwable) {
			return this;
		}

		@Override
		public Span remoteServiceName(String remoteServiceName) {
			return this;
		}

		@Override
		public boolean remoteIpAndPort(@Nullable String remoteIp, int remotePort) {
			return true;
		}

		@Override
		public void abandon() {
			// NOP
		}

		@Override
		public void finish() {
			// NOP
		}

		@Override
		public void finish(long timestamp) {
			// NOP
		}

		@Override
		public void flush() {
			// NOP
		}

		@Override
		public void inject(Request request) {
			// NOP
		}

		@Override
		public Scope maybeScope() {
			return NoopScope.INSTANCE;
		}

		@Override
		public Span cacheScope() {
			return this;
		}

		@Override
		public String traceIdString() {
			return "";
		}

		@Override
		public String spanIdString() {
			return "";
		}

		@Override
		public String parentIdString() {
			return "";
		}

		@Override
		public Long traceId() {
			return null;
		}

		@Override
		public Long spanId() {
			return null;
		}

		@Override
		public Long parentId() {
			return null;
		}

		@Override
		public Object unwrap() {
			return null;
		}
	}

	public enum NoopScope implements Scope {
		INSTANCE;

		@Override
		public void close() {
			// NOP
		}

		@Override
		public Object unwrap() {
			return null;
		}
	}

	public enum NoopTracing implements ITracing {
		INSTANCE;

		@Override
		public SpanContext exportAsync() {
			return EmptySpanContext.INSTANCE;
		}

		@Override
		public Scope importAsync(SpanContext snapshot) {
			return NoopScope.INSTANCE;
		}

		@Override
		public RequestContext clientRequest(Request request) {
			return NoOpContext.NO_OP_PROGRESS_CONTEXT;
		}

		@Override
		public RequestContext serverReceive(Request request) {
			return NoOpContext.NO_OP_PROGRESS_CONTEXT;
		}

		@Override
		public List<String> propagationKeys() {
			return Collections.emptyList();
		}

		@Override
		public Span consumerSpan(MessagingRequest request) {
			return NoOpTracer.NO_OP_SPAN;
		}

		@Override
		public Span producerSpan(MessagingRequest request) {
			return NoOpTracer.NO_OP_SPAN;
		}

		@Override
		public boolean isNoop() {
			return true;
		}

		@Override
		public boolean hasCurrentSpan() {
			return false;
		}

		@Override
		public Span currentSpan() {
			return NoOpTracer.NO_OP_SPAN;
		}

		@Override
		public Span nextSpan() {
			return NoOpTracer.NO_OP_SPAN;
		}

		@Override
		public MessagingTracing<MessagingRequest> messagingTracing() {
			return EmptyMessagingTracing.INSTANCE;
		}

		@Override
		public Object unwrap() {
			return null;
		}
	}

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
			return NoOpTracer.NO_OP_SPAN;
		}

		@Override
		public Span consumerSpan(MessagingRequest request) {
			return NoOpTracer.NO_OP_SPAN;
		}
	}

	public enum EmptyMessage implements Message<Object> {
		INSTANCE;
		private static final Object OBJ_INSTANCE = new Object();

		@Override
		public Object get() {
			return OBJ_INSTANCE;
		}
	}

	public enum EmptyExtractor implements Extractor<MessagingRequest> {
		INSTANCE;

		@Override
		public Message extract(MessagingRequest request) {
			return EmptyMessage.INSTANCE;
		}
	}

	public enum EmptyInjector implements Injector<MessagingRequest> {
		INSTANCE;

		@Override
		public void inject(Span span, MessagingRequest request) {
			// NOP
		}
	}

	public enum EmptySpanContext implements SpanContext {
		INSTANCE;

		@Override
		public boolean isNoop() {
			return true;
		}

		@Override
		public Object unwrap() {
			return this;
		}
	}
}
