package com.mawen.agent.plugin.bridge.trace;

import com.mawen.agent.plugin.api.trace.SpanContext;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/24
 */
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
