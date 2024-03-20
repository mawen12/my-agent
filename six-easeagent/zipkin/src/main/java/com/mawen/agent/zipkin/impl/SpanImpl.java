package com.mawen.agent.zipkin.impl;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import brave.Tracing;
import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import com.mawen.agent.plugin.api.trace.Request;
import com.mawen.agent.plugin.api.trace.Scope;
import com.mawen.agent.plugin.api.trace.Span;
import com.mawen.agent.plugin.bridge.NoOpTracer;
import org.jetbrains.annotations.Nullable;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/20
 */
public class SpanImpl implements Span {

	private static final Map<Kind, brave.Span.Kind> KINDS;

	static {
		Map<Kind, brave.Span.Kind> kinds = new EnumMap<>(Kind.class);
		kinds.put(Kind.CLIENT, brave.Span.Kind.CLIENT);
		kinds.put(Kind.SERVER, brave.Span.Kind.SERVER);
		kinds.put(Kind.PRODUCER, brave.Span.Kind.PRODUCER);
		kinds.put(Kind.CONSUMER, brave.Span.Kind.CONSUMER);
		KINDS = Collections.unmodifiableMap(kinds);
	}

	private final Tracing tracing;
	private final brave.Span span;
	private CurrentTraceContext.Scope scope;
	private final TraceContext.Injector<Request> injector;

	public SpanImpl(Tracing tracing, brave.Span span, TraceContext.Injector<Request> injector) {
		this.tracing = tracing;
		this.span = span;
		this.injector = injector;
	}

	public static Span build(Tracing tracing, brave.Span span, boolean cachedScope, TraceContext.Injector<? extends Request> injector) {
		if (span == null) {
			return NoOpTracer.NO_OP_SPAN;
		}

		TraceContext.Injector<Request> ci = (TraceContext.Injector<Request>) injector;
		SpanImpl eSpan = new SpanImpl(tracing, span, ci);

		if (cachedScope) {
			eSpan.cacheScope();
		}

		return eSpan;
	}

	public static Span build(Tracing tracing, brave.Span span, TraceContext.Injector<Request> injector) {
		return build(tracing, span, false, injector);
	}

	public static brave.Span.Kind braveKind(Kind kind) {
		return KINDS.get(kind);
	}

	public static brave.Span nextBraveSpan(Tracing tracing, TraceContext.Extractor<? extends Request> extractor, Request request) {
		TraceContext maybeParent = tracing.currentTraceContext().get();
		// Unlike message consumers, we try current span before trying extraction.
		// This is the proper order because the span in scope should take precedence over a potentially stale header entry.
		brave.Span span = null;
		if (maybeParent == null) {
			TraceContext.Extractor<Request> rExtractor = (TraceContext.Extractor<Request>) extractor;
			TraceContextOrSamplingFlags extracted = rExtractor.extract(request);
			span = tracing.tracer().nextSpan(extracted);
		} else { // if we have a span in scope assume headers were cleared before
			span = tracing.tracer().newChild(maybeParent);
		}
		if (span.isNoop()) {
			return span;
		}

		setInfo(span, request);
		return span;
	}

	private static void setInfo(brave.Span span, Request request) {
		Kind kind = request.kind();
		if (kind != null) {
			span.kind(SpanImpl.braveKind(kind));
		}
		span.name(request.name());
	}

	@Override
	public boolean isNoop() {
		return span.isNoop();
	}

	@Override
	public Span name(String name) {
		span.name(name);
		return this;
	}

	@Override
	public Span tag(String key, String value) {
		if (key == null || value == null) {
			return this;
		}
		span.tag(key, value);
		return this;
	}

	@Override
	public Span annotate(String value) {
		span.annotate(value);
		return this;
	}

	@Override
	public Span start() {
		span.start();
		return this;
	}

	@Override
	public Span start(long timestamp) {
		span.start(timestamp);
		return this;
	}

	@Override
	public Span kind(@Nullable Kind kind) {
		span.kind(KINDS.get(kind));
		return this;
	}

	@Override
	public Span annotate(long timestamp, String value) {
		span.annotate(timestamp, value);
		return this;
	}

	@Override
	public Span error(Throwable throwable) {
		span.error(throwable);
		return this;
	}

	@Override
	public Span remoteServiceName(String remoteServiceName) {
		span.remoteServiceName(remoteServiceName);
		return this;
	}

	@Override
	public boolean remoteIpAndPort(@Nullable String remoteIp, int remotePort) {
		span.remoteIpAndPort(remoteIp, remotePort);
		return false;
	}

	@Override
	public void abandon() {
		span.abandon();
	}

	@Override
	public void finish() {
		closeScope();
		span.finish();
	}

	@Override
	public void finish(long timestamp) {
		closeScope();
		span.finish(timestamp);
	}

	@Override
	public void flush() {
		span.flush();
	}

	@Override
	public void inject(Request request) {
		injector.inject(span.context(), request);
	}

	@Override
	public Scope maybeScope() {
		return new ScopeImpl(tracing.currentTraceContext().maybeScope(span.context()));
	}

	@Override
	public Span cacheScope() {
		if (scope != null) {
			return this;
		}
		scope = tracing.currentTraceContext().maybeScope(span.context());
		return this;
	}

	@Override
	public String traceIdString() {
		return span.context().traceIdString();
	}

	@Override
	public String spanIdString() {
		return span.context().spanIdString();
	}

	@Override
	public String parentIdString() {
		return span.context().parentIdString();
	}

	@Override
	public Long traceId() {
		return span.context().traceId();
	}

	@Override
	public Long spanId() {
		return span.context().spanId();
	}

	@Override
	public Long parentId() {
		return span.context().parentId();
	}

	@Override
	public Object unwrap() {
		return span;
	}

	private void closeScope() {
		if (scope != null) {
			scope.close();
		}
	}
}
