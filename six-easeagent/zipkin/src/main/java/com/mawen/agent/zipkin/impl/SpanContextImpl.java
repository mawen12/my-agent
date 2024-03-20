package com.mawen.agent.zipkin.impl;

import brave.propagation.TraceContext;
import com.mawen.agent.plugin.api.trace.SpanContext;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/20
 */
public class SpanContextImpl implements SpanContext {

	private final TraceContext traceContext;

	public SpanContextImpl(TraceContext traceContext) {
		this.traceContext = traceContext;
	}

	@Override
	public boolean isNoop() {
		return false;
	}

	@Override
	public Object unwrap() {
		return traceContext;
	}
}
