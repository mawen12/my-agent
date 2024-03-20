package com.mawen.agent.zipkin.impl;

import brave.propagation.CurrentTraceContext;
import com.mawen.agent.plugin.api.trace.Scope;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/20
 */
public class ScopeImpl implements Scope {

	private final CurrentTraceContext.Scope scope;

	public ScopeImpl(CurrentTraceContext.Scope scope) {
		this.scope = scope;
	}

	@Override
	public void close() {
		scope.close();
	}

	@Override
	public Object unwrap() {
		return scope;
	}
}
