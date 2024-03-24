package com.mawen.agent.plugin.bridge.trace;

import com.mawen.agent.plugin.api.trace.Scope;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/24
 */
public enum NoOpScope implements Scope {
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
