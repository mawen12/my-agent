package com.mawen.agent.plugin.api.otlp.common;

import com.mawen.agent.plugin.api.trace.Span;
import com.mawen.agent.plugin.bridge.Agent;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class OtlpSpanContext {
	OtlpSpanContext() {
	}

	public static SpanContext getLogSpanContext() {
		var context = Agent.getContext();
		SpanContext spanContext;

		if (!context.currentTracing().hasCurrentSpan()) {
			spanContext = SpanContext.getInvalid();
		}
		else {
			var span = context.currentTracing().currentSpan();
			spanContext = new AgentSpanContext(span, TraceFlags.getSampled(), TraceState.getDefault());
		}
		return spanContext;
	}

	static class AgentSpanContext implements SpanContext {
		String traceId;
		String spanId;
		TraceFlags flags;
		TraceState state;
		boolean remote;

		public AgentSpanContext(Span span, TraceFlags flags, TraceState state) {
			this(span, flags, state, false);
		}

		public AgentSpanContext(Span span, TraceFlags flags, TraceState state, boolean isRemote) {
			this.traceId = Long.toHexString(span.traceId());
			this.spanId = Long.toHexString(span.spanId());
			this.flags = flags;
			this.state = state;
			this.remote = isRemote;
		}

		@Override
		public String getTraceId() {
			return this.traceId;
		}

		@Override
		public String getSpanId() {
			return this.spanId;
		}

		@Override
		public TraceFlags getTraceFlags() {
			return this.flags;
		}

		@Override
		public TraceState getTraceState() {
			return this.state;
		}

		@Override
		public boolean isRemote() {
			return this.remote;
		}
	}
}
