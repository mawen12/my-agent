package com.mawen.agent.zipkin.impl;

import brave.Span;
import brave.propagation.Propagation;
import com.mawen.agent.plugin.api.trace.Request;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/20
 */
public class RemoteSetterImpl<R extends Request> implements Propagation.RemoteSetter<R> {

	private final Span.Kind kind;

	public RemoteSetterImpl(Span.Kind kind) {
		this.kind = kind;
	}

	@Override
	public Span.Kind spanKind() {
		return kind;
	}

	@Override
	public void put(R request, String fieldName, String value) {
		request.setHeader(fieldName, value);
	}
}
