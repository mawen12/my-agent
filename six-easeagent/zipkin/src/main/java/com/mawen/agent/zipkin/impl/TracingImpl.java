package com.mawen.agent.zipkin.impl;

import java.util.List;
import java.util.function.Supplier;

import brave.propagation.CurrentTraceContext;
import brave.propagation.Propagation;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.api.InitializeContext;
import com.mawen.agent.plugin.api.context.RequestContext;
import com.mawen.agent.plugin.api.trace.ITracing;
import com.mawen.agent.plugin.api.trace.MessagingRequest;
import com.mawen.agent.plugin.api.trace.MessagingTracing;
import com.mawen.agent.plugin.api.trace.Request;
import com.mawen.agent.plugin.api.trace.Scope;
import com.mawen.agent.plugin.api.trace.Span;
import com.mawen.agent.plugin.api.trace.SpanContext;
import com.mawen.agent.plugin.bridge.NoOpContext;
import com.mawen.agent.plugin.bridge.NoOpTracer;
import com.mawen.agent.zipkin.impl.message.MessagingTracingImpl;
import org.checkerframework.checker.units.qual.A;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/20
 */
public class TracingImpl implements ITracing {

	private static final Logger log = LoggerFactory.getLogger(TracingImpl.class);

	private final Supplier<InitializeContext> supplier;
	private final brave.Tracing tracing;
	private final brave.Tracer tracer;

	private final TraceContext.Injector<Request> defaultZipkinInjector;
	private final TraceContext.Injector<Request> clientZipkinInjector;
	private final TraceContext.Extractor<Request> defaultZipkinExtractor;
	private final MessagingTracing<MessagingRequest> messagingTracing;
	private final List<String> propagationKeys;

	private TracingImpl(Supplier<InitializeContext> supplier, brave.Tracing tracing) {
		this.supplier = supplier;
		this.tracing = tracing;
		this.tracer = tracing.tracer();
		this.propagationKeys = tracing.propagation().keys();
		Propagation<String> propagation = tracing.propagation();

		this.defaultZipkinInjector = propagation.injector(Request::setHeader);
		this.clientZipkinInjector = propagation.injector(new RemoteSetterImpl<>(brave.Span.Kind.CLIENT));
		this.defaultZipkinExtractor = propagation.extractor(Request::header);
		this.messagingTracing = MessagingTracingImpl.build(tracing);
	}

	public static ITracing build(Supplier<InitializeContext> supplier, brave.Tracing tracing) {
		if (tracing == null) {
			return NoOpTracer.NO_OP_TRACING;
		}
		return new TracingImpl(supplier, tracing);
	}

	@Override
	public SpanContext exportAsync() {
		TraceContext traceContext = currentTraceContext();
		return traceContext != null ? new SpanContextImpl(traceContext) : NoOpTracer.NO_OP_SPAN_CONTEXT;
	}

	@Override
	public Scope importAsync(SpanContext snapshot) {
		if (snapshot.isNoop()) {
			return NoOpTracer.NO_OP_SCOPE;
		}

		Object context = snapshot.unwrap();
		if (context instanceof TraceContext traceContext) {
			CurrentTraceContext.Scope scope = tracing().currentTraceContext().maybeScope(traceContext);
			return new ScopeImpl(scope);
		}
		else {
			log.warn("import async span to brave.Tracing fail: SpanContext.unwrap() result Class<{}> must be Class<{}>",
					context.getClass().getName(), TraceContext.class.getName());
		}
		return NoOpTracer.NO_OP_SCOPE;
	}

	@Override
	public RequestContext clientRequest(Request request) {
		brave.Span span = SpanImpl.nextBraveSpan(tracing, defaultZipkinExtractor, request);
		AsyncRequest asyncRequest = new AsyncRequest(request);
		clientZipkinInjector.inject(span.context(), asyncRequest);
		Span newSpan = build(span, request.cacheScope());
		return new RequestContextImpl(newSpan, newSpan.maybeScope(), asyncRequest);
	}

	@Override
	public RequestContext serverReceive(Request request) {
		TraceContext maybeParent = tracing.currentTraceContext().get();
		// Unlike message consumers, we try current span before trying extraction.
		// This is the proper order because the span in scope should take precedence over a potentially stale header entry.
		brave.Span span = null;
		if (maybeParent == null) {
			TraceContextOrSamplingFlags extracted = defaultZipkinExtractor.extract(request);
			span = extracted.context() == null
					? tracer().joinSpan(extracted.context())
					: tracer().nextSpan(extracted);
		} else {
			span = tracing.tracer().newChild(maybeParent);
		}

		if (span.isNoop()) {
			return NoOpContext.NO_OP_PROGRESS_CONTEXT;
		}

		setInfo(span, request);
		AsyncRequest asyncRequest = new AsyncRequest(request);
		defaultZipkinInjector.inject(span.context(), asyncRequest);
		Span newSpan = build(span, request.cacheScope());
		return new RequestContextImpl(newSpan, newSpan.maybeScope(), asyncRequest);
	}

	@Override
	public List<String> propagationKeys() {
		return propagationKeys;
	}

	@Override
	public Span consumerSpan(MessagingRequest request) {
		return this.messagingTracing.consumerSpan(request);
	}

	@Override
	public Span producerSpan(MessagingRequest request) {
		return this.messagingTracing.producerSpan(request);
	}

	@Override
	public boolean isNoop() {
		return false;
	}

	@Override
	public boolean hasCurrentSpan() {
		return tracing().currentTraceContext().get() != null;
	}

	@Override
	public Span currentSpan() {
		Span span = NoOpTracer.NO_OP_SPAN;
		if (tracer != null) {
			span = build(tracer.currentSpan());
		}
		return NoOpTracer.noNullSpan(span);
	}

	@Override
	public Span nextSpan() {
		return build(tracer().nextSpan(), false);
	}

	@Override
	public MessagingTracing<MessagingRequest> messagingTracing() {
		return messagingTracing;
	}

	@Override
	public Object unwrap() {
		return tracing;
	}

	private brave.Tracer tracer() {
		return this.tracer;
	}

	private brave.Tracing tracing() {
		return this.tracing;
	}

	private Span build(brave.Span span) {
		return build(span, false);
	}

	private Span build(brave.Span span, boolean cacheScope) {
		return SpanImpl.build(tracing(), span, cacheScope, defaultZipkinInjector);
	}

	private void setInfo(brave.Span span, Request request) {
		Span.Kind kind = request.kind();
		if (kind != null) {
			span.kind(SpanImpl.braveKind(kind));
		}
		span.name(request.name());
	}

	private TraceContext currentTraceContext() {
		if (tracer == null) {
			log.debug("trace was null.");
			return null;
		}

		brave.Span span = tracer.currentSpan();
		if (span == null) {
			return null;
		}
		return span.context();
	}
}
