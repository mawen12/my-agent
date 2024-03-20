package com.mawen.agent.zipkin.impl;

import brave.Span;
import brave.propagation.Propagation;
import com.mawen.agent.plugin.api.trace.Request;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/20
 */
public class RemoteGetterImpl<R extends Request> implements Propagation.RemoteGetter<R> {

	private final Span.Kind kind;

	public RemoteGetterImpl(Span.Kind kind) {
		this.kind = kind;
	}

	@Override
	public Span.Kind spanKind() {
		return kind;
	}

	@Override
	public String get(R request, String fieldName) {
		return request.header(fieldName);
	}
}
